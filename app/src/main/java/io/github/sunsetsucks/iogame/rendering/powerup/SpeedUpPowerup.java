package io.github.sunsetsucks.iogame.rendering.powerup;

import android.graphics.Bitmap;

import io.github.sunsetsucks.iogame.Util;
import io.github.sunsetsucks.iogame.rendering.player.Player;
import io.github.sunsetsucks.iogame.rendering.player.Runner;

/**
 * Created by ssuri on 8/9/16.
 */
public class SpeedUpPowerup extends Powerup
{
    private static final Bitmap texture = Util.loadBitmap("drawable/pizza");

    public SpeedUpPowerup()
    {
        super(texture);
    }

    public boolean doesContact(Player p)
    {
        return p instanceof Runner;
    }
}
