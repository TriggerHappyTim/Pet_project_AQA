#!/bin/bash

# EVS Testing Framework - Test Runner Script
# This script provides convenient commands for running tests in different configurations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="test"
BROWSER="chrome"
THREAD_COUNT="1"
TEST_TYPE="all"
DOCKER_COMPOSE_FILE="docker-compose.yml"

# Function to print usage
print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -e, --environment ENV    Test environment (test, dev, staging, prod) [default: test]"
    echo "  -b, --browser BROWSER    Browser to use (chrome, firefox) [default: chrome]"
    echo "  -t, --threads COUNT      Number of parallel threads [default: 1]"
    echo "  -g, --groups GROUPS      Test groups to run (smoke, regression, api, security)"
    echo "  -d, --docker             Run tests in Docker containers"
    echo "  --local                  Run tests locally (without Docker)"
    echo "  --smoke                  Run only smoke tests"
    echo "  --regression             Run regression tests"
    echo "  --security               Run security tests"
    echo "  --api                    Run API tests"
    echo "  --performance            Run performance tests"
    echo "  --allure                 Generate and serve Allure report"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --smoke --docker          # Run smoke tests in Docker"
    echo "  $0 --regression -b firefox   # Run regression tests with Firefox"
    echo "  $0 --api --local             # Run API tests locally"
    echo "  $0 --allure                  # Generate Allure report"
}

# Function to log messages
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -b|--browser)
            BROWSER="$2"
            shift 2
            ;;
        -t|--threads)
            THREAD_COUNT="$2"
            shift 2
            ;;
        -g|--groups)
            TEST_GROUPS="$2"
            shift 2
            ;;
        -d|--docker)
            USE_DOCKER=true
            shift
            ;;
        --local)
            USE_LOCAL=true
            shift
            ;;
        --smoke)
            TEST_TYPE="smoke"
            TEST_GROUPS="smoke"
            shift
            ;;
        --regression)
            TEST_TYPE="regression"
            TEST_GROUPS="regression"
            shift
            ;;
        --security)
            TEST_TYPE="security"
            TEST_GROUPS="security"
            shift
            ;;
        --api)
            TEST_TYPE="api"
            TEST_GROUPS="api"
            shift
            ;;
        --performance)
            TEST_TYPE="performance"
            TEST_GROUPS="performance"
            shift
            ;;
        --allure)
            GENERATE_ALLURE=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

# Function to run tests locally
run_tests_local() {
    log_info "Running $TEST_TYPE tests locally with $BROWSER browser"

    local mvn_cmd="mvn clean test"
    local test_class=""

    case $TEST_TYPE in
        smoke)
            test_class="*SmokeTest*"
            ;;
        regression)
            test_class="*RegressionTest*"
            ;;
        security)
            test_class="*SecurityTest*"
            ;;
        api)
            test_class="*ApiTest*"
            ;;
        performance)
            test_class="*PerformanceTest*"
            ;;
        *)
            # Run all tests
            ;;
    esac

    if [[ -n "$test_class" ]]; then
        mvn_cmd="$mvn_cmd -Dtest=$test_class"
    fi

    if [[ -n "$TEST_GROUPS" ]]; then
        mvn_cmd="$mvn_cmd -Dgroups=$TEST_GROUPS"
    fi

    # Set environment variables
    export ENVIRONMENT=$ENVIRONMENT
    export BROWSER=$BROWSER
    export THREAD_COUNT=$THREAD_COUNT

    log_info "Executing: $mvn_cmd"
    eval "$mvn_cmd"

    if [[ $? -eq 0 ]]; then
        log_success "Tests completed successfully"
        generate_allure_report
    else
        log_error "Tests failed"
        exit 1
    fi
}

# Function to run tests in Docker
run_tests_docker() {
    log_info "Running $TEST_TYPE tests in Docker with $BROWSER browser"

    # Check if Docker is running
    if ! docker info >/dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker first."
        exit 1
    fi

    # Build the test image
    log_info "Building Docker image..."
    docker build -t evs-testing-framework .

    # Set environment variables for Docker
    local env_vars="-e ENVIRONMENT=$ENVIRONMENT -e BROWSER=$BROWSER -e THREAD_COUNT=$THREAD_COUNT"

    # Add credentials if available
    if [[ -f ".env" ]]; then
        env_vars="$env_vars --env-file .env"
    fi

    local test_cmd="mvn clean test"

    case $TEST_TYPE in
        smoke)
            test_cmd="$test_cmd -Dtest=*SmokeTest*"
            ;;
        regression)
            test_cmd="$test_cmd -Dtest=*RegressionTest*"
            ;;
        security)
            test_cmd="$test_cmd -Dtest=*SecurityTest*"
            ;;
        api)
            test_cmd="$test_cmd -Dtest=*ApiTest*"
            ;;
        performance)
            test_cmd="$test_cmd -Dtest=*PerformanceTest*"
            ;;
    esac

    if [[ -n "$TEST_GROUPS" ]]; then
        test_cmd="$test_cmd -Dgroups=$TEST_GROUPS"
    fi

    # Run tests in Docker
    log_info "Starting Docker containers..."
    docker-compose up -d selenium-hub chrome-node firefox-node allure-server

    log_info "Waiting for Selenium Grid to be ready..."
    sleep 10

    log_info "Running tests..."
    docker run --rm --network container:selenium-hub \
        -v "$(pwd)":/app \
        -v "$(pwd)/allure-results":/app/allure-results \
        $env_vars \
        evs-testing-framework \
        bash -c "cd /app && $test_cmd"

    if [[ $? -eq 0 ]]; then
        log_success "Tests completed successfully"
        generate_allure_report
    else
        log_error "Tests failed"
    fi

    # Cleanup
    log_info "Cleaning up Docker containers..."
    docker-compose down
}

# Function to generate Allure report
generate_allure_report() {
    if [[ "$GENERATE_ALLURE" == "true" ]] || [[ "$TEST_TYPE" != "all" ]]; then
        log_info "Generating Allure report..."
        if command -v allure >/dev/null 2>&1; then
            allure generate allure-results --clean -o allure-report
            allure open allure-report &
            log_success "Allure report generated and opened"
        else
            log_warn "Allure CLI not found. Install it to generate reports: https://docs.qameta.io/allure/"
        fi
    fi
}

# Main execution
main() {
    log_info "EVS Testing Framework Test Runner"
    log_info "Environment: $ENVIRONMENT, Browser: $BROWSER, Threads: $THREAD_COUNT"

    # Determine execution mode
    if [[ "$USE_DOCKER" == "true" ]]; then
        run_tests_docker
    elif [[ "$USE_LOCAL" == "true" ]] || [[ "$USE_DOCKER" != "true" ]]; then
        run_tests_local
    else
        # Auto-detect: prefer Docker if available
        if docker info >/dev/null 2>&1; then
            log_info "Docker detected, running tests in containers"
            run_tests_docker
        else
            log_info "Docker not available, running tests locally"
            run_tests_local
        fi
    fi
}

# Run main function
main "$@"