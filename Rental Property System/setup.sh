#!/bin/bash
# Setup script for Rental Property Information System
# This script checks dependencies and downloads Maven dependencies

set -e  # Exit on error

echo "Setup: Rental Property IS"

echo "Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    exit 1
fi

echo "Java version:"
java -version
echo ""

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed"
    exit 1
fi

echo "Maven version:"
mvn -version
echo ""

# Download dependencies
echo "Downloading project dependencies..."
mvn dependency:resolve

echo "Setup complete"
echo "  - Build the project: ./build.sh"
echo "  - Run the project: ./run.sh"
echo "  - Build and run: ./build-and-run.sh"
echo "  - Package as JAR: ./package.sh"
