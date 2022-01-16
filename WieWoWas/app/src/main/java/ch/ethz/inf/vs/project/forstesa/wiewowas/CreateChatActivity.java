package ch.ethz.inf.vs.project.forstesa.wiewowas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.project.forstesa.wiewowas.Location.UserLocation;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatDescription;
import ch.ethz.inf.vs.project.forstesa.wiewowas.tcpclient.ConnectTask;

public class CreateChatActivity extends AppCompatActivity {
    public static int chatID = -2;
    private SeekBar seekBar;
    private double longitude;
    private double latitude;
    private EditText groupname;
    private EditText tag1;
    private EditText tag2;
    private EditText tag3;
    private String author;
    private boolean alreadyCreating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void init() {

        author = this.getIntent().getStringExtra(ChatListActivity.TAG_USERNAME);

        groupname = findViewById(R.id.name_field);
        seekBar = findViewById(R.id.Seekbar);
        tag1 = findViewById(R.id.tag1);
        tag2 = findViewById(R.id.tag2);
        tag3 = findViewById(R.id.tag3);

        final TextView seekValue = findViewById(R.id.valueSeekbar);
        seekValue.setText("1m");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                seekValue.setText(String.format("%sm", String.valueOf(value)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void onClickCreateChat(View v) {

        if (alreadyCreating) {
            return;
        }

        alreadyCreating = true;

        // retrieve coordinates and save them in longitude/latitude
        UserLocation.requestLocation(this, new UserLocation.LocationCallback() {
            @Override
            public void onNewLocationAvailable(Location location) {

                longitude = location.getLongitude();
                latitude = location.getLatitude();

                send_and_receive();
            }
        });

    }

    private void send_and_receive() {
        String chatName = groupname.getText().toString();

        int radius = seekBar.getProgress();
        String[] tags = new String[]{tag1.getText().toString(), tag2.getText().toString(), tag3.getText().toString()};


        ChatDescription newChat = new ChatDescription(chatName, longitude, latitude, radius, tags);
        JsonObject joChat = newChat.getAsJson();
        ConnectTask.tcp.sendMessage(joChat);


        int i = 0;
        while (CreateChatActivity.chatID < -1 && i < 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (chatID > 0) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatListActivity.TAG_GROUPNAME, chatName);
            intent.putExtra(ChatActivity.CHAT_ID, chatID);
            intent.putExtra(ChatListActivity.TAG_USERNAME, author);
            intent.putExtra(ChatActivity.NEW_CHAT, true);

            chatID = -1;
            alreadyCreating = false;

            startActivity(intent);
        } else {
            alreadyCreating = false;
            Toast toast = Toast.makeText(this, R.string.create_chat_error, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
