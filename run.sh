#!/bin/bash
echo "Lancement de l'application HR Management..."

# 1. Tentative avec Maven global
if command -v mvn >/dev/null 2>&1; then
    echo "Utilisation de Maven..."
    mvn compile exec:java
    if [ $? -eq 0 ]; then exit 0; fi
fi

# 2. Tentative avec le Maven Wrapper
if [ -f "./mvnw" ]; then
    echo "Tentative avec Maven Wrapper..."
    chmod +x ./mvnw
    ./mvnw compile exec:java
    if [ $? -eq 0 ]; then exit 0; fi
    echo "Le Maven Wrapper a echoue."
fi

# 3. Repli sur la compilation manuelle
echo "Tentative de lancement manuel..."
mkdir -p target/classes

echo "Compilation des sources..."
find src/main/java -name "*.java" > sources.txt
javac -cp "lib/*" -d target/classes @sources.txt
if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation."
    rm sources.txt
    exit 1
fi
rm sources.txt

echo "Demarrage de l'application..."
java -cp "target/classes:lib/*" com.hrmanager.App
