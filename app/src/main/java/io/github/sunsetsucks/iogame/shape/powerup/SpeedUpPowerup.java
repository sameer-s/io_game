package io.github.sunsetsucks.iogame.shape.powerup;

import android.graphics.Bitmap;

import io.github.sunsetsucks.iogame.Util;
import io.github.sunsetsucks.iogame.shape.player.Player;
import io.github.sunsetsucks.iogame.shape.player.Runner;

/**
 * Created by ssuri on 8/9/16.
 */
public class SpeedUpPowerup extends Powerup
{
    private static final Bitmap texture = Util.loadBitmap("drawable/machine");

    public SpeedUpPowerup()
    {
        super(texture);
    }

    public boolean doesContact(Player p)
    {
        return p instanceof Runner;
    }
}
