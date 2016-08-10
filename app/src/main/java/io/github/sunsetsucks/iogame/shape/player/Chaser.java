package io.github.sunsetsucks.iogame.shape.player;

import android.graphics.Bitmap;

import java.util.Timer;
import java.util.TimerTask;

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

    private Timer timer;
    @Override
    public void speedChange()
    {
        speed = .5f * BASE_SPEED;

        if(timer != null)
        {
            timer.cancel();
        }

        timer = new Timer();

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                speed = BASE_SPEED;
                timer = null;
            }
        }, 5000);
    }
}
