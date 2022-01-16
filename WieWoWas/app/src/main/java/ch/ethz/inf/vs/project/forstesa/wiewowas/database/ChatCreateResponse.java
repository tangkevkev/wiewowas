package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonObject;

/**
 * Send this message as a response to
 */
public class ChatCreateResponse extends Message {
    public static final String TYPE_CREATE_RESPONSE = "type_create_response";

    private static final String CREATE_RESPONSE_CHAT_ID = "create_response_chat_id";
    private static final int ILLEGAL_CHAT_ID = -1;

    private final int chatID;

    /**
     * Create a chat response object.
     *
     * @param chatID the id of the newly created chat.
     */
    public ChatCreateResponse(int chatID) {
        this.chatID = chatID;
    }

    /**
     * Create a chat response object just to parse the received message.
     *
     * @param jo the received string as json object.
     */
    public ChatCreateResponse(JsonObject jo) {
        this.chatID = jo.get(CREATE_RESPONSE_CHAT_ID).getAsInt();
    }

    /**
     * Use this method to find out whether the chat creation succeeded or not.
     *
     * @return true if success, false otherwise
     */
    public boolean chatCreationSuccess() {
        return chatID != ILLEGAL_CHAT_ID;
    }

    /**
     * Get the chat id. Use {@link #chatCreationSuccess()} first, to find out whether it succeeded or
     * compare value with {@link #ILLEGAL_CHAT_ID}.
     *
     * @return the chat id if success, {@link #ILLEGAL_CHAT_ID} otherwise
     */
    public int getChatID() {
        return chatID;
    }

    @Override
    JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_CREATE_RESPONSE);
        jo.addProperty(CREATE_RESPONSE_CHAT_ID, chatID);
        return jo;
    }
}