package Server;

import Common.LoggerInitiator;
import Common.Protocol;
import Common.Request;
import Common.Response;

import java.io.*;
import java.net.*;

public class Server_UDP extends Server{
    DatagramSocket socket;
    public Server_UDP(int port) {
        super(port);
        this.protocol = Protocol.UDP;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        logger = LoggerInitiator.setup("Server_"+ protocol);
        System.out.println("UDP server initiated.");
    }

    @Override
    public Request getRequest() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket query = new DatagramPacket(buffer, buffer.length);
            socket.receive(query);
            System.out.println("UDP packet received.");

            try(ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
                ObjectInputStream objIn = new ObjectInputStream(byteIn)) {
                Request request = (Request) objIn.readObject();
                request.clientAddress = query.getAddress();
                request.clientPort = query.getPort();
                return request;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void response(Response response, Request request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(response);

            DatagramPacket responsePacket = new DatagramPacket(byteOut.toByteArray(), byteOut.toByteArray().length, request.clientAddress, request.clientPort);
            socket.send(responsePacket);
            System.out.printf("Sent response to client %s:%s.%n", request.clientAddress, request.clientPort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void run() {
        while (true) {
            Request request = this.getRequest();
            if(request == null)
                continue;
            this.log(request);
            Response response;
            switch (request.operation) {
                case GET -> response = this.get(request.key);
                case PUT -> response = this.put(request.key, request.value);
                case DELETE -> response = this.delete(request.key);
                default -> {
                    response = new Response(false, "Invalid operation query.");
                    this.log(new Response(false, String.format("Received an invalid operation query from %s:%s.", request.clientAddress, request.clientPort)));
                }
            }
            this.response(response, request);
        }
    }

    @Override
    void close() {

    }
}
