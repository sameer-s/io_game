package io.github.sunsetsucks.iogame.network.message;

/**
 * Created by ssuri on 8/3/16.
 */
public interface MessageBroadcaster
{
    void broadcastMessage(MessageConvertible messageConvertible);
}
