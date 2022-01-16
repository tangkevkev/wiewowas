package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonObject;

public class JoinMessage extends Message {

    public static final String TYPE_JOIN_REQUEST = "type_join_request";
    private static final String CHAT_ID = "chat_id";

    public static final String MESSAGE_JOIN_SUCCESS = createSimpleMessage("type_join_success").toString();
    public static final String MESSAGE_JOIN_FAIL = createSimpleMessage("type_join_fail").toString();

    private final int chat_id;

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
    public JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_JOIN_REQUEST);
        jo.addProperty(CHAT_ID, chat_id);
        return jo;
    }
}
