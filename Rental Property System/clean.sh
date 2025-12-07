#!/bin/bash

set -e 

echo "Cleaning Rental Property IS..."

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

mvn clean

echo "Clean complete"
