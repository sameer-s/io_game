package io.github.sunsetsucks.iogame.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.github.sunsetsucks.iogame.Util;

import static io.github.sunsetsucks.iogame.Util.toast;

/**
 * Created by Sameer on 2016-07-27.
 *
 * Notes/links:
 * https://developer.android.com/training/connect-devices-wirelessly/nsd.html
 * http://www.javaworld.com/article/2073344/core-java/use-select-for-high-speed-networking.html
 * http://tutorials.jenkov.com/java-nio/selectors.html
 *
 */

public class GameConnection
{
    static final String SERVICE_TYPE = "_http._tcp.";

    private Selector selector;

    private Handler handler;

    private final String name = UUID.randomUUID().toString();

    public GameConnection(Handler handler)
    {
        this.handler = handler;

        try
        {
            registerService();
            initNetwork();
        } catch (IOException e)
        {
            toast("We were unable to set up the network because of an exception: %s.", e.getClass().getName());
        }

        try
        {
            runServer();
        } catch (IOException e)
        {
            toast("An error (%s) occurred while handling network input.", e.getClass().getName());
        }
    }

    private ServerSocket ss = null;
    private final ByteBuffer buffer = ByteBuffer.allocate(16384); // buffer for reading
    private List<String> messages = new LinkedList<>();
    private SparseIntArray messageLocations = new SparseIntArray();

    private void openChannel(InetSocketAddress address) throws IOException
    {
        SocketChannel sc = SocketChannel.open();
        sc.connect(address);
        sc.configureBlocking(false);

        sc.register(selector, SelectionKey.OP_CONNECT);
    }

    public void sendMessage(String s)
    {
        for (SelectionKey key : selector.keys())
        {
            if (key.attachment() instanceof ChannelAttachment && ((ChannelAttachment) key.attachment()).name.equals(name))
            {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        }
        messages.add(s);
    }

    String serviceName = "IO_Style_Game";

    public void registerService()
    {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();


        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(0);

        final NsdManager nsdManager = (NsdManager) Util.context.getSystemService(Context.NSD_SERVICE);

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, new NsdManager.RegistrationListener()
        {
            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo)
            {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = serviceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode)
            {
                // Registration failed!  Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo)
            {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode)
            {
                // Unregistration failed.  Put debugging code here to determine why.
            }
        });

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener()
        {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType)
            {
                toast("Service discovery started");
            }

            @Override
            public void onServiceFound(final NsdServiceInfo service)
            {
                // A service was found!  Do something with it.
                toast("Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE))
                {
                    toast("Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName))
                {
                    toast("Same machine: " + serviceName);
                } else if (service.getServiceName().contains("NsdChat"))
                {
                    nsdManager.resolveService(service, new NsdManager.ResolveListener()
                    {

                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode)
                        {
                            toast("Resolve failed" + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo)
                        {
                            toast("Resolve Succeeded. " + serviceInfo);

                            if (serviceInfo.getServiceName().equals(serviceName))
                            {
                                Log.d("Connection", "Same IP.");
                                return;
                            }
                            int port = serviceInfo.getPort();
                            InetAddress host = serviceInfo.getHost();

                            try
                            {
                                openChannel(new InetSocketAddress(host, port));
                            }
                            catch (IOException e)
                            {
                                toast("Unable to open channel due to %s", e.getClass().getName());
                            }
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service)
            {
                toast("service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType)
            {
                toast("Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode)
            {
                toast("Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode)
            {
                toast("Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        });
    }


    private void initNetwork() throws IOException
    {
        selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        ServerSocket ss = ssc.socket();
        InetSocketAddress isa = new InetSocketAddress(0);
        ss.bind(isa);

        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void runServer() throws IOException
    {
        //noinspection InfiniteLoopStatement
        while (true)
        {
            int in = selector.select();

            if (in == 0) continue;

            Set<SelectionKey> keys = selector.selectedKeys();

            for (SelectionKey key : keys)
            {
                if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT)
                {
                    Socket s = ss.accept();

                    SocketChannel sc = s.getChannel();
                    sc.configureBlocking(false);

                    sc.register(selector, SelectionKey.OP_READ);
                } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ)
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

                        Bundle bundle = new Bundle();
                        if (key.attachment() != null)
                            bundle.putString("attachment", key.attachment().toString());
                        bundle.putString("data", sb.toString());

                        Message message = new Message();
                        message.setData(bundle);

                        handler.sendMessage(message);

//                        toast("[%s]: %s", key.attachment(), sb);
                    } catch (IOException e)
                    {
                        toast("Unable to read data from network due to %s.", e.getClass().getName());
                    } finally
                    {
                        if (sc != null && sc.isOpen())
                        {
                            sc.close();
                        }
                    }
                } else if ((key.readyOps() & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT)
                {
                    SocketChannel sc = (SocketChannel) key.channel();
                    if (sc.finishConnect())
                    {
                        key.interestOps(0);
                        key.attach(name);
                    }
                } else if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
                {
                    int keyId = ((ChannelAttachment) key.attachment()).id;
                    if (messageLocations.get(keyId, -1) < 0)
                    {
                        messageLocations.put(keyId, 0);
                    }

                    if (messages.size() <= messageLocations.get(keyId))
                    {
                        key.interestOps(0);
                        continue;
                    }

                    byte[] message = messages.get(messageLocations.get(keyId)).getBytes();
                    SocketChannel sc = (SocketChannel) key.channel();
                    sc.write(ByteBuffer.wrap(message));
                    messageLocations.put(keyId, messageLocations.get(keyId) + 1);

                    if (messages.size() == messageLocations.get(keyId))
                    {
                        key.interestOps(0);
                    }
                }
            }

            // cleans the sparsearray and map so they don't get too big

            int lowest = Integer.MAX_VALUE;
            for (int i = 0; i < messageLocations.size(); i++)
            {
                int val = messageLocations.get(messageLocations.keyAt(i));
                lowest = Math.min(lowest, val);
            }

            if (lowest >= messages.size())
            {
                messages = new LinkedList<>();
                messageLocations = new SparseIntArray();
            }

            keys.clear();
        }
    }
}
