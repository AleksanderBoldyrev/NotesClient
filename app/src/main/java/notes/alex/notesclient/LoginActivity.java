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
    //private View mProgressView;
    private EditText mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mLoginFormView = (EditText) findViewById(R.id.login);
        //populateAutoComplete();

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

        //mLoginFormView = findViewById(R.id.login_form);

        _socketWorker = new SocketWorker();
        _swThread = new Thread(_socketWorker);
        _swThread.start();

        //ShowDialog("Test", "Test");
        //_serverIO = new ServerSendTask();
    }

    /*private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        //getLoaderManager().initLoader(0, null, this);
    }*/

    /*private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }*/

    /**
     * Callback received when a permissions request has been completed.
     */
   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }*/

    /*public int Login(String _log, String _passw) {
        int suc = CommonData.SERV_NO;
        ArrayList<String> s = new ArrayList<String>();
        s.add(_log);
        s.add(_passw);

        String st = _parser.Build(s, CommonData.O_LOGIN);
        SendToServer(st);
        String str = WaitForServer();

        if (!str.equals("")) {
            ArrayList<Integer> buff = _parser.ParseListOfInteger(str);
            if (buff.size() > 1)
                if (buff.get(0) == CommonData.O_RESPOND) {
                    if (buff.get(1) == CommonData.SERV_YES) {
                        _login = _log;
                        _pass = _passw;
                        _isAuth = true;
                        //_stage = 0;
                        //LoadBasicDataFromServer();
                        suc = CommonData.SERV_YES;
                        _lastLoginRes = true;
                    }
                    else
                        _lastLoginRes = false;
                }
        }
        return suc;
    }*/

    private void createUser()
    {
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
                        //_isAuth = true;
                        //ShowDialog("User created!", "All fine. Try to login!");
                        attemptLogin();
                    }
                }
        }
        if (suc == CommonData.SERV_NO)
            ShowDialog("User was not created!", "Sorry. Try again later!");
        //return suc;         //-----------------------------------------------------------------------------------------
    }

    /*private String serverIO(final String str)
    {
        String res = new String();
        _serverIO = new ServerSendTask();
              _serverIO.execute(str);
        try {
            res = _serverIO.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return res;
    }*/

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
        //return res;            //-----------------------------------------------------------------------------------------
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
                        //_login = _log;
                        //_pass = _passw;
                        //_isAuth = true;
                        //_stage = 0;
                        //LoadBasicDataFromServer();
                        suc = CommonData.SERV_YES;
                        _lastLoginRes = true;
                        //Intent mainAct = new Intent(LoginActivity.this, MainActivity.class);
                        // translate variables to main activity
                        //startActivity(mainAct);
                    } else
                        _lastLoginRes = false;
                }
        }

        if (suc == CommonData.SERV_YES)
        {
            Intent mainAct = new Intent(this, MainActivity.class);
            startActivity(mainAct);
        }
        else
        {
            ShowDialog("Login error", "Wrong user data! Please, try again!");
        }
        /*if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }*/
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
   /*
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

*/

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
}

