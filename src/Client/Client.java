package Client;
import Common.*;

import java.io.IOException;
import java.net.InetAddress;

/**
 * An abstract class for clients
 */
public abstract class Client {
    InetAddress serverIPAddress;
    int port;
    Protocol protocol;
    LogHandler logger;
    public Client(InetAddress serverIPAddress, int port) {
        this.serverIPAddress = serverIPAddress;
        this.port = port;
    }

    /**
     * Three basic operations:
     * 1) PUT (key, value)
     * 2) GET (key)
     * 3) DELETE(key)
     * Use timeout to handle unresponsive server, log out and continue
     * @param operation PUT/GET/DELETE
     * @param key key to store
     * @param value value to store
     */
    public abstract void request(Operation operation, String key, String value);

    /**
     * pre-populate some data by client in the server key-value store before interaction
     */
    public void prepopulate() {
        request(Operation.PUT, "google", "www.google.com");
        request(Operation.PUT, "Facebook", "www.facebook.com");
        request(Operation.PUT, "LinkedIn", "www.linkedin.com");
        request(Operation.PUT, "Uber", "www.uber.com");
        request(Operation.PUT, "Amazon", "www.amazon.com");
    }

    /**
     * close the client
     */
    public abstract void close() throws IOException;
}
