##Makefile commands to run the application

### compile code
make 

### run server
make run_server

### run single client
make run_client

### run experiment 3 (from problem statement) withnumber of clients N as the argument.
#### Example: for N=4 clients executing concurrently - ./run_experiments_3.sh 4
make run_experiment_3

### run experiment 4 (from problem statement)
make run_experiment_4

### clean class files
make clean

### clean data files
make clean_data

### clean downloaded files
make clean_downloaded_data

### clean logfiles
make clean_logfiles
