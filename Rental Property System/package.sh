#!/bin/bash

set -e 

echo "Packaging Rental Property IS..."

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

mvn clean package

echo "Package complete"
echo "JAR file location: target/rentalpropertysystem-1.0-SNAPSHOT.jar"
