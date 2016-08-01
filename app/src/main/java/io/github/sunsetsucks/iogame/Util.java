package io.github.sunsetsucks.iogame;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import io.github.sunsetsucks.iogame.network.GameConnection;

/**
 * Created by Sameer on 2016-07-27.
 */
public class Util
{
    public static Context context = null;
    public static GameConnection connection = null;

    public static void toast(final int length, final Object text, final Object... params)
    {
        if(!(context instanceof Activity))
        {
            throw new IllegalStateException("Util.context must be an Activity to create toast");
        }

        ((Activity) context).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String txt = String.format(text.toString(), params);
                Toast.makeText(context, txt, length).show();
                Log.d("Toast", txt);
            }
        });
    }

    public static void toast(final Object text, final Object... params)
    {
        toast(Toast.LENGTH_SHORT, text, params);
    }
}
