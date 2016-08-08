package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by ssuri on 8/2/16.
 */
public class UDPWriteThread extends Thread
{
    private DatagramSocket socket;
    private byte[] message = null;

    private final Object lock = new Object();

    public UDPWriteThread(DatagramSocket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        while(!socket.isClosed())
        {
            synchronized (lock)
            {
                try
                {
                    if (message != null)
                    {
                        DatagramPacket packet = new DatagramPacket(
                                message, message.length, socket.getRemoteSocketAddress());
                        socket.send(packet);
                    }
                }
                catch (IOException e)
                {
                    Log.e("iogame_networking", "Error sending datagram");
                }
            }
        }
    }

    public void writeMessage(byte[] message)
    {
        synchronized (lock)
        {
            this.message = message;
        }
    }
}
