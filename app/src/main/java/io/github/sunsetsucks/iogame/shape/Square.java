package io.github.sunsetsucks.iogame.shape;

/**
 * Created by ssuri on 7/25/16.
 *
 */
public class Square extends Shape
{
    @Override
    public float[] getCoords()
    {
        return new float[]
        {
            -0.5f, 0.5f, 0.0f, // tl
            -0.5f, -0.5f, 0.0f, // BL
            0.5f, -0.5f, 0.0f, // BR
            0.5f, 0.5f, 0.0f // TR
        };
    }

    @Override
    public short[] getDrawOrder()
    {
        return new short[] { 0, 1, 2, 0, 2, 3 };
    }
}
