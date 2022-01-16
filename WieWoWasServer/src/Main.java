import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

/**
 * This class starts listening on the port. It uses different threads to manage
 * different connections in parallel.
 */
public final class Main {
    private static final int DEFAULT_PORT = 4446;
    private static final String NO_PORT_ARG = "no port specified, using default: "
            + DEFAULT_PORT;
    private static final String ILLEGAL_PORT_ARG = "not valid format for port; using default: " + DEFAULT_PORT;

    private final int port;
    private final List<ConnectionRunnable> connectionThreads = new LinkedList<>();

    /**
     * Creates a new Main-class. Don't allow this constructor to be called by
     * another class. This class is only initialized from the main method.
     *
     * @param port the port where the server will listen
     */
    private Main(int port) {
        this.port = port;
        // this thread removes all the "dead" threads every 24h
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(86400000);
                } catch (InterruptedException e) {
                    Log.error(e);
                    break;
                }
                Log.serverLog("automated thread cleanup started");
                removeDeadThreads();
            }
        }).start();
    }

    /**
     * Initializes the main class and starts the server.
     *
     * @param args the first element should contain the port number, otherwise the
     *             default port will be used
     */
    public static void main(String[] args) {
        int port = readPort(args);
        Main main = new Main(port);
        main.startServer();
    }

    /**
     * Read the first argument as integer. If it fails (due to non-existence or
     * invalid number format), it returns the default port number.
     *
     * @param args the command line arguments
     * @return the read port number or the default port number if the input was
     * invalid
     */
    private static int readPort(String[] args) {
        if (args == null || args.length == 0) {
            Log.log(NO_PORT_ARG);
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            Log.error(ILLEGAL_PORT_ARG);
            return DEFAULT_PORT;
        }
    }

    /**
     * Start the server on the given port.
     */
    private void startServer() {
        Log.serverLog("server started on port " + port);

        MessageObservable messageObservable = new MessageObservable();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                try {
                    ConnectionRunnable connRun = new ConnectionRunnable(serverSocket.accept(), messageObservable);
                    Thread thread = new Thread(connRun);
                    connectionThreads.add(connRun);
                    thread.start();
                } catch (VirtualMachineError e) {
                    Log.error(e);
                    stopThreads();
                } catch (Throwable t) {
                    Log.error(t);
                } finally {
                    removeDeadThreads();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (connectionThreads.size() > 0)
            stopThreads();
        Log.log("server stopped");
        System.exit(0);
    }

    /**
     * Go through the list of connection threads and interrupt each one.
     */
    private void stopThreads() {
        int oldThreadCount = connectionThreads.size();
        connectionThreads.stream().filter(ConnectionRunnable::isRunning).forEach(ConnectionRunnable::stop);
        removeDeadThreads();
        int newThreadCount = connectionThreads.size();
        Log.serverLog("stopped " + (oldThreadCount - newThreadCount) + " threads");
        Log.serverLog(newThreadCount + " threads still running");
    }

    private void removeDeadThreads() {
        int oldThreadCount = connectionThreads.size();
        connectionThreads.removeIf(c -> c == null || !c.isRunning());
        int newThreadCount = connectionThreads.size();
        Log.serverLog("removed " + (oldThreadCount - newThreadCount) + " threads");
        Log.serverLog(newThreadCount + " threads still running");
    }

}
