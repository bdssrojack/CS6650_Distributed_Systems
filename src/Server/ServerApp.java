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

        Server server_TCP = new Server_TCP(port);
//        Server server_UDP = new Server_UDP(port);

        while (true) {
            Request reqTCP = server_TCP.getRequest();
            server_TCP.log(reqTCP);
            Response resTCP;
            switch (reqTCP.operation) {
                case GET -> resTCP = server_TCP.get(reqTCP.key);
                case PUT -> resTCP = server_TCP.put(reqTCP.key, reqTCP.value);
                case DELETE -> resTCP = server_TCP.delete(reqTCP.key);
                default -> {
                    resTCP = new Response(false, "Invalid operation query.");
                    server_TCP.log(new Response(false, String.format("Received an invalid operation query from %s:%s.", reqTCP.clientAddress, server_TCP.port)));
                }
            }
            server_TCP.response(resTCP, reqTCP);

//            Request reqUDP = server_UDP.getRequest();
//            server_UDP.log(reqUDP);
//            Response resUDP;
//            switch (reqUDP.operation) {
//                case GET -> resUDP = server_UDP.get(reqUDP.key);
//                case PUT -> resUDP = server_UDP.put(reqUDP.key, reqUDP.value);
//                case DELETE -> resUDP = server_UDP.delete(reqUDP.key);
//                default -> {
//                    resUDP = new Response(false, "Invalid operation query.");
//                    server_UDP.log(new Response(false, String.format("Received an invalid operation query from %s:%s.", reqUDP.clientAddress, server_UDP.port)));
//                }
//            }
//            server_UDP.response(resUDP, reqUDP);
        }
    }
}
