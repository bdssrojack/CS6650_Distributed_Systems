package Server;

import Common.*;

import java.io.*;
import java.net.*;

public class Server_TCP extends Server {
    ServerSocket server;
    Socket socket;
    ObjectOutputStream objOut;
    ObjectInputStream objIn;

    public Server_TCP(int port) {
        super(port);
        this.protocol = Protocol.TCP;
        logger = LoggerInitiator.setup("Server_" + protocol);
        System.out.println("TCP server initiated.");
    }

    @Override
    public Request getRequest() throws EOFException {
        try {
            return (Request) objIn.readObject();
        } catch (EOFException e) {
            throw e;
        } catch (SocketException e) {
            System.out.println("Client disconnected.");
            return null;
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

    void run() {
        try {
            server = new ServerSocket(port);

            while (true) {
                socket = server.accept();
                objOut = new ObjectOutputStream(socket.getOutputStream());
                objIn = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    try {
                        Request request = this.getRequest();
                        if (request == null)
                            break;
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
                    } catch (EOFException e) {
                        break;
                    }
                }
                this.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    void close() {
        try {
            objOut.close();
            objIn.close();
            socket.close();
//            server.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("TCP server closed.");
    }
}
