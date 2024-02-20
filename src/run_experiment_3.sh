#!/bin/bash

# Check if the number of clients is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <number of clients>"
    exit 1
fi

NUM_CLIENTS=$1

# Function to run a client with the specified number of threads
run_client() {
    java Client_FS "8kb" &
}


echo "Running experiment with $NUM_CLIENTS clients..."
for (( i=1; i<=NUM_CLIENTS; i++ ))
do
    run_client
done

# Wait for all background processes to finish
wait
echo "Experiment with $NUM_CLIENTS clients completed."
