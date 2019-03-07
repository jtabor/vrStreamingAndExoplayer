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
                    "uniform samplerExternalOES s_texture[4];\n" +
                    "varying vec2 texCoordinates;\n" +
                    "void main(){\n" +
                    "\tgl_FragColor = texture2D(s_texture[1],texCoordinates);\n" +
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
            0f,0f,
            1f,1f,
            0f,1f,
            0f,0f,
            1f,0f,
            1f,1f
    };

    float color[] = {1f,0f,0f};

    int texIds[];
    float allTiles[];
    float allTexCoords[];

    FloatBuffer vertexBuffer;
    FloatBuffer texCoordsBuffer;

    int glslProgram;
    int vPosition;
    int vColor;
    int texCoord;

    int textureHandles[];

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
        allTiles = new float[baseTile.length*4];
        allTexCoords = new float[oneTexCoords.length*4];
        System.arraycopy(baseTile,0,allTiles,0,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords,0,oneTexCoords.length);
        System.arraycopy(addToArray(baseTile,0f,1f,0f),0,allTiles,baseTile.length,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords,oneTexCoords.length,oneTexCoords.length);
        System.arraycopy(addToArray(baseTile,1f,1f,0f),0,allTiles,baseTile.length*2,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords,oneTexCoords.length*2,oneTexCoords.length);
        System.arraycopy(addToArray(baseTile,1f,0f,0f),0,allTiles,baseTile.length*3,baseTile.length);
        System.arraycopy(oneTexCoords,0,allTexCoords,oneTexCoords.length*3,oneTexCoords.length);

        ByteBuffer bb = ByteBuffer.allocateDirect(allTiles.length*4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(allTiles);
        vertexBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(allTexCoords.length*4);
        bb2.order(ByteOrder.nativeOrder());
        texCoordsBuffer = bb2.asFloatBuffer();
        texCoordsBuffer.put(allTexCoords);
        texCoordsBuffer.position(0);
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
        st.updateTexture();
        //display textures on screen.  Decide when to do it.
        GLES20.glUseProgram(glslProgram);
        checkErrors("1");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkErrors("2");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureHandles[0]);
        checkErrors("3");
        int programTexHandle = GLES20.glGetUniformLocation(glslProgram,"s_texture[0]");
        checkErrors("4");
        GLES20.glUniform1i(programTexHandle,0);
        checkErrors("5");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        checkErrors("2");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureHandles[1]);
        checkErrors("3");
        programTexHandle = GLES20.glGetUniformLocation(glslProgram,"s_texture[1]");
        checkErrors("4");
        GLES20.glUniform1i(programTexHandle,1);
        checkErrors("5");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        checkErrors("2");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureHandles[2]);
        checkErrors("3");
        programTexHandle = GLES20.glGetUniformLocation(glslProgram,"s_texture[2]");
        checkErrors("4");
        GLES20.glUniform1i(programTexHandle,2);
        checkErrors("5");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        checkErrors("2");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureHandles[3]);
        checkErrors("3");
        programTexHandle = GLES20.glGetUniformLocation(glslProgram,"s_texture[3]");
        checkErrors("4");
        GLES20.glUniform1i(programTexHandle,3);
        checkErrors("5");

        vPosition = GLES20.glGetAttribLocation(glslProgram,"vPosition");
        checkErrors("6");
        GLES20.glEnableVertexAttribArray(vPosition);
        checkErrors("7");
        GLES20.glVertexAttribPointer(vPosition,3,GLES20.GL_FLOAT,false,3*4,vertexBuffer);
        checkErrors("8");
        texCoord = GLES20.glGetAttribLocation(glslProgram,"texCoord");
        GLES20.glEnableVertexAttribArray(texCoord);
        GLES20.glVertexAttribPointer(texCoord,2,GLES20.GL_FLOAT,false,2*4,texCoordsBuffer);
        vColor = GLES20.glGetUniformLocation(glslProgram,"vColor");
        checkErrors("9");
        GLES20.glUniform3fv(vColor,1,color,0);
        checkErrors("10");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,allTiles.length/3);
        checkErrors("11");
        GLES20.glDisableVertexAttribArray(vPosition);

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
