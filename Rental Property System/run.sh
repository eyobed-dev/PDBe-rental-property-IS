#!/bin/bash

set -e 

echo "Running Rental Property IS..."

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

mvn exec:java -Dexec.mainClass="cz.vut.fit.pdbe.xnuriey00.Main"
