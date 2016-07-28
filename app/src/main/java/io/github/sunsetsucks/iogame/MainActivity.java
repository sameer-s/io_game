package io.github.sunsetsucks.iogame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import io.github.sunsetsucks.iogame.view.IOGameGLSurfaceView;

public class MainActivity extends AppCompatActivity
{
    private IOGameGLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Util.context = this;

        glView = new IOGameGLSurfaceView(this);
        setContentView(glView);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        glView.setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && glView.getVisibility() == View.GONE)
        {
            glView.setVisibility(View.VISIBLE);
        }
    }
}
