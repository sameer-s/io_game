package io.github.sunsetsucks.iogame.shape;

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
    BLACK       = {0, 0, 0, 0},
    CYAN        = {0, 1, 1, 1},
    MAGENTA     = {1, 0, 1, 1},
    YELLOW      = {1, 1, 0, 0};

    public static float[] make(double red, double green, double blue, double alpha)
    {
        return new float[] {(float) red, (float) green, (float) blue, (float) alpha};
    }
}
