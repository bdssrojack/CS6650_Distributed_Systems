import com.cs6650.server_client.Request;
import com.cs6650.server_client.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

public class LogHandler extends Logger {

    public LogHandler(String name) {
        super(name, null);
        FileHandler fileHandler;
        File dir = new File("logs");
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            fileHandler = new FileHandler("logs/" + name + new SimpleDateFormat("M-d-yyyy HH_mm").format(Calendar.getInstance().getTime()) + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SimpleFormatter simpleFormatter = new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord lr) {
                return String.format("[%1$tF %1$tT.%1$tL] [%2$s] %3$s%n", Calendar.getInstance().getTime(), lr.getLevel().getLocalizedName(), lr.getMessage());
            }
        };
        fileHandler.setFormatter(simpleFormatter);

        this.addHandler(fileHandler);
        this.setUseParentHandlers(false); //remove console handler
    }

    public void log(Response response) {
        if (response.getStatus()) {
            logInfo(response.getMsg());
        } else {
            logErr(response.getMsg());
        }
    }

    public void log(Request request) {
        String content = MessageLib.REQUEST(
                request.getOperation(),
                request.getKey(),
                request.getValue());
        logInfo(content);
    }

    public void logInfo(String content) {
        this.info(content);
        System.out.println(content);
    }

    public void logErr(String err) {
        this.severe(err);
        System.out.println(err);
    }

}
