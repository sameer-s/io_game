package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by ssuri on 8/2/16.
 */
public class NetworkReadThread extends Thread
{
    private Socket socket;
    private NetworkHandler handler;

    public NetworkReadThread(Socket socket)
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
        BufferedReader in;
        while (true)
        {
            try
            {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e)
            {
                continue;
            }

            break;
        }

        try
        {
            String line;
            while ((line = in.readLine()) != null)
            {
                handler.receiveNetworkMessage(line);
            }
        } catch (IOException e)
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
