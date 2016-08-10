package io.github.sunsetsucks.iogame.shape.player;

import android.graphics.Bitmap;

import java.util.Timer;
import java.util.TimerTask;

import io.github.sunsetsucks.iogame.Util;

/**
 * Created by ssuri on 8/9/16.
 */
public class Runner extends Player
{
    private static final Bitmap texture = Util.loadBitmap("drawable/cat1");
    private static final Bitmap texture2 = Util.loadBitmap("drawable/cat2");

    public Runner(byte compId)
    {
        super(texture, texture2, compId, false);
    }

    private Timer timer;
    @Override
    public void speedChange()
    {
        speed = 2f * BASE_SPEED;

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
