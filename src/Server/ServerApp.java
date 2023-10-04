package Server;

/**
 * Entry point of the server as an app, works for both UDP and TCP
 */
public class ServerApp {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Invalid arguments, please follow the format: <port number> <protocol (UDP/TCP)>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String protocol = args[1];

        Server server;

        if (protocol.equals("TCP")) {
            server = new Server_TCP(port);
        } else if (protocol.equals("UDP")) {
            server = new Server_UDP(port);
        } else {
            System.err.println("Invalid protocol type, type either TCP or UDP");
            return;
        }

        System.out.println("Server started.");
        server.run();
    }
}
