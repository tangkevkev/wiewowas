package ch.ethz.inf.vs.project.forstesa.wiewowas.server;

/**
 * Created by Kevin on 15.11.2017.
 * Contains the host name and port of the server. Adapt the fields to choose between different servers.
 */

public final class ServerConfiguration {
    public static final String HOST = "wg101.hopto.org";  // here a server is running
    //public static final String HOST = "10.0.2.2";       // use with emulator and locally running server
    public static final int PORT = 4446;

    private ServerConfiguration() {
    }
}
