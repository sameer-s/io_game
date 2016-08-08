package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by ssuri on 8/2/16.
 */
public class TCPReadThread extends Thread
{
    private Socket socket;
    private NetworkHandler handler;

    public TCPReadThread(Socket socket)
    {
        this.socket = socket;
    }

    public void setHandler(NetworkHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public void run()
    {
        ObjectInputStream in;
        while (true)
        {
            try
            {
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e)
            {
                continue;
            }

            break;
        }

        try
        {
            Object obj;
            while ((obj = in.readObject()) != null)
            {
                handler.receiveTCPMessage((Serializable) obj);
            }
        } catch (IOException | ClassNotFoundException e)
        {
            Log.d("iogame_networking", "Failed to read line from socket");
        }

        try
        {
            in.close();
        }
        catch (IOException e)
        {
            Log.e("iogame_networking", "Failed to close socket input stream");
        }
    }
}
