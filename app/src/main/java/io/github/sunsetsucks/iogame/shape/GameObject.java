package io.github.sunsetsucks.iogame.shape;

import android.opengl.Matrix;

import com.google.gson.annotations.Expose;

import java.util.HashMap;


/**
 * Created by ssuri on 7/26/16.
 *
 */
public abstract class GameObject
{
    @Expose public float rotation = 0f;
    @Expose public float translationX = 0f;
    @Expose public float translationY = 0f;
    @Expose public float scaleX = 1f;
    @Expose public float scaleY = 1f;
    @Expose public String name = null;

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

    public static class GameObjectMap extends HashMap<String, GameObject>
    {
        public void put(GameObject obj)
        {
            put(obj.name, obj);
        }
    }
}
