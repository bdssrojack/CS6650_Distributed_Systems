package Server;

import Common.LoggerInitiator;
import Common.Protocol;
import Common.Request;
import Common.Response;

import java.io.*;
import java.net.*;

public class Server_UDP extends Server{
    public Server_UDP(int port) {
        super(port);
        this.protocol = Protocol.UDP;
        logger = LoggerInitiator.setup("Server_"+ protocol);
        System.out.println("UDP server initiated.");
    }

    @Override
    public Request getRequest() {
        try (DatagramSocket socket = new DatagramSocket(port)){
            byte[] buffer = new byte[1024];
            DatagramPacket query = new DatagramPacket(buffer, buffer.length);
            socket.receive(query);

            try(ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
                ObjectInputStream objIn = new ObjectInputStream(byteIn)) {

                System.out.println("UDP packet received.");

                Request request = (Request) objIn.readObject();
                request.clientAddress = query.getAddress(); //127.0.0.1
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
        try (DatagramSocket socket = new DatagramSocket(port);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(response);
            byte[] responseData = byteOut.toByteArray();

            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, request.clientAddress, request.clientPort);
            socket.send(responsePacket);
            System.out.printf("Sent response to client %s:%s.%n", request.clientAddress, request.clientPort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void close() {

    }
}
