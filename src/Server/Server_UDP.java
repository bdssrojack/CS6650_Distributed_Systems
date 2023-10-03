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
            System.err.println("Exception on establishing datagram socket.");
            logger.severe("Exception on establishing datagram socket.");
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
            System.out.println("UDP packet received from client.");

            try(ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
                ObjectInputStream objIn = new ObjectInputStream(byteIn)) {
                Request request = (Request) objIn.readObject();
                request.clientAddress = query.getAddress();
                request.clientPort = query.getPort();
                return request;
            } catch (IOException e) {
                System.err.println("Cannot establish input stream.");
                logger.severe("Cannot establish input stream.");
            } catch (ClassNotFoundException e) {
                System.err.println("Unable to parse request object from input stream, class not found.");
                logger.severe("Unable to parse request object from input stream, class not found.");
            }
        } catch (IOException e) {
            System.err.println("Cannot receive request from input stream.");
            logger.severe("Cannot receive request from input stream.");
        }
        return null;
    }

    @Override
    public void response(Response response, Request request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(response);

            DatagramPacket responsePacket = new DatagramPacket(byteOut.toByteArray(), byteOut.toByteArray().length, request.clientAddress, request.clientPort);
            socket.send(responsePacket);
            System.out.printf("Sent response to client %s:%s.%n", request.clientAddress, request.clientPort);
        } catch (IOException e) {
            System.err.println("Cannot write response to output stream and send to client.");
            logger.severe("Cannot write response to output stream and send to client.");
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
    void close() {}
}
