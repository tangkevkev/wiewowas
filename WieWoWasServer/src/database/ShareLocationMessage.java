package database;

import com.google.gson.JsonObject;

public class ShareLocationMessage extends Message {

    public static final String TYPE_SHARE_LOCATION = "type_share_location";

    private static final String USER_LATITUDE = "user_latitude";
    private static final String USER_LONGITUDE = "user_longitude";
    private static final String USER_NAME = "user_name";
    private static final String LOCATION_DESCRIPTION = "location_description";

    private final UserLocation user_location;

    @SuppressWarnings("unused")
    public ShareLocationMessage(String type, String user_name, double user_latitude, double user_longitude, String description) {
        this.user_location = new UserLocation(user_name, description, user_latitude, user_longitude);
    }

    public ShareLocationMessage(JsonObject jo) {
        this.user_location = new UserLocation(jo.get(USER_NAME).getAsString(), jo.get(LOCATION_DESCRIPTION).getAsString(), jo.get(USER_LATITUDE).getAsDouble(), jo.get(USER_LONGITUDE).getAsDouble());
    }

    public double getUserLatitude() {
        return user_location.getLatitude();
    }

    public double getUserLongitude() {
        return user_location.getLongitude();
    }

    @SuppressWarnings("unused")
    public String getUserName() {
        return user_location.getUserName();
    }

    public String getDescription() {
        return user_location.getDescription();
    }

    @Override
    JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_SHARE_LOCATION);
        jo.addProperty(USER_NAME, user_location.getUserName());
        jo.addProperty(USER_LATITUDE, user_location.getLatitude());
        jo.addProperty(USER_LONGITUDE, user_location.getLongitude());
        jo.addProperty(LOCATION_DESCRIPTION, user_location.getDescription());
        return jo;
    }
}
