#!/bin/bash

# Define server IP and port
SERVER_IP="127.0.0.1"
SERVER_PORT=8080

# Define the number of clients and threads per client
NUM_CLIENTS=4
NUM_THREADS=10

# Define the number of experiments per file size
NUM_EXPERIMENTS=5

# Array of file sizes
FILE_SIZES=("128b" "512b" "2kb" "8kb" "32kb")

# Function to run a client instance
run_client() {
    local fileSize=$1
    java Client_FS $fileSize &
}

# Main loop to run experiments for each file size
for fileSize in "${FILE_SIZES[@]}"; do
    echo "Running experiment for file size: $fileSize"

    # Run specified number of client instances for each file size
    for (( i=0; i<$NUM_CLIENTS; i++ )); do
        run_client $fileSize
    done

    # Wait for all background processes to finish
    wait
    echo "Experiment completed for file size: $fileSize"
done
