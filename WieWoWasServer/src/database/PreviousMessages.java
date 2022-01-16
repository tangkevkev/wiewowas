package database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.LinkedList;

/**
 * to enable sending the previous messages of a chat (after joining the chat) in Json format with type information
 */
public class PreviousMessages extends Message {

    private static final String TYPE_GET_PREVIOUS_CHAT_MESSAGES = "type_get_previous_chat_messages";
    private static final String PREVIOUS_CHAT_MESSAGES = "previous_chat_messages";

    private final Collection<ChatMessage> chatMessages;

    @SuppressWarnings("unused")
    public PreviousMessages(JsonObject jo) {
        this.chatMessages = new LinkedList<>();
        for (JsonElement je : jo.get(PREVIOUS_CHAT_MESSAGES).getAsJsonArray()) {
            chatMessages.add(new ChatMessage(je.getAsJsonObject()));
        }
    }

    public PreviousMessages(Collection<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @SuppressWarnings("unused")
    public Collection<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    @Override
    JsonObject getAsJson() {
        JsonArray ja = new JsonArray();
        for (ChatMessage item : chatMessages) {
            ja.add(item.getAsJson());
        }
        JsonObject jo = createSimpleMessage(TYPE_GET_PREVIOUS_CHAT_MESSAGES);
        jo.add(PREVIOUS_CHAT_MESSAGES, ja);
        return jo;
    }
}
