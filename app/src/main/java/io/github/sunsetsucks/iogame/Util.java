package io.github.sunsetsucks.iogame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Sameer on 2016-07-27.
 */
public class Util
{
    public static Context context = null;

    public static void toast(final int length, final Object text, final Object... params)
    {
        assertActivity("create toast");

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

    private static void assertActivity(String text)
    {
        if(!(context instanceof Activity))
        {
            throw new IllegalStateException("Util.context must be an Activity to" + text);
        }
    }

    public static void toast(final Object text, final Object... params)
    {
        toast(Toast.LENGTH_SHORT, text, params);
    }

    public static void ltoast(final Object text, final Object... params)
    {
        toast(Toast.LENGTH_LONG, text, params);
    }

    public static void alert(String title, String text, DialogInterface.OnClickListener listener)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("OK", listener)
                .create()
                .show();
    }

    public static String getDeviceStatus(int deviceStatus)
    {
        switch (deviceStatus)
        {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}
