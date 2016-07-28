package io.github.sunsetsucks.iogame;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

import static io.github.sunsetsucks.iogame.Util.toast;

import io.github.sunsetsucks.iogame.network.GameConnection;
import io.github.sunsetsucks.iogame.view.IOGameGLSurfaceView;

public class MainActivity extends AppCompatActivity
{
    private IOGameGLSurfaceView glView;
    private Thread networkThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Util.context = this;

        glView = new IOGameGLSurfaceView(this);
        setContentView(glView);

//        setContentView(R.layout.activity_main);

        networkThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();

                GameConnection conn = new GameConnection(new Handler()
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        String data = msg.getData().getString("data");
                        toast("Message: %s", data);
                    }
                });

                Util.connection = conn;

                conn.discoverServices();
                conn.runServerLoop();
            }
        });

        networkThread.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(glView != null) glView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        {
            Util.connection.closeAllChannels();
        }
        catch(IOException e)
        {
            toast("Error in closing network channels");
        }
        Util.connection = null;

        networkThread.interrupt();

        Util.connection.deregisterService();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (glView != null && hasFocus && glView.getVisibility() == View.GONE)
        {
            glView.setVisibility(View.VISIBLE);
        }
    }

}
