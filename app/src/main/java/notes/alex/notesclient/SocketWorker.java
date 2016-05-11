package notes.alex.notesclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

/**
 * Created by Alex on 01.05.2016.
 */
public final class SocketWorker implements Runnable {
    private static Socket _sock;
    private static BufferedReader _in;
    private static PrintWriter _out;
    private static ServerSendTask _serverIO;

    public static String serverIO(final String str)
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
    }

    @Override
    public void run() {
        _serverIO = new ServerSendTask();
        try {
            _sock = new Socket(CommonData.HOST, CommonData.PORT);
            _in = new BufferedReader(new InputStreamReader(_sock.getInputStream()));
            _out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_sock.getOutputStream())), true);//(_sock.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*String str = new String();
        boolean foo = true;
        while (true)
        {
            // trying to read data
            try {
                str = _in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                foo = false;
            }
            if (str.length()>0) {
                System.out.println("Client received: " + str);
                setOutBuf(str);
            }

            // trying to send something
            str = getInBuf();
            if (str.length()>0)
            {
                System.out.println("Client send to server:" + str);
                _out.println(str);
                str = "";
                setInBuf(str);
            }
            try {
                sleep(CommonData.SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }

    /*public synchronized String getInBuf()
    {
        return inBuf;
    }

    public synchronized String getOutBuf()
    {
        return outBuf;
    }

    public synchronized void setInBuf(final String str)
    {
        inBuf = str;
    }

    public synchronized void setOutBuf(final String str)
    {
        outBuf = str;
    }

    public String ReceiveData() {
        String str = "";
        try {

            str = _in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (str.length()>0)
            System.out.println("Client received: " + str);
        return str;
    }
*/
    public static void SendToServer(String str) {
        System.out.println("Client send to server:" + str);

          _out.println(str);
       // _out.flush();

    }

    public static String WaitForServer() {
        int i;
        String str = new String();
        for (i = CommonData.RETRIES_COUNT; i > 0; i--) {
            try {
                sleep(CommonData.SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                //if (_in.ready())
                {
                    str = _in.readLine();
                    if (str!=null && str.length()>0) {
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }

        }
        return str;
    }

}
