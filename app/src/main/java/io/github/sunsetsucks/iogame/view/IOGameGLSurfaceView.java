package io.github.sunsetsucks.iogame.view;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.sunsetsucks.iogame.Util;
import io.github.sunsetsucks.iogame.shape.Color;
import io.github.sunsetsucks.iogame.shape.GameObject;
import io.github.sunsetsucks.iogame.shape.player.Chaser;
import io.github.sunsetsucks.iogame.shape.player.Player;
import io.github.sunsetsucks.iogame.shape.Square;
import io.github.sunsetsucks.iogame.shape.player.Runner;
import io.github.sunsetsucks.iogame.shape.powerup.GrowthPowerup;
import io.github.sunsetsucks.iogame.shape.powerup.InvincibilityPowerup;
import io.github.sunsetsucks.iogame.shape.powerup.Powerup;
import io.github.sunsetsucks.iogame.shape.powerup.SpeedDownPowerup;
import io.github.sunsetsucks.iogame.shape.powerup.SpeedUpPowerup;

/**
 * Created by ssuri on 7/25/16.
 */
public class IOGameGLSurfaceView extends GLSurfaceView
{
    public Renderer renderer;

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
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    private float targetX = 0, targetY = 0;

    private MotionEvent lastEvent;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e)
    {
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN
                || e.getActionMasked() == MotionEvent.ACTION_MOVE)
        {

            float xScreen = e.getX();
            float yScreen = e.getY();

            WindowManager wm = (WindowManager) Util.context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x, screenHeight = size.y;

            targetX = (xScreen / screenWidth) * -2.0f + 1.0f + renderer.cameraX;
            targetY = (yScreen / screenHeight) * -2.0f + 1.0f
                    + renderer.cameraY;

            targetX = Util.clamp(targetX, -14.6f, 15);
            targetY = Util.clamp(targetY, -15.7f, 13.7f);

            lastEvent = e;
        }

        return true;
    }

    public void udpUpdate(byte[] bytes)
    {
        Player p;

        byte compId = Player.compId(bytes);

        if (renderer.players.containsKey(compId))
        {
            if (Player.isChaser(bytes))
            {
                p = new Chaser(compId);
            } else
            {
                p = new Runner(compId);
            }

            renderer.players.put(compId, p);
        }
        else
        {
            p = renderer.players.get(compId);
        }

        p.fromBytes(bytes);
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
        public List<GameObject> toDraw = new ArrayList<>();
        public List<Powerup> powerups = new ArrayList<>();
        public Map<Byte, Player> players = new HashMap<>();
        public List<Byte> deadPlayers = new ArrayList<>();

        private float cameraX = 0f, cameraY = 0f;

        // model view projection
        private final float[] mvpMatrix = new float[16], projectionMatrix = new float[16], viewMatrix = new float[16];

        public void generateObject()
        {
            Random r = new Random();
            float x, y;
            x = (float) Math.random() * 15;
            if (r.nextBoolean())
                x = x * -1.0f;
            y = (float) Math.random() * 15;
            if (r.nextBoolean())
                y = y * -1.0f;

            Powerup p;
            int rand = r.nextInt(100);
            if (rand < 25) // 25 percent
            {
                p = new SpeedUpPowerup();
            } else if (rand < 20) // 25 percent
            {
                p = new InvincibilityPowerup();
            } else if (rand < 30) // 25 percent
            {
                p = new SpeedDownPowerup();
            } else // 25 percent
            {
                p = new GrowthPowerup();
            }

            p.translationX = x;
            p.translationY = y;
            powerups.add(p);

            Util.broadcastMessage(p.toSerializable(false), true);
        }

        public boolean checkCollision(GameObject a, GameObject b)
        {
            boolean aCollision = a.translationX + a.scaleX >= b.translationX
                    && b.translationX + b.scaleX >= a.translationX;
            boolean bCollision = a.translationY + a.scaleY >= b.translationY
                    && b.translationY + b.scaleY >= a.translationY;
            return aCollision && bCollision;
        }

        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            float[] color = Color.SARCOLINE;
            GLES20.glClearColor(color[0], color[1], color[2], color[3]);

            if (Util.isHost)
            {
                players.put(Util.compId, new Chaser(Util.compId));
                for (int i = 0; i < 16; i++) generateObject();
            } else
            {
                players.put(Util.compId, new Runner(Util.compId));
            }

            toDraw.add(new Square(Util.loadBitmap("drawable/grid")).setState(0f, 0f, -1f, 30f, 30f));
        }

        long lastTime = -1;

        public void onDrawFrame(GL10 unused)
        {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            Player player = players.get(Util.compId);

            long thisTime = System.nanoTime();
            if (lastTime != -1)
            {
                float playerX = player.translationX;
                float playerY = player.translationY;

                float totalDistance = (float) Math.hypot(targetX - playerX,
                        targetY - playerY);

                if (totalDistance != 0)
                {
                    float distanceToTravel = Math.min(totalDistance,
                            player.getSpeed() * ((thisTime - lastTime) / 1_000_000_000f));

                    float ratio = distanceToTravel / totalDistance;

                    playerX = (playerX + (ratio * (targetX - playerX)));
                    playerY = (playerY + (ratio * (targetY - playerY)));

                    playerX = Util.clamp(playerX, -15, 14.5f);
                    playerY = Util.clamp(playerY, -15.5f, 13.6f);

                    cameraX = player.translationX = playerX;
                    cameraY = player.translationY = playerY;
                } else if (lastEvent != null)
                {
                    onTouchEvent(lastEvent);
                }

                Util.broadcastMessage(player.toBytes(), false);
            }
            lastTime = thisTime;

            Matrix.setLookAtM(viewMatrix, 0, /* eye */ cameraX, cameraY, -3f,
                    /* center */ cameraX, cameraY, 0f, /* up */ 0f, 1f, 0f);

            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            // Checks collisions and notifies the relevant host
            if (Util.isHost)
            {
                for (Byte b : players.keySet())
                {
                    List<Powerup> toRemove = new ArrayList<>();
                    for (Powerup p : powerups)
                    {
                        if (checkCollision(p, players.get(b)) && p.doesContact(players.get(b)))
                        {
                            toRemove.add(p);
                            generateObject();

                            Serializable message = p.toSerializable(true);
                            Util.broadcastMessage(message, true);
                        }
                    }
                    powerups.removeAll(toRemove);
                }

                List<Byte> toRemove = new ArrayList<>();
                for (byte b : players.keySet())
                {
                    if (b == 0) continue;

                    if (checkCollision(player, players.get(b)))
                    {
                        Util.broadcastMessage(players.get(b).destroyedMessage(), true);
                        toRemove.add(b);
                    }
                }

                for (Byte b : toRemove)
                {
                    players.remove(b);
                }
            }

            // Draw all three kinds of game objects
            for (GameObject go : toDraw) go.draw(mvpMatrix);
            for (Powerup pow : powerups) pow.draw(mvpMatrix);
            for (Player p : players.values()) p.draw(mvpMatrix);
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