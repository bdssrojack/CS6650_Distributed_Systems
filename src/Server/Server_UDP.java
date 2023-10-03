package Server;

import Common.LogHandler;
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
            logger.logErr("Exception on establishing datagram socket.");
        }
        logger = new LogHandler("Server_"+ protocol);
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
                logger.log(request);
                return request;
            } catch (IOException e) {
                logger.logErr("Cannot establish input stream.");
            } catch (ClassNotFoundException e) {
                logger.logErr("Unable to parse request object from input stream, class not found.");
            }
        } catch (IOException e) {
            logger.logErr("Cannot receive request from input stream.");
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
            logger.logInfo(String.format("Sent response to client %s:%s.%n", request.clientAddress, request.clientPort));
        } catch (IOException e) {
            logger.logErr("Cannot write response to output stream and send to client.");
        }
    }

    void run() {
        while (true) {
            Request request = this.getRequest();
            if(request == null)
                continue;

            Response response;
            switch (request.operation) {
                case GET -> response = this.get(request.key);
                case PUT -> response = this.put(request.key, request.value);
                case DELETE -> response = this.delete(request.key);
                default -> {
                    response = new Response(false, String.format("Received an invalid operation query from %s:%s.", request.clientAddress, request.clientPort));
                    logger.log(response);
                }
            }
            this.response(response, request);
        }
    }

    @Override
    void close() {}
}
