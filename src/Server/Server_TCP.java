package Server;

import Common.LoggerInitiator;
import Common.Protocol;
import Common.Request;
import Common.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server_TCP extends Server{
    ServerSocket server;
    Socket socket;
    ObjectOutputStream objOut;
    ObjectInputStream objIn;

    public Server_TCP(int port) {
        super(port);
        this.protocol = Protocol.TCP;
        logger = LoggerInitiator.setup("Server_" + protocol);
        try {
            server = new ServerSocket(port);
            socket = server.accept();
            objOut = new ObjectOutputStream(socket.getOutputStream());
            objIn = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("TCP server initiated.");
    }

    @Override
    public Request getRequest() {
        try {
            return (Request) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void response(Response response, Request request) {
        try {
            objOut.writeObject(response);
            objOut.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Response sent to client.");
    }


    @Override
    void close() {
        try {
            objOut.close();
            objIn.close();
            socket.close();
            server.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("TCP server closed.");
    }
}
