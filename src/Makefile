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

run_server:
	$(JR) Server_FS

run_client:
	$(JR) Client_FS

run_client_th:
	$(JR) Client_FS 8kb

run_test:
	$(JR) Test