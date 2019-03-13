package com.google.android.exoplayer2.video;

import android.os.Build;
import android.util.Log;
import android.view.Choreographer;

/**
 * Created by josht on 2/27/2019.
 */

public class VrVideoSync {

    static int bufferLength = 0;
    static int nextFrameToRender = 0;

    static boolean bufferReady[][];  //[buffer number][frameId]
    static long bufferTimestampTarget[];



    int frameId;

    public VrVideoSync(){

    }
    //this should be called when the decoder renders a frame, should tell GL thread to render that.
    public void decoderRendered(int bufferIndex){ //call this after a buffer has been rendered.

    }
    //this should be called when GL thread writes a frame to the renderTexture.
    public void renderedToTexture(int frameIdRendered){
 
    }
    //this is called by rendering thread to check if it should render a frame (if there's a spot in the buffer for it)
    public boolean shouldRender(long timestampRenderTarget){
        boolean toReturn = false;
        for (int i = 0; i < bufferLength; i++){
            if (bufferTimestampTarget[i] == timestampRenderTarget){
                return true;
            }
        }


    }

    //Renders the buffer we want to render to.
    public int getBufferToRenderTo(int frameId){

    }


    private int getNextFreeBuffer(){
        for (int i = 0; i < bufferLength; i++){
            int frameToCheck = (i + nextFrameToRender) % bufferLength;
            if (!bufferReady[frameToCheck][frameId]){
                return frameToCheck;
            }
        }
        return -1;
    }
}
