import java.io.IOException;
import java.util.Arrays;

public class ServerApp {
    public static void main(String[] args) throws InterruptedException {
        // start coordinator
        new Thread(() -> {
            try {
                new CoordinatorImpl();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();



        // start participants
        for (int p : Utils.replicaPorts) {
            new Thread(() -> {
                try {
                    new ParticipantImpl(p);
                } catch (IOException | InterruptedException e) {
                    System.err.printf("ServerApp: Participant %s init failed.\n", p);
                }
            }).start();
        }

    }
}
