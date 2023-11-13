# Instruction
Follow the instructions bellow to start the gRPC server and client.

## Build(Optional)
***You can skip building by using the pre-generated `.jar` file provided***
* Unzip the compressed folder in your file system locally.
* Open the project in IDEA, press `ctrl` twice, run `mvn clean install`

## Run
1. If you skipped building, download the provided jar file to local file system then navigate to it in terminal, and go to step `3`
2. If you built, then you can run the server and client in two ways:
   1. In IDEA, click the start button for main function in `src/main/java/ServerApp` to run the server first, then start the client in `src/main/java/ClientApp`. Go to step `4`
   2. In command line, navigate to `~/CS6650_Project2/target`
3. Run `java -jar .\server.jar` to start the server. Open a new terminal session in the same directory, run `java -jar .\client.jar` to start a client
4. Follow the command line prompt to interact with client. Press `q` to stop the client, press `ctrl + c` on the server session to stop server 
5. To check out the log, see `/logs/`

## Executive Summary
### Assignment Overview
This project is aimed to expose the student in a more practical environment of distributed system development by implementing
remote method invoke for a client server module. Inter process communication is a well known paradigm in software development, especially
in distributed system building, and RPC/RMI is a very important part it. By implementing a classic client-server communication with RPC, 
student can get great practice and the essence of distributed system.

### Technical Impression
To discover the industrial standard and broad my tech stack, I chose to use gRPC to implement the remote call. gRPC is a super powerful, 
widely used framework for inter-process communication. It uses protocol buffer as the communication protocol that makes the communication
between services implemented in different programming languages become possible. It's also very handy that can automatically generate
the fundamental code for RPC, such that developers can focus on the business logic without worry about the underlying framework.
The performance of gRPC is also impressive, as well as its scalability.

Maven is another new technology that I learned from this project. It is a dependency manager that handles all the dependencies of your java
project. It will import any package, plugin and dependency you'd like to cover in the project automatically so you don't have to concern about it.
With the help of Maven, build can never be such easy with just one mvn command. 