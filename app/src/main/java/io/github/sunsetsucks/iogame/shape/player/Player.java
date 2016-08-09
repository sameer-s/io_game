package io.github.sunsetsucks.iogame.shape.player;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import io.github.sunsetsucks.iogame.shape.Square;

/**
 * Created by Sameer on 2016-08-08.
 */
public abstract class Player extends Square
{
    protected static final float BASE_SPEED = 2.0f;
    protected float speed = BASE_SPEED;

    private byte poweredUp = 0, type = 0, compId;

    protected Player(Bitmap texture, byte compId, boolean isChaser)
    {
        super(texture);

        type = isChaser ? (byte) 0b10000 : 0;
        this.compId = compId;
    }

    public void speedChange()
    {
        // TODO: actually speed up (if runner) or slow down (if chaser), implement timer code.
    }

    public void powerUp()
    {
        poweredUp = 0b1000;
        // TODO: actually make powered up, implement timer code. Make sure that when the timer expires, you reset the value of poweredUp to 0.
    }

    public float getSpeed()
    {
        return speed;
    }

    public boolean isPoweredUp()
    {
        return poweredUp != 0;
    }

    public boolean isChaser()
    {
        return type != 0;
    }

    public byte[] toBytes()
    {
        byte[] bytes = new byte[9];
        System.arraycopy(ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putFloat(translationX).putFloat(translationY).array(), 0, bytes, 0, 8);
        bytes[8] = (byte) (type | poweredUp | compId);
        return bytes;
    }

    public static boolean isChaser(byte[] bytes)
    {
        return (bytes[8] & 0b10000) != 0;
    }

    public static byte compId(byte[] bytes)
    {
        return (byte) (bytes[8] & 0b111);
    }

    public void fromBytes(byte[] bytes)
    {
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        translationX = buf.getFloat();
        translationY = buf.getFloat();
        byte b = buf.get();

        type = (byte) (b & 0b10000);
        poweredUp = (byte) (b & 0b1000);
        compId = (byte) (b & 0b111);
    }

    @SuppressWarnings("unchecked")
    public Serializable destroyedMessage()
    {
        HashMap map = new HashMap<>();
        map.put("type", "playerDied");
        map.put("compId", compId);

        return map;
    }
}
