# Instruction
Follow the instructions bellow to start the gRPC server and client.

## Build(Optional)
***You can skip building by using the pre-generated `.jar` file provided***
* Unzip the compressed folder in your file system locally.
* Open the project in IDEA, press `ctrl` twice, run `mvn clean install`
* right-click the folder `target/generated-sources` -> Mark Directory as -> Generated Sources Root

## Run
1. If you skipped building, download the provided jar file to local file system then navigate to it in terminal, and go to step `3`
2. If you built, then you can run the server and client in two ways, you can either:
   1. In IDEA, click the start button for main function in `src/main/java/ServerApp` to run the server first, then start the client in `src/main/java/ClientApp`. Go to step `4`. Or:
   2. In command line, navigate to `~/CS6650_Project3/target`
3. Run `java -jar .\server.jar` to start the server. Open a new terminal session in the same directory, run `java -jar .\client.jar` to start a client
4. Follow the command line prompt to interact with client. Press `q` to stop the client, press `s` to switch replica connection, press `ctrl + c` on the server session to stop server 
5. Check out the log in dir `/logs/`

## Executive Summary
### Assignment Overview
### Technical Impression