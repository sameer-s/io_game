package io.github.sunsetsucks.iogame.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by ssuri on 8/2/16.
 */
public class NetworkWriteThread extends Thread
{
    private Socket socket;
    private String message = null;

    private final Object lock = new Object();

    public NetworkWriteThread(Socket socket)
    {
        this.socket = socket;
    }

    private PrintWriter out;

    @Override
    public void run()
    {

        while (true)
        {
            try
            {
                out = new PrintWriter(socket.getOutputStream(), true);
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
                    out.println(message);
                    message = null;
                }
            }
        }

        out.close();
    }

    public void writeMessage(String messaqe)
    {
        synchronized (lock)
        {
            this.message = messaqe;
        }
    }
}
