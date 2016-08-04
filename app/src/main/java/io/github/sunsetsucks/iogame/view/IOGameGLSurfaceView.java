package io.github.sunsetsucks.iogame.view;


import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import static io.github.sunsetsucks.iogame.shape.GameObject.GameObjectMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.sunsetsucks.iogame.Util;
import io.github.sunsetsucks.iogame.shape.Color;
import io.github.sunsetsucks.iogame.shape.GameObject;
import io.github.sunsetsucks.iogame.shape.Shape;
import io.github.sunsetsucks.iogame.shape.Square;

/**
 * Created by ssuri on 7/25/16.
 */
public class IOGameGLSurfaceView extends GLSurfaceView
{
    public Renderer renderer;
    private Context context;

    //    private static final String rand = UUID.randomUUID().toString();
    private static final String rand = "abcd";


    public IOGameGLSurfaceView(Context context)
    {
        super(context);
        init(context);
    }

    public IOGameGLSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }



    private void init(Context context)
    {
        this.context = context;

        setEGLContextClientVersion(2);

        setPreserveEGLContextOnPause(true);

        renderer = new Renderer();

        setRenderer(renderer);
        //        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        //TODO remove
        if(!Util.isHost) return true;

        float xScreen = e.getX();
        float yScreen = e.getY();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x, screenHeight = size.y;

        float x = (xScreen / screenWidth) * -2.0f + 1.0f;
        float y = (yScreen / screenHeight) * -2.0f + 1.0f;

        renderer.toDraw.get("player" + rand).translationX = x;
        renderer.toDraw.get("player" + rand).translationY = y;

        return true;
    }

    // format x,y
    // TODO remove
    @Deprecated
    public void setShapeX_Y(String name, float x, float y)
    {
        if (renderer.toDraw.size() < 1)
        {
            return;
        }

        renderer.toDraw.get(name).translationX = x;
        renderer.toDraw.get(name).translationY = y;
    }

    public static class Renderer implements GLSurfaceView.Renderer
    {
        public GameObjectMap toDraw = new GameObjectMap();

        private final float[] mvpMatrix = new float[16], // model view projection
                projectionMatrix = new float[16],
                viewMatrix = new float[16];

        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            float[] color = Color.SARCOLINE;
            GLES20.glClearColor(color[0], color[1], color[2], color[3]);

            toDraw.put(((Shape) new Square().setState(0f, .5f, .25f, 1f, 1f)).setColor(Color.randomColor(rand)).setName("player" + rand));
        }

        public void onDrawFrame(GL10 unused)
        {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            for (String s : toDraw.keySet())
            {
                GameObject go = toDraw.get(s);
                go.draw(mvpMatrix);

                if (go.name.contains(rand) && Util.isHost /* TODO remove */)
                {
//                    Util.broadcastMessage(go);
                }
            }
        }

        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;

            // apply the projection matrix
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
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
