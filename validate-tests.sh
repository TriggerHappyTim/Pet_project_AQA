#!/bin/bash

# EVS Testing Framework - Validation Script
# Проверяет основные проблемы в UI тестах

echo "=== EVS Testing Framework Validation ==="
echo

# Проверка наличия pom.xml
if [ ! -f "pom.xml" ]; then
    echo "❌ ERROR: pom.xml not found"
    exit 1
fi
echo "✅ pom.xml found"

# Проверка основных директорий
directories=("src/test/java" "src/main/java" "src/test/resources")
for dir in "${directories[@]}"; do
    if [ -d "$dir" ]; then
        echo "✅ Directory $dir exists"
    else
        echo "❌ ERROR: Directory $dir not found"
    fi
done

echo
echo "=== Checking test files ==="

# Проверка основных тестовых файлов
test_files=(
    "src/test/java/com/bft/LK_Insurence/CryptoProCertificateTest.java"
    "src/test/java/com/bft/test/base/UITestBase.java"
    "src/test/java/com/bft/gui/CryptoProDemoPage.java"
)

for file in "${test_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ Test file $file exists"

        # Проверка импортов
        if grep -q "SoftAssert" "$file"; then
            echo "  ✅ SoftAssert import found in $file"
        else
            echo "  ⚠️  SoftAssert import not found in $file"
        fi

    else
        echo "❌ ERROR: Test file $file not found"
    fi
done

echo
echo "=== Checking for common issues ==="

# Проверка на hardcoded credentials
echo "Checking for hardcoded credentials..."
credentials_found=$(find src -name "*.java" -exec grep -l "zirnbirnshtein\|44ywIEU\|P@\$Sw0rd\|72q+kE" {} \; | wc -l)
if [ "$credentials_found" -gt 0 ]; then
    echo "❌ ERROR: Found $credentials_found files with hardcoded credentials"
    find src -name "*.java" -exec grep -l "zirnbirnshtein\|44ywIEU\|P@\$Sw0rd\|72q+kE" {} \;
else
    echo "✅ No hardcoded credentials found"
fi

# Проверка на отсутствующие конструкторы
echo "Checking for missing constructors..."
missing_constructors=$(grep -r "new CryptoProDemoPage()" src/test/java/ | wc -l)
if [ "$missing_constructors" -gt 0 ]; then
    echo "❌ ERROR: Found $missing_constructors instances of CryptoProDemoPage() without parameters"
else
    echo "✅ All CryptoProDemoPage instances have required parameters"
fi

# Проверка на конфликты импортов
echo "Checking for import conflicts..."
import_conflicts=$(find src -name "*.java" -exec grep -l "import.*TestStrategyType.*;" {} \; | xargs grep -l "TestStrategyType" | wc -l)
if [ "$import_conflicts" -gt 0 ]; then
    echo "⚠️  Found $import_conflicts files with potential TestStrategyType conflicts"
else
    echo "✅ No TestStrategyType import conflicts found"
fi

echo
echo "=== Validation Summary ==="
echo "Basic validation completed. Run 'mvn compile' for full compilation check."
# ЗАКОММЕНТИРОВАНО: SecuritySystemTest не используется
# echo "Run 'mvn test -Dtest=SecuritySystemTest' to test security features."