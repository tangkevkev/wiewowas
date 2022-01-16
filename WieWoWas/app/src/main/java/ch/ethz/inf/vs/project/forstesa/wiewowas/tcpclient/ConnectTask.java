package ch.ethz.inf.vs.project.forstesa.wiewowas.tcpclient;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.project.forstesa.wiewowas.ChatActivity;
import ch.ethz.inf.vs.project.forstesa.wiewowas.ChatListActivity;
import ch.ethz.inf.vs.project.forstesa.wiewowas.CreateChatActivity;
import ch.ethz.inf.vs.project.forstesa.wiewowas.LoginActivity;
import ch.ethz.inf.vs.project.forstesa.wiewowas.MainActivity;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatCreateResponse;

import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatCreateResponse.TYPE_CREATE_RESPONSE;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatDescriptionList.TYPE_SEND_CHATS;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatMessage.TYPE_TEXT_MESSAGE;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.LocationResponseMessage.TYPE_LOCATION_RESPONSE;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.MESSAGE_LOGIN_FAIL;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.MESSAGE_REGISTER_FAIL;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.TYPE;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.TYPE_LOGIN_FAIL;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.TYPE_LOGIN_SUCCESS;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.TYPE_REGISTER_FAIL;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.TYPE_REGISTER_SUCCESS;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.PreviousMessages.TYPE_GET_PREVIOUS_CHAT_MESSAGES;

/**
 * Created by Christian on 28.11.17.
 * This runnable can be run in background to listen for messages from the server.
 */
public class ConnectTask implements Runnable {
    public static TcpConnection tcp;

    public ConnectTask() {
        tcp = new TcpConnection(new TcpConnection.OnMessageReceived() {
            @Override
            public void messageReceived(JsonObject message) {
                handleMessage(message);
            }
        });
    }

    @Override
    public void run() {
        tcp.run();
    }

    private void handleMessage(JsonObject... values) {
        // read types of messages
        for (JsonObject value : values) {
            String type = value.get(TYPE).getAsString();


            // handle different messages
            switch (type) {

                // response to login messages
                case TYPE_LOGIN_FAIL:
                    // schmop said writing a static field of an inactive activity is ok. hope this works
                    // used to see if login was successful or not
                    MainActivity.main_loginResponse = MESSAGE_LOGIN_FAIL;
                    LoginActivity.login_loginResponse = MESSAGE_LOGIN_FAIL;
                    break;
                case TYPE_REGISTER_FAIL:
                    MainActivity.main_loginResponse = MESSAGE_REGISTER_FAIL;
                    LoginActivity.login_loginResponse = MESSAGE_REGISTER_FAIL;
                    break;

                case TYPE_LOGIN_SUCCESS:
                case TYPE_REGISTER_SUCCESS:
                    MainActivity.main_loginResponse = "true";
                    LoginActivity.login_loginResponse = "true";
                    break;

                case TYPE_SEND_CHATS:
                    ChatListActivity.replied = true;
                    ChatListActivity.chatList = value;
                    break;

                case TYPE_CREATE_RESPONSE:
                    ChatCreateResponse cresponse = new ChatCreateResponse(value.getAsJsonObject());
                    CreateChatActivity.chatID = cresponse.getChatID();
                    break;

                case TYPE_GET_PREVIOUS_CHAT_MESSAGES:
                    ChatActivity.replied = true;
                    ChatActivity.previousMessages = value;
                    break;

                case TYPE_TEXT_MESSAGE:
                    ChatActivity.receivedMessage.add(value);
                    break;

                case TYPE_LOCATION_RESPONSE:
                    ChatActivity.locationReplied = true;
                    ChatActivity.locations = value;
                    break;

                default:
                    break;
            }
        }
    }
}