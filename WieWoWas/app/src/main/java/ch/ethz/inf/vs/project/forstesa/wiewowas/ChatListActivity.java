package ch.ethz.inf.vs.project.forstesa.wiewowas;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import ch.ethz.inf.vs.project.forstesa.wiewowas.Location.UserLocation;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatDescription;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatDescriptionAdapter;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatDescriptionList;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.JoinMessage;
import ch.ethz.inf.vs.project.forstesa.wiewowas.tcpclient.ConnectTask;

import static ch.ethz.inf.vs.project.forstesa.wiewowas.ChatActivity.CHAT_ID;
import static ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message.MESSAGE_CHAT_LIST_REQUEST;

public class ChatListActivity extends AppCompatActivity {
    static final String TAG_USERNAME = "tag_username";
    static final String TAG_GROUPNAME = "tag_groupname";
    static final String TAG_ID = "tag_id";
    // used for handling response from server
    public static boolean replied = false;
    public static JsonObject chatList;
    ArrayList<String> searchedTags = new ArrayList<>();
    private String username;
    private int id;
    private ArrayList<ChatDescription> chatDescriptionArrayList;
    private ChatDescriptionAdapter chatDescriptionAdapter;
    private Context context;
    private BroadcastReceiver broadcastReceiver;
    private ChatListHandler chatListHandler;
    private boolean alreadyRetrieving;
    // users coordinates
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        this.context = this;
        init();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }

    private void init() {
        username = getIntent().getStringExtra(TAG_USERNAME);
        id = getIntent().getIntExtra(TAG_ID, -1);
        chatDescriptionArrayList = new ArrayList<>();
        chatDescriptionAdapter = new ChatDescriptionAdapter(this, R.layout.row_item, chatDescriptionArrayList);
        ListView listView = findViewById(R.id.grouplist);
        listView.setAdapter(chatDescriptionAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                int chatID = chatDescriptionArrayList.get(index).getChatID();

                JoinMessage jreq = new JoinMessage(chatID);
                ConnectTask.tcp.sendMessage(jreq.getAsJson());

                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra(TAG_USERNAME, username);
                i.putExtra(TAG_GROUPNAME, chatDescriptionAdapter.getItem(index).getChatName());
                i.putExtra(TAG_ID, id);
                i.putExtra(CHAT_ID, chatID);
                startActivity(i);
            }
        });
        retrieveChat();

    }

    public void onClickRetrieveChat(View v) {
        retrieveChat();
    }

    private void retrieveChat() {

        if (alreadyRetrieving) {
            return;
        }

        alreadyRetrieving = true;
        ConnectTask.tcp.sendMessage(MESSAGE_CHAT_LIST_REQUEST);

        chatListHandler = new ChatListHandler(this);

        // this should be quick, no need for non-blocking task
        UserLocation.requestLocation(this, new UserLocation.LocationCallback() {
            @Override
            public void onNewLocationAvailable(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();

                chatListHandler.execute(longitude, latitude, id);

            }
        });
    }

    private void filterChatList(String[] filterTags) {
        chatDescriptionAdapter.filter(filterTags);
    }

    private String[] getTags() {
        TreeSet<String> tags = new TreeSet<>();
        for (ChatDescription g : chatDescriptionArrayList) {
            for (String s : g.getTags()) {
                if (!s.trim().isEmpty())
                    tags.add(s.toLowerCase());
            }
        }
        return tags.toArray(new String[tags.size()]);
    }

    public void onClickStartCreateChatActivity(View v) {
        Intent intent = new Intent(this, CreateChatActivity.class);
        intent.putExtra(ChatListActivity.TAG_USERNAME, username);
        startActivity(intent);
    }

    public void onClickSearch(View v) {
        if (chatDescriptionArrayList.isEmpty())
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] tags = getTags();
        // Boolean array for initial selected items
        final boolean[] checkedTags = new boolean[tags.length];
        for (int i = 0; i < tags.length; i++) {
            if (searchedTags.contains(tags[i]))
                checkedTags[i] = true;
        }

        final List<String> tagList = Arrays.asList(tags);
        builder.setMultiChoiceItems(tags, checkedTags, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                // Update the current focused item's checked status
                checkedTags[which] = isChecked;
                // Get the current focused item
                String currentItem = tagList.get(which);

                // Notify the current action
                Toast.makeText(getApplicationContext(),
                        currentItem + " " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });
        // Specify the dialog is not cancelable
        builder.setCancelable(false);

        // Set a title for alert dialog
        builder.setTitle("Filter with tags");

        // Set the positive/yes button click listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click positive button
                List<String> result = new ArrayList<>();
                for (int i = 0; i < checkedTags.length; i++) {
                    boolean checked = checkedTags[i];
                    if (checked) {
                        result.add(tagList.get(i));
                        searchedTags.add(tagList.get(i));
                    } else {
                        if (searchedTags.contains(tagList.get(i))) {
                            searchedTags.remove(tagList.get(i));
                            result.remove(tagList.get(i));
                        }
                    }

                }
                filterChatList(result.toArray(new String[result.size()]));
            }
        });

        // Set the negative/no button click listener
        builder.setNegativeButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                searchedTags.clear();
                chatDescriptionAdapter.reset();
            }
        });

        // Set the neutral/cancel button click listener
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click the neutral button
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }

    // methods used for asynchron execution:
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("LIST_RETRIEVED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context p_context, Intent p_intent) {
                LinkedList<ChatDescription> chatList = null;

                if (chatListHandler == null) {
                    return;
                }

                try {
                    chatList = (LinkedList<ChatDescription>) chatListHandler.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (chatList == null) {
                    Toast toast = Toast.makeText(ChatListActivity.this, R.string.chat_list_error, Toast.LENGTH_LONG);
                    toast.show();
                } else if (chatList.isEmpty()) {
                    Toast toast = Toast.makeText(ChatListActivity.this, R.string.no_chats, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    chatDescriptionAdapter.clear();
                    for (ChatDescription cd : chatList) {
                        if (!chatDescriptionAdapter.contains(cd))
                            chatDescriptionAdapter.add(cd);
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    public class ChatListHandler extends AsyncTask<Object, List<ChatDescription>, List<ChatDescription>> {

        private Context context;

        ChatListHandler(Context context) {
            this.context = context;

        }

        private LinkedList<ChatDescription> getList(JsonObject joChats, double longitude, double latitude) {
            if (!joChats.isJsonNull()) {
                ChatDescriptionList chatList = new ChatDescriptionList(joChats);
                Collection<ChatDescription> chats = chatList.getChatDescriptions();
                LinkedList<ChatDescription> result = new LinkedList<>();

                // Create location for the current location
                UserLocation.GPSCoordinates loc = new UserLocation.GPSCoordinates(latitude, longitude);

                for (ChatDescription cd : chats) {
                    // create location for every chat
                    UserLocation.GPSCoordinates origin = new UserLocation.GPSCoordinates(cd.getLatitude(), cd.getLongitude());

                    // check if user close enough to location or user already joined
                    if (loc.distance(origin) < (float) cd.getRadius() || cd.isJoined()) {
                        result.add(cd);
                    }
                }
                return result;
            } else {
                return null;
            }
        }

        @Override
        protected List<ChatDescription> doInBackground(Object[] params) {
            int i = 0;
            while (!ChatListActivity.replied && i < 200) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return null;
                }
                i++;
            }

            JsonObject chat = ChatListActivity.chatList;
            if (ChatListActivity.replied && !chat.isJsonNull()) {

                // set to false for the next retrieve
                ChatListActivity.replied = false;
                double lng = (double) params[0];
                double lat = (double) params[1];

                return getList(chat, lng, lat);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ChatDescription> chatList) {
            alreadyRetrieving = false;
            Intent intent = new Intent("LIST_RETRIEVED");
            context.sendBroadcast(intent);
        }

    }

}
