package io.github.sunsetsucks.iogame.network;

import java.io.Serializable;

/**
 * Created by Sameer on 2016-08-02.
 */
public interface NetworkHandler
{
    void receiveNetworkMessage(Serializable message);
    void addNewConnection(NetworkConnection connection);
}