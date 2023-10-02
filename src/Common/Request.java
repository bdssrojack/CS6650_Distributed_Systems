package Common;

import java.net.InetAddress;

/**
 * Class of request created by client and received and deserialized in server side
 */
public class Request implements java.io.Serializable{
    public Operation operation;
    public Protocol protocol;
    public String key, value;
    public InetAddress clientAddress;
    public int clientPort;

    public Request(Operation operation, Protocol protocol, String key, String value) {
        this.operation = operation;
        this.protocol = protocol;
        this.key = key;
        this.value = value;
    }
}
