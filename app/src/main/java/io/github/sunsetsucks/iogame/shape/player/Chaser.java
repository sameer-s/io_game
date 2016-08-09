package io.github.sunsetsucks.iogame.shape.player;

import android.graphics.Bitmap;

import io.github.sunsetsucks.iogame.Util;

/**
 * Created by ssuri on 8/9/16.
 */
public class Chaser extends Player
{
    private static final Bitmap texture = Util.loadBitmap("drawable/sanic");

    public Chaser(byte compId)
    {
        super(texture, compId, true);
    }
}
