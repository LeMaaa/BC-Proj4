all:
ifeq ($(OS),Windows_NT)
	# assume windows
	javac -Xlint -g *.java
else
	# assume Linux
	javac -Xlint -g -cp ".:hash/commons-codec-1.11.jar:" *.java
endif

clean:
	rm lib/*.class
	rm ./*.class
   
