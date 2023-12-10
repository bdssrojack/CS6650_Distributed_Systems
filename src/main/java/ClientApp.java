import com.cs6650.server_client.Operation;
import com.cs6650.server_client.Request;
import com.cs6650.server_client.Response;
import com.cs6650.server_client.ServiceGrpc;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientApp {
    private static final LogHandler logger = new LogHandler(Utils.genLogFilePath(ClientApp.class.getSimpleName()));
    private final ServiceGrpc.ServiceBlockingStub blockingStub;

    public ClientApp(Channel channel) {
        blockingStub = ServiceGrpc.newBlockingStub(channel);
    }

    public void operate(Operation operation, String key, String value) {
        Request request = Request.newBuilder().setOperation(operation).setKey(key).setValue(value).build();
        logger.log(request);
        Response response = blockingStub.operate(request);
        logger.log(response);
    }

    public void prePop() {
        System.out.println("Pre-populating...");
        operate(Operation.PUT, "Amazon", "www.amazon.com");
        operate(Operation.PUT, "Twitter", "www.twitter.com");
        operate(Operation.PUT, "LinkedIn", "www.linkedin.com");
        operate(Operation.PUT, "Tesla", "www.tesla.com");
        operate(Operation.PUT, "Google", "www.google.com");
    }

    public static void main(String[] args) {
        // TODO: leader election
        String target = Utils.replicas[0];

        // connect to server
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        ClientApp client = new ClientApp(channel);
        System.out.printf("Connected to leader proposer %s\n", target);

        client.prePop();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String operation, key, value;

            System.out.println("=================================================================================================");
            System.out.println("Choose the operation: 1)PUT, 2)GET, 3)DELETE. Type 'q' to exit.");
            operation = scanner.nextLine();

            // quit
            if (operation.equals("q")) {
                break;
            }

            // change server node
//            if (operation.equals("s")) {
//                System.out.println("Choose a node to connect to by typing the index.");
//                for(int i = 0; i < Utils.replicas.length; i++){
//                    System.out.println((i+1) + ". " + Utils.replicas[i]);
//                }
//
//                int newNodeIdx = Integer.parseInt(scanner.nextLine()) - 1;
//                if(newNodeIdx < 0 || newNodeIdx >= Utils.replicas.length){
//                    System.err.printf("Invalid index, provide a number between 1 to %s.\n", Utils.replicas.length);
//                    continue;
//                }
//
//                System.out.printf("Shutting down current channel to node %s. \n", target);
//                channel.shutdownNow();
//                if(channel.isShutdown()){
//                    System.out.printf("Current channel to %s is shut down.\n", target);
//                    target = Utils.replicas[newNodeIdx];
//                    System.out.printf("Connecting to %s.\n", target);
//                    channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
//                    client = new ClientApp(channel);
//                    System.out.printf("Successfully switched connection to %s.\n", target);
//                } else {
//                    System.out.printf("Failed to switch connection, still connecting to %s.\n", target);
//                }
//                continue;
//            }

            System.out.print("Type the KEY: ");
            key = scanner.nextLine();

            switch (operation) {
                case "1" -> {
                    System.out.print("Type the VALUE: ");
                    value = scanner.nextLine();
                    if(value.isEmpty()){
                        System.err.println("Value cannot be empty.");
                        continue;
                    }
                    client.operate(Operation.PUT, key, value);
                }
                case "2" -> client.operate(Operation.GET, key, "");
                case "3" -> client.operate(Operation.DELETE, key, "");
                default -> System.err.println("Invalid choice, please type 1, 2 or 3 to choose an operation.");
            }
        }

        // RPC shutdown
        try {
            channel.shutdownNow().awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.printf("Client channel shutdown error. %s\n", e);
        }
    }

}
