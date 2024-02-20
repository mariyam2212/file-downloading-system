#!/bin/bash

# Parameters
SERVER_IP="127.0.0.1"      # Replace with your server's IP
SERVER_PORT=8080           # Replace with your server's port
NUM_THREADS_PER_CLIENT=10  # Number of threads per client
NUM_FILES_TO_DOWNLOAD=5    # Number of files each thread will download
NUM_EXPERIMENTS=5          # Number of times each client repeats the experiment

# Function to run a client with the specified number of threads
run_client() {
    java Client_FS "8kb" &
}

# Run clients for different numbers of concurrent clients (N)
for N in 2 4 8 16
do
    echo "Running experiment with $N clients..."
    for (( i=1; i<=N; i++ ))
    do
        run_client
    done

    # Wait for all background processes to finish
    wait
    echo "Experiment with $N clients completed."
done
