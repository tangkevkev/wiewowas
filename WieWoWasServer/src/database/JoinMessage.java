package database;

import com.google.gson.JsonObject;

public class JoinMessage extends Message {

    public static final String TYPE_JOIN_REQUEST = "type_join_request";
    private static final String CHAT_ID = "chat_id";

    private final int chat_id;

    @SuppressWarnings("unused")
    public JoinMessage(int chat_id) {
        this.chat_id = chat_id;
    }

    public JoinMessage(JsonObject jo) {
        this.chat_id = jo.get(CHAT_ID).getAsInt();
    }

    public int getChatID() {
        return chat_id;
    }

    @Override
    JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_JOIN_REQUEST);
        jo.addProperty(CHAT_ID, chat_id);
        return jo;
    }
}
