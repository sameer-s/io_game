package io.github.sunsetsucks.iogame.network;

import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import io.github.sunsetsucks.iogame.Util;

/**
 * Created by Sameer on 2016-08-02.
 */
public class NetworkConnection
{
    private UDPWriteThread udpWrite;
    private TCPWriteThread tcpWrite;
    private Socket socket;
    private static DatagramSocket dSocketRead;
    private DatagramSocket dSocketWrite;

    public NetworkConnection(Socket socket, NetworkHandler handler)
    {
        this.socket = socket;

        if(dSocketRead == null)
        {
            try
            {
                dSocketRead = new DatagramSocket(Util.PORT);
            } catch (SocketException e)
            {
                Log.e("iogame_networking", "An error occurred initializing the datagram socket (read)");
            }
        }

        try
        {
            dSocketWrite = new DatagramSocket();
            dSocketWrite.connect(new InetSocketAddress(socket.getInetAddress(), Util.PORT));
        }
        catch (SocketException e)
        {
            Log.e("iogame_networking", "An error occurred initializing the datagram socket (write)");
        }

        // TCP
        TCPReadThread tcpRead = new TCPReadThread(socket);
        tcpRead.setHandler(handler);

        tcpWrite = new TCPWriteThread(socket);

        tcpRead.start();
        tcpWrite.start();

        // UDP
        UDPReadThread udpRead = new UDPReadThread(dSocketRead);
        udpRead.setHandler(handler);

        udpWrite = new UDPWriteThread(dSocketWrite);

        udpRead.start();
        udpWrite.start();
    }

    public void write(Serializable message, boolean reliable)
    {
        if(reliable)
        {
            tcpWrite.writeMessage(message);
        }
        else
        {
            udpWrite.writeMessage((byte[]) message);
        }
    }

    public void stop()
    {
        try
        {
            socket.close();
            dSocketRead.close();
            dSocketWrite.close();
        }
        catch (IOException e)
        {
            Log.e("iogame_networking", "Failed to close socket");
        }
    }
}
