package io.github.sunsetsucks.iogame.network.message;

import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashMap;

import io.github.sunsetsucks.iogame.network.NetworkConnection;

/**
 * Created by Sameer on 2016-08-02.
 */
public class Message extends HashMap<String, Object> implements MessageConvertible<Message>
{
    public static void send(MessageConvertible convertible, NetworkConnection connection)
    {
        convertible.toMessage().send(connection);
    }

    public static void send(MessageConvertible convertible, Collection<NetworkConnection> connections)
    {
        convertible.toMessage().send(connections);
    }

    public void send(NetworkConnection connection)
    {
        connection.write(new Gson().toJson(this));
    }

    public void send(Collection<NetworkConnection> connections)
    {
        for(NetworkConnection connection : connections)
        {
            send(connection);
        }
    }

    public static Message from(String string)
    {
        return new Gson().fromJson(string, Message.class);
    }

    @Override
    public Message toMessage()
    {
        return this;
    }

    @Override
    public Message from(Message message)
    {
        return message;
    }
}
