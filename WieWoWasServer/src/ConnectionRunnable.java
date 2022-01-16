import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import database.*;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;


/**
 * This runnable handles a connection with a client. The socket needs to be
 * passed in the constructor. This should be run in a separate thread.
 */
public class ConnectionRunnable implements Runnable, MessageObservable.MessageSentObserver {

    private static final long ONE_DAY_IN_MS = 86400000;

    private final Socket socket;
    private final Consumer<String> messageConsumer;
    private final Runnable closeOutputStream;
    private final DatabaseInterface di;
    private final MessageObservable messageObservable;

    private boolean isRunning = true;

    private boolean userIsLoggedIn = false;
    private String username = null;
    private int userID = -1;
    private Set<Integer> joinedChatIDs = new HashSet<>();

    ConnectionRunnable(Socket socket, MessageObservable messageObservable) {
        if (socket == null)
            throw new IllegalArgumentException("socket is null");
        this.socket = socket;
        try {
            PrintWriter outputWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            messageConsumer = msg -> {
                outputWriter.println(msg);
                outputWriter.flush();
                Log.log("sent \"" + msg + "\" to \"" + username + "\"");
            };
            closeOutputStream = outputWriter::close;
        } catch (IOException e) {
            e.printStackTrace();
            Log.error(e);
            throw new IllegalStateException("couldn't get output stream");
        }
        di = DatabaseInterface.getDatabaseInterface();
        this.messageObservable = messageObservable;
    }

    /**
     * Handles the connection.
     */
    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            JsonParser jsonParser = new JsonParser();
            JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            while (socket.isConnected() && !socket.isClosed()) {
                JsonElement jsonElement = jsonParser.parse(jsonReader);
                if (jsonElement.isJsonObject()) {
                    JsonObject message = jsonElement.getAsJsonObject();
                    Log.log("received \"" + message + "\" from \"" + username + "\"");
                    handleMessage(message);
                } else {
                    break;
                }
            }
        } catch (IllegalArgumentException ignore) {
        } catch (Exception e) {
            Log.error(e);
        } finally {
            Log.log("connection with client \"" + username + "\" closed");
            messageObservable.deleteObserver(this);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            isRunning = false;
        }
    }

    synchronized void stop() {
        closeOutputStream.run();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    synchronized boolean isRunning() {
        return isRunning;
    }

    private void handleMessage(JsonObject message) {
        JsonElement messageTypeElement = message.get(Message.TYPE);
        if (messageTypeElement == null) {
            Log.error("no type specified: " + message.toString());
            return;
        }
        switch (messageTypeElement.getAsString()) {
            case Login.TYPE_REGISTER_REQUEST:
                handleRegisterRequest(message);
                break;
            case Login.TYPE_LOGIN_REQUEST:
                handleLoginRequest(message);
                break;
            case Message.TYPE_REQUEST_CHATS:
                handleChatListRequest();
                break;
            case ChatMessage.TYPE_TEXT_MESSAGE:
                handleTextMessage(message);
                break;
            case JoinMessage.TYPE_JOIN_REQUEST:
                handleJoinRequest(message);
                break;
            case LeaveMessage.TYPE_LEAVE_REQUEST:
                handleLeaveRequest(message);
                break;
            case RequestLocationsMessage.TYPE_REQUEST_LOCATIONS:
                handleLocationRequest(message);
                break;
            case ShareLocationMessage.TYPE_SHARE_LOCATION:
                handleShareLocation(message);
                break;
            case ToggleLocationSharingMessage.TYPE_TOGGLE_SHARING:
                handleToggleLocationSharing(message);
                break;
            case ChatDescription.TYPE_CREATE_CHAT:
                handleChatCreation(message);
                break;
            default:
                Log.error("illegal type request");
                ErrorMessage error = new ErrorMessage(ErrorMessage.ERROR_ILLEGAL_TYPE);
                messageConsumer.accept(error.getAsString());
        }
    }

    /**
     * Parse the json string and check if registration is possible.
     */
    private synchronized void handleRegisterRequest(JsonObject jsonObject) {
        Login login = new Login(jsonObject);
        String username = login.getUsername();
        String password = login.getPassword();
        int localUserID = di.registerUser(username, password);
        if (localUserID == -1) {
            messageConsumer.accept(Message.MESSAGE_REGISTER_FAIL);
        } else {
            messageConsumer.accept(Message.MESSAGE_REGISTER_SUCCESS);
            login(username, localUserID);
        }
    }

    /**
     * Parse the json string and check whether the credentials are valid.
     */
    private synchronized void handleLoginRequest(JsonObject jsonObject) {
        Login login = new Login(jsonObject);
        String username = login.getUsername();
        String password = login.getPassword();
        int localUserID = di.login(username, password);
        if (localUserID == -1) {
            messageConsumer.accept(Message.MESSAGE_LOGIN_FAIL);
        } else {
            messageConsumer.accept(Message.MESSAGE_LOGIN_SUCCESS);
            login(username, localUserID);
        }
    }

    private synchronized void handleChatListRequest() {
        if (userIsLoggedIn) {
            ChatDescriptionList cdl = new ChatDescriptionList(di.getAllChats(userID));
            String answer = cdl.getAsString();
            messageConsumer.accept(answer);
        }
    }

    private synchronized void handleTextMessage(JsonObject jsonObject) {
        if (userIsLoggedIn) {
            ChatMessage chatMessage = new ChatMessage(jsonObject);
            di.storeTextMessage(userID, chatMessage.getChatID(), chatMessage.getContent());
            messageObservable.notifyAll(chatMessage);
        }
    }

    /**
     * Sends the answer back to the client (the chat id if it worked and -1 if it didn't work)
     */
    private synchronized void handleChatCreation(JsonObject jsonObject) {
        if (userIsLoggedIn) {
            ChatDescription chatDescription = new ChatDescription(jsonObject);
            int chatID = di.createChat(chatDescription);
            di.joinChat(userID, chatID);
            ChatCreateResponse chatCreateResponse = new ChatCreateResponse(chatID);
            messageConsumer.accept(chatCreateResponse.getAsString());
        }
    }

    private synchronized void handleJoinRequest(JsonObject jsonObject) {
        if (userIsLoggedIn) {
            int chatID = new JoinMessage(jsonObject).getChatID();
            if (!joinedChatIDs.contains(chatID) && !di.isInChat(userID, chatID))
                if (di.joinChat(userID, chatID))
                    joinedChatIDs.add(chatID);
                else {
                    Log.error("user " + userID + " couldn't join chat " + chatID);
                    return;
                }

            // get the last couple of messages of a chat directly after joining the chat
            PreviousMessages previousMessages = new PreviousMessages(di.getMessages(chatID, ONE_DAY_IN_MS));
            String pre = previousMessages.getAsString();
            messageConsumer.accept(pre);
        }
    }

    private synchronized void handleLeaveRequest(JsonObject jsonObject) {
        if (userIsLoggedIn) {
            LeaveMessage leaveMessage = new LeaveMessage(jsonObject);
            if (di.leaveChat(userID, leaveMessage.getChatID()))
                messageConsumer.accept(LeaveMessage.MESSAGE_LEAVE_SUCCESS);
            else
                messageConsumer.accept(LeaveMessage.MESSAGE_LEAVE_FAIL);
        }
    }

    private synchronized void handleLocationRequest(JsonObject jsonObject) {
        if (userIsLoggedIn) {
            RequestLocationsMessage requestLocationsMessage = new RequestLocationsMessage(jsonObject);
            LocationResponseMessage response = new LocationResponseMessage(di.getLocations(requestLocationsMessage.getChatID()));
            messageConsumer.accept(response.getAsString());
        }
    }

    private synchronized void handleShareLocation(JsonObject jsonObject) {
        if (userIsLoggedIn) {
            ShareLocationMessage shareLocationMessage = new ShareLocationMessage(jsonObject);
            di.storeLocation(userID, shareLocationMessage.getDescription(), shareLocationMessage.getUserLatitude(), shareLocationMessage.getUserLongitude());
        }
    }

    private void handleToggleLocationSharing(JsonObject jsonObject) {
        if (userIsLoggedIn) {
            ToggleLocationSharingMessage toggleLocationSharingMessage = new ToggleLocationSharingMessage(jsonObject);
            di.set_loc_sharing(userID, toggleLocationSharingMessage.isLocationShared());
        }
    }

    private synchronized void login(String username, int userID) {
        this.username = username;
        this.userID = userID;
        this.userIsLoggedIn = true;
        // as soon as the client is logged in he will get chat updates
        messageObservable.addObserver(this);
        Log.log("client \"" + username + "\" logged in");
    }

    @Override
    public synchronized void messageSent(ChatMessage msg) {
        if (!username.equals(msg.getAuthor()) && di.isInChat(userID, msg.getChatID())) {
            messageConsumer.accept(msg.getAsString());
        }
    }
}
