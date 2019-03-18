package com.research.jtabor.tilesynctest;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.video.VrVideoSync;

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

    int numberOfTiles = 5;
    long loadTimestamp = 0l;

    SurfaceTest st;
    Context context;

    int frameWidth = 0;
    int frameHeight = 0;
    VrVideoSync videoSync;

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

    //private final String combinedVertexShader = "";

    private final String combinedFragmentShader = "//FRAGMENT SHADER\n" +
            "precision mediump float;\n" +
            "uniform vec3 vColor;\n" +
            "uniform sampler2D s_texture;\n" +
            "varying vec2 texCoordinates;\n" +
            "void main(){\n" +
            "\tgl_FragColor = texture2D(s_texture,texCoordinates);\n" +
            "}";



    private int vertexShader;
    private int fragmentShader;

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
    static float bigTexCoords[] = {
            1f,1f,
            0f,0f,
            1f,0f,
            1f,1f,
            0f,1f,
            0f,0f
    };
    static float bigTile[] = {
            1f,1f,0f,
            -1f, -1f, 0f,
            1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f,
            -1f, -1f, 0f
    };

    float color[] = {1f,0f,0f};

    int texIds[];
    float allTiles[][];
    float allTexCoords[][];

    int bufferLength = 2;

    int renderTextures[] = new int[bufferLength];
    int FBOTexture[] = new int [bufferLength];

    FloatBuffer vertexBuffer[] = new FloatBuffer[numberOfTiles + 1];
    FloatBuffer texCoordsBuffer[]  = new FloatBuffer[numberOfTiles + 1];

    int glslProgram;
    int glslProgramOneTile;
    int vPosition_render;
    int vPosition_all;
    int texCoord_render;
    int texCoord_all;
    int textureLocation_render;
    int textureLocation_all;


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
    private float[] scaleArrayInY(float[] input, float scaleAmount){
        float[] toReturn = new float[input.length];
        for (int i = 0; i < input.length; i = i+3){
            toReturn[i+1] = input[i+1]*scaleAmount; //only mess with y.

        }
        return toReturn;
    }
    public void setContext(Context newContext){
        context = newContext;
    }
    //create the textures here and link them to Exoplayer
    private float[] generateBaseTile(float xWidth, float yWidth){
        float[] toReturn = {
                -1f, -1f, 0f,
                -1f + xWidth, -1f + yWidth, 0f,
                -1f, -1f + yWidth, 0f,
                -1f, -1f, 0f,
                -1f + xWidth, -1f, 0f,
                -1f + xWidth, -1f + yWidth, 0f
        };
        return toReturn;
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        //Generate the texcoords and vertex coords for our 4 tiles.  (This is hardcoded, might want to make a more robust one later..
        allTiles = new float[numberOfTiles][baseTile.length];
        allTexCoords = new float[numberOfTiles][oneTexCoords.length];

        if (numberOfTiles == 1){ //One is special case..  Since it's full screen.  All others divided along x.

        }
        int numberOfTilesDisplayed = numberOfTiles;
        if ((numberOfTiles%2)==1){ //round up nearest even number.
            numberOfTilesDisplayed++;
        }
        float yWidth = 2f/(((float)numberOfTilesDisplayed)/2f);
        baseTile = generateBaseTile(1,yWidth);
        for (int i = 0; i < numberOfTilesDisplayed/2; i++){
            //Do the left side.
            System.arraycopy(addToArray(baseTile,0f,((float)i)*yWidth,0f),0,allTiles[2*i],0,baseTile.length);
            System.arraycopy(oneTexCoords,0,allTexCoords[2*i],0,oneTexCoords.length);
            if( 2*i + 1 != numberOfTiles) {
                System.arraycopy(addToArray(baseTile, 1f, ((float) i) * yWidth, 0f), 0, allTiles[2 * i + 1], 0, baseTile.length);
                System.arraycopy(oneTexCoords, 0, allTexCoords[2 * i + 1], 0, oneTexCoords.length);
            }
            //Do the right side.

        }

        for (int i = 0; i < numberOfTiles; i++) {
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

        ByteBuffer bb = ByteBuffer.allocateDirect(bigTile.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer[numberOfTiles] = bb.asFloatBuffer();
        vertexBuffer[numberOfTiles].put(bigTile);
        vertexBuffer[numberOfTiles].position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(bigTexCoords.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        texCoordsBuffer[numberOfTiles] = bb2.asFloatBuffer();
        texCoordsBuffer[numberOfTiles].put(bigTexCoords);
        texCoordsBuffer[numberOfTiles].position(0);

        videoSync = new VrVideoSync(2,numberOfTiles);

        Looper.prepare();
        st = new SurfaceTest(numberOfTiles,context,1920,1080,true,false);
//        st.init("http://pages.cs.wisc.edu/~tabor/bunny_test_1080_60-tiles.mpd");
//        st.init("http://pages.cs.wisc.edu/~tabor/bunny_test_1080_60.mp4");
        st.init("http://pages.cs.wisc.edu/~tabor/newV3a-tiles.mpd");
        textureHandles = st.getTextureIds();
        st.initExoplayer();
        loadTimestamp = System.currentTimeMillis();
        st.startVideo();

        glslProgram = GLES20.glCreateProgram();
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderSource);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderSource);
        GLES20.glAttachShader(glslProgram,vertexShader);
        GLES20.glAttachShader(glslProgram,fragmentShader);
        GLES20.glLinkProgram(glslProgram);
//setup pointers for program.
        vPosition_render = GLES20.glGetAttribLocation(glslProgram, "vPosition");
        texCoord_render = GLES20.glGetAttribLocation(glslProgram, "texCoord");
        texCoord_render = GLES20.glGetUniformLocation(glslProgram, "s_texture");


        glslProgramOneTile = GLES20.glCreateProgram();
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderSource);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,combinedFragmentShader);
        GLES20.glAttachShader(glslProgramOneTile,vertexShader);
        GLES20.glAttachShader(glslProgramOneTile,fragmentShader);
        GLES20.glLinkProgram(glslProgramOneTile);
