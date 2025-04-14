## Overview
This is a **client-server mode file downloading system**. It involves the creation of a client and server program. The server hosts a list of files for the client to download. The client will get the file list from the server and then request for downloading one or more files of them from the server. Client implementation is then scaled to evaluate the performance of the server.

## Makefile commands to run the application

* ### compile code
`make`

* ### run server
`make run_server`

* ### run single client
`make run_client`

* ### run experiment 3 (from problem statement) withnumber of clients N as the argument.
_Example: for N=4 clients executing concurrently - ./run_experiments_3.sh 4_\
`make run_experiment_3 <number_of_clients>`

* ### run experiment 4 (from problem statement)
`make run_experiment_4`

* ### clean class files
`make clean`

* ### clean data files
`make clean_data`

* ### clean downloaded files
`make clean_downloaded_data`

* ### clean logfiles
`make clean_logfiles`
