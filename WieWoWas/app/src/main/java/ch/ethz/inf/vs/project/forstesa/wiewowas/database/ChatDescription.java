package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to make communication with database easier. Also it provides a constructor
 * {@link #ChatDescription(JsonObject)} to parse a json object and it has a method {@link #getAsString()}
 * which returns a string in json format which can be sent over the network.
 */
public class ChatDescription extends Message {
    public static final String TYPE_CREATE_CHAT = "type_create_chat";

    private static final String CHAT_DESCRIPTION_NAME = "chat_name";
    private static final String CHAT_DESCRIPTION_LONGITUDE = "chat_longitude";
    private static final String CHAT_DESCRIPTION_LATITUDE = "chat_latitude";
    private static final String CHAT_DESCRIPTION_RADIUS = "chat_radius";
    private static final String CHAT_DESCRIPTION_JOINED = "chat_joined";
    private static final String CHAT_DESCRIPTION_TAG_ARRAY = "chat_tag_array";
    private static final String CHAT_DESCRIPTION_CHAT_ID = "chat_id";
    private static final int INVALID_ID = -1;

    private final String chatName;
    private final double longitude;
    private final double latitude;
    private final int radius;
    /* if true the user is in this chat */
    private final boolean joined;
    private final String[] tags;
    private final int chatID;


    /**
     * This constructor can be used by the clients if they want to create a new chat.
     * Int this case they do not need to pass a chat id (because it isn't known yet).
     * In this case the chatID is set to {@link #INVALID_ID}.
     */
    public ChatDescription(String chatName, double longitude, double latitude, int radius, String[] tags) {
        this(chatName, longitude, latitude, radius, true, tags, INVALID_ID);
    }

    /**
     * This constructor is used to instantiate an ChatDescription which will be sent
     * to the client in a ChatDescriptionList. Here it is also needed to have a {@link #chatID}
     * so that the client knows it!
     */
    public ChatDescription(String chatName, double longitude, double latitude, int radius, boolean joined,
                           String[] tags, int chatID) {
        this.chatName = chatName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.joined = joined;
        this.tags = tags;
        this.chatID = chatID;
    }

    public ChatDescription(JsonObject jo) {
        this.chatName = jo.get(CHAT_DESCRIPTION_NAME).getAsString();
        this.longitude = jo.get(CHAT_DESCRIPTION_LONGITUDE).getAsDouble();
        this.latitude = jo.get(CHAT_DESCRIPTION_LATITUDE).getAsDouble();
        this.radius = jo.get(CHAT_DESCRIPTION_RADIUS).getAsInt();
        this.joined = jo.get(CHAT_DESCRIPTION_JOINED).getAsBoolean();
        this.chatID = jo.get(CHAT_DESCRIPTION_CHAT_ID).getAsInt();
        List<String> tags = new ArrayList<>();
        //jo.get(CHAT_DESCRIPTION_TAG_ARRAY).getAsJsonArray().forEach(je -> tags.add(je.getAsString()));
        JsonArray descriptionArr = jo.get(CHAT_DESCRIPTION_TAG_ARRAY).getAsJsonArray();
        for (JsonElement je : descriptionArr) { tags.add(je.getAsString()); }
        this.tags = new String[tags.size()];
        tags.toArray(this.tags);
    }

    public String getChatName() {
        return chatName;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isJoined() {
        return joined;
    }

    public String[] getTags() {
        return tags;
    }

    public int getChatID() {
        return chatID;
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, TYPE_CREATE_CHAT);
        jo.addProperty(CHAT_DESCRIPTION_NAME, chatName);
        jo.addProperty(CHAT_DESCRIPTION_LONGITUDE, longitude);
        jo.addProperty(CHAT_DESCRIPTION_LATITUDE, latitude);
        jo.addProperty(CHAT_DESCRIPTION_RADIUS, radius);
        jo.addProperty(CHAT_DESCRIPTION_JOINED, joined);
        jo.addProperty(CHAT_DESCRIPTION_CHAT_ID, chatID);

        JsonArray ja = new JsonArray();
        for (String tag : tags)
            ja.add(tag);
        jo.add(CHAT_DESCRIPTION_TAG_ARRAY, ja);

        return jo;
    }

    public boolean hasTag(String tag) {
        for (String s : tags) {
            if (s.toLowerCase().equals(tag))
                return true;
        }
        return false;
    }


    /**
     * @return true if it contains one of the tags.
     */
    public boolean hasTag(String[] tag) {
        if (tag == null || tag.length == 0) {
            return true;
        }
        for (String aTag : tag) {
            if (hasTag(aTag))
                return true;
        }
        return false;
    }

    /**
     * @return true if it contains all the tags
     */
    public boolean hasAllTags(String[] tag) {
        if (tag == null || tag.length == 0) {
            return true;
        }
        for (String aTag : tag) {
            if (!hasTag(aTag))
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        ChatDescription cd = (ChatDescription) obj;
        return (chatID == cd.chatID);
    }
}
