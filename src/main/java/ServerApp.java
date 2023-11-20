import java.io.IOException;
import java.util.Arrays;

public class ServerApp {
    public static void main(String[] args) {
        // start coordinator
        new Thread(() -> {
            try {
                new CoordinatorImpl();
            } catch (IOException | InterruptedException e) {
                System.err.printf("Coordinator init failed. %s\n", e);
            }
        }).start();

        // start participants
        for (int p : Utils.replicaPorts) {
            new Thread(() -> {
                try {
                    new ParticipantImpl(p);
                } catch (IOException | InterruptedException e) {
                    System.err.printf("Participant %s init failed. %s\n", p, e);
                }
            }).start();
        }
    }
}
