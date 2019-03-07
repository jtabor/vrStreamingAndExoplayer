package com.research.jtabor.tilesynctest;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.Player;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import exoaartest.research.josht.exoplayerlibrarytest.SurfaceTest;

/**
 * Created by josht on 3/2/2019.
 */



public class BufferedRenderer implements GLSurfaceView.Renderer {

    SurfaceTest st;
    Context context;

    private final String vertexShaderSource =
            "//VERTEX SHADER\n" +
                    "attribute vec3 vPosition;\n" +
                    "attribute vec2 texCoord;\n" +
                    "varying vec2 texCoordinates;\n" +
                    "void main() {\n" +
                    "\ttexCoordinates = texCoord;\n" +
                    "\tgl_Position = vec4(vPosition.x,vPosition.y,vPosition.z,1f);\n" +
                    "}";

    private final String fragmentShaderSource =
            "//FRAGMENT SHADER\n" +
                    "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform vec3 vColor;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "varying vec2 texCoordinates;\n" +
                    "void main(){\n" +
                    "\tgl_FragColor = texture2D(s_texture,texCoordinates);\n" +
                    "}";

    private int vertexShader;
    private int fragmentShader;
//    static float baseTile[] =  {
//            0f,0f,0f,
//            0.5f,0.5f,0f,
//            0f,0.5f,0f,
//            0f,0f,0f,
//            0.5f,0f,0f,
//            0.5f,0.5f,0f
//    };
static float baseTile[] =  {
        -1f,-1f,0f,
        0f,0f,0f,
        -1f,0f,0f,
        -1f,-1f,0f,
        0f,-1f,0f,
        0f,0f,0f
};
    static float oneTexCoords[] = {
            0f,1f,
            1f,0f,
            0f,0f,
            0f,1f,
            1f,1f,
            1f,0f
    };

    float color[] = {1f,0f,0f};

    int texIds[];
    float allTiles[][];
    float allTexCoords[][];

    FloatBuffer vertexBuffer[] = new FloatBuffer[4];
    FloatBuffer texCoordsBuffer[]  = new FloatBuffer[4];

    int glslProgram;
    int vPosition;
    int vColor;
    int texCoord;

    int textureHandles[];

    int textureUnits[] = {
            GLES20.GL_TEXTURE0,
            GLES20.GL_TEXTURE1,
            GLES20.GL_TEXTURE2,
            GLES20.GL_TEXTURE3,
            GLES20.GL_TEXTURE4,
            GLES20.GL_TEXTURE5,
            GLES20.GL_TEXTURE6,
            GLES20.GL_TEXTURE7};

    private float[] addToArray(float[] input, float x, float y, float z){
        float[] toReturn = new float[input.length];
        for (int i = 0; i < input.length; i = i+3){
            toReturn[i] = input[i] + x;
            toReturn[i+1] = input[i+1] + y;
            toReturn[i+2] = input[i+2] + z;
        }
        return toReturn;
    }
    public void setContext(Context newContext){
        context = newContext;
    }
    //create the textures here and link them to Exoplayer
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        //Generate the texcoords and vertex coords for our 4 tiles.  (This is hardcoded, might want to make a more robust one later..
        allTiles = new float[4][baseTile.length];
        allTexCoords = new float[4][oneTexCoords.length];
        System.arraycopy(baseTile,0,allTiles[0],0,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords[0],0,oneTexCoords.length);
        System.arraycopy(addToArray(baseTile,0f,1f,0f),0,allTiles[1],0,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords[1],0,oneTexCoords.length);
        System.arraycopy(addToArray(baseTile,1f,1f,0f),0,allTiles[2],0,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords[2],0,oneTexCoords.length);
        System.arraycopy(addToArray(baseTile,1f,0f,0f),0,allTiles[3],0,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords[3],0,oneTexCoords.length);


        for (int i = 0; i < 4; i++) {
            ByteBuffer bb = ByteBuffer.allocateDirect(allTiles[i].length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer[i] = bb.asFloatBuffer();
            vertexBuffer[i].put(allTiles[i]);
            vertexBuffer[i].position(0);

            ByteBuffer bb2 = ByteBuffer.allocateDirect(allTexCoords[i].length * 4);
            bb2.order(ByteOrder.nativeOrder());
            texCoordsBuffer[i] = bb2.asFloatBuffer();
            texCoordsBuffer[i].put(allTexCoords[i]);
            texCoordsBuffer[i].position(0);

        }
        Looper.prepare();
        st = new SurfaceTest(4,context,1920,1080,true,false);
        st.init("http://pages.cs.wisc.edu/~tabor/bunny_test_1080_60-tiles.mpd");
        textureHandles = st.getTextureIds();
        st.initExoplayer();
        st.startVideo();

        glslProgram = GLES20.glCreateProgram();
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderSource);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderSource);
        GLES20.glAttachShader(glslProgram,vertexShader);
        GLES20.glAttachShader(glslProgram,fragmentShader);
        GLES20.glLinkProgram(glslProgram);


    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //do nothing..  just a prototype.
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width/height;
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        //display textures on screen.  Decide when to do it.
        st.updateTexture();

        for (int i = 0; i < 4; i++) {
            GLES20.glUseProgram(glslProgram);
            checkErrors("1");
            GLES20.glActiveTexture(textureUnits[i]);
            checkErrors("2");
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureHandles[i]);
            checkErrors("3");
            int programTexHandle = GLES20.glGetUniformLocation(glslProgram, "s_texture");
            checkErrors("4");
            GLES20.glUniform1i(programTexHandle, i);
            checkErrors("5");
            vPosition = GLES20.glGetAttribLocation(glslProgram, "vPosition");
            checkErrors("6");
            GLES20.glEnableVertexAttribArray(vPosition);
            checkErrors("7");
            GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer[i]);
            checkErrors("8");
            texCoord = GLES20.glGetAttribLocation(glslProgram, "texCoord");
            GLES20.glEnableVertexAttribArray(texCoord);
            GLES20.glVertexAttribPointer(texCoord, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordsBuffer[i]);
            vColor = GLES20.glGetUniformLocation(glslProgram, "vColor");
            checkErrors("9");
            GLES20.glUniform3fv(vColor, 1, color, 0);
            checkErrors("10");
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, allTiles[i].length / 3);
            checkErrors("11");
            GLES20.glDisableVertexAttribArray(vPosition);
        }

    }
    private int loadShader(int type, String shaderSource){
        int shaderHandle = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shaderHandle,shaderSource);
        GLES20.glCompileShader(shaderHandle);
        checkErrors("Loading Shader: " + shaderSource);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shaderHandle,GLES20.GL_COMPILE_STATUS,status,0);
        if (status[0] != GLES20.GL_TRUE){
            Log.e("JOSH-GRAPHICS","Shader not compiled: " + GLES20.glGetShaderInfoLog(shaderHandle));
        }
        return shaderHandle;
    }
    private void checkErrors(String errorTag){
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
            Log.e("JOSH-GRAPHICS",errorTag + ": " + error);
        }
    }
}
