package database;

import com.google.gson.JsonObject;


/**
 * This class contains some basic message types. All other messages extend this class and implement
 * the method {@link #getAsJson()} to provide a json form of it.
 */
public abstract class Message {
    public static final String TYPE = "message_type";

    /*
     * Simple types of messages without needing an own subclass
     */
    // a request for all chats
    public static final String TYPE_REQUEST_CHATS = "type_request_chats";
    // an answer from the server that the registration succeeded
    public static final String TYPE_REGISTER_SUCCESS = "type_register_success";
    // an answer from the server that the registration failed
    public static final String TYPE_REGISTER_FAIL = "type_register_fail";
    // an answer from the server that the login succeeded
    public static final String TYPE_LOGIN_SUCCESS = "type_login_success";
    // an answer from the server that the login failed
    public static final String TYPE_LOGIN_FAIL = "type_login_fail";

    /*
     * Simple messages ready to send. Correspond with the above types of the messages.
     */
    public static final String MESSAGE_CHAT_LIST_REQUEST = createSimpleMessage(TYPE_REQUEST_CHATS).toString();
    public static final String MESSAGE_REGISTER_SUCCESS = createSimpleMessage(TYPE_REGISTER_SUCCESS).toString();
    public static final String MESSAGE_REGISTER_FAIL = createSimpleMessage(TYPE_REGISTER_FAIL).toString();
    public static final String MESSAGE_LOGIN_SUCCESS = createSimpleMessage(TYPE_LOGIN_SUCCESS).toString();
    public static final String MESSAGE_LOGIN_FAIL = createSimpleMessage(TYPE_LOGIN_FAIL).toString();


    /**
     * This method can be used to initialize a json object directly with a type.
     *
     * @param type the type of the message
     * @return a json object containing the type
     */
    static JsonObject createSimpleMessage(String type) {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, type);
        return jo;
    }

    /**
     * Get the message as json object.
     *
     * @return the json object containing all the information.
     */
    abstract JsonObject getAsJson();

    /**
     * Get the message as string for sending it to the client/server.
     *
     * @return the message as json object string
     */
    public final String getAsString() {
        return getAsJson().toString();
    }
}
