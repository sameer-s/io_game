package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by ssuri on 8/2/16.
 */
public class TCPWriteThread extends Thread
{
    private Socket socket;
    private Object message = null;

    private final Object lock = new Object();

    public TCPWriteThread(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        ObjectOutputStream out;
        while (true)
        {
            try
            {
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e)
            {
                continue;
            }

            break;
        }

        while(!socket.isClosed())
        {
            synchronized (lock)
            {
                if (message != null)
                {
                    try
                    {
                        out.writeObject(message);
                    }
                    catch (IOException e)
                    {
                        Log.e("iogame_networking", "Failed to write message");
                    }

                    message = null;
                }
            }
        }
        try
        {
            out.close();
        }
        catch (IOException e)
        {
            Log.e("iogame_networking", "Failed to close socket output stream");
        }
    }

    public void writeMessage(Serializable messaqe)
    {
        synchronized (lock)
        {
            this.message = messaqe;
        }
    }
}
