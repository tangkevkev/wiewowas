package database;

import com.google.gson.JsonObject;

public class LeaveMessage extends Message {

    public static final String TYPE_LEAVE_REQUEST = "type_leave_request";
    private static final String CHAT_ID = "chat_id";

    public static final String MESSAGE_LEAVE_SUCCESS = createSimpleMessage("type_leave_success").toString();
    public static final String MESSAGE_LEAVE_FAIL = createSimpleMessage("type_leave_fail").toString();

    private final int chat_id;

    @SuppressWarnings("unused")
    public LeaveMessage(int chat_id) {
        this.chat_id = chat_id;
    }

    public LeaveMessage(JsonObject jo) {
        this.chat_id = jo.get(CHAT_ID).getAsInt();
    }

    public int getChatID() {
        return chat_id;
    }

    @Override
    JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_LEAVE_REQUEST);
        jo.addProperty(CHAT_ID, chat_id);
        return jo;
    }
}

