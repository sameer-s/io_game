package io.github.sunsetsucks.iogame.shape.powerup;

import android.graphics.Bitmap;

import io.github.sunsetsucks.iogame.Util;
import io.github.sunsetsucks.iogame.shape.player.Chaser;
import io.github.sunsetsucks.iogame.shape.player.Player;

/**
 * Created by ssuri on 8/9/16.
 */
public class SpeedDownPowerup extends Powerup
{
    private static final Bitmap texture = Util.loadBitmap("drawable/tomato");

    public SpeedDownPowerup()
    {
        super(texture);
    }

    public boolean doesContact(Player p)
    {
        return p instanceof Chaser;
    }
}
