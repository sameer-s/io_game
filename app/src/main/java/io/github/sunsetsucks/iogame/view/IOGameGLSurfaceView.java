package io.github.sunsetsucks.iogame.view;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.sunsetsucks.iogame.shape.Shape;
import io.github.sunsetsucks.iogame.shape.Square;

/**
 * Created by ssuri on 7/25/16.
 *
 */
public class IOGameGLSurfaceView extends GLSurfaceView
{
    private Renderer renderer;

    public IOGameGLSurfaceView(Context context)
    {
        super(context);

        setEGLContextClientVersion(2);

        renderer = new Renderer();

        setRenderer(renderer);

    }

    public IOGameGLSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public static class Renderer implements GLSurfaceView.Renderer
    {
        private List<Shape> shapesToDraw = new ArrayList<>();

        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            shapesToDraw.add(new Square());
        }

        public void onDrawFrame(GL10 unused)
        {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            for(Shape s : shapesToDraw)
            {
                s.draw();
            }
        }

        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            GLES20.glViewport(0, 0, width, height);
        }

        public static int loadShader(int type, String shaderCode)
        {
            int shader = GLES20.glCreateShader(type);

            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            return shader;
        }
    }
}
