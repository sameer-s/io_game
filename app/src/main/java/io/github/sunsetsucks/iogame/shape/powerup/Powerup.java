package io.github.sunsetsucks.iogame.shape.powerup;

/**
 * Created by ssuri on 8/9/16.
 */

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.github.sunsetsucks.iogame.shape.Square;
import io.github.sunsetsucks.iogame.shape.player.Player;

public abstract class Powerup extends Square
{
    protected Powerup(Bitmap texture)
    {
        super(texture);
        scaleX = .5f;
        scaleY = .5f;
    }

    public abstract boolean doesContact(Player other);

    @SuppressWarnings("unchecked")
    public Serializable toSerializable(boolean destroy)
    {
        HashMap map = new HashMap<>();
        map.put("type", "powerup");
        map.put("x", translationX);
        map.put("y", translationY);
        map.put("destroy", destroy);
        map.put("class", this.getClass().getSimpleName());

        return map;
    }

    public static Powerup fromSerializable(Serializable serializable)
    {
        Map map = (Map) serializable;
        Powerup p;

        switch ((String) map.get("class"))
        {
            case "GrowthPowerup":
                p = new GrowthPowerup();
                break;
            case "InvincibilityPowerup":
                p = new InvincibilityPowerup();
                break;
            case "SpeedDownPowerup":
                p = new SpeedDownPowerup();
                break;
            case "SpeedUpPowerup":
                p = new SpeedUpPowerup();
                break;
            default:
                throw new IllegalArgumentException("Input serializable map does not have appropriate \"class\" type. Found: " + map.get("class"));
        }

        p.translationX = (Float) map.get("x");
        p.translationY = (Float) map.get("y");

        return p;
    }
}
