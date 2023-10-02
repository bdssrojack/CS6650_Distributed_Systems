package Client;

import Common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Subclass of Client, worked as the TCP client
 */
public class Client_TCP extends Client {
    Socket socket;
    ObjectOutputStream objOut;
    ObjectInputStream objIn;

    public Client_TCP(InetAddress serverIPAddress, int port) {
        super(serverIPAddress, port);
        this.protocol = Protocol.TCP;
        logger = LoggerInitiator.setup("Client_" + protocol);
        try {
            socket = new Socket(serverIPAddress, port);
            socket.setSoTimeout(5000);
            objOut = new ObjectOutputStream(socket.getOutputStream());
            objIn = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("TCP client created");
    }

    @Override
    public void request(Operation operation, String key, String value) {
        Request request = new Request(operation, Protocol.TCP, key, value);
        Response response;
        try {
            objOut.writeObject(request);
            objOut.flush();
            System.out.println("TCP request sent. Waiting for response.");

            response = (Response) objIn.readObject();
            log(response);
        } catch (SocketTimeoutException e) {
            log(new Response(false, String.format("TCP socket response timed out, did not operate %s key: %s value: %s", operation.toString(), key, value)));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        objOut.close();
        objIn.close();
        socket.close();
        System.out.println("TCP client closed.");
    }

}
