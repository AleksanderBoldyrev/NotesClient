package notes.alex.notesclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_CONTACTS;
import static java.lang.Thread.sleep;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity { //implements LoaderCallbacks<Cursor> {

    private static final RequestsParser _parser = new RequestsParser();
    private SocketWorker _socketWorker;
    private Thread _swThread;

    private String _lastHost;
     private int _selectedNote;
    private int _selectedVersion;
    private boolean _lastLoginRes;
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;

    // UI references.
    //private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mHostView;
    //private View mProgressView;
    private EditText mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mLoginFormView = (EditText) findViewById(R.id.login);
        //populateAutoComplete();
        mHostView = (EditText) findViewById(R.id.host);
        mHostView.setText(CommonData.HOST);
        _lastHost = CommonData.HOST;
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button createUserButton = (Button) findViewById(R.id.createUserButton);
        Button removeUserButton = (Button) findViewById(R.id.removeUserButton);
        Button aboutButton = (Button) findViewById(R.id.aboutButton);
        Button exitButton = (Button) findViewById(R.id.exitButton);

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        createUserButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });
        removeUserButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                removeUser();
            }
        });
        aboutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutCalled();
            }
        });
        exitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean Init()
    {
        String host = mHostView.getText().toString();
        if (_socketWorker==null || (_socketWorker!=null && !_socketWorker._isReady) || !host.equals(_lastHost)) {
            _socketWorker = new SocketWorker(host);
            _swThread = new Thread(_socketWorker);
            _swThread.start();
            try {
                sleep(512);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (_socketWorker._isReady)
                return true;
            else {
                ShowDialog("Error", "Could not connect to server!");
                return false;
            }
        }
        return true;
    }

    private void createUser()
    {
        if (Init()) {
            String _log = mLoginFormView.getText().toString();
            String _passw = mPasswordView.getText().toString();
            int suc = CommonData.SERV_NO;
            ArrayList<String> s = new ArrayList<String>();
            s.add(_log);
            boolean add = s.add(_passw);
            String st = _parser.Build(s, CommonData.O_CREATE_U);
            String str = SocketWorker.serverIO(st);
            if (!str.equals("")) {
                ArrayList<Integer> buff = _parser.ParseListOfInteger(str);
                if (buff.size() > 1)
                    if (buff.get(0) == CommonData.O_RESPOND) {
                        if (buff.get(1) == CommonData.SERV_YES) {
                            attemptLogin();
                        }
                    }
            }
            if (suc == CommonData.SERV_NO)
                ShowDialog("User was not created!", "Sorry. Try again later!");
        }
    }

    public void ShowDialog(String title, String message)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void removeUser()
    {
        if (Init()) {
            String log = mLoginFormView.getText().toString();
            String pass = mPasswordView.getText().toString();
            int res = CommonData.SERV_NO;
            ArrayList<String> buf = new ArrayList<String>();
            buf.add(log);
            buf.add(pass);
            String st = _parser.Build(buf, CommonData.O_DELETE_U);
            String str = SocketWorker.serverIO(st);
            if (!str.equals("")) {
                ArrayList<Integer> buff = _parser.ParseListOfInteger(str);
                if (buff.size() > 1)
                    if (buff.get(0) == CommonData.O_RESPOND) {
                        if (buff.get(1) == CommonData.SERV_YES) {
                            res = CommonData.SERV_YES;
                        }
                    }
            }
        }
    }

    private void aboutCalled()
    {
        Intent aboutAct = new Intent(this, AboutActivity.class);
        startActivity(aboutAct);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (Init()) {
            String _log = mLoginFormView.getText().toString();
            String _passw = mPasswordView.getText().toString();
            int suc = CommonData.SERV_NO;
            ArrayList<String> s = new ArrayList<String>();
            s.add(_log);
            s.add(_passw);

            String st = _parser.Build(s, CommonData.O_LOGIN);
            String str = SocketWorker.serverIO(st);

            if (!str.equals("")) {
                ArrayList<Integer> buff = _parser.ParseListOfInteger(str);
                if (buff.size() > 1)
                    if (buff.get(0) == CommonData.O_RESPOND) {
                        if (buff.get(1) == CommonData.SERV_YES) {
                            suc = CommonData.SERV_YES;
                            _lastLoginRes = true;
                        } else
                            _lastLoginRes = false;
                    }
            }

            if (suc == CommonData.SERV_YES) {
                Intent mainAct = new Intent(this, MainActivity.class);
                startActivity(mainAct);
            } else {
                ShowDialog("Login error", "Wrong user data! Please, try again!");
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

}

