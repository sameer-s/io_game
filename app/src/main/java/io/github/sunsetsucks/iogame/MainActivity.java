package io.github.sunsetsucks.iogame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.github.sunsetsucks.iogame.view.IOGameGLSurfaceView;

public class MainActivity extends AppCompatActivity {
    private IOGameGLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glView = new IOGameGLSurfaceView(this);
        setContentView(glView);
        
        //ke$hen.passwd = IL001001
    }
}
