package io.github.sunsetsucks.iogame.network;

/**
 * Created by Sameer on 2016-08-02.
 */
public interface NetworkHandler
{
    void receiveNetworkMessage(String message);
    void addNewConnection(NetworkConnection connection);
}