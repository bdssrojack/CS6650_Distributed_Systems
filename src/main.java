import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

public class main {
    public static void main(String[] args) {
        try {
            InetAddress address = InetAddress.getByName("0.0.0.0");
            System.out.println(address);

            FileHandler fileHandler = new FileHandler("client " + new SimpleDateFormat("M-d-yyyy HH_mm_ss").format(Calendar.getInstance().getTime()) + ".log");

            SimpleFormatter simpleFormatter = new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format("[%1$tF %1$tT.%1$tL] [%2$s] %3$s%n",
                            java.util.Calendar.getInstance().getTime(),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            };

            fileHandler.setFormatter(simpleFormatter);
            Logger logger = Logger.getGlobal();
            logger.addHandler(fileHandler);
            logger.info("Log msg");
            logger.severe("error");
            logger.fine("fine");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
//public static void main(String[] args) throws Exception {
//    final String format = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";
//    final String key = "java.util.logging.SimpleFormatter.format";
//
//    test(format);
//    test(System.getProperty(key, format));
//    test(LogManager.getLogManager().getProperty(key));
//    test(new SimpleFormatter());
//}
//
//    private static void test(Formatter f) {
//        LogRecord record = newLogRecord();
//        System.out.println(f.format(record));
//    }
//
//    private static LogRecord newLogRecord() {
//        LogRecord r = new LogRecord(Level.INFO, "Message");
//        r.setSourceClassName("sourceClassName");
//        r.setSourceMethodName("sourceMethodName");
//        r.setLoggerName("loggerName");
//        r.setThrown(new Throwable("thrown"));
//        return r;
//    }
//
//    private static void test(String format) {
//        if (format != null) {
//            LogRecord record = newLogRecord();
//            Throwable t = record.getThrown();
//            System.out.println(String.format(format,
//                    new java.util.Date(record.getMillis()),
//                    record.getSourceClassName(),
//                    record.getLoggerName(),
//                    record.getLevel().getLocalizedName(),
//                    record.getMessage(),
//                    t != null ? t.toString() : ""));
//            //TODO: Place printStackTrace into a string.
//        } else {
//            System.out.println("Format is null.");
//        }
//    }
}
