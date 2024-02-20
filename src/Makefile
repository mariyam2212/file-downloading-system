JC = javac
JR = java
JFLAGS  = -g -Wall

default: all

all:
	$(JC) *.java

clean:
	$(RM) *.class

clean_data:
	$(RM) data/*.txt

clean_downloaded_data:
	$(RM) downloads/*.txt

clean_logfiles:
	$(RM) logs/*

generate_server_files:
	chmod +x generate_files.sh
	./generate_files.sh

run_server:
	$(JR) Server_FS

run_client:
	$(JR) Client_FS

run_client_th:
	$(JR) Client_FS 8kb

# provide number of clients in argument. E.g for N=2: make run_experiment_3 2
run_experiment_3:
	chmod +x run_experiment_3.sh
	./run_experiment_3.sh 2

run_experiment_4:
	chmod +x run_experiment_4.sh
	./run_experiment_4.sh

run_test:
	$(JR) Test