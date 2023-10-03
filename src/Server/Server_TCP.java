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
            Request request = (Request) objIn.readObject();
            System.out.println("Request received from client.");
            return request;
        } catch (EOFException e) {
            throw e;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("TCP server cannot read request object from input stream.");
            logger.severe("TCP server cannot read request object from input stream.");
        }
        return null;
    }

    @Override
    public void response(Response response, Request request) {
        try {
            objOut.writeObject(response);
            objOut.flush();
        } catch (IOException e) {
            System.err.println("TCP server cannot write response object to output stream.");
            logger.severe(String.format("Cannot write response: %s object to output stream to client %s:%s.", response.content, request.clientAddress, request.clientPort));
        }
        System.out.println("Response sent to client.");
    }

    void run() {
        try {
            server = new ServerSocket(port);

            while (true) {
                socket = server.accept();
                objIn = new ObjectInputStream(socket.getInputStream());
                objOut = new ObjectOutputStream(socket.getOutputStream());

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
                                this.log(new Response(false, String.format("Received an invalid operation query from %s:%s.", request.clientAddress, port)));
                            }
                        }
                        this.response(response, request);
                    } catch (EOFException e) {
                        System.err.println("Current client disconnected.");
                        logger.severe("Current client disconnected.");
                        break;
                    }
                }
                this.close();
            }
        } catch (IOException e) {
            System.err.println("Exception on socket and stream initiation.");
            logger.severe("Exception on socket and stream initiation.");
        }
    }


    @Override
    void close() {
        try {
            objOut.close();
            objIn.close();
            socket.close();
        } catch (IOException e) {
            logger.severe("Exception on closing server.");
        }
        System.out.println("TCP server closed.");
    }
}
