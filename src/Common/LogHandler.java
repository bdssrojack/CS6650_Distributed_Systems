package Common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogHandler {
    Logger logger;

    public LogHandler(String name) {
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(name + new SimpleDateFormat("M-d-yyyy HH_mm").format(Calendar.getInstance().getTime()) + ".log");
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

        logger = Logger.getGlobal();
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false); //remove console handler
    }

    public void logInfo(String content) {
        logger.info(content);
        System.out.println(content);
    }

    public void logErr(String err) {
        logger.severe(err);
        System.out.println(err);
    }

    public void log(Response response) {
        if (response.status) {
            logger.info(response.content);
            System.out.println(response.content);
        } else {
            logger.severe(response.content);
            System.out.println(response.content);
        }
    }

    public void log(Request request) {
        String content = String.format("Received %s query from %s:%s, key: %s, value(if applicable): %s.",
                request.operation.toString(),
                request.clientAddress,
                request.clientPort,
                request.key,
                request.value);
        logger.info(content);
        System.out.println(content);
    }


}
