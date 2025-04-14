#!/bin/bash

DIR="data/"

# Create the directory if it doesn't exist
mkdir -p "${DIR}"

# Create files of specific sizes
dd if=/dev/zero of="${DIR}128b.txt" bs=128 count=1
dd if=/dev/zero of="${DIR}512b.txt" bs=512 count=1
dd if=/dev/zero of="${DIR}2kb.txt" bs=1K count=2
dd if=/dev/zero of="${DIR}8kb.txt" bs=1K count=8
dd if=/dev/zero of="${DIR}32kb.txt" bs=1K count=32

echo "Files created successfully."
