package notes.alex.notesclient;

import android.os.AsyncTask;

/**
 * Created by Alex on 01.05.2016.
 */
public class ServerSendTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        SocketWorker.SendToServer(params[0]);
        String str = SocketWorker.WaitForServer();
        System.out.println("Received from socket: " + str);
        return str;
    }
}