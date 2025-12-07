#!/bin/bash


set -e 

echo "Building Rental Property IS..."

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not insfound. failed to build"
    exit 1
fi

java -version

# Clean and compile
echo "Cleaning previous builds..."
mvn clean

echo "Compiling project..."
mvn compile

echo "Build completed successfully!"
