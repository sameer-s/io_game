package io.github.sunsetsucks.iogame.network;

import java.net.InetAddress;

/**
 * Created by Sameer on 2016-07-27.
 */
public class GameClient
{
    private GameConnection connection;

    public GameClient(GameConnection connection, InetAddress address, int port)
    {
        this.connection = connection;
    }

    public void sendMessage(String msg)
    {
    }
}
