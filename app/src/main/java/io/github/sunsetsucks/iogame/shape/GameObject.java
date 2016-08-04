package io.github.sunsetsucks.iogame.shape;

import android.opengl.Matrix;

import java.util.HashMap;

import io.github.sunsetsucks.iogame.network.message.Message;
import io.github.sunsetsucks.iogame.network.message.MessageConvertible;


/**
 * Created by ssuri on 7/26/16.
 *
 */
public abstract class GameObject implements MessageConvertible
{
    public float rotation = 0f;
    public float translationX = 0f, translationY = 0f;
    public float scaleX = 1f, scaleY = 1f;
    public String name = null;

    public GameObject setState(float rotation, float translationX, float translationY, float scaleX, float scaleY)
    {
        this.rotation = rotation;
        this.translationX = translationX;
        this.translationY = translationY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;

        return this;
    }

    public GameObject setName(String name)
    {
        this.name = name;
        return this;
    }

    protected float[] getMovementMatrix()
    {
        float[] movementMatrix = new float[16], rotationMatrix = new float[16], scaleMatrix = new float[16];

        Matrix.setIdentityM(movementMatrix, 0); // reset transformations
        Matrix.setIdentityM(scaleMatrix, 0);

        Matrix.translateM(movementMatrix, 0, translationX, translationY, 0);
        Matrix.setRotateM(rotationMatrix, 0, rotation, 0, 0, -1.0f);
        Matrix.scaleM(scaleMatrix, 0, scaleX, scaleY, 1f);

        float[] tempMatrix = movementMatrix.clone();
        Matrix.multiplyMM(movementMatrix, 0, tempMatrix, 0, rotationMatrix, 0);

        tempMatrix = movementMatrix.clone();
        Matrix.multiplyMM(movementMatrix, 0, tempMatrix, 0, scaleMatrix, 0);

        return movementMatrix;
    }

    public abstract void draw(float[] mvpMatrix);

    @Override
    public Message toMessage()
    {
        Message message = new Message();
        message.put("command", this.getClass().getSimpleName() + "_update");
        message.put("name", name);
        message.put("class", this.getClass().getName());
        message.put("rotation", rotation);
        message.put("translationX", translationX);
        message.put("translationY", translationY);
        message.put("scaleX", scaleX);
        message.put("scaleY", scaleY);

        return message;
    }

    @Override
    public GameObject from(Message message)
    {
        this.name = (String) message.get("name");
        this.rotation = ((Double) message.get("rotation")).floatValue();
        this.translationX = ((Double) message.get("translationX")).floatValue();
        this.translationY = ((Double) message.get("translationY")).floatValue();
        this.scaleX = ((Double) message.get("scaleX")).floatValue();
        this.scaleY = ((Double) message.get("scaleY")).floatValue();

        return this;
    }

    public static class GameObjectMap extends HashMap<String, GameObject>
    {
        public void put(GameObject obj)
        {
            put(obj.name, obj);
        }
    }
}
