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

	private static final float SPEED = 2.0f;

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
		if (!Util.isHost)
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

		private final float[] mvpMatrix = new float[16], // model view
															// projection
				projectionMatrix = new float[16], viewMatrix = new float[16];

		public void generateObject() // FIXME hardcoded to a 30 x 30 board
		{
			Random r = new Random();
			float x, y;
			x = (float) Math.random() * 15;
			if (r.nextBoolean())
				x = x * -1.0f;
			y = (float) Math.random() * 15;
			if (r.nextBoolean())
				y = y * -1.0f;
			toDraw.put(new Square(Util.loadBitmap("drawable/zorua"))
					.setState(0f, x, y, 0.4f, 0.5f).setName("runner" + rand));
		}

		public boolean checkCollision(GameObject a, GameObject b) // check if
																	// two given
																	// objects
																	// collide
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

			toDraw.put(new Square(Util.loadBitmap("drawable/sanic"))
					.setState(0f, 0f, 0f, 1f, 1f).setName("player" + rand));
			generateObject();
			toDraw.put(new Square(Util.loadBitmap("drawable/grid"))
					.setState(0f, 0f, -1f, 30f, 30f)
					.setName("background" + rand));
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
				}
				else if (lastEvent != null){
					onTouchEvent(lastEvent);
				}
			}
			lastTime = thisTime;

			Matrix.setLookAtM(viewMatrix, 0, /* eye */ cameraX, cameraY, -3f,
					/* center */ cameraX, cameraY, 0f, /* up */ 0f, 1f, 0f);

			Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

			for (String s : toDraw.keySet())
			{
				GameObject go = toDraw.get(s);
				go.draw(mvpMatrix);

				// noinspection StatementWithEmptyBody
				if (go.name.contains(rand) && Util.isHost /* TODO remove */)
				{
					// Util.broadcastMessage(go);
				}
			}

			if (checkCollision(toDraw.get("player" + rand),
					toDraw.get("runner" + rand))) // should probably not
													// hardcode index 1?
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