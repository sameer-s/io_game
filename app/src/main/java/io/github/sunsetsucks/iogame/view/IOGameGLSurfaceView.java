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

        import java.util.ArrayList;
        import java.util.List;

        import javax.microedition.khronos.egl.EGLConfig;
        import javax.microedition.khronos.opengles.GL10;

        import io.github.sunsetsucks.iogame.shape.Color;
        import io.github.sunsetsucks.iogame.shape.GameObject;
        import io.github.sunsetsucks.iogame.shape.Shape;
        import io.github.sunsetsucks.iogame.shape.Square;

public class IOGameGLSurfaceView extends GLSurfaceView
{
    private Renderer renderer;
    private Context context;

    //Variables for Character movement and interaction.
    public static int currentX = 500;
    public static int currentY = 500;
    public static int speed = 5;
    public static int ScreenW = 2000;
    public static int ScreenH = 1500;
    public static boolean exist = true;

    public IOGameGLSurfaceView(Context context)
    {
        super(context);

        this.context = context;

        setEGLContextClientVersion(2);

        setPreserveEGLContextOnPause(true);

        renderer = new Renderer();

        setRenderer(renderer);

//        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public IOGameGLSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        float xScreen = e.getX();
        float yScreen = e.getY();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x, screenHeight = size.y;

        float x = (xScreen / screenWidth) * -2.0f + 1.0f;
        float y = (yScreen / screenHeight) * -2.0f + 1.0f;

        float Bx = (xScreen / screenWidth) * -2.0f +1.5f;
        float By = (yScreen / screenHeight) * -2.0f +1.5f;

        currentX = currentX + (int)(y*speed);
        currentY = currentY + (int)(x*speed);

        //Boundaries for Stage
        if(currentX <= 0) currentX=0;
        else if(currentX >= ScreenW) currentX=ScreenW;
        else if(currentY <=0) currentY=0;
        else if (currentY >= ScreenH) currentY = ScreenH;
        else {
            renderer.toDraw.get(0).translationX = x;
            renderer.toDraw.get(0).translationY = y;
            //renderer.toDraw.get(0).rotation += 2;

          // Power up Movement, currently incomplete, teleports powerups from their starting postition.
           if(exist) {
               renderer.toDraw.get(1).translationX = -Bx;
               renderer.toDraw.get(1).translationY = -By;
           }
        }
        //  System.out.println("("+currentX+","+currentY+")");
        return true;
    }

    public static class Renderer implements GLSurfaceView.Renderer
    {
        private List<GameObject> toDraw = new ArrayList<>();

        private final float[] mvpMatrix        = new float[16], // model view projection
                projectionMatrix = new float[16],
                viewMatrix       = new float[16];

        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            float[] color = Color.SARCOLINE;
            GLES20.glClearColor(color[0], color[1], color[2], color[3]);
//          shapesToDraw.add(((Shape) new Square().setState(45f, .5f, -.25f, 1f, 1f)).setColor(Color.GLAUCOUS));
            toDraw.add(((Shape) new Square().setState(0f, .5f, .25f, .6f, .6f)).setColor(Color.GLAUCOUS));
            generateObject();
        }

        public void generateObject() { //randomly place small target on screen
            float x, y;
            x = (float) Math.random();
            if(randomSign())
                x = x * -1.0f;
            y = (float) Math.random();
            if(randomSign())
                y = y * -1.0f;
            toDraw.add(((Shape) new Square().setState(0f, x, y, 0.25f, 0.25f)).setColor(Color.COQUELICOT));
        }

        private boolean randomSign()
        {
            double sign = Math.random();
            if(sign <= 0.5) return true;
            return false;
        }

        public boolean checkCollision(GameObject a, GameObject b) //check if two given objects collided
        {
            boolean aCollision = a.translationX + a.scaleX >= b.translationX && b.translationX + b.scaleX >= a.translationX;
            boolean bCollision = a.translationY + a.scaleY >= b.translationY && b.translationY + b.scaleY >= a.translationY;
            return aCollision && bCollision;
        }


        public void onDrawFrame(GL10 unused)
        {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            for(GameObject go : toDraw) go.draw(mvpMatrix);

            if(checkCollision(toDraw.get(0), toDraw.get(1))) //should probably not hardcode index 1?
            {
                toDraw.remove(1);
                speed =12;
                generateObject();
                exist =true;
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
