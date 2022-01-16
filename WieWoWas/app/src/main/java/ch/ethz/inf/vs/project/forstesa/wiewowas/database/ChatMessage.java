package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import android.support.annotation.NonNull;

import java.sql.Timestamp;

/**
 * Created by dell on 11/22/17.
 */

import com.google.gson.JsonObject;

/**
 * This class can be used to make communication with database easier. Also it provides a constructor
 * {@link #ChatMessage(JsonObject)} to parse a json object and it has a method {@link #getAsString()}
 * which returns a string in json format which can be sent over the network.
 */
public class ChatMessage extends Message {
    public static final String TYPE_CREATE_CHAT = "type_create_chat";
    public static final String TYPE_TEXT_MESSAGE = "type_text_message"; // type for text messages that are sent by the client

    private static final String CHAT_ID = "chat_id";
    private static final String CONTENT = "chat_message";
    private static final String AUTHOR = "chat_author";
    private static final String TIME_IN_MILLIS = "chat_timestamp";

    private final int chatID;
    private final String content;
    private final String author;
    private final long timeInMillis;

    public ChatMessage(int chat_id, String content, String author, long timeInMillis) {
        this.chatID = chat_id;
        this.content = content;
        this.author = author;
        this.timeInMillis = timeInMillis;
    }

    public ChatMessage(JsonObject jo) {
        this.chatID = jo.get(CHAT_ID).getAsInt();
        this.content = jo.get(CONTENT).getAsString();
        this.author = jo.get(AUTHOR).getAsString();
        this.timeInMillis = jo.get(TIME_IN_MILLIS).getAsLong();
    }

    public int getChatID() {
        return chatID;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public boolean belongsTo(@NonNull String username){
        return author.equals(username) ;
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_TEXT_MESSAGE);
        jo.addProperty(CHAT_ID, chatID);
        jo.addProperty(CONTENT, content);
        jo.addProperty(AUTHOR, author);
        jo.addProperty(TIME_IN_MILLIS, timeInMillis);
        return jo;
    }
}
