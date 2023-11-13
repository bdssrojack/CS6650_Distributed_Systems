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
    private static final LogHandler logger = new LogHandler(ClientApp.class.getSimpleName());
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

    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:14514";
        if(args.length > 0){
            target = args[0];
        }

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        ClientApp client = new ClientApp(channel);

        client.prePop();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String operation, key, value;

            System.out.println("\nChoose the operation: 1)PUT, 2)GET, 3)DELETE. Type 'q' to exit");
            operation = scanner.nextLine();

            if (operation.equals("q")) {
                break;
            }

            System.out.print("Type the KEY: ");
            key = scanner.nextLine();

            switch (operation) {
                case "1" -> {
                    System.out.print("Type the VALUE: ");
                    value = scanner.nextLine();
                    client.operate(Operation.PUT, key, value);
                }
                case "2" -> client.operate(Operation.GET, key, "");
                case "3" -> client.operate(Operation.DELETE, key, "");
                default -> System.err.println("Invalid choice, please type 1, 2 or 3 to choose an operation.");
            }

        }

        channel.shutdownNow().awaitTermination(60, TimeUnit.SECONDS);
    }

}
