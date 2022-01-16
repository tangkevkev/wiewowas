package database;

import com.google.gson.JsonObject;

public class UserLocation {

    private static final String USER_ID = "location_user_name";
    private static final String DESCRIPTION = "location_description";
    private static final String LATITUDE = "location_latitude";
    private static final String LONGITUDE = "location_longitude";

    private final String user_name;
    private final String description;

    private final double latitude;
    private final double longitude;

    public UserLocation(String user_name, String description, double latitude, double longitude) {
        this.user_name = user_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public UserLocation(JsonObject jo) {
        this.user_name = jo.get(USER_ID).getAsString();
        this.description = jo.get(DESCRIPTION).getAsString();
        this.latitude = jo.get(LATITUDE).getAsDouble();
        this.longitude = jo.get(LONGITUDE).getAsDouble();
    }

    public String getUserName() {
        return user_name;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public JsonObject getAsJson() {
        JsonObject jo = new JsonObject();
        jo.addProperty(USER_ID, user_name);
        jo.addProperty(DESCRIPTION, description);
        jo.addProperty(LATITUDE, latitude);
        jo.addProperty(LONGITUDE, longitude);
        return jo;
    }
}
