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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.sunsetsucks.iogame.Util;
import io.github.sunsetsucks.iogame.shape.Color;
import io.github.sunsetsucks.iogame.shape.GameObject;
import io.github.sunsetsucks.iogame.shape.Square;

import static io.github.sunsetsucks.iogame.shape.GameObject.GameObjectMap;

/**
 * Created by ssuri on 7/25/16.
 */
public class IOGameGLSurfaceView extends GLSurfaceView
{
    public Renderer renderer;

    //    private static final String rand = UUID.randomUUID().toString();
    private static final String rand = "abcd";


    public IOGameGLSurfaceView(Context context)
    {
        super(context);
        init();
    }

    public IOGameGLSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }


    private void init()
    {
        setEGLContextClientVersion(2);

        setPreserveEGLContextOnPause(true);


        renderer = new Renderer();

        setRenderer(renderer);
        //        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    private float startX = 0, startY = 0;
    private float targetX = 0, targetY = 0;
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        //TODO remove
        if (!Util.isHost) return true;

        float xScreen = e.getX();
        float yScreen = e.getY();
        WindowManager wm = (WindowManager) Util.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x, screenHeight = size.y;

        startX = renderer.toDraw.get("player" + rand).translationX;
        startY = renderer.toDraw.get("player" + rand).translationY;

        float x = targetX = (xScreen / screenWidth) * -2.0f + 1.0f + renderer.cameraX;
        float y = targetY = (yScreen / screenHeight) * -2.0f + 1.0f + renderer.cameraY;

//        renderer.toDraw.get("player" + rand).translationX = x;
//        renderer.toDraw.get("player" + rand).translationY = y;

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

    public static int loadShader(int type, String shaderCode)
    {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public class Renderer implements GLSurfaceView.Renderer
    {
        public GameObjectMap toDraw = new GameObjectMap();
        private float cameraX = 0f, cameraY = 0f;

        private final float[] mvpMatrix = new float[16], // model view projection
                projectionMatrix = new float[16],
                viewMatrix = new float[16];

        public void generateObject() //hardcoded to a 30 x 30 board
        {
            float x, y;
            x = (float) Math.random() * 15;
            if(randomSign())
                x = x * -1.0f;
            y = (float) Math.random() * 15;
            if(randomSign())
                y = y * -1.0f;
            toDraw.put(new Square(Util.loadBitmap("drawable/zorua")).setState(0f, x, y, 0.4f, 0.5f).setName("runner" + rand));
        }

        private boolean randomSign()
        {
            double sign = Math.random();
            if(sign <= 0.5)
                return true;
            else
                return false;
        }

        public boolean checkCollision(GameObject a, GameObject b) //check if two given objects collide
        {
            boolean aCollision = a.translationX + a.scaleX >= b.translationX && b.translationX + b.scaleX >= a.translationX;
            boolean bCollision = a.translationY + a.scaleY >= b.translationY && b.translationY + b.scaleY >= a.translationY;
            return aCollision && bCollision;
        }

        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            float[] color = Color.SARCOLINE;
            GLES20.glClearColor(color[0], color[1], color[2], color[3]);

            toDraw.put(new Square(Util.loadBitmap("drawable/sanic")).setState(0f, 0f, 0f, 1f, 1f).setName("player" + rand));
            generateObject();
            toDraw.put(new Square(Util.loadBitmap("drawable/grid")).setState(0f, 0f, -1f, 30f, 30f).setName("background" + rand));
        }

        private long lastTime = -1;
        public void onDrawFrame(GL10 unused)
        {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // ORIGINAL:
//            Matrix.setLookAtM(viewMatrix, 0,
//                    /* eye    */ 0, 0, -3,
//                    /* center */ 0f, 0f, 0f,
//                    /* up     */ 0f, 1.0f, 0.0f);

            GameObject player = toDraw.get("player" + rand);

            long thisTime = System.nanoTime();
            if(lastTime != -1)
            {
                float tpf = (thisTime - lastTime) / 1_000_000_000f;

                tpf /= 2; // speed control

                if(targetX - startX != 0)
                {
                    float percent = Math.max((player.translationX - startX) / (targetX - startX) + tpf, 1);

                    percent = 1; // FIXME interpolation disabled since it doesn't work

                    float playerX = startX + (percent * (targetX - startX));
                    float playerY = startY + (percent * (targetY - startY));

                    player.translationX = playerX;
                    player.translationY = playerY;

                    if(percent == 1)
                    {
                        startX = playerX;
                        startY = playerY;
                    }
                }

            }
            lastTime = thisTime;

            cameraX = player.translationX;
            cameraY = player.translationY;

            Matrix.setLookAtM(viewMatrix, 0,
                    /* eye    */ cameraX, cameraY, -3f,
                    /* center */ cameraX, cameraY, 0f,
                    /* up     */ 0f, 1f, 0f);

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

            if(checkCollision(toDraw.get("player" + rand), toDraw.get("runner" + rand))) //should probably not hardcode index 1?
            {
                toDraw.remove("runner" + rand);
                generateObject();
            }
        }

        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;

            // apply the projection matrix
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }
    }
}