package io.github.sunsetsucks.iogame.rendering;

import android.graphics.Bitmap;

/**
 * Created by ssuri on 7/25/16.
 *
 */
public class Square extends Shape
{
    public Square()
    {
        super();
    }

    public Square(Bitmap texture)
    {
        super(texture);
    }

    public Square(Bitmap texture, Bitmap texture2)
    {
        super(texture, texture2);
    }


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

    @Override
    public float[] getUVs()
    {
        return new float[] {
                0, 0,
                0, 1,
                1, 1,
                1, 0,
        };
    }
}
