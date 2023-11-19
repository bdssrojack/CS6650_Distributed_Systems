public class Utils {
    // address and port of participant replicas
    public static final int[] replicaPorts = {14515, 14516, 14517, 14518, 14519};
    public static final String[] replicas;
    static {
        replicas = new String[replicaPorts.length];
        for (int i = 0; i < replicaPorts.length; i++) {
            replicas[i] = "localhost:" + replicaPorts[i];
        }
    }

    // address and port of coordinator
    public static final int coordinatorPort = 14514;
    public static final String coordinator = "localhost:"+coordinatorPort;

}
