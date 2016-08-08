package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import io.github.sunsetsucks.iogame.Util;

/**
 * Created by ssuri on 8/2/16.
 */
public class UDPReadThread extends Thread
{
    private DatagramSocket socket;
    private NetworkHandler handler;

    public UDPReadThread(DatagramSocket socket)
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
        try
        {
            while (!socket.isClosed())
            {
                byte[] buffer = new byte[8];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                handler.receiveUDPMessage(packet.getData());
            }
        } catch (IOException e)
        {
            Log.d("iogame_networking", "Error reading datagram packet");
        }
    }
}
