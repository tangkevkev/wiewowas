package database;

import com.google.gson.JsonObject;

/**
 * This class is used to send error messages. It provides a constructor
 * {@link #ErrorMessage(JsonObject)} to parse a json object and it has a method
 * {@link #getAsString()} which returns a string in json format which can be sent over the network.
 */
public class ErrorMessage extends Message {
    public static final String TYPE_ERROR = "type_error";
    public static final int ERROR_ILLEGAL_TYPE = -1;

    private static final String ERROR_CODE = "error_code";

    private final int errorCode;


    public ErrorMessage(int errorCode) {
        this.errorCode = errorCode;
    }

    @SuppressWarnings("unused")
    public ErrorMessage(JsonObject jo) {
        this.errorCode = jo.get(ERROR_CODE).getAsInt();
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(TYPE_ERROR);
        jo.addProperty(ERROR_CODE, errorCode);
        return jo;
    }
}
