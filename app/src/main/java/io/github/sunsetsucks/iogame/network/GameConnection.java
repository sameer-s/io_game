package io.github.sunsetsucks.iogame.network;

import android.os.Handler;
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

/**
 * Created by Sameer on 2016-07-27.
 *
 * Notes/links:
 * https://developer.android.com/training/connect-devices-wirelessly/nsd.html
 * http://www.javaworld.com/article/2073344/core-java/use-select-for-high-speed-networking.html
 * http://tutorials.jenkov.com/java-nio/selectors.html
 *
 * This is in no way finished.
 */

public class GameConnection
{
    static final String SERVICE_TYPE = "_http._tcp.";

    private Selector selector;
    private Socket socket;

    private Handler handler;

    public GameConnection(Handler handler)
    {
        this.handler = handler;

        try
        {
            initNetwork();
        }
        catch (IOException e)
        {
            Util.toast("We were unable to set up the network because of an exception: %s.", e.getClass().getName());
        }

        try
        {
            runServer();
        }
        catch (IOException e)
        {
            Util.toast("An error (%s) occurred while handling network input.", e.getClass().getName());
        }
    }

    private ServerSocketChannel ssc = null;
    private ServerSocket ss = null;
    private int port = 0;
    private final ByteBuffer buffer = ByteBuffer.allocate(16384);

    private void initNetwork() throws IOException
    {
        selector = Selector.open();

        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        ServerSocket ss = ssc.socket();
        InetSocketAddress isa = new InetSocketAddress(0);
        ss.bind(isa);

        port = ss.getLocalPort();

        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void runServer() throws IOException
    {
        while(true)
        {
            int in = selector.select();

            if(in == 0) continue;

            Set<SelectionKey> keys = selector.selectedKeys();

            for(SelectionKey key : keys)
            {
                if((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT)
                {
                    Socket s = ss.accept();

                    SocketChannel sc = s.getChannel();
                    sc.configureBlocking(false);

                    sc.register(selector, SelectionKey.OP_READ);
                }
                else if((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ)
                {
                    SocketChannel sc = null;
                    try
                    {
                        sc = (SocketChannel) key.channel();

                        buffer.clear();
                        sc.read(buffer);
                        buffer.flip();

                        if (buffer.limit() == 0)
                        {
                            key.cancel();
                            sc.socket().close();
                        }

                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < buffer.limit(); i++)
                        {
                            byte b = buffer.get(i);
                            sb.append((char) b);
                        }

                        Util.toast("[%s]: %s", key.attachment(), sb);
                    }
                    catch(IOException e)
                    {
                        Util.toast("Unable to read data from network due to %s.", e.getClass().getName());
                    }
                    finally
                    {
                        if(sc != null && sc.isOpen())
                        {
                            sc.close();
                        }
                    }
                }
            }

            keys.clear();
        }
    }
}
