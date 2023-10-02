package Common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerInitiator {
    public static Logger setup(String name) {
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(name + new SimpleDateFormat("M-d-yyyy HH_mm").format(Calendar.getInstance().getTime()) + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SimpleFormatter simpleFormatter = new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord lr) {
                return String.format("[%1$tF %1$tT.%1$tL] [%2$s] %3$s%n",
                        Calendar.getInstance().getTime(),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage());
            }
        };
        fileHandler.setFormatter(simpleFormatter);

        Logger logger = Logger.getGlobal();
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false); //remove console handler
        return logger;
    }
}
