package ch.ethz.inf.vs.project.forstesa.wiewowas.server;

import com.google.gson.JsonObject;

/**
 * Example
 */
public final class Message {
    static final String TYPE = "message_type";
    static final String TYPE_ERROR = "type_error";
    static final String ERROR_CODE = "error_code";
    static final String TYPE_RETRIEVE_CHATS = "type_retrieve_chat_lists";
    static final String TYPE_RETRIEVE_MY_CHATS = "type_retrieve_my_chat_lists";
    static final String TYPE_LOGIN_REQUEST = "type_login_request";
    static final String LOGIN_REQUEST_USER_NAME = "login_request_user_name";
    static final String LOGIN_REQUEST_PASSWORD = "login_request_password";
    static final String TYPE_REGISTER_REQUEST = "type_register_request";
    static final String REGISTER_REQUEST_USER_NAME = "register_request_user_name";
    static final String REGISTER_REQUEST_PASSWORD = "register_request_password";
    static final String TYPE_REGISTER_SUCCESS = "type_register_success";
    static final String TYPE_REGISTER_FAIL = "type_register_fail";
    static final String TYPE_LOGIN_SUCCESS = "type_login_success";
    static final String TYPE_LOGIN_FAIL = "type_login_fail";
    static final String TYPE_CHAT_MESSAGE = "type_chat_message";
    static final String CHAT_MESSAGE_TIMESTAMP = "chat_timestamp";
    static final String CHAT_MESSAGE_CONTENT = "chat_message";
    static final String TYPE_CHAT_DESCRIPTION = "type_chat_description";
    static final String CHAT_DESCRIPTION_NAME = "chat_name";
    static final String CHAT_DESCRIPTION_LONGITUDE = "chat_longitude";
    static final String CHAT_DESCRIPTION_LATITUDE = "chat_latitude";
    static final String CHAT_DESCRIPTION_RADIUS = "chat_radius";

    static final int ERROR_ILLEGAL_TYPE = -1;


    static final String MESSAGE_REGISTER_SUCCESS = createSimpleMessage(TYPE_REGISTER_SUCCESS);
    static final String MESSAGE_REGISTER_FAIL = createSimpleMessage(TYPE_REGISTER_FAIL);
    static final String MESSAGE_LOGIN_SUCCESS = createSimpleMessage(TYPE_LOGIN_SUCCESS);
    static final String MESSAGE_LOGIN_FAIL = createSimpleMessage(TYPE_LOGIN_FAIL);


    private Message() {
    }

    @SuppressWarnings("Duplicates")
    static String createRegisterRequestMessage(String username, String password) {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, TYPE_REGISTER_REQUEST);
        jo.addProperty(REGISTER_REQUEST_USER_NAME, username);
        jo.addProperty(REGISTER_REQUEST_PASSWORD, password);
        return jo.toString();
    }

    @SuppressWarnings("Duplicates")
    static String createLoginRequestMessage(String username, String password) {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, TYPE_LOGIN_REQUEST);
        jo.addProperty(LOGIN_REQUEST_USER_NAME, username);
        jo.addProperty(LOGIN_REQUEST_PASSWORD, password);
        return jo.toString();
    }

    @SuppressWarnings("Duplicates")
    static String createChatMessage(String content, long timestamp) {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, TYPE_CHAT_MESSAGE);
        jo.addProperty(CHAT_MESSAGE_TIMESTAMP, timestamp);
        jo.addProperty(CHAT_MESSAGE_CONTENT, content);
        return jo.toString();
    }

    static String createChatDescription(String chatName, double longitude, double latitude, int radius) {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, TYPE_CHAT_DESCRIPTION);
        jo.addProperty(CHAT_DESCRIPTION_NAME, chatName);
        jo.addProperty(CHAT_DESCRIPTION_LONGITUDE, longitude);
        jo.addProperty(CHAT_DESCRIPTION_LATITUDE, latitude);
        jo.addProperty(CHAT_DESCRIPTION_RADIUS, radius);
        return jo.toString();
    }

    static String createError(int errorCode) {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, TYPE_ERROR);
        jo.addProperty(ERROR_CODE, errorCode);
        return jo.toString();
    }

    private static String createSimpleMessage(String type) {
        JsonObject jo = new JsonObject();
        jo.addProperty(TYPE, type);
        return jo.toString();
    }
}
