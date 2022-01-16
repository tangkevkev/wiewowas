package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonObject;

public class ToggleLocationSharingMessage extends Message {

    public static final String TYPE_TOGGLE_SHARING = "type_toggle_sharing";

    private static final String LOCATION_SHARED = "location_shared";

    private final boolean location_shared;

    @SuppressWarnings("unused")
    public ToggleLocationSharingMessage(boolean location_shared) {
        this.location_shared = location_shared;
    }

    public ToggleLocationSharingMessage(JsonObject jo) {
        this.location_shared = jo.get(LOCATION_SHARED).getAsBoolean();
    }

    public boolean isLocationShared() {
        return location_shared;
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_TOGGLE_SHARING);
        jo.addProperty(LOCATION_SHARED, location_shared);
        return jo;
    }
}
