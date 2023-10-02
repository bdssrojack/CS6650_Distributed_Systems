package Client;

import Common.*;

import java.io.*;
import java.net.*;

/**
 * Subclass of Client, worked as the UDP client
 */
public class Client_UDP extends Client {
    DatagramSocket socket;
//    ByteArrayOutputStream byteOut;
//    ObjectOutputStream objOut;
//    ByteArrayInputStream byteIn;
//    ObjectInputStream objIn;

    public Client_UDP(InetAddress serverIPAddress, int port) {
        super(serverIPAddress, port);
        protocol = Protocol.UDP;
        logger = LoggerInitiator.setup("Client_"+ protocol);
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
//            byteOut = new ByteArrayOutputStream();
//            objOut = new ObjectOutputStream(byteOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void request(Operation operation, String key, String value) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
            Request request = new Request(operation, Protocol.UDP, key, value);
            objOut.writeObject(request);

            DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), byteOut.toByteArray().length, serverIPAddress, port);
            socket.send(packet);
            System.out.println("UDP packet sent. Waiting for response.");

            byte[] buffer = new byte[1024];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            try(ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
                ObjectInputStream objIn = new ObjectInputStream(byteIn)){
                Response response = (Response) objIn.readObject();
                log(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (SocketTimeoutException e) {
            log(new Response(false, String.format("UDP packet receive timed out, did not operate %s key: %s value: %s", operation.toString(), key, value)));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        System.out.println("UDP client closed.");
    }
}
