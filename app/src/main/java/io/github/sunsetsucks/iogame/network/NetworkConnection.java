package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by Sameer on 2016-08-02.
 */
public class NetworkConnection
{
    private NetworkReadThread read;
    private NetworkWriteThread write;
    private Socket socket;

    public NetworkConnection(Socket socket, NetworkHandler handler)
    {
        this.socket = socket;

        read = new NetworkReadThread(socket);
        read.setHandler(handler);
        write = new NetworkWriteThread(socket);

        read.start();
        write.start();
    }

    public void write(Serializable message)
    {
        write.writeMessage(message);
    }

    public void stop()
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            Log.e("iogame_networking", "Failed to close socket");
        }
    }
}
