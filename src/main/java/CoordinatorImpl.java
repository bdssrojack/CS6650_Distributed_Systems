import com.cs6650.server_client.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CoordinatorImpl {
    private LogHandler logger;
    private final Server server;

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println(" *** shutting down coordinator server since JVM is shutting down ***");
            try {
                CoordinatorImpl.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println(" *** coordinator server shut down ***");
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

    static class ServerImpl extends ServiceGrpc.ServiceImplBase {
        //TODO: log out info

        /**
         * Call from participant to inform the coordinator that new transaction has been raised.
         */
        @Override
        public void newRequest(Trans transaction, StreamObserver<Response> responseObserver){
            boolean commit = true;
            for(String replicaHost : Utils.replicas){
                ManagedChannel channel = Grpc.newChannelBuilder(replicaHost, InsecureChannelCredentials.create()).build();
                Response res = ServiceGrpc.newBlockingStub(channel).canCommit(transaction);
                commit = commit && res.getStatus();
                channel.shutdownNow();
            }

            Response response = null;
            if(commit){
                for(String replicaHost : Utils.replicas){
                    ManagedChannel channel = Grpc.newChannelBuilder(replicaHost, InsecureChannelCredentials.create()).build();
                    Response commitRes = ServiceGrpc.newBlockingStub(channel).doCommit(transaction.getTid());
                    if(response == null)
                        response = commitRes;
                    channel.shutdownNow();
                }
            } else {
                for(String replicaHost : Utils.replicas){
                    ManagedChannel channel = Grpc.newChannelBuilder(replicaHost, InsecureChannelCredentials.create()).build();
                    Response abortRes = ServiceGrpc.newBlockingStub(channel).doAbort(transaction.getTid());
                    if (response == null)
                        response = abortRes;
                    channel.shutdownNow();
                }
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * Call from participant to ask for the decision on a transaction by tid when it
         * has voted Yes but has still had no reply after some delay. Used to recover from server
         * crash or delayed messages.
         */
        @Override
        public void getDecision(Tid tid, StreamObserver<Response> responseStreamObserver) {

        }

        /**
         * Call from participant to confirm that it has committed the transaction with tid.
         */
        @Override
        public void haveCommitted(Tid tid, StreamObserver<Response> responseStreamObserver) {

        }
    }

    public CoordinatorImpl() throws IOException, InterruptedException {
        logger = new LogHandler("Coordinator_");
        server = Grpc.newServerBuilderForPort(Utils.coordinatorPort, InsecureServerCredentials.create())
                .addService(new ServerImpl())
                .executor(Executors.newFixedThreadPool(10))
                .build();
        this.start();
        System.out.println("Coordinator started.");
        this.blockUntilShutdown();
    }

}
