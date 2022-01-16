package ch.ethz.inf.vs.project.forstesa.wiewowas;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.project.forstesa.wiewowas.database.Login;
import ch.ethz.inf.vs.project.forstesa.wiewowas.database.Message;
import ch.ethz.inf.vs.project.forstesa.wiewowas.tcpclient.ConnectTask;

public class LoginActivity extends AppCompatActivity {

    final static String KEY_IS_LOGGED_IN = "KEY_LOGGED_IN";
    final static String KEY_USERNAME = "KEY_USERNAME";
    final static String KEY_PASSWORD = "KEY_PASSWORD";
    private final static String ERROR_PASSWORD = "invalid password";
    private final static String ERROR_USERNAME = "invalid username";
    public static String login_loginResponse = "";
    static SharedPreferences prefs;
    //Field for entering username. Possible to extend it to an autocomplete field
    private AutoCompleteTextView mUsernameView;
    //Field for entering password.
    private EditText mPasswordView;
    //View showing a progresscircle, during login in.
    private View mProgressView;
    private View mLoginFormView;
    //We create a taskprocess which does the login in the background
    private UserLoginTask mAuthTask;
    private boolean isRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mUsernameView = findViewById(R.id.username);
        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
        mPasswordView = findViewById(R.id.password);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Action trigerred by pushing the loginbutton
    public void onClickLogin(View v) {
        isRegister = false;
        attemptLogin();
    }

    public void onClickRegister(View v) {
        isRegister = true;
        attemptLogin();
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return; //We're already attempting a login
        }
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false; //If login fails
        View focusView = null; //Used to focus on username/passwordview if input is not valid

        //Check for valid input!
        if (TextUtils.isEmpty(password.trim()) || !isPasswordValid(password.trim())) {
            mPasswordView.setError(ERROR_PASSWORD);
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError("no empty username");
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUserNameValid(username)) {
            mUsernameView.setError(ERROR_USERNAME);
            focusView = mUsernameView;
            cancel = true;
        }
        //Check for valid input end

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            mAuthTask = new UserLoginTask(username, password, isRegister, this);

            // changed the big async task to a thread, now threadPoolExector not used anymore
            mAuthTask.execute();
        }

    }

    private boolean isUserNameValid(String username) {
        return username != null && username.length() >= 1 && username.length() <= 50 && !username.equals("####FAILED####");
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.length() >= 1 && password.length() <= 20;
    }

    /*No need to pay attention to it, I took it over from  the predefined LoginActivity
      It only displays some nice animation during the login*/
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final Context context;
        private final String password;
        private final boolean isRegister;

        UserLoginTask(String username, String password, Boolean isRegister, Context context) {
            this.username = username;
            this.context = context;
            this.password = password;
            this.isRegister = isRegister;
        }

        /**
         * Kurze Info: Zuerst wird doInBackground aufgerufen. Der Rückgabewert von doInBackground wird
         * automatisch an der Methode onPostExecute weitergegeben und ausgeführt.
         */
        @Override
        protected Boolean doInBackground(Void... params) {

            // create login message for server depending on isRegister
            if (isRegister) {
                JsonObject login = new Login(username, password, true).getAsJson();
                ConnectTask.tcp.sendMessage(login);
            } else {
                JsonObject login = new Login(username, password, false).getAsJson();
                ConnectTask.tcp.sendMessage(login);
            }

            // i used for timeout while waiting for server response
            int i = 0;
            while (login_loginResponse.isEmpty() && i < 100) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return false;
                }
                i++;

            }

            // check if login was successful. login_loginResponse is set by the task handling the server answers
            if (login_loginResponse.equals("true")) {
                login_loginResponse = "";
                return true;
            } else if (login_loginResponse.equals(Message.MESSAGE_LOGIN_FAIL)) {
                login_loginResponse = "invalid password or username";
                return false;
            } else if (login_loginResponse.equals(Message.MESSAGE_REGISTER_FAIL)) {
                login_loginResponse = "username already exists";
                return false;
            } else {
                login_loginResponse = "timeout";
                return false;
            }


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
                prefs.edit().putString(KEY_USERNAME, username).apply();
                prefs.edit().putString(KEY_PASSWORD, password).apply();
                Intent intent = new Intent(context, ChatListActivity.class);
                intent.putExtra(ChatListActivity.TAG_USERNAME, username);
                startActivity(intent);
            } else {
                mPasswordView.setError(login_loginResponse);
                login_loginResponse = "";
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }
}
