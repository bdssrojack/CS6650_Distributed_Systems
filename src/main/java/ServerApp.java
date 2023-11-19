import java.io.IOException;
import java.util.Arrays;

public class ServerApp {
    public static void main(String[] args) {
        // start coordinator
        new CoordinatorImpl(Arrays.asList(Utils.replicas));

        // start participants
        for(int p : Utils.replicaPorts){
            new Thread(()->{
                try {
                    new ParticipantImpl(p, Utils.coordinator);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

    }
}
