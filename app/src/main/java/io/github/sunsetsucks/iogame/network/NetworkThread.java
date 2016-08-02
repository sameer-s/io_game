package io.github.sunsetsucks.iogame.network;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import io.github.sunsetsucks.iogame.Util;

import static java.nio.channels.SelectionKey.*;

/**
 * Created by ssuri on 8/2/16.
 */
public class NetworkThread extends Thread
{
    private static final int PORT = 54362;

    private boolean isServer = false;

    private Selector selector;
    private ServerSocket ss;

    private ByteBuffer buffer = ByteBuffer.allocate(16384);

    private Handler handler;

    public NetworkThread(Handler handler)
    {
        this.handler = handler;

        try
        {
            selector = Selector.open();

            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);

            ss = ssc.socket();
            InetSocketAddress isa = new InetSocketAddress(PORT);
            ss.bind(isa);

            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e)
        {
            Util.alert("ERROR", "Error setting up network", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    ((Activity) Util.context).finish();
                }
            });
        }
    }

    public NetworkThread setIsServer(boolean isServer)
    {
        this.isServer = isServer;
        return this;
    }

    @Override
    public void run()
    {

    }

    private boolean keyHas(SelectionKey key, int value)
    {
        return (key.readyOps() & value) == value;
    }

    public void runServer() throws IOException
    {
        //noinspection InfiniteLoopStatement
        while (true)
        {
            Set<SelectionKey> keys = selector.selectedKeys();

            for (SelectionKey key : keys)
            {
                if (keyHas(key, OP_ACCEPT))
                {
                    Socket s = ss.accept();

                    SocketChannel sc = s.getChannel();
                    sc.configureBlocking(false);

                    sc.register(selector, SelectionKey.OP_READ);
                } else if (keyHas(key, OP_CONNECT))
                {
                    SocketChannel sc = (SocketChannel) key.channel();
                    if (sc.finishConnect())
                    {
                        key.interestOps(SelectionKey.OP_READ);
                    }
                } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ)
                {
                    SocketChannel sc = null;
                    try
                    {
                        sc = (SocketChannel) key.channel();

                        buffer.clear();
                        sc.read(buffer);
                        buffer.flip();

                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < buffer.limit(); i++)
                        {
                            byte b = buffer.get(i);
                            sb.append((char) b);
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString("data", sb.toString());

                        Message message = new Message();
                        message.setData(bundle);

                        handler.sendMessage(message);
                    } catch (IOException e)
                    {
                        Log.d("networking", "Unable to read data from network due to " + e.getClass().getName());
                    } finally
                    {
                        if (sc != null && sc.isOpen())
                        {
                            sc.close();
                        }
                    }
                }
            }
        }
    }
}
