package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class can be used to make communication with database easier. Also it provides a constructor
 * {@link #ChatDescriptionList(JsonObject)} to parse a json object and it has a method
 * {@link #getAsString()} which returns a string in json format which can be sent over the network.
 */
public class ChatDescriptionList extends Message {
    public static final String TYPE_SEND_CHATS = "type_send_chats";

    private static final String CHATS_ARRAY = "request_chats_array";

    private final Collection<ChatDescription> chatDescriptions;

    public ChatDescriptionList(Collection<ChatDescription> chatDescriptions) {
        this.chatDescriptions = chatDescriptions;
    }

    public ChatDescriptionList(JsonObject jo) {
        this.chatDescriptions = new ArrayList<>();
        for (JsonElement je : jo.get(CHATS_ARRAY).getAsJsonArray()) {
            chatDescriptions.add(new ChatDescription(je.getAsJsonObject()));
        }
    }

    public static boolean isOfThisType(JsonObject jsonObject) {
        String type = jsonObject.get(TYPE).getAsString();
        return type.equals(TYPE_SEND_CHATS);
    }

    public Collection<ChatDescription> getChatDescriptions() { return chatDescriptions; }

    @Override
    public JsonObject getAsJson() {
        JsonArray ja = new JsonArray();
        for (ChatDescription cd : chatDescriptions) {
            ja.add(cd.getAsJson());
        }

        JsonObject jo = createSimpleMessage(TYPE_SEND_CHATS);
        jo.add(CHATS_ARRAY, ja);

        return jo;
    }
}
