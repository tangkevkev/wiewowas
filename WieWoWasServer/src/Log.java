import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class can be used to log things so that it is accessible over the web (if running on the server)
 */
public class Log {

    private static final File LOG_FILE = new File("log.txt");
    private static final File SERVER_LOG_FILE = new File("server_log.txt");
    private static final File ERROR_FILE = new File("error.txt");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static PrintWriter logWriter;
    private static PrintWriter serverLogWriter;
    private static PrintWriter errorWriter;

    static {
        try {
            logWriter = new PrintWriter(LOG_FILE);
            serverLogWriter = new PrintWriter(SERVER_LOG_FILE);
            errorWriter = new PrintWriter(ERROR_FILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("log output will only be written to standard output and standard error");
            errorWriter = serverLogWriter = logWriter = new PrintWriter(new OutputStream() {
                @Override
                public void write(int b) {
                    // just discard input
                }
            });
        }
    }

    /**
     * Write a new line with the message. Also prints it to standard output.
     *
     * @param msg the logged message
     */
    synchronized static void log(String msg) {
        String formatted = getFormatted(msg);
        logWriter.println(formatted);
        logWriter.flush();
        System.out.println(formatted);
    }

    /**
     * Write a new line with the message. This is used for server logging (such as how many current connections)
     *
     * @param msg the log message
     */
    synchronized static void serverLog(String msg) {
        serverLogWriter.println(getFormatted(msg));
        serverLogWriter.flush();
    }

    /**
     * Write a new line with the message. Also prints it to standard error output.
     *
     * @param msg the logged error message
     */
    synchronized static void error(String msg) {
        String formatted = getFormatted(msg);
        errorWriter.println(formatted);
        errorWriter.flush();
        System.err.println(formatted);
    }

    /**
     * Write the stack trace of an exception. Also prints it to standard error output.
     *
     * @param t the logged error message
     */
    synchronized static void error(Throwable t) {
        errorWriter.println(getFormatted(t.getMessage()));
        t.printStackTrace(errorWriter);
        errorWriter.flush();
        t.printStackTrace();
    }

    private static String getFormatted(String msg) {
        return String.format("%s: %s", SDF.format(new Date()), msg);
    }
}
