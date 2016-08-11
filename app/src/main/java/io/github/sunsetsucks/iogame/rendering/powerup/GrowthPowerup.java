package io.github.sunsetsucks.iogame.rendering.powerup;

import android.graphics.Bitmap;

import io.github.sunsetsucks.iogame.Util;
import io.github.sunsetsucks.iogame.rendering.player.Chaser;
import io.github.sunsetsucks.iogame.rendering.player.Player;

/**
 * Created by ssuri on 8/9/16.
 */
public class GrowthPowerup extends Powerup
{
    private static final Bitmap texture = Util.loadBitmap("drawable/burger");

    public GrowthPowerup()
    {
        super(texture);
    }

    public boolean doesContact(Player p)
    {
        return p instanceof Chaser;
    }
}
