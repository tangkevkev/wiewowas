package ch.ethz.inf.vs.project.forstesa.wiewowas.tcpclient;


import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import static ch.ethz.inf.vs.project.forstesa.wiewowas.server.ServerConfiguration.HOST;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.server.ServerConfiguration.PORT;

/**
 * Created by CNaits on 13.11.17.
 * Handles the tcp connection to the server.
 */

public class TcpConnection {
    // message to the server
    private JsonObject message;
    // handles received messages
    private OnMessageReceived messageListener = null;
    // while true, connection open
    private boolean running = false;
    // used to send messages to the server
    private PrintWriter outBuffer;

    // constructor
    TcpConnection(OnMessageReceived listener) {
        messageListener = listener;
    }

    // Sends message to server
    public void sendMessage(JsonObject message) {

        if (outBuffer != null && !outBuffer.checkError()) {
            outBuffer.println(message);
            outBuffer.flush();
        }
    }

    // Close the connection
    public void stopClient() {

        // this will lead to closing the socket in run()
        running = false;

        if (outBuffer != null) {
            outBuffer.flush();
            outBuffer.close();
        }

        messageListener = null;
        outBuffer = null;
        message = null;
    }

    void run() {

        running = true;

        try {

            InetAddress serverAddress = InetAddress.getByName(HOST);

            try (Socket socket = new Socket(serverAddress, PORT)) {


                outBuffer = new PrintWriter(socket.getOutputStream());

                try {
                    InputStream inputStream = socket.getInputStream();
                    JsonParser jsonParser = new JsonParser();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
                    JsonElement jsonElement;
                    //as long as the connection is up, the client listens for messages
                    while (running && socket.isConnected() && !socket.isClosed()) {

                        jsonElement = jsonParser.parse(jsonReader);
                        if (jsonElement.isJsonObject()) {
                            message = jsonElement.getAsJsonObject();
                            messageListener.messageReceived(message);
                        }
                    }


                } catch (IllegalArgumentException ignore) {
                    ignore.printStackTrace();
                } catch (IOException | JsonIOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                outBuffer.close();

            }
        } catch (Exception ignored) {
        }
    }


    public interface OnMessageReceived {
        void messageReceived(JsonObject message);
    }
}
