import com.cs6650.server_client.*;
import com.google.protobuf.Empty;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParticipantImpl implements Participant {
    private int port;
    private ConcurrentHashMap<String, Request> tmp;
    private ConcurrentHashMap<String, String> store;
    private LogHandler logger;
    private final Server server;
    private final ManagedChannel coordinatorChannel;
    private final ServiceGrpc.ServiceBlockingStub coordinatorStub;

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println(" *** shutting down participant server since JVM is shutting down ***");
            try {
                ParticipantImpl.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println(" *** participant server shut down ***");
        }));
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() throws InterruptedException {
        coordinatorChannel.shutdownNow().awaitTermination(30, TimeUnit.SECONDS);
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
        //TODO: log out info

        @Override
        public void operate(Request request, StreamObserver<Response> responseObserver) {
            logger.log(request);

            // case GET
            Operation o = request.getOperation();
            String key = request.getKey();
            if (o == Operation.GET) {
                Response response;
                if (store.containsKey(key)) {
                    response = Response.newBuilder().setStatus(true).setMsg(MessageLib.GET_SUCCEED(key, store.get(key))).build();
                } else {
                    response = Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                logger.log(response);
                return;
            } else if (o == Operation.DELETE && !store.contains(key)) {
                responseObserver.onNext(Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build());
                responseObserver.onCompleted();
                return;
            } else if (o == Operation.UNRECOGNIZED) {
                responseObserver.onNext(Response.newBuilder().setStatus(false).setMsg(MessageLib.INVALID_OPERATION).build());
                responseObserver.onCompleted();
                return;
            }

            // case PUT/DELETE
            // generate transaction id
            String tid = genTid();

            // place the new operation in temperate storage
            tmp.put(tid, request);

            // inform coordinator
            Response response = coordinatorStub.newRequest(Trans.newBuilder().setTid(Tid.newBuilder().setTid(tid).build()).setRequest(request).build());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.log(response);
        }

        @Override
        public void canCommit(Trans transaction, StreamObserver<Response> responseObserver) {
            tmp.put(transaction.getTid().getTid(), transaction.getRequest());
            responseObserver.onNext(Response.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        }

        @Override
        public void doCommit(Tid tid, StreamObserver<Response> responseObserver){
            Request request = tmp.get(tid.getTid());
            Response response = null;
            Operation o = request.getOperation();
            String key = request.getKey(), value = request.getValue();
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
                case DELETE -> {
                    if (store.containsKey(key)) {
                        value = store.get(key);
                        store.remove(key);
                        response = Response.newBuilder().setStatus(true).setMsg(MessageLib.DELETE_SUCCEED(key, value)).build();
                    }
                }
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public ParticipantImpl(int port) throws IOException, InterruptedException {
        this.port = port;
        this.store = new ConcurrentHashMap<>();
        this.tmp = new ConcurrentHashMap<>();
        logger = new LogHandler("Participant_" + port + "_");

        coordinatorChannel = Grpc.newChannelBuilder(Utils.coordinator, InsecureChannelCredentials.create()).build();
        coordinatorStub = ServiceGrpc.newBlockingStub(coordinatorChannel);
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

    private String genTid() {
        return "" + port + System.currentTimeMillis();
    }

}
