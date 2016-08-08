package io.github.sunsetsucks.iogame.shape;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
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
    private FloatBuffer uvBuffer;

    // Default shaders
    private static final int vertexShader = IOGameGLSurfaceView.loadShader(GLES20.GL_VERTEX_SHADER, "uniform mat4 uMVPMatrix;attribute vec4 vPosition;void main() {  gl_Position = uMVPMatrix * vPosition;}");
    private static final int fragmentShader = IOGameGLSurfaceView.loadShader(GLES20.GL_FRAGMENT_SHADER, "precision mediump float;uniform vec4 vColor;void main() {  gl_FragColor = vColor;}");

    // Texture shaders
    private static final int vertexShaderT = IOGameGLSurfaceView.loadShader(GLES20.GL_VERTEX_SHADER, "uniform mat4 uMVPMatrix;attribute vec4 vPosition;attribute vec2 a_texCoord;varying vec2 v_texCoord;void main() {  gl_Position = uMVPMatrix * vPosition;  v_texCoord = a_texCoord;}");
    private static final int fragmentShaderT = IOGameGLSurfaceView.loadShader(GLES20.GL_FRAGMENT_SHADER, "precision mediump float;varying vec2 v_texCoord;uniform sampler2D s_texture;void main() {  gl_FragColor = texture2D( s_texture, v_texCoord );}");

    private static final int VERTEX_STRIDE = 3 * 4; // 3 coordinates per vertex, 4 bytes per vertex

    private int[] textureNames;

    private int glProgram;
    private Bitmap texture;

    public abstract float[] getCoords();
    public abstract short[] getDrawOrder();
    public abstract float[] getUVs();

    public float[] color = Color.RED;

    public Shape()
    {
        this(null);
    }

    public Shape(Bitmap texture)
    {
        float[] coords = getCoords();
        short[] drawOrder = getDrawOrder();
        float[] uvs = getUVs();

        // initialize byte buffer for coords
        ByteBuffer cb = ByteBuffer.allocateDirect(coords.length * 4); // (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        vertexBuffer = cb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2); // (# of coordinate values * 2 bytes per short)
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // initialize byte buffer for UV coords
        ByteBuffer uvb = ByteBuffer.allocateDirect(uvs.length * 4); // (# of coordinate values * 4 bytes per float)
        uvb.order(ByteOrder.nativeOrder());
        uvBuffer = uvb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        // Texture
        if(texture != null)
        {
            textureNames = new int[1];
            GLES20.glGenTextures(1, textureNames, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);

            texture.recycle();

            glProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(glProgram, vertexShaderT);
            GLES20.glAttachShader(glProgram, fragmentShaderT);
            GLES20.glLinkProgram(glProgram);
        }
        // Solid Color
        else
        {
            glProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(glProgram, vertexShader);
            GLES20.glAttachShader(glProgram, fragmentShader);
            GLES20.glLinkProgram(glProgram);
        }

        this.texture = texture;
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
                VERTEX_STRIDE, vertexBuffer);

        // Texture
        if(texture != null)
        {
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_BLEND);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[0]);

            int texCoordHandle = GLES20.glGetAttribLocation(glProgram, "a_texCoord");
            GLES20.glEnableVertexAttribArray(texCoordHandle);
            GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);
        }
        // Solid Color
        else
        {
            // let us access (and set) the current color
            int colorHandle = GLES20.glGetUniformLocation(glProgram, "vColor");

            // set color
            GLES20.glUniform4fv(colorHandle, 1, color, 0);
        }

        // apply matrix transformation
        int mvpMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, resultMatrix, 0);

        int textureSampleHandle = 0;
        // More texturing
        if(texture != null)
        {
            GLES20.glGetUniformLocation(glProgram, "s_texture");
            GLES20.glUniform1i(textureSampleHandle, 0); // corresponds to GLES20.GL_TEXTURE0
        }

        // draw shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, getDrawOrder().length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // disables our access to the vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        if(texture != null)
        {
            GLES20.glDisableVertexAttribArray(textureSampleHandle);
            GLES20.glDisable(GLES20.GL_BLEND);
        }
    }
}