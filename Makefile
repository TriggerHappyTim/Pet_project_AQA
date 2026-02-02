# EVS Testing Framework - Makefile
# Convenient commands for development and testing

.PHONY: help build test clean docker-up docker-down docker-logs allure

# Default target
help: ## Show this help message
	@echo "EVS Testing Framework - Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Development commands
build: ## Build the project
	mvn clean compile

test: ## Run all tests locally
	./scripts/run-tests.sh --local

test-smoke: ## Run smoke tests
	./scripts/run-tests.sh --smoke --local

test-regression: ## Run regression tests
	./scripts/run-tests.sh --regression --local

test-security: ## Run security tests
	./scripts/run-tests.sh --security --local

test-api: ## Run API tests
	./scripts/run-tests.sh --api --local

# Docker commands
docker-build: ## Build Docker image
	docker build -t evs-testing-framework .

docker-up: ## Start all Docker services
	docker-compose up -d

docker-down: ## Stop all Docker services
	docker-compose down

docker-logs: ## Show Docker logs
	docker-compose logs -f

docker-test: ## Run tests in Docker
	./scripts/run-tests.sh --docker

docker-test-smoke: ## Run smoke tests in Docker
	./scripts/run-tests.sh --smoke --docker

docker-test-regression: ## Run regression tests in Docker
	./scripts/run-tests.sh --regression --docker

# Allure commands
allure: ## Generate and open Allure report
	./scripts/run-tests.sh --allure

allure-docker: ## Open Allure report from Docker
	open http://localhost:5050

# Cleanup commands
clean: ## Clean build artifacts
	mvn clean
	rm -rf allure-results allure-report target

clean-docker: ## Clean Docker artifacts
	docker-compose down -v
	docker system prune -f
	docker image rm evs-testing-framework || true

clean-all: clean clean-docker ## Clean all artifacts

# Setup commands
setup: ## Initial project setup
	@echo "Setting up EVS Testing Framework..."
	@if [ ! -f .env ]; then \
		cp docker/env.example .env; \
		echo "Created .env file from example"; \
	fi
	@echo "Setup complete. Edit .env file with your credentials."

setup-docker: ## Setup Docker environment
	@echo "Setting up Docker environment..."
	docker network create testing-network || true
	docker pull selenium/hub:4.15.0
	docker pull selenium/node-chrome:4.15.0
	docker pull selenium/node-firefox:4.15.0
	docker pull frankescobar/allure-docker-service:latest
	@echo "Docker environment ready."

# Utility commands
lint: ## Run code quality checks
	mvn checkstyle:check
	mvn spotbugs:check

security-scan: ## Run security scanning
	gitleaks detect --verbose --redact

deps: ## Analyze dependencies
	mvn dependency:tree
	mvn dependency:analyze

# CI/CD simulation
ci-validate: ## Simulate CI validate stage
	@echo "=== CI: Validate Stage ==="
	make build
	make lint

ci-test: ## Simulate CI test stage
	@echo "=== CI: Test Stage ==="
	make test-smoke
	make test-api

ci-security: ## Simulate CI security stage
	@echo "=== CI: Security Stage ==="
	make test-security
	make security-scan

ci-report: ## Simulate CI report stage
	@echo "=== CI: Report Stage ==="
	make allure

# Development environment
dev-up: ## Start development environment
	docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d

dev-down: ## Stop development environment
	docker-compose -f docker-compose.yml -f docker-compose.override.yml down

dev-logs: ## Show development environment logs
	docker-compose -f docker-compose.yml -f docker-compose.override.yml logs -f

# Database commands
db-up: ## Start database only
	docker-compose --profile db up -d postgres-db

db-down: ## Stop database
	docker-compose --profile db down

db-reset: ## Reset database
	docker-compose --profile db down -v
	docker-compose --profile db up -d postgres-db

# Windows compatibility (using batch files)
windows-test:
	scripts\run-tests.bat --local

windows-docker-test:
	scripts\run-tests.bat --docker

# Info commands
info: ## Show project information
	@echo "=== EVS Testing Framework ==="
	@echo "Java version: $$(java -version 2>&1 | head -n 1)"
	@echo "Maven version: $$(mvn -version | head -n 1)"
	@echo "Docker version: $$(docker --version)"
	@echo "Environment: $$(cat .env | grep ENVIRONMENT | cut -d'=' -f2)"

versions: ## Show versions of key tools
	@echo "=== Tool Versions ==="
	@echo "Java: $$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)"
	@echo "Maven: $$(mvn -version | head -n 1 | grep -oP 'Apache Maven \K[^\s]+')"
	@echo "Docker: $$(docker --version | cut -d' ' -f3 | tr -d ',')"
	@echo "Docker Compose: $$(docker-compose --version | cut -d' ' -f4)"
	@if command -v allure >/dev/null 2>&1; then \
		echo "Allure: $$(allure --version | cut -d' ' -f2)"; \
	else \
		echo "Allure: Not installed"; \
	fi