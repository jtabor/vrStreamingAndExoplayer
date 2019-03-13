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
        Log.d("JOSH-SYNC","Decoder marked as dirty " + frameId);
        int targetBuffer = -1;
        for (int i = 0; i < bufferLength; i++){
            if (timestampTarget == bufferTimestampTarget[i]){
                renderedToBuffer[frameId] = targetBuffer;
                decoderDirty[frameId] = true;
                return;
            }
            //Might wanna add something here to try to claim the buffer again.
        }
        decoderDirty[frameId] = false;  //Didn't find a valid frame, so we forget the rendering.
    }

    //this should be called when GL thread writes a frame to the renderTexture.


    private boolean findAndClaimNextEmptyBuffer(long newTimestampeTarget){
        Log.d("JOSH-SYNC","in findAndClaimBuffer.");

        boolean toReturn = false;
        for (int i = 0; i < bufferLength; i++){
            if(!bufferInUse[i]){
                Log.d("JOSH-SYNC","Tried to reserve buffer:" + i + " with timestamp: " + newTimestampeTarget);
                bufferInUse[i] = true;
                bufferTimestampTarget[i] = newTimestampeTarget;
                toReturn = true;
                break;
            }
        }
        return toReturn;
    }
    //this is called by rendering thread to check if it should render a frame (if there's a spot in the buffer for it)
    public boolean shouldRender(long timestampRenderTarget){
        Log.d("JOSH-SYNC","in ShouldRender: " + timestampRenderTarget);
        boolean isLate = true;
        //Try to find an already allocated buffer.
        for (int i = 0; i < bufferLength; i++){
            if (bufferTimestampTarget[i] == timestampRenderTarget && bufferInUse[i]){
                return true;
            }
        }

        //Try to claim a new one if none are available.
        if(findAndClaimNextEmptyBuffer(timestampRenderTarget)){
            return true;//no empty buffer -> don't render yet.
        }

        //if any frames are earlier, then we're waiting for one to be available.
        for (int i = 0; i < bufferLength; i++){
            if (bufferTimestampTarget[i] < timestampRenderTarget){
                return false;
            }
        }

        //none available and all are more recent then the current frame.  Drop it.
        return true;
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
        int toReturn = -1;
        long earliestTime = Long.MAX_VALUE;
        for (int i = 0; i < bufferLength; i++){
            if (bufferReadyToRender(i) && bufferTimestampTarget[i] < earliestTime){
                toReturn = i;
                earliestTime = bufferTimestampTarget[i];
            }
        }
        return toReturn;
    }

    public void bufferRendered(int bufferId){
        for (int i = 0; i < numberOfTiles; i++){
            bufferReady[bufferId][i] = false;
        }
        bufferInUse[bufferId] = false;
    }

}
