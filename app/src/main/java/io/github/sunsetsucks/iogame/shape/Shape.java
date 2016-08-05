package io.github.sunsetsucks.iogame.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import io.github.sunsetsucks.iogame.view.IOGameGLSurfaceView;

/**
 * Created by ssuri on 7/25/16.
 *
 */
public abstract class Shape extends GameObject
{
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // Default shaders
//    private static final int vertexShader = IOGameGLSurfaceView.Renderer.loadShader(GLES20.GL_VERTEX_SHADER, "uniform mat4 uMVPMatrix;attribute vec4 vPosition;void main() {  gl_Position = uMVPMatrix * vPosition;}");
//    private static final int fragmentShader = IOGameGLSurfaceView.Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, "precision mediump float;uniform vec4 vColor;void main() {  gl_FragColor = vColor;}");

    // Texture shaders
    private static final int vertexShader = IOGameGLSurfaceView.Renderer.loadShader(GLES20.GL_VERTEX_SHADER, "attribute vec4 position; attribute vec4 inputTextureCoordinate; varying vec2 textureCoordinate; void main() { gl_Position = position; textureCoordinate = inputTextureCoordinate.xy; }");
    private static final int fragmentShader = IOGameGLSurfaceView.Renderer.loadShader(GLES20.GL_VERTEX_SHADER, "varying highp vec2 textureCoordinate; uniform sampler2D videoFrame; void main() { gl_FragColor = texture2D(videoFrame, textureCoordinate); }");

    private final int vertexCount = getCoords().length / 3; // 3 coordinates per vertex
    private final int vertexStride = 3 * 4; // 3 coordinates per vertex, 4 bytes per vertex

    private int glProgram;

    public abstract float[] getCoords();
    public abstract short[] getDrawOrder();
    public abstract float[] getTextureCoords();
    public float[] color = Color.RED;

    public Shape()
    {
        float[] coords = getCoords();
        short[] drawOrder = getDrawOrder();

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4); // (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2); // (# of coordinate values * 2 bytes per short)
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        glProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(glProgram, vertexShader);
        GLES20.glAttachShader(glProgram, fragmentShader);
        GLES20.glLinkProgram(glProgram);
    }

    public Shape setColor(float[] color)
    {
        this.color = color;
        return this;
    }

    public void draw(float[] mvpMatrix)
    {
        float[] resultMatrix =  new float[16];
        Matrix.multiplyMM(resultMatrix, 0, mvpMatrix, 0, getMovementMatrix(), 0);

        GLES20.glUseProgram(glProgram);

        // lets us access (and set) the vertex array
        int positionHandle = GLES20.glGetAttribLocation(glProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the coordinate data
        GLES20.glVertexAttribPointer(positionHandle, 3, // coordinates per vertex
                GLES20.GL_FLOAT, false, // false -> data is not normalized
                vertexStride, vertexBuffer);

        // let us access (and set) the current color
        int colorHandle = GLES20.glGetUniformLocation(glProgram, "vColor");

        // set color
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // apply matrix transformation
        int mvpMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, resultMatrix, 0);

        // draw shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, getDrawOrder().length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // disables our access to the vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}