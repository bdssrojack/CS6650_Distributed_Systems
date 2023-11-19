import com.cs6650.server_client.*;
import com.google.protobuf.Empty;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CoordinatorImpl implements Coordinator {
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

    class ServerImpl extends ServiceGrpc.ServiceImplBase {
        //TODO: log out info

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
            String msg;
            String key = transaction.getRequest().getKey(), value = transaction.getRequest().getValue();
            Operation operation = transaction.getRequest().getOperation();

            if(commit){
                for(String replicaHost : Utils.replicas){
                    ManagedChannel channel = Grpc.newChannelBuilder(replicaHost, InsecureChannelCredentials.create()).build();
                    Response commitRes = ServiceGrpc.newBlockingStub(channel).doCommit(transaction.getTid());
                    channel.shutdownNow();
                }
                switch (operation){
                    case PUT -> response = Response.newBuilder().setStatus(true).setMsg(MessageLib.PUT_SUCCEED(key, value)).build();
                    case DELETE -> response = Response.newBuilder().setStatus(true).setMsg(MessageLib.DELETE_SUCCEED(key, value)).build();
                }
            } else {
                for(String replicaHost : Utils.replicas){
                    ManagedChannel channel = Grpc.newChannelBuilder(replicaHost, InsecureChannelCredentials.create()).build();
                    ServiceGrpc.newBlockingStub(channel).doAbort(transaction.getTid());
                    channel.shutdownNow();
                }
                switch (operation){
                    case PUT -> response = Response.newBuilder().setStatus(false).setMsg(MessageLib.UPDATE_FAILED(key)).build();
                    case DELETE -> response = Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build();
                }
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
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

    public void haveCommitted(String tid) {

    }

    public void newRequest(String tid, Request request){

    }

    public boolean getDecision(String tid) {
        return false;
    }


}
