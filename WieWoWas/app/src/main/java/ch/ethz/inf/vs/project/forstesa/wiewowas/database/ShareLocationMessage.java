package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonObject;

public class ShareLocationMessage extends Message {

    public static final String TYPE_SHARE_LOCATION = "type_share_location";

    private static final String USER_LATITUDE = "user_latitude";
    private static final String USER_LONGITUDE = "user_longitude";
    private static final String USER_NAME = "user_name";
    private static final String LOCATION_DESCRIPTION = "location_description";

    private final ServerLocation server_location;

    @SuppressWarnings("unused")
    public ShareLocationMessage(String type, String user_name, double user_latitude, double user_longitude, String description) {
        this.server_location = new ServerLocation(user_name, description, user_latitude, user_longitude);
    }

    public ShareLocationMessage(JsonObject jo) {
        this.server_location = new ServerLocation(jo.get(USER_NAME).getAsString(), jo.get(LOCATION_DESCRIPTION).getAsString(), jo.get(USER_LATITUDE).getAsDouble(), jo.get(USER_LONGITUDE).getAsDouble());
    }

    public double getUserLatitude() {
        return server_location.getLatitude();
    }

    public double getUserLongitude() {
        return server_location.getLongitude();
    }

    @SuppressWarnings("unused")
    public String getUserName() {
        return server_location.getUserName();
    }

    public String getDescription() {
        return server_location.getDescription();
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_SHARE_LOCATION);
        jo.addProperty(USER_NAME, server_location.getUserName());
        jo.addProperty(USER_LATITUDE, server_location.getLatitude());
        jo.addProperty(USER_LONGITUDE, server_location.getLongitude());
        jo.addProperty(LOCATION_DESCRIPTION, server_location.getDescription());
        return jo;
    }
}
