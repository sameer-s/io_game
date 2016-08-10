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

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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

	private static final float BASE_SPEED = 4.0f;
	private static float SPEED = BASE_SPEED;

	public static final float distan = 2;

	public static PowerUp[] powerUps = new PowerUp[16];

	// private static final String rand = UUID.randomUUID().toString();
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
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	private float targetX = 0, targetY = 0;

	private MotionEvent lastEvent;
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent e)
	{
		// TODO remove
		if(!Util.isHost)
			return true;

		if(e.getActionMasked() == MotionEvent.ACTION_DOWN  || e.getActionMasked() == MotionEvent.ACTION_MOVE)
		{

			float xScreen = e.getX();
			float yScreen = e.getY();

			WindowManager wm = (WindowManager) Util.context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int screenWidth = size.x, screenHeight = size.y;

			targetX = (xScreen / screenWidth) * -2.0f + 1.0f
					+ renderer.cameraX;
			targetY = (yScreen / screenHeight) * -2.0f + 1.0f
					+ renderer.cameraY;

			targetX = Util.clamp(targetX, -15, 15);
			targetY = Util.clamp(targetY, -15.7f, 13.6f);

			lastEvent = e;

		}
		return true;
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
		private int globalTimer = 0, limitTimer = 5;
		boolean speed = false, slow = false, grow = false, invincible = false, canCollide = true;

		private final float[] mvpMatrix = new float[16], // model view projection
				projectionMatrix = new float[16], viewMatrix = new float[16];

		public void speedupTimer() {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					globalTimer++;
					SPEED = BASE_SPEED * 2.5f;
					if (globalTimer == limitTimer) {
						SPEED = BASE_SPEED;
						timer.cancel();
						return;
					}
				}
			}, 0, 1000);
		}

		public void slowdownTimer() {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					globalTimer++;
					SPEED = BASE_SPEED / 2.5f;
					if (globalTimer == limitTimer) {
						SPEED = BASE_SPEED;
						timer.cancel();
						return;
					}
				}
			}, 0, 1000);
		}

		public void growUp() {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					globalTimer++;
					toDraw.get("grow" + rand).setState(0.75f, 0.75f);
					if (globalTimer == limitTimer) {
						toDraw.get("grow" + rand).setState(0.5f, 0.5f);
						timer.cancel();
						return;
					}
				}
			}, 0, 1000);
		}

		public void invincibility() {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					globalTimer++;
					canCollide = false;
					if (globalTimer == limitTimer) {
						canCollide = true;
						timer.cancel();
						return;
					}
				}
			}, 0, 1000);
		}

		public void generateObject(int number)
		{
			Random r = new Random();
			float x, y;
			x = (float) Math.random() * distan;
			if(r.nextBoolean())
				x = x * -1.0f;
			y = (float) Math.random() * distan;
			if(r.nextBoolean())
				y = y * -1.0f;
			int type = TypeGen();
			if(type==1)toDraw.put(new Square(Util.loadBitmap("drawable/pizza")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + number));
			else if(type==2) toDraw.put(new Square(Util.loadBitmap("drawable/tomato")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + number));
			else if(type==3) toDraw.put(new Square(Util.loadBitmap("drawable/burger")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + number));
			else if(type==4) toDraw.put(new Square(Util.loadBitmap("drawable/cookie")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + number));
			powerUps[number] = new PowerUp(type, number);
			System.out.println("Spawning at: "+ "("+x+","+y+")");
		}

		public void intialObject()
		{
			for(int i = 0; i<16; i++){
				Random r = new Random();
				float x, y;
				x = (float) Math.random() * distan;
				if (r.nextBoolean())
					x = x * -1.0f;
				y = (float) Math.random() * distan;
				if (r.nextBoolean())
					y = y * -1.0f;
				int type = TypeGen();
				if(type==1)toDraw.put(new Square(Util.loadBitmap("drawable/pizza")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + i));
				else if(type==2) toDraw.put(new Square(Util.loadBitmap("drawable/tomato")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + i));
				else if(type==3) toDraw.put(new Square(Util.loadBitmap("drawable/burger")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + i));
				else if(type==4) toDraw.put(new Square(Util.loadBitmap("drawable/cookie")).setState(0f, x, y, 0.4f, 0.5f).setName("object" + i));
				powerUps[i] = new PowerUp(type,i);
				System.out.println("Spawning at: "+ "("+x+","+y+")");
			}
		}

		public int TypeGen()
		{
			int temp=0;
			double RAND = Math.random();
			if(RAND < 0.3) temp =1;
			else if(RAND >=0.3 && RAND < 0.5)  temp=2;
			else if(RAND>= 0.5 && RAND <0.7) temp =3;
			else if(RAND >=0.7)temp=4;
			return temp;
		}

		/*
			if(speed)
				toDraw.put(new Square(Util.loadBitmap("drawable/pizza")).setState(0f, x, y, 0.5f, 0.5f).setName("speedup" + rand));
			if(slow)
				toDraw.put(new Square(Util.loadBitmap("drawable/tomato")).setState(0f, x, y, 0.5f, 0.5f).setName("slowdown" + rand));
			if(grow)
				toDraw.put(new Square(Util.loadBitmap("drawable/burger")).setState(0f, x, y, 0.5f, 0.5f).setName("grow" + rand));
			if(invincible)
				toDraw.put(new Square(Util.loadBitmap("drawable/cookie")).setState(0f, x, y, 0.5f, 0.5f).setName("invincible" + rand));
			toDraw.put(new Square(Util.loadBitmap("drawable/pizza")).setState(0f, x, y, 0.5f, 0.5f).setName("speedup" + rand)); //just a backup object
			System.out.println(x + ", " + y);
		*/

		public boolean checkCollision(GameObject a, GameObject b)
		{
			if(!canCollide)
				return false;
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

			toDraw.put(new Square(Util.loadBitmap("drawable/sanic")).setState(0f, 0f, 0f, 1f, 1f).setName("player" + rand));
			intialObject();
			toDraw.put(new Square(Util.loadBitmap("drawable/grid")).setState(0f, 0f, -1f, 30f, 30f).setName("background" + rand));
		}

		long lastTime = -1;
		public void onDrawFrame(GL10 unused)
		{
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

			GameObject player = toDraw.get("player" + rand);

			long thisTime = System.nanoTime();
			if(lastTime != -1)
			{
				float playerX = player.translationX;
				float playerY = player.translationY;

				float totalDistance = (float) Math.hypot(targetX - playerX, targetY - playerY);

				if(totalDistance != 0)
				{
					float distanceToTravel = Math.min(totalDistance, SPEED * ((thisTime - lastTime) / 1_000_000_000f));

					float ratio = distanceToTravel / totalDistance;

					playerX = (playerX + (ratio * (targetX - playerX)));
					playerY = (playerY + (ratio * (targetY - playerY)));

					playerX = Util.clamp(playerX, -15, 15);
					playerY = Util.clamp(playerY, -15.7f, 13.6f);

					cameraX = player.translationX = playerX;
					cameraY = player.translationY = playerY;

					System.out.println("Player: " + playerX + ", " + playerY);
				}
				else if (lastEvent != null){
					onTouchEvent(lastEvent);
				}
			}
			lastTime = thisTime;

			Matrix.setLookAtM(viewMatrix, 0, /* eye */ cameraX, cameraY, -3f,
					/* center */ cameraX, cameraY, 0f, /* up */ 0f, 1f, 0f);

			Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

			for(String s : toDraw.keySet())
			{
				GameObject go = toDraw.get(s);
				go.draw(mvpMatrix);

				// noinspection StatementWithEmptyBody
				if(go.name.contains(rand) && Util.isHost /* TODO remove */)
				{
					// Util.broadcastMessage(go);
				}
			}

			if(checkCollision(toDraw.get("player" + rand), toDraw.get("speedup" + rand)))
			{
				speed = true;
				generateObject(1);
				speedupTimer();
				toDraw.remove("speedup" + rand);
				speed = false;
			}

			if(checkCollision(toDraw.get("player" + rand), toDraw.get("slowdown" + rand)))
			{
				slow = true;
				generateObject(2);
				slowdownTimer();
				toDraw.remove("slowdown" + rand);
				slow = false;
			}

			if(checkCollision(toDraw.get("player" + rand), toDraw.get("grow" + rand))) {
				grow = true;
				generateObject(3);
				growUp();
				toDraw.remove("grow" + rand);
				grow = false;
			}

			if(checkCollision(toDraw.get("player" + rand), toDraw.get("invincible" + rand)))
			{
				speed = true;
				canCollide = false;
				generateObject(4);
				invincibility();
				toDraw.remove("invincible" + rand);
				speed = false;
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