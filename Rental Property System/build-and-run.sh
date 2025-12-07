#!/bin/bash
# Build and Run script for Rental Property Information System

set -e 
echo "Build and Run started for Rental Property IS"

# Build the project
./build.sh

echo "Build success. Starting application..."

# Run the application
mvn exec:java -Dexec.mainClass="cz.vut.fit.pdbe.xnuriey00.Main"
