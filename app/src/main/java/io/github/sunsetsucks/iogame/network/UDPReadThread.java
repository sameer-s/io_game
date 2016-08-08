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
                byte[] buffer = new byte[Util.DATAGRAM_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                Log.d("iogame_debug", "ready to recieve packet len=" + buffer.length); // FIXME: 8/8/16
                socket.receive(packet);

                Log.d("iogame_debug", "received packet"); // FIXME: 8/8/16

                handler.receiveUDPMessage(packet.getData());
            }
        } catch (IOException e)
        {
            Log.d("iogame_networking", "Error reading datagram packet");
        }
    }
}
