package Client;

import Common.*;

import java.io.*;
import java.net.*;

/**
 * Subclass of Client, worked as the UDP client
 */
public class Client_UDP extends Client {
    DatagramSocket socket;

    public Client_UDP(InetAddress serverIPAddress, int port) {
        super(serverIPAddress, port);
        protocol = Protocol.UDP;
        logger = new LogHandler("Client_"+ protocol);
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
        } catch (IOException e) {
            logger.logErr("Unable to establish socket.");
        }
        System.out.println("UDP client initiated.");
    }

    @Override
    public void request(Operation operation, String key, String value) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
            Request request = new Request(operation, Protocol.UDP, key, value);
            objOut.writeObject(request);

            DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), byteOut.toByteArray().length, serverIPAddress, port);
            socket.send(packet);
            System.out.println("UDP packet sent. Waiting for server response.");

            byte[] buffer = new byte[1024];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            System.out.println("UDP packet response received from server.");

            try(ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
                ObjectInputStream objIn = new ObjectInputStream(byteIn)){
                Response response = (Response) objIn.readObject();
                logger.log(response);
            } catch (IOException e) {
                logger.logErr("Unable to read response object from input stream.");
            }
        } catch (SocketTimeoutException e) {
            logger.log(new Response(false, String.format("UDP packet receive timed out, did not operate %s key: %s value: %s", operation.toString(), key, value)));
        } catch (IOException | ClassNotFoundException e) {
            logger.logErr("Exception on write and send request object.");
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        System.out.println("UDP client closed.");
    }
}
