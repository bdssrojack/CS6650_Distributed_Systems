package Client;
import Common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * The entry point of client as an app, the order of console arguments are:
 * 1.host name or IP address; 2.port number; 3.protocol
 */
public class ClientApp {
    public static void main(String[] args) throws IOException {
        String addrStr = args[0];
        int port = Integer.parseInt(args[1]);
        String protocol = args[2];

        InetAddress ServerIPAddr;
        Client client;
        try{
            ServerIPAddr = InetAddress.getByName(addrStr);
            if (protocol.equals("TCP")) {
                client = new Client_TCP(ServerIPAddr, port);
            } else if (protocol.equals("UDP")) {
                client = new Client_UDP(ServerIPAddr, port);
            } else {
                System.out.println("Invalid input of protocol, type either TCP or UDP");
                return;
            }

        } catch (UnknownHostException e) {
            System.out.println("Cannot find the host by name, provide a valid address.");
            return;
        }

        client.prepopulate();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String operation, key, value;

            System.out.println("Choose the operation: 1)PUT, 2)GET, 3)DELETE. Type 'q' to exit");
            operation = scanner.nextLine();

            if (operation.equals("q")) {
                break;
            }
            switch (operation) {
                case "1":
                    System.out.print("Type the KEY: ");
                    key = scanner.nextLine();
                    System.out.print("Type the VALUE: ");
                    value = scanner.nextLine();
                    client.request(Operation.PUT, key, value);
                    break;
                case "2":
                    System.out.print("Type the KEY: ");
                    key = scanner.nextLine();
                    client.request(Operation.GET, key, "");
                    break;
                case "3":
                    System.out.print("Type the KEY: ");
                    key = scanner.nextLine();
                    client.request(Operation.DELETE, key, "");
                    break;
                default:
                    System.out.println("Invalid choice, please type 1, 2 or 3 to choose an operation.");
                    continue;
            }
        }

        client.close();
    }
}