//setup pointers for program
        vPosition_all = GLES20.glGetAttribLocation(glslProgramOneTile, "vPosition");
        texCoord_all = GLES20.glGetAttribLocation(glslProgramOneTile, "texCoord");
        texCoord_all = GLES20.glGetUniformLocation(glslProgramOneTile, "s_texture");

        GLES20.glGenTextures(bufferLength,renderTextures,0);
        for (int i = 0; i < bufferLength; i++){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,renderTextures[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 2048,2048, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            FBOTexture[i] = createFBOTexture(2048,2048, renderTextures[i]);
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //do nothing..  just a prototype.
        frameHeight = height;
        frameWidth = width;
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width/height;
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
//        if (System.currentTimeMillis() < loadTimestamp + HOLDOFF_MS_BEFORE_PLAY){
//            return;
//        }
//        else{
//            st.startVideo();
//        }
        //display textures on screen.  Decide when to do it.
        st.updateTexture();

        for (int i = 0; i < numberOfTiles; i++) {
            int bufferTarget = 0;
            if (videoSync.getBufferToRenderTo(i) > -1){
                bufferTarget = videoSync.getBufferToRenderTo(i);
                //Log.d("JOSH-SYNC","rendering buffer: " + i + " to Buffer: " + bufferTarget);
            }
            else{
//                Log.d("JOSH-SYNC","continued");
                continue;

            }
            GLES20.glUseProgram(glslProgram);
            checkErrors("1");
            GLES20.glActiveTexture(textureUnits[0]);
            checkErrors("2");
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureHandles[i]);
            checkErrors("4");
            GLES20.glUniform1i(textureLocation_render, 0);
            checkErrors("5");
            GLES20.glEnableVertexAttribArray(vPosition_render);
            checkErrors("7");
            GLES20.glVertexAttribPointer(vPosition_render, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer[i]);
            checkErrors("8");
            GLES20.glEnableVertexAttribArray(texCoord_render);
            checkErrors("9");
            GLES20.glVertexAttribPointer(texCoord_render, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordsBuffer[i]);
            checkErrors("10");
            //GLES20.glBindRenderbuffer(GLES20.GL_FRAMEBUFFER,FBOTexture);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,FBOTexture[bufferTarget]);
            GLES20.glViewport(0,0,2048,2048);
            checkErrors("11");
            //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, allTiles[i].length / 3);
            checkErrors("12");
            videoSync.renderedToTexture(i);
        }

        int bufferToRender = videoSync.getReadyBuffer();
        if (bufferToRender < 0 ) {
            return;
        }
        GLES20.glUseProgram(glslProgramOneTile);
        GLES20.glActiveTexture(textureUnits[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextures[bufferToRender]);
        checkErrors("a4");
        GLES20.glUniform1i(textureLocation_all, 0);
        checkErrors("a6");
        GLES20.glEnableVertexAttribArray(vPosition_all);
        checkErrors("a7");
        GLES20.glVertexAttribPointer(vPosition_all, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer[numberOfTiles]);
        checkErrors("a8");
        GLES20.glEnableVertexAttribArray(texCoord_all);
        GLES20.glVertexAttribPointer(texCoord_all, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordsBuffer[numberOfTiles]);
        //draw the render texture at the end.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, frameWidth, frameHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, allTiles[0].length / 3);
        checkErrors("a11");
        videoSync.bufferRendered(bufferToRender);

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
    int renderBufferActual;
    private int createFBOTexture(int width, int height, int renderTexture) {
        int[] temp = new int[1];
        GLES20.glGenFramebuffers(1, temp, 0);
        checkErrors("x4");
        int handleID = temp[0];

//        GLES20.glGenRenderbuffers(1,temp,0);
        checkErrors("x5");

        renderBufferActual = temp[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handleID);
        checkErrors("x6");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,renderTexture);
        checkErrors("x7");

        int fboTex = renderTexture;
        //GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,renderBufferActual);
        checkErrors("x8");

//        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GLES20.GL_RGBA4,2048,2048);
        checkErrors("x9");

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTex, 0);
        checkErrors("x10");
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("JOSH","Framebuffer error: " + status);
            throw new IllegalStateException("GL_FRAMEBUFFER status incomplete");
        }
//        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_RENDERBUFFER,renderBufferActual);
        checkErrors("x11");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        checkErrors("x12");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        checkErrors("x12");
        return handleID;
    }
    private void checkErrors(String errorTag){
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
            Log.e("JOSH-GRAPHICS",errorTag + ": " +  ": " + GLU.gluErrorString(error));
        }
    }
}
