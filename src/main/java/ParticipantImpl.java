import com.cs6650.server_client.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParticipantImpl {
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

        /**
         * Call from clients of an operation request.
         */
        @Override
        public void operate(Request request, StreamObserver<Response> responseObserver) {
            logger.log(request);

            // case GET/Invalid
            Operation o = request.getOperation();
            String key = request.getKey();
            if (o == Operation.GET) {
                Response response;
                // Thread safety: no further operation after get()
                String value = store.get(key);
                if (value != null) {
                    // key value pair exists
                    response = Response.newBuilder().setStatus(true).setMsg(MessageLib.GET_SUCCEED(key, value)).build();
                } else {
                    // no such key in store
                    response = Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                logger.log(response);
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
            Response response = coordinatorStub.prepareTransaction(Trans.newBuilder().setTid(Tid.newBuilder().setTid(tid).build()).setRequest(request).build());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.log(response);
        }

        /**
         * Call from coordinator to ask whether it can commit a transaction.
         */
        @Override
        public void canCommit(Trans transaction, StreamObserver<Response> responseObserver) {
            logger.logInfo(String.format("Preparing %s [%s].", MessageLib.REQUEST(transaction.getRequest()), transaction.getTid().getTid()));
            // store the request in temporary storage
            tmp.put(transaction.getTid().getTid(), transaction.getRequest());
            // always return true as simplification
            responseObserver.onNext(Response.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
            logger.logInfo(String.format("Prepared %s [%s].", MessageLib.REQUEST(transaction.getRequest()), transaction.getTid().getTid()));
        }

        /**
         * Call from coordinator to tell all participants to commit its part of a transaction.
         */
        @Override
        public void doCommit(Tid tid, StreamObserver<Response> responseObserver) {
            Request request = tmp.get(tid.getTid());
            tmp.remove(tid.getTid());
            Response response = null;
            Operation o = request.getOperation();
            String key = request.getKey(), value = request.getValue();

            logger.logInfo(String.format("Committing %s [%s].", MessageLib.REQUEST(request), tid.getTid()));

            switch (o) {
                case PUT -> {
                    if (value.isBlank() || value.isEmpty()) {
                        response = Response.newBuilder().setStatus(false).setMsg(MessageLib.UPDATE_FAILED(key)).build();
                    } else {
                        // Thread safety: no further operation after put()
                        String preValue = store.put(key, value);
                        if (preValue != null) {
                            // Updated existing tuple with new value
                            response = Response.newBuilder().setStatus(true).setMsg(MessageLib.UPDATE_SUCCEED(key, value)).build();
                        } else {
                            // No previous value associated with the key, new tuple added
                            response = Response.newBuilder().setStatus(true).setMsg(MessageLib.ADD_SUCCEED(key, value)).build();
                        }
                    }
                }
                case DELETE -> {
                    // Thread safety: no further operation after remove()
                    value = store.remove(key);
                    if (value != null) {
                        // key value pair removed
                        response = Response.newBuilder().setStatus(true).setMsg(MessageLib.DELETE_SUCCEED(key, value)).build();
                    } else {
                        // no such key value pair in store
                        response = Response.newBuilder().setStatus(false).setMsg(MessageLib.GET_FAILED(key)).build();
                    }
                }
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.logInfo(String.format("Committed %s [%s].", MessageLib.REQUEST(request), tid.getTid()));
        }

        /**
         * Call from coordinator to tell participant to abort its part of a transaction
         */
        @Override
        public void doAbort(Tid tid, StreamObserver<Response> responseObserver) {

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

    private String genTid() {
        return "" + port + System.currentTimeMillis();
    }

}
