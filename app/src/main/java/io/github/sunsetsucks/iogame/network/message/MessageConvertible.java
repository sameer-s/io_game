package io.github.sunsetsucks.iogame.network.message;

/**
 * Created by Sameer on 2016-08-02.
 */
public interface MessageConvertible<T extends MessageConvertible>
{
    Message toMessage();
    T from(Message message);
}
