package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;

import ch.ethz.inf.vs.project.forstesa.wiewowas.Location.UserLocation;

public class LocationResponseMessage extends Message {

    public static final String TYPE_LOCATION_RESPONSE = "type_location_response";
    private static final String LOCATIONS = "locations";

    private final LinkedList<ServerLocation> locations;

    public LocationResponseMessage(LinkedList<ServerLocation> locations) {
        this.locations = locations;
    }

    public LocationResponseMessage(JsonObject jo) {
        this.locations = new LinkedList<>();
        for (JsonElement je : jo.get(LOCATIONS).getAsJsonArray()) {
            this.locations.add(new ServerLocation(je.getAsJsonObject()));
        }
    }

    public LinkedList<ServerLocation> getLocations() {
        return locations;
    }

    @Override
    JsonObject getAsJson() {
        JsonArray ja = new JsonArray();
        for (ServerLocation item : locations) {
            ja.add(item.getAsJson());
        }
        JsonObject jo = createSimpleMessage(TYPE_LOCATION_RESPONSE);
        jo.add(LOCATIONS, ja);
        return jo;
    }

    public LinkedList<User> getUserList(double latitude, double longitude) {
        LinkedList<User> result = new LinkedList<>();
        LinkedList<ServerLocation> serverLocations = this.getLocations();
        UserLocation.GPSCoordinates user = new UserLocation.GPSCoordinates(latitude, longitude);
        for (ServerLocation location : serverLocations) {
            UserLocation.GPSCoordinates loc = new UserLocation.GPSCoordinates(location.getLatitude(), location.getLongitude());
            result.add(new User(location.getUserName(), loc.distance(user), location.getDescription()));
        }
        return result;
    }
}
