package Client;

import Common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

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
            System.err.println("Unable to establish socket and stream.");
            logger.severe("Unable to establish socket and stream.");
        }
        System.out.println("TCP client initiated");
    }

    @Override
    public void request(Operation operation, String key, String value) {
        Request request = new Request(operation, Protocol.TCP, key, value);
        try {
            request.clientAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Unable to get local host address.");
            return;
        }
        request.clientPort = port;
        Response response;
        try {
            objOut.writeObject(request);
            objOut.flush();
            System.out.println("TCP request sent. Waiting for response.");

            response = (Response) objIn.readObject();
            System.out.println("TCP response received.");
            log(response);
        } catch (SocketTimeoutException e) {
            System.err.println("TCP socket response timed out.");
            log(new Response(false, String.format("TCP socket response timed out, did not operate %s key: %s value: %s", operation.toString(), key, value)));
        } catch (IOException e) {
            System.err.println("TCP Client cannot write request object to output stream.");
            logger.severe("TCP Client cannot write request object to output stream.");
        } catch (ClassNotFoundException e) {
            System.err.println("TCP Client cannot read response object to input stream.");
            logger.severe("TCP Client cannot read response object to input stream.");
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
