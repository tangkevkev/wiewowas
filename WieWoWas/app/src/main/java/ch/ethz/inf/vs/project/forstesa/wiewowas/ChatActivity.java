package ch.ethz.inf.vs.project.forstesa.wiewowas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.ethz.inf.vs.project.forstesa.wiewowas.Location.UserLocation;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatMessage;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ChatMessageAdapter;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.LeaveMessage;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.LocationResponseMessage;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.PreviousMessages;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.RequestLocationsMessage;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ServerLocation;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ShareLocationMessage;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.ToggleLocationSharingMessage;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.User;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.UserAdapter;
import ch.ethz.inf.vs.project.forstesa.wiewowas.tcpclient.ConnectTask;

public class ChatActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    static final String CHAT_ID = "chat_id";
    static final String NEW_CHAT = "new_chat";
    public static int chatId;
    // for loading the last messages
    public static JsonObject previousMessages;
    public static boolean replied = false;
    // for new message receives
    public static LinkedList<JsonObject> receivedMessage = new LinkedList<>();
    public static boolean locationReplied;
    public static JsonObject locations;
    Thread singleMessageReceiver;
    private UserAdapter userAdapter;
    //Used for messageview (chat)
    private ChatMessageAdapter messageAdapter;
    private String author;
    private EditText editTextSendMessage;
    private BroadcastReceiver broadcastReceiver;
    private MessageHandler messageHandler;
    private boolean shouldContinue = true;
    private boolean is_new_chat;
    // location
    private double longitude;
    private double latitude;
    private EditText locationDescription;
    private View refreshButton;
    private locationHandler locationHandler;
    private BroadcastReceiver locationReceiver;
    private boolean locationRetrieving;

    private final String IS_SHARING = "IS_SHARING";
    private final String LOCATION_NAME = "LOCATION_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
    }

    private void init() {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        refreshButton = findViewById(R.id.buttonRefresh);
        final Switch sharingSwitch = findViewById(R.id.switchLocation);
        locationDescription = findViewById(R.id.tv_shareLocation);
        locationDescription.setText(sharedPreferences.getString(LOCATION_NAME, ""));
        sharingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    sharedPreferences.edit().putBoolean(IS_SHARING, true).apply();

                    ToggleLocationSharingMessage activate = new ToggleLocationSharingMessage(true);
                    ConnectTask.tcp.sendMessage(activate.getAsJson());

                        sendLocation(locationDescription.getText().toString());

                }
                else {

                    sharedPreferences.edit().putBoolean(IS_SHARING, false).apply();

                    // send message to tell the server location sharing is deactivated
                    ToggleLocationSharingMessage deactivate = new ToggleLocationSharingMessage(false);
                    ConnectTask.tcp.sendMessage(deactivate.getAsJson());
                }
            }
        });
        sharingSwitch.setChecked(sharedPreferences.getBoolean(IS_SHARING, false));
        locationDescription.addTextChangedListener(new TextWatcher() {
            final android.os.Handler handler = new android.os.Handler();
            Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        String locationName = s.toString();
                        if (sharingSwitch.isChecked()) {
                            sendLocation(locationName);
                        }

                        sharedPreferences.edit().putString(LOCATION_NAME, locationName).apply();
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        });

        chatId = getIntent().getIntExtra(ChatActivity.CHAT_ID, 0);
        is_new_chat = getIntent().getBooleanExtra(ChatActivity.NEW_CHAT, false);

        author = getIntent().getStringExtra(ChatListActivity.TAG_USERNAME);
        setTitle(getIntent().getStringExtra(ChatListActivity.TAG_GROUPNAME));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        editTextSendMessage = findViewById(R.id.edittext_sendMsg);

        receivedMessage.clear();
        shouldContinue = true;

        //UserLocation init
        //Init the listview for the slidebar (navigationview)
        ArrayList<User> userArrayList = new ArrayList<>();
        userAdapter = new UserAdapter(this, R.layout.user_row_item, userArrayList);

        ListView listViewUser = (ListView) findViewById(R.id.listSlider);
        listViewUser.setAdapter(userAdapter);

        ListView listViewMessage = (ListView) findViewById(R.id.listViewMessage);
        listViewMessage.setDivider(null);
        messageAdapter = new ChatMessageAdapter(getApplicationContext(), R.layout.msg_right, author );
        listViewMessage.setAdapter(messageAdapter);
        //Init our chat as listview

        // Load old messages
        messageHandler = new MessageHandler(this);
        messageHandler.execute();

        singleMessageReceiver = new Thread(new singleMessageHandler(this));
        singleMessageReceiver.start();

    }

    //Add Messages to the chat
    void addMessage(@NonNull List<ChatMessage> chatMessages) {
        for(ChatMessage cm : chatMessages){
            receivedMessage.add(cm.getAsJson());
        }
    }

    void addMessage(@NonNull ChatMessage cm) {
        receivedMessage.add(cm.getAsJson());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ChatListActivity.class);
        intent.putExtra(ChatListActivity.TAG_USERNAME, author);
        startActivity(intent);
    }

    /**
     * NEW
     */

    public void onClickRefreshLocation(View v) {
        //Breiti Animation
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.button_rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        refreshButton.startAnimation(rotation);

        if (locationRetrieving) {
            return;
        }

        locationRetrieving = true;
        RequestLocationsMessage requestLocations = new RequestLocationsMessage(chatId);
        ConnectTask.tcp.sendMessage(requestLocations.getAsJson());

        locationHandler = new locationHandler(this);

        UserLocation.requestLocation(this, new UserLocation.LocationCallback() {
            @Override
            public void onNewLocationAvailable(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();

                locationHandler.execute(longitude, latitude);
            }
        });

        //Zum Breiti Animation stoppe: refreshButton.clearAnimation();
    }

    public void sendLocation(final String locationName) {

        UserLocation.requestLocation(this, new UserLocation.LocationCallback() {
            @Override
            public void onNewLocationAvailable(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();

                ShareLocationMessage sendLocation = new ShareLocationMessage(ShareLocationMessage.TYPE_SHARE_LOCATION, author, latitude, longitude, locationName);

                ConnectTask.tcp.sendMessage(sendLocation.getAsJson());

            }
        });
    }


    public void onClickSendMessage(View v) {
        if (editTextSendMessage.getText().toString().isEmpty())
            return;
        String msg = editTextSendMessage.getText().toString();

        ChatMessage chatMessage = new ChatMessage(chatId, msg, author, System.currentTimeMillis());
        messageAdapter.add(chatMessage);

        messageAdapter.notifyDataSetChanged();
        editTextSendMessage.setText("");

        ConnectTask.tcp.sendMessage(chatMessage.getAsJson());
    }

    public void onClickLeave(View v) {
        LeaveMessage leave = new LeaveMessage(chatId);
        ConnectTask.tcp.sendMessage(leave.getAsJson());
        Intent intent = new Intent(this, ChatListActivity.class);
        intent.putExtra(ChatListActivity.TAG_USERNAME, author);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // methods used for asynchron execution:
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(locationReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("MESSAGES_RECEIVED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context p_context, Intent p_intent) {
                LinkedList<ChatMessage> messageList = null;

                if (messageHandler == null) {
                    return;
                }

                try {
                    messageList = (LinkedList<ChatMessage>) messageHandler.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (is_new_chat) {
                    return;
                } else if (messageList == null) {
                    Toast toast = Toast.makeText(ChatActivity.this, R.string.message_list_error, Toast.LENGTH_LONG);
                    toast.show();
                } else if (messageList.isEmpty()) {
                    Toast toast = Toast.makeText(ChatActivity.this, R.string.no_messages, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    while (!messageList.isEmpty()) {
                        ChatMessage msg = messageList.getFirst();
                        messageAdapter.add(msg);
                        messageList.remove(msg);
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        shouldContinue = true;

        IntentFilter locationFilter = new IntentFilter("LOCATIONS_RECEIVED");
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                LinkedList<User> locationList = null;
                userAdapter.clear();

                if (locationHandler == null) {
                    return;
                }

                try {
                    locationList = (LinkedList<User>) locationHandler.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (locationList == null) {
                    Toast toast = Toast.makeText(ChatActivity.this, R.string.location_list_error, Toast.LENGTH_LONG);
                    toast.show();
                } else if (locationList.isEmpty() || (locationList.size() == 1 && locationList.getFirst().getName().equals(author))) {
                    Toast toast = Toast.makeText(ChatActivity.this, R.string.no_locations, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    UserLocation.GPSCoordinates user = new UserLocation.GPSCoordinates(latitude, longitude);

                    while (!locationList.isEmpty()) {
                        User loc = locationList.getFirst();
                        locationList.removeFirst();
                        if (!loc.getName().equals(author) && !loc.getLocationName().isEmpty()) {
                            // UserLocation.GPSCoordinates coord = new UserLocation.GPSCoordinates(loc.getLatitude(), loc.getLongitude());
                            userAdapter.add(loc);
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                }
                locationRetrieving = false;
                refreshButton.clearAnimation();
            }
        };
        registerReceiver(locationReceiver, locationFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shouldContinue = false;
        receivedMessage.clear();
    }


    // this AsyncTask loads the last few messages the server sends when entering a chat
    @SuppressLint("StaticFieldLeak")
    public class MessageHandler extends AsyncTask<Object, List<ChatMessage>, List<ChatMessage>> {

        private Context context;

        public MessageHandler(Context context) {
            this.context = context;

        }

        private LinkedList<ChatMessage> getMessages(JsonObject joMessages) {

            if (!joMessages.isJsonNull()) {

                // initialise result list
                LinkedList<ChatMessage> result = new LinkedList<ChatMessage>();
                PreviousMessages messagesList = new PreviousMessages(joMessages);
                Collection<ChatMessage> messages = messagesList.getChatMessages();

                for (ChatMessage msg : messages) {
                    // add every message to the result
                    result.add(msg);
                }

                return result;

            } else {
                return null;
            }
        }

        @Override
        protected List<ChatMessage> doInBackground(Object[] params) {
            int i = 0;
            while (!replied && i < 100) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return null;
                }
                i++;
            }

            JsonObject msg = previousMessages;
            if (replied && !msg.isJsonNull()) {
                // set to false for the next retrieve
                replied = false;

                return getMessages(msg);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ChatMessage> msg) {
            Intent intent = new Intent("MESSAGES_RECEIVED");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.sendBroadcast(intent);
        }

    }

    // this thread handles now incoming messages
    public class singleMessageHandler implements Runnable {

        private Activity activity;

        private singleMessageHandler(Activity act) {
            this.activity = act;
        }

        @Override
        public void run() {

            while (shouldContinue) {
                while (ChatActivity.receivedMessage.isEmpty()) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                final JsonObject jo = receivedMessage.pollFirst();
                int messageChatID = jo.get(CHAT_ID).getAsInt();
                if (messageChatID == chatId) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageAdapter.add(new ChatMessage(jo));
                        }
                    });
                }
            }
            return;
        }
    }

    // handles location updates
    public class locationHandler extends AsyncTask<Object, List<User>, List<User>> {

        private Context context;

        public locationHandler(Context context) {
            this.context = context;
        }

        private LinkedList<User> sortLocations(JsonObject joLocation, final double longitude, final double latitude) {

            if (!joLocation.isJsonNull()) {
                LocationResponseMessage locs = new LocationResponseMessage(joLocation);
        /*        LinkedList<ServerLocation> result = locs.getLocations();
                final UserLocation.GPSCoordinates user = new UserLocation.GPSCoordinates(latitude, longitude);
                Collections.sort(result, new Comparator<ServerLocation>() {
                    @Override
                    public int compare(ServerLocation o1, ServerLocation o2) {
                        UserLocation.GPSCoordinates coord1 = new UserLocation.GPSCoordinates(o1.getLatitude(), o2.getLongitude());
                        UserLocation.GPSCoordinates coord2 = new UserLocation.GPSCoordinates(o2.getLatitude(), o1.getLongitude());
                        if (coord1.distance(user) < coord2.distance(user)) {
                            return -1;
                        }
                        else {
                            return 1;
                        }
                    }
                }); */
                LinkedList<User> result = locs.getUserList(latitude, longitude);
                Collections.sort(result, new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        if (o1.getDistanceTo() < o2.getDistanceTo()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                return result;
            }
            else {
                return null;
            }
        }

        @Override
        protected List<User> doInBackground(Object[] params) {
            int i = 0;
            while(!locationReplied && i < 100) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return null;
                }
                i++;
            }
            final JsonObject loc = locations;
            if (locationReplied && !loc.isJsonNull()) {
                locationReplied = false;

                double lng = (double) params[0];
                double lat = (double) params[1];

                return sortLocations(loc, lng, lat);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<User> locations) {
            Intent intent = new Intent("LOCATIONS_RECEIVED");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.sendBroadcast(intent);
        }
    }
}