package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import io.github.sunsetsucks.iogame.Util;

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

                        Log.d("iogame_debug", String.format("Sending packet to %s. From: %s", socket.getRemoteSocketAddress(),  socket.getLocalSocketAddress()));
                        socket.send(packet);

                        message = null;
                     }
                }
                catch (IOException e)
                {
                    Log.e("iogame_networking", "Error sending datagram");
                    e.printStackTrace();
                }
            }
        }
    }

    public void writeMessage(byte[] message)
    {
        if(message.length != Util.DATAGRAM_SIZE)
        {
            throw new IllegalArgumentException("message.length for UDP must be equal to Util.DATAGRAM_SIZE");
        }

        synchronized (lock)
        {
            this.message = message;
        }
    }
}
