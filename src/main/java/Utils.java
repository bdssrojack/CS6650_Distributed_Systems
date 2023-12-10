import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    // address and port of server node replicas
    public static final int[] replicaPorts = {14515, 14516, 14517, 14518, 14519};
    public static final String[] replicas;
    static {
        replicas = new String[replicaPorts.length];
        for (int i = 0; i < replicaPorts.length; i++) {
            replicas[i] = "localhost:" + replicaPorts[i];
        }
    }

    /**
     * Generate the path of the log file
     * @param name name of the instance, e.g. Acceptor, Proposer
     * @return file path
     */
    public static String genLogFilePath(String name) {
        return "logs/" + name + new SimpleDateFormat("M-d-yyyy HH_mm").format(Calendar.getInstance().getTime()) + ".log";
    }

}
