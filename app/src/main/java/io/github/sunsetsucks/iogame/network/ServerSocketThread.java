package io.github.sunsetsucks.iogame.network;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ssuri on 7/26/16.
 *
 */
public class ServerSocketThread extends Thread
{
    private ServerSocket serverSocket;
    static final int PORT = 53000;
    private Context context;

    public void setContext(Context context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        try
        {
            go();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void go() throws IOException
    {
        serverSocket = new ServerSocket(PORT);
        Toast.makeText(context, "Your IP is: " + serverSocket.getInetAddress().getHostName(), Toast.LENGTH_LONG).show();
        serverSocket.accept();
    }
}
