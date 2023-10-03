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
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Invalid arguments, please follow the format: <host name/IP address> <port number> <protocol (UDP/TCP)>");
            return;
        }

        String addrStr = args[0];
        int port = Integer.parseInt(args[1]);
        String protocol = args[2];

        InetAddress ServerIPAddr;
        Client client;

        try {
            ServerIPAddr = InetAddress.getByName(addrStr);
            if (protocol.equals("TCP")) {
                client = new Client_TCP(ServerIPAddr, port);
            } else if (protocol.equals("UDP")) {
                client = new Client_UDP(ServerIPAddr, port);
            } else {
                System.err.println("Invalid input of protocol, type either TCP or UDP");
                return;
            }
        } catch (UnknownHostException e) {
            System.err.println("Cannot find the host, provide a valid address.");
            return;
        }

        System.out.println("Pre-populating with initial data.");
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
                case "1" -> {
                    System.out.print("Type the KEY: ");
                    key = scanner.nextLine();
                    System.out.print("Type the VALUE: ");
                    value = scanner.nextLine();
                    client.request(Operation.PUT, key, value);
                }
                case "2" -> {
                    System.out.print("Type the KEY: ");
                    key = scanner.nextLine();
                    client.request(Operation.GET, key, "");
                }
                case "3" -> {
                    System.out.print("Type the KEY: ");
                    key = scanner.nextLine();
                    client.request(Operation.DELETE, key, "");
                }
                default -> {
                    System.out.println("Invalid choice, please type 1, 2 or 3 to choose an operation.");
                    continue;
                }
            }
        }

        try {
            client.close();
        } catch (IOException e) {
            System.err.println("Exception on closing client.");
        }
    }
}
