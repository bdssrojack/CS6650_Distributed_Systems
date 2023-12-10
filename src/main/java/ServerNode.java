import com.cs6650.server_client.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a ServerNode class that plays the role of Proposer, Acceptor, and Learner in the Paxos algorithm
 */
public class ServerNode {
    // Common
    private int port;
    private LogHandler logger;
    private Server server;
    private int quorum = Utils.replicas.length / 2 + 1; // majority number
    private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>(); // key-value store, filled during learning phase

    // Proposer
    private String[] acceptors = Utils.replicas;

    // Acceptor
    private String[] learners = Utils.replicas;
    private String promisedId; // promised proposal id of a value
    private String acceptedId; // accepted proposal id of a value
    private ConcurrentHashMap<String, PaxosValue> acceptedProposal = new ConcurrentHashMap<>(); // accepted proposal id and value

    // Learner
    private ConcurrentHashMap<String, Integer> learnCount = new ConcurrentHashMap<>(); // Count the learning message for an accepted proposal

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println(" *** shutting down server since JVM is shutting down ***");
            try {
                ServerNode.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println(" *** server shut down ***");
        }));
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Implementation of Service, contains the business logic
     */
    class ServerImpl extends ServiceGrpc.ServiceImplBase {
        @Override
        public void operate(Request request, StreamObserver<Response> responseStreamObserver) {
            logger.log(request);
            Response response = null;

            // case GET/Invalid
            Operation o = request.getOperation();
            String key = request.getKey();
            if (o == Operation.GET) {
                // Call getConsensus() on each peer
                HashMap<String, Integer> consensusCount = new HashMap<>();
                for (String replicaHost : learners) {
                    ManagedChannel channel = Grpc.newChannelBuilder(replicaHost, InsecureChannelCredentials.create()).build();
                    Response res = ServiceGrpc.newBlockingStub(channel).getConsensus(request);
                    if (res.getStatus()) {
                        consensusCount.putIfAbsent(res.getMsg(), 0);
                        consensusCount.put(res.getMsg(), consensusCount.get(res.getMsg()) + 1);
                    }
                    channel.shutdownNow();
                }

                // return the value with consensus
                String value = null;
                for (String v : consensusCount.keySet()) {
                    if (consensusCount.get(v) >= quorum)
                        value = v;
                }

                if (value != null) {
                    response = Response.newBuilder().setStatus(true).setMsg(MessageLib.GET_SUCCEED(key, value)).build();
                } else {
                    // agreed value doesn't exist
                    response = Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build();
                }

                responseStreamObserver.onNext(response);
                responseStreamObserver.onCompleted();
                logger.log(response);
                return;
            } else if (o == Operation.UNRECOGNIZED) {
                responseStreamObserver.onNext(Response.newBuilder().setStatus(false).setMsg(MessageLib.INVALID_OPERATION).build());
                responseStreamObserver.onCompleted();
                return;
            }

            // case PUT/DELETE
            // generate proposal id
            String pid = genPid();
            System.out.printf("Starting PAXOS with Pid %s\n", pid);

            // start PAXOS
            String acceptedPid = "";
            PaxosValue acceptedValue = null, proposedValue = PaxosValue.newBuilder().setKey(request.getKey()).setVal(request.getValue()).build();
            int promisedCount = 0;
            Proposal proposal = Proposal.newBuilder().setN(pid).setV(proposedValue).build();
            for (String acceptorHost : acceptors) {
                System.out.printf("Sending prepare message to %s\n", acceptorHost);
                ManagedChannel channel = Grpc.newChannelBuilder(acceptorHost, InsecureChannelCredentials.create()).build();
                Promise promise;
                try {
                    promise = ServiceGrpc.newBlockingStub(channel).withDeadlineAfter(2000, TimeUnit.MILLISECONDS).prepare(proposal);
                    System.out.println("Promise received.");
                } catch (StatusRuntimeException e) {
                    System.err.println("Promise timed out, restarting paxos");
                    channel.shutdownNow();
                    operate(request, responseStreamObserver);
                    return;
                }

                if (promise.getNAccepted().equals("N")) {
                    // once the NACK received, retry the proposal with a higher sequence number
                    operate(request, responseStreamObserver);
                    response = Response.newBuilder().setStatus(false).setMsg("NACK received from acceptor.").build();
                    logger.log(response);
                    responseStreamObserver.onNext(response);
                    responseStreamObserver.onCompleted();
                    return;
                }

                if (acceptedPid.compareTo(promise.getNAccepted()) < 0) {
                    acceptedPid = promise.getNAccepted();
                    acceptedValue = promise.getVAccepted();
                    System.out.printf("Promised pid: %s, promised value: %s-%s\n", acceptedPid, acceptedValue.getKey(), acceptedValue.getVal());
                }

                promisedCount++;
                channel.shutdownNow();
            }

            if (promisedCount >= quorum) {
                // promise granted from the majority of nodes
                if (!acceptedPid.isEmpty()) {
                    System.out.println("No previously accepted proposal");
                    proposedValue = acceptedValue;
                }
                // send accept! message to acceptors
                int acceptedCount = 0;
                for (String acceptorHost : acceptors) {
                    System.out.printf("Sending accept message to %s\n", acceptorHost);
                    ManagedChannel channel = Grpc.newChannelBuilder(acceptorHost, InsecureChannelCredentials.create()).build();
                    Response res = ServiceGrpc.newBlockingStub(channel).accept(Proposal.newBuilder().setN(pid).setV(proposedValue).build());
                    System.out.printf("Accept received, status: %s\n", res.getStatus());
                    if (res.getStatus())
                        acceptedCount++;
                    channel.shutdownNow();
                }
                if (acceptedCount >= quorum) {
                    response = Response.newBuilder().setStatus(true).setMsg("Proposal accepted by the majority of server nodes.").build();
                    logger.log(response);
                } else {
                    response = Response.newBuilder().setStatus(false).setMsg("Proposal not been accepted.").build();
                    logger.log(response);
                }
            } else {
                // didn't get enough promises
                response = Response.newBuilder().setStatus(false).setMsg("Didn't gain enough promises").build();
                logger.log(response);
            }

            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
            logger.log(response);
        }

        @Override
        public void prepare(Proposal proposal, StreamObserver<Promise> promiseStreamObserver) {
            // simulate acceptor random failure
            if(Math.random()%3 == 0){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Promise promise;
            if (promisedId == null || proposal.getN().compareTo(promisedId) > 0) {
                System.out.printf("Incoming proposal %s is newer than current promised %s, promise to this one\n", proposal.getN(), promisedId);
                promisedId = proposal.getN();
                if(acceptedId == null){
                    promise = Promise.newBuilder().
                            setNCurr(proposal.getN()).
                            setNAccepted("").build();
                    System.out.println("Didn't accept any proposal yet.");
                }else{
                    promise = Promise.newBuilder().
                            setNCurr(proposal.getN()).
                            setNAccepted(acceptedId).
                            setVAccepted(acceptedProposal.get(acceptedId)).build();
                    System.out.printf("Accepted proposal %s, return with value %s-%s\n", acceptedId, acceptedProposal.get(acceptedId).getKey(), acceptedProposal.get(acceptedId).getVal());
                }
            } else {
                // return a NACK to proposer, inform it to retry with a higher sequence number
                promise = Promise.newBuilder().setNCurr(proposal.getN()).setNAccepted("N").build();
                System.err.println("Promised to a higher proposal, return NACK.");
            }
            promiseStreamObserver.onNext(promise);
            promiseStreamObserver.onCompleted();
        }

        @Override
        public void accept(Proposal proposal, StreamObserver<Response> responseStreamObserver) {
            Response response;
            if(promisedId == null || proposal.getN().compareTo(promisedId) >= 0){
                System.out.printf("Accepting proposal %s\n", proposal.getN());
                acceptedId = proposal.getN();
                promisedId = proposal.getN();
                acceptedProposal.put(proposal.getN(), proposal.getV());
                for(String learnerHost : learners){
                    ManagedChannel channel = Grpc.newChannelBuilder(learnerHost, InsecureChannelCredentials.create()).build();
                    ServiceGrpc.newBlockingStub(channel).learn(proposal);
                    channel.shutdownNow();
                }

                response = Response.newBuilder().setStatus(true).build();
            }else {
                response = Response.newBuilder().setStatus(false).build();
                System.err.println("Promised to a higher proposal, return NACK.");
            }
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
            logger.log(response);
        }

        @Override
        public void learn(Proposal proposal, StreamObserver<Response> responseStreamObserver) {
            Response response = Response.newBuilder().build();
            learnCount.putIfAbsent(proposal.getN(), 0);
            learnCount.put(proposal.getN(), learnCount.get(proposal.getN())+1);
            System.out.printf("Learning proposal %s, %s-%s, count = %s\n", proposal.getN(), proposal.getV().getKey(), proposal.getV().getVal(), learnCount.get(proposal.getN()));
            if(learnCount.get(proposal.getN()) >= quorum){
                System.out.printf("Got enough learn message, writing %s-%s into storage.\n", proposal.getV().getKey(), proposal.getV().getVal());
                // write the proposed key-value pair into storage
                if(proposal.getV().getVal().isEmpty()){
                    store.remove(proposal.getV().getKey());
                }else{
                    store.put(proposal.getV().getKey(), proposal.getV().getVal());
                }
                // refresh the consensus for next paxos instance
                System.out.println("Clear paxos instance");
                if(acceptedId != null) {
                    acceptedProposal.remove(acceptedId);
                    acceptedId = null;
                }
                promisedId = null;
                response = Response.newBuilder().setStatus(true).build();
            }
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }

        @Override
        public void getConsensus(Request request, StreamObserver<Response> responseStreamObserver) {
            Response response;
            String val = store.get(request.getKey());
            if (val == null) {
                response = Response.newBuilder().setStatus(false).build();
            } else {
                response = Response.newBuilder().setStatus(true).setMsg(val).build();
            }
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }
    }

    public ServerNode(int port) throws IOException, InterruptedException {
        this.port = port;
        logger = new LogHandler(Utils.genLogFilePath("ServerNode_" + port + "_"));

        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new ServerImpl())
                .executor(Executors.newFixedThreadPool(10))
                .build();

        this.start();
        System.out.printf("Server node %s started.\n", port);
        this.blockUntilShutdown();
    }

    /**
     * Generate the globally unique, monotonic increasing sequence number of a proposal
     */
    private String genPid() {
        return "" + System.currentTimeMillis() + port;
    }
}
