package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonObject;

public class RequestLocationsMessage extends Message {

    public static final String TYPE_REQUEST_LOCATIONS = "type_request_locations";

    private static final String CHAT_ID = "chat_id";

    private final int chat_id;


    public RequestLocationsMessage(int chat_id) {
        this.chat_id = chat_id;
    }

    public RequestLocationsMessage(JsonObject jo) {
        this.chat_id = jo.get(CHAT_ID).getAsInt();
    }

    public int getChatID() {
        return chat_id;
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_REQUEST_LOCATIONS);
        jo.addProperty(CHAT_ID, chat_id);
        return jo;
    }
}