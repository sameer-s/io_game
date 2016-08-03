package io.github.sunsetsucks.iogame.shape;

import android.opengl.Matrix;

import io.github.sunsetsucks.iogame.network.message.Message;
import io.github.sunsetsucks.iogame.network.message.MessageConvertible;


/**
 * Created by ssuri on 7/26/16.
 *
 */
public abstract class GameObject implements MessageConvertible
{
    public float rotation;
    public float translationX = 0f, translationY = 0f;
    public float scaleX = 1f, scaleY = 1f;

    public GameObject setState(float rotation, float translationX, float translationY, float scaleX, float scaleY)
    {
        this.rotation = rotation;
        this.translationX = translationX;
        this.translationY = translationY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;

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
        message.put("class", this.getClass());
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
        try
        {
            Class _class = (Class) message.get("class");

            GameObject gameObject = (GameObject) _class.newInstance();
            gameObject.rotation = (Float) message.get("rotation");
            gameObject.translationX = (Float) message.get("translationX");
            gameObject.translationY = (Float) message.get("translationY");
            gameObject.scaleX = (Float) message.get("scaleX");
            gameObject.scaleY = (Float) message.get("scaleY");

            return gameObject;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            return null;
        }
    }
}
