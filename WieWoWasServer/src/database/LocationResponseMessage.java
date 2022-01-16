package database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;

public class LocationResponseMessage extends Message {

    public static final String TYPE_LOCATION_RESPONSE = "type_location_response";
    private static final String LOCATIONS = "locations";

    private final LinkedList<UserLocation> locations;

    public LocationResponseMessage(LinkedList<UserLocation> locations) {
        this.locations = locations;
    }

    @SuppressWarnings("unused")
    public LocationResponseMessage(JsonObject jo) {
        this.locations = new LinkedList<>();
        for (JsonElement je : jo.get(LOCATIONS).getAsJsonArray()) {
            this.locations.add(new UserLocation(je.getAsJsonObject()));
        }
    }

    @SuppressWarnings("unused")
    public LinkedList<UserLocation> getLocations() {
        return locations;
    }

    @Override
    JsonObject getAsJson() {
        JsonArray ja = new JsonArray();
        for (UserLocation item : locations) {
            ja.add(item.getAsJson());
        }
        JsonObject jo = createSimpleMessage(TYPE_LOCATION_RESPONSE);
        jo.add(LOCATIONS, ja);
        return jo;
    }
}
