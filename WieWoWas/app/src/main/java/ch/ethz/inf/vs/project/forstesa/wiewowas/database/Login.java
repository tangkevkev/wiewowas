package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import com.google.gson.JsonObject;

/**
 * This is also used for registering users. Note that in this case change the boolean
 * {@link #isRegister} to true.
 */
public class Login extends Message {
    public static final String TYPE_LOGIN_REQUEST = "type_login_request";
    public static final String TYPE_REGISTER_REQUEST = "type_register_request";
    private static final String USERNAME = "login_request_user_name";
    private static final String PASSWORD = "login_request_password";

    private final String username;
    private final String password;
    private final boolean isRegister;

    public Login(String username, String password, boolean isRegister) {
        this.username = username;
        this.password = password;
        this.isRegister = isRegister;
    }

    public Login(JsonObject jo) {
        this.username = jo.get(USERNAME).getAsString();
        this.password = jo.get(PASSWORD).getAsString();
        String type = jo.get(TYPE).getAsString();
        switch (type) {
            case TYPE_LOGIN_REQUEST:
                isRegister = false;
                break;
            case TYPE_REGISTER_REQUEST:
                isRegister = true;
                break;
            default:
                throw new IllegalArgumentException("the type of the json object is invalid: " + type);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRegister() {
        return isRegister;
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jo = createSimpleMessage(isRegister ? TYPE_REGISTER_REQUEST : TYPE_LOGIN_REQUEST);
        jo.addProperty(USERNAME, username);
        jo.addProperty(PASSWORD, password);
        return jo;
    }
}
