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

import io.github.sunsetsucks.iogame.game.GameInfo;
import io.github.sunsetsucks.iogame.shape.Color;
import io.github.sunsetsucks.iogame.shape.GameObject;
import io.github.sunsetsucks.iogame.shape.Shape;
import io.github.sunsetsucks.iogame.shape.Square;

public class IOGameGLSurfaceView extends GLSurfaceView
{
    private Renderer renderer;
    private Context context;

    //Variables for Character movement and interaction.
    public static int currentX = 1500;
    public static int currentY = 1000;
    public static int SpawnX = currentX;
    public static int SpawnY = currentY;

    public static int speed = 4;

    public static int ScreenW = 3000;
    public static int ScreenH = 2000;

    public static int SpawnD = 400;

    public static float powerX = 0;
    public static float powerY = 0;

    public static double Pspeed = 0.0025;

    public static int Ptotal =1;

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

        currentX = currentX + (int)(y*speed);
        currentY = currentY + (int)(x*speed);

        //Boundaries for Stage
        if(currentX <= 0) {
            currentX = 0;
        }
        else if(currentX >= ScreenW){
            currentX=ScreenW;
        }
        else if(currentY <=0){
            currentY=0;
        }
        else if (currentY >= ScreenH){
            currentY = ScreenH;
        }

        else {
            renderer.toDraw.get(0).translationX = x/5;
            renderer.toDraw.get(0).translationY = y/5;

        //    System.out.println("Player: ("+x+","+y+")");
        // System.out.println("Stage: ("+currentX+","+currentY+")");

        //    if(pExist) {
                CurrentXPosition(x);
                CurrentYPosition(y);
                renderer.toDraw.get(1).translationX = powerX;
                renderer.toDraw.get(1).translationY = powerY;
         //   }
        }
        return true;
    }

    public static float CurrentXPosition(float x2){
        if(x2 < -0.1 && x2>=-0.5) powerX = powerX + (float)(speed*(Pspeed));
        if(x2<-0.5) powerX = powerX + (float)(speed*(2*Pspeed));
         if(x2 >0.1 && x2<0.5) powerX = powerX - (float)(speed*(Pspeed));
         if(x2 >=0.5) powerX = powerX - (float)(speed*(2*Pspeed));
        return x2;
    }

    public static float CurrentYPosition(float y2){
        if(y2 < -0.1 && y2>=-0.5) powerY = powerY + (float)(speed*(Pspeed));
         if(y2<-0.5) powerY = powerY + (float)(speed*(2*Pspeed));
         if(y2 >0.1 && y2<0.5) powerY = powerY -(float)(speed*(2*Pspeed));
         if(y2 >=0.5) powerY = powerY - (float)(speed*(2*Pspeed));
        return y2;
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
            toDraw.add(((Shape) new Square().setState(0f, .5f, .25f, .6f, .6f)).setColor(Color.GLAUCOUS));
            generateObject();
        }

        public void generateObject() { //randomly place small target on screen
            float x, y;
            x = (float) Math.random();
            if(randomSign())
                x = x * -1.2f;
            y = (float) Math.random();
            if(randomSign())
                y = y * -1.2f;
            powerX = x;
            powerY = y;
           // pExist=true;
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
            //System.out.println("Working!!");
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
               // pExist=false;

                toDraw.remove(1);
                //System.out.println("Worked");
                speed++;
                generateObject();
            }
         //   if(currentX - SpawnD >= SpawnX || SpawnX +SpawnD <= currentX || currentY - SpawnD >= SpawnY || SpawnY + SpawnD <= currentY) powerupSpawn();

        }

    /*    public void powerupSpawn(){
            double Rand = Math.random();
                if(Rand <= 0.4){
                    pExist=true;
                    generateObject();
                }
                    else{
                    //System.out.println("No Spawn");
                    SpawnX = currentX;
                    SpawnY = currentY;
                    //System.out.println("Working!");
                }

        } */

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
