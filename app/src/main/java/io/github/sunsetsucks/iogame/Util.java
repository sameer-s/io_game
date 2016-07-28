package io.github.sunsetsucks.iogame;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Sameer on 2016-07-27.
 */
public class Util
{
    public static Context context = null;

    public static void toast(final int length, final CharSequence text, final Object... params)
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
                Toast.makeText(context, String.format(text.toString(), params), length).show();
            }
        });
    }

    public static void toast(final CharSequence text, final Object... params)
    {
        toast(Toast.LENGTH_LONG, text, params);
    }
}
