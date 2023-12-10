import java.io.IOException;
import java.util.Arrays;

public class ServerApp {
    public static void main(String[] args) {
        // start server nodes
        for (int p : Utils.replicaPorts) {
            new Thread(() -> {
                try {
                    new ServerNode(p);
                } catch (IOException | InterruptedException e) {
                    System.err.printf("Server node %s init failed. %s\n", p, e);
                }
            }).start();
        }
    }
}
