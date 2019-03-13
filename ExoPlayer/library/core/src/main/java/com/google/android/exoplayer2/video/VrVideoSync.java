package com.google.android.exoplayer2.video;

import android.os.Build;
import android.util.Log;
import android.view.Choreographer;

import com.google.android.exoplayer2.decoder.Buffer;

import java.util.Arrays;

/**
 * Created by josht on 2/27/2019.
 */

public class VrVideoSync {

    static int bufferLength = 0;
    static int nextFrameToRender = 0;

    static boolean bufferReady[][];  //[buffer number][frameId]
    static long bufferTimestampTarget[];
    static boolean bufferInUse[];

    static int renderedToBuffer[];
    static boolean decoderDirty[];

    int frameId;
    static int numberOfTilesRegistered = 0;
    static int numberOfTiles = 0;

    int nextFrame = 0;

    //call this to init the class.
    public VrVideoSync(int BufferLength, int NumberOfTiles){
        bufferLength = BufferLength;
        numberOfTiles = NumberOfTiles;
        bufferTimestampTarget = new long[bufferLength];
        Arrays.fill(bufferTimestampTarget,0l);
        bufferInUse = new boolean[bufferLength];
        Arrays.fill(bufferInUse,false);
        renderedToBuffer = new int[numberOfTiles];
        Arrays.fill(renderedToBuffer,0-1);
        decoderDirty = new boolean[numberOfTiles];
        Arrays.fill(decoderDirty,false);
        bufferReady = new boolean[bufferLength][numberOfTiles];
        for (int i = 0; i < bufferLength; i ++){
            Arrays.fill(bufferReady[i],false);
        }
    }
    //call this to init for each decoder.
    public VrVideoSync(){
        frameId = numberOfTilesRegistered;
        numberOfTilesRegistered++;
    }
    //this should be called when the decoder renders a frame, should tell GL thread to render that.
    public void decoderRendered(long timestampTarget){ //call this after a buffer has been rendered.
        for (int i = 0; i < bufferLength; i++){
            int bufferIndex = (i + nextFrame) % bufferLength;
            if(!bufferReady[bufferIndex][frameId]){
                renderedToBuffer[frameId] = i;
                decoderDirty[frameId] = true;
                return;
            }
        }
        //Dropped.
        decoderDirty[frameId] = false;

    }

    //this should be called when GL thread writes a frame to the renderTexture.


    //this is called by rendering thread to check if it should render a frame (if there's a spot in the buffer for it)
    public boolean shouldRender(long timestampRenderTarget){
        if (decoderDirty[frameId]){
            return false;
        }

        for (int i = 0; i < bufferLength; i++){
            int bufferIndex = (i + nextFrame) & bufferLength;
            if(!bufferReady[i][frameId]){
                return true;
            }
        }
        //none available
        return false;
    }

    //Renders the buffer we want to render to.
    public int getBufferToRenderTo(int frameId){
        if (decoderDirty[frameId]){
            return renderedToBuffer[frameId];
        }
        return -1;
    }

    public void renderedToTexture(int frameId){
        bufferReady[renderedToBuffer[frameId]][frameId] = true;
        renderedToBuffer[frameId] = -1;
        decoderDirty[frameId] = false;
    }

    private boolean bufferReadyToRender(int bufferId){
        for (int i = 0; i < numberOfTiles;i++){
            if (!bufferReady[bufferId][i]){
                return false;
            }
        }
        return true;
    }
    public int getReadyBuffer(){
        if (bufferReadyToRender(nextFrame)){
            return nextFrame;
        }
        return -1;
    }

    public void bufferRendered(int bufferId){
        for (int i = 0; i < numberOfTiles; i++){
            bufferReady[bufferId][i] = false;
        }
        bufferInUse[bufferId] = false;
        nextFrame++;
        if (nextFrame == bufferLength){
            nextFrame  =0;
        }
    }

}
