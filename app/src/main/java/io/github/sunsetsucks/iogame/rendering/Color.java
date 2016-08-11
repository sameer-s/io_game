package io.github.sunsetsucks.iogame.rendering;

import java.util.Random;

/**
 * Created by ssuri on 7/25/16.
 *
 */
public abstract class Color
{
    public static final float[]
    RED         = {1, 0, 0, 1},
    GREEN       = {0, 1, 0, 1},
    BLUE        = {0, 0, 1, 1},
    WHITE       = {1, 1, 1, 1},
    BLACK       = {0, 0, 0, 1},
    CYAN        = {0, 1, 1, 1},
    MAGENTA     = {1, 0, 1, 1},
    YELLOW      = {1, 1, 0, 1},
    COQUELICOT  = makeInt(236, 73, 8, 255),
    EBURNEAN    = makeInt(245, 240, 246, 255),
    SARCOLINE   = makeInt(250, 223, 174, 255),
    GLAUCOUS    = makeInt(96, 130, 182, 255);

    public static float[] make(double red, double green, double blue, double alpha)
    {
        return new float[] {(float) red, (float) green, (float) blue, (float) alpha};
    }

    public static float[] makeInt(int red, int green, int blue, int alpha)
    {
        return make(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);
    }

    public static float[] randomColor(long seed)
    {
        Random r = new Random(seed);
        return makeInt(r.nextInt(256), r.nextInt(256), r.nextInt(256), (int)  255);
    }

    public static float[] randomColor(String str)
    {
        return randomColor(str.hashCode());
    }
}
