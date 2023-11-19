import com.cs6650.server_client.Operation;
import com.cs6650.server_client.Request;
import com.cs6650.server_client.Response;
import com.cs6650.server_client.ServiceGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParticipantImpl implements Participant {
    private String coordinator;
    private int port;
    /*
     * Store the prepared commitment locally
     * Key: tid
     * Value: operation | key | value
     */
    private ConcurrentHashMap<String, Request> tmp;
    private ConcurrentHashMap<String, String> store;
    private LogHandler logger;
    private final Server server;

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println(" *** shutting down gRPC server since JVM is shutting down ***");
            try {
                ParticipantImpl.this.stop();
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
        public void operate(Request request, StreamObserver<Response> responseObserver) {
            Operation o = request.getOperation();
            Response response = null;
            String key = request.getKey(), value = request.getValue();
            logger.log(request);

            switch (o) {
                case PUT -> {
                    if (value.isBlank() || value.isEmpty()) {
                        response = Response.newBuilder().setStatus(false).setMsg(MessageLib.UPDATE_FAILED(key)).build();
                    } else if (store.containsKey(key)) {
                        store.put(key, value);
                        response = Response.newBuilder().setStatus(true).setMsg(MessageLib.UPDATE_SUCCEED(key, value)).build();
                    } else {
                        store.put(key, value);
                        response = Response.newBuilder().setStatus(true).setMsg(MessageLib.ADD_SUCCEED(key, value)).build();
                    }
                }
                case GET -> {
                    if (store.containsKey(key)) {
                        value = store.get(key);
                        response = Response.newBuilder().setStatus(true).setMsg(MessageLib.GET_SUCCEED(key, value)).build();
                    } else {
                        response = Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build();
                    }
                }
                case DELETE -> {
                    if (store.containsKey(key)) {
                        value = store.get(key);
                        store.remove(key);
                        response = Response.newBuilder().setStatus(true).setMsg(MessageLib.DELETE_SUCCEED(key, value)).build();
                    } else {
                        response = Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build();
                    }
                }
                default ->
                        response = Response.newBuilder().setStatus(false).setMsg(MessageLib.INVALID_OPERATION).build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.log(response);
        }
    }

    public ParticipantImpl(int port, String coordinatorHost) throws IOException, InterruptedException {
        this.port = port;
        this.coordinator = coordinatorHost;
        this.store = new ConcurrentHashMap<>();
        logger = new LogHandler("Participant_" + port + "_");
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new ServerImpl())
                .executor(Executors.newFixedThreadPool(10))
                .build();

        this.start();
        System.out.printf("Participant %s started.\n", port);
        this.blockUntilShutdown();
    }

    public boolean canCommit(String tid) {
        return true;
    }

    public void doCommit(String tid) {

    }

    public void doAbort(String tid) {

    }

}
