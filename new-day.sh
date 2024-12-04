#!/bin/bash

# Check if the argument (date) is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <date>"
    exit 1
fi

# The argument passed to the script
DATE=$1

# Compile the project
echo "Compiling the project..."
mvn compile
if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

# Run the NewDay class with the provided date argument
echo "Running NewDay with argument: $DATE"
mvn exec:java -Dexec.mainClass=main.NewDay -Dexec.args="$DATE"
if [ $? -ne 0 ]; then
    echo "Execution failed. Exiting."
    exit 1
fi

echo "Done."
