package Server;

import Common.Request;
import Common.Response;

import java.net.*;
import java.io.*;

/**
 * Entry point of the server as an app, works for both UDP and TCP
 */
public class ServerApp {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String protocol = args[1];

        Server server;

        if (protocol.equals("TCP")) {
            server = new Server_TCP(port);
        } else if (protocol.equals("UDP")) {
            server = new Server_UDP(port);
        } else {
            System.err.println("Invalid protocol type, type either TCP or UDP");
            return;
        }

        server.run();
//        while (true) {
//            Request request = server.getRequest();
//            if(request == null)
//                continue;
//            server.log(request);
//            Response response;
//            switch (request.operation) {
//                case GET -> response = server.get(request.key);
//                case PUT -> response = server.put(request.key, request.value);
//                case DELETE -> response = server.delete(request.key);
//                default -> {
//                    response = new Response(false, "Invalid operation query.");
//                    server.log(new Response(false, String.format("Received an invalid operation query from %s:%s.", request.clientAddress, request.clientPort)));
//                }
//            }
//            server.response(response, request);
//        }
    }
}
