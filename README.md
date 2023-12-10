# Instruction
Follow the instructions bellow to start the gRPC server and client.

## Build(Optional)
***You can skip building by using the pre-generated `.jar` file provided***
* Unzip the compressed folder in your file system locally.
* Open the project in IDEA, press `ctrl` twice, run `mvn clean package`
* right-click the folder `target/generated-sources` -> Mark Directory as -> Generated Sources Root

## Run
1. If you skipped building, download the provided jar file to local file system then navigate to it in terminal, and go to step `3`
2. If you built, then you can run the server and client in two ways, you can either:
   1. In IDEA, click the start button for main function in `src/main/java/ServerApp` to run the server first, then start the client in `src/main/java/ClientApp`. Go to step `4`. Or:
   2. In command line, navigate to `~/CS6650_Projects/target`
3. Run `java -jar .\server.jar` to start the server. Open a new terminal session in the same directory, run `java -jar .\client.jar` to start a client
4. Follow the command line prompt to interact with client. Press `q` to stop the client, press `ctrl + c` on the server session to stop server 
5. Check out the log in dir `/logs/`

## Executive Summary
### Assignment Overview
This project requires to implement a distributed key-value store with PAXOS to sync up 5 server replicas. First and most important task is to
understand PAXOS, what it is, what it solves and how it works. In short, it's an algorithm for multiple processes to achieve consensus on a value in an unreliable communication environment.
In this project, the "value" in fact represents the key value pair to be modified in the store. Every node need to process the same operation for a key-value pair on their own instance during the same PAXOS round. 
The structure of server node and the work flow to reach consensus is strictly aligned with the original algorithm. The extras in the project are 1. agreement handling, 2. failure handling, 3. termination.

The consensus of the proposed value (actually the key value pair operation) is solidified in the learning stage, in another work, the store modification happens on the learning stage when the agreement is achieved.

The program simulated the failure of acceptors by manually timeout the acceptors' response of prepare function call. The proposer will restart another round of PAXOS once timeout detected.  

Since this is a basic PAXOS implementation, we need to terminate the PAXOS epoch each time the consensus made for one value. Unlike multi-PAXOS, the cache of promised and accepted proposal will be refreshed for starting the next round of process for another value.

### Technical Impression
PAXOS, as the diamond on the crown of distributed consensus algorithm, is not easy to understand from a scratch. L.Lamport thoroughly described
the theory and concept of PAXOS in a 10-page short essay, but left many rabbit holes for implementation. For example, it didn't specify how the message
delivery should be, how to handle error and failure etc. I have to admit it is the hardest project among the entire course. By reading a lot about PAXOS, 
I've constructed a general impression of it and implemented it with customized messaging and value processing. Below are some useful links which helped a lot.

* [Understanding PAXOS](https://understandingpaxos.wordpress.com/)
* [Essential PAXOS](https://github.com/cocagne/paxos/tree/master)
* [PAXOS Wiki Page](https://en.wikipedia.org/wiki/Paxos_(computer_science)#)
* [NEAT ALGORITHMS - PAXOS](http://harry.me/blog/2014/12/27/neat-algorithms-paxos/)
* [Quora Answer](https://www.quora.com/Distributed-Systems/In-distributed-systems-what-is-a-simple-explanation-of-the-Paxos-algorithm)
* [Consensus](https://people.cs.rutgers.edu/~pxk/417/notes/content/consensus.html)
* And of course, [PAXOS Made Simple](https://www.microsoft.com/en-us/research/publication/paxos-made-simple/?from=https://research.microsoft.com/en-us/um/people/lamport/pubs/paxos-simple.pdf&type=exact)