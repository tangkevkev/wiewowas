package ch.ethz.inf.vs.project.forstesa.wiewowas;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.project.forstesa.wiewowas.Location.PermissionDialogFragment;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.Login;
import ch.ethz.inf.vs.project.forstesa.wiewowas.tcpclient.ConnectTask;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    public static String main_loginResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preInit();
    }

    // Methods that have to be executed first when starting the app.
    private void preInit() {
        // For phones with virus scanner?
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // first time starting the app permission has to be given to use location services
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DialogFragment permission_dialog = new PermissionDialogFragment();
            permission_dialog.show(this.getSupportFragmentManager(), "permission");
        }

        // Starting the connection
        Thread connection = new Thread(new ConnectTask());
        connection.start();
    }


    // initialisation when the START button is clicked.
    public void onClickInit(View v) {
        //Um zu verhindern das man sich jedes mal einloggen muss.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isLoggedIn;
        String default_Value = "####FAILED####";
        String username = sharedPreferences.getString(LoginActivity.KEY_USERNAME, default_Value);
        String password = sharedPreferences.getString(LoginActivity.KEY_PASSWORD, default_Value);

        if (username.equals(default_Value) || password.equals(default_Value)) {
            isLoggedIn = false;
        } else {

            /*
             *      When they correspond: set isLoggedIn to true
             */
            // send login message to server
            JsonObject login = new Login(username, password, false).getAsJson();
            ConnectTask.tcp.sendMessage(login);

            // wait for answer. i for timeout
            int i = 0;
            while (main_loginResponse.isEmpty() && i < 50) {
                try {
                    sleep(100);
                } catch (InterruptedException ignored) {
                }
                i++;
            }

            // check if login successful. main_loginResponse is set by the task handling the server answers
            if (main_loginResponse.equals("true")) {
                isLoggedIn = true;
                main_loginResponse = "";
            } else {
                isLoggedIn = false;
                main_loginResponse = "";
            }
        }


        if (isLoggedIn) {
            Intent intent = new Intent(this, ChatListActivity.class);
            intent.putExtra(ChatListActivity.TAG_USERNAME, username);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectTask.tcp.stopClient();
    }
}
