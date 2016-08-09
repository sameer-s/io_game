package io.github.sunsetsucks.iogame.shape.player;

import android.graphics.Bitmap;

import io.github.sunsetsucks.iogame.Util;

/**
 * Created by ssuri on 8/9/16.
 */
public class Runner extends Player
{
    private static final Bitmap texture = Util.loadBitmap("drawable/zorua");

    public Runner(byte compId)
    {
        super(texture, compId, false);
    }
}
