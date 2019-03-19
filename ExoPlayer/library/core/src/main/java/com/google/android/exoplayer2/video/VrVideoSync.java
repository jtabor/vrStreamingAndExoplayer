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

    static boolean DISABLED = false;

    static int bufferLength = 0;
    static int nextFrameToRender = 0;

    static boolean bufferReady[][];  //[buffer number][frameId]
    static long renderedTimestamp[][];
    static long bufferTimestampTarget[];
    static boolean bufferInUse[];

    static long mostRecentTimestamp[];

    static int renderedToBuffer[];
    static boolean decoderDirty[];
    static boolean decoderStarted[];
    static boolean gotFirstFrame[];
    int frameId;
    static int numberOfTilesRegistered = 0;
    static int numberOfTiles = 0;

    static boolean hasDroppedFrame[];
    static boolean needsCatchUp[];
    static boolean doubleDirty[];

    static int nextFrame = 0;

    long lastRender = 0;

    static long dropTimestamp;
    static boolean requestDrop = false;
    static int framesDropped = 0;
    static long dropGaurd = 0;

    static long newestTimestamp = 0;
    static long lastRenderedTimestamp = 0;
    static final long DROP_GUARD_INTERVAL = 18000; //60Hz.
//    static final long DROP_GUARD_INTERVAL = 36000; //30Hz.
    static final long NEW_FRAME_GUARD = 14000; //60Hz.
//    static final long NEW_FRAME_GUARD = 28000; //30Hz.
    static long buffersRendered = 0;

    //call this to init the class.
    public VrVideoSync(int BufferLength, int NumberOfTiles){
        bufferLength = BufferLength;
        numberOfTiles = NumberOfTiles;
        bufferTimestampTarget = new long[numberOfTiles];
        Arrays.fill(bufferTimestampTarget,0l);
        bufferInUse = new boolean[bufferLength];
        Arrays.fill(bufferInUse,false);
//        buffersRendered = new long[bufferLength];
//        Arrays.fill(buffersRendered,0);
        mostRecentTimestamp = new long[numberOfTiles];
        Arrays.fill(mostRecentTimestamp,0);
        doubleDirty = new boolean[numberOfTiles];
        Arrays.fill(doubleDirty,false);
        decoderStarted = new boolean[numberOfTiles];
        Arrays.fill(decoderStarted,false);
        renderedToBuffer = new int[numberOfTiles];
        Arrays.fill(renderedToBuffer,-1);
        gotFirstFrame = new boolean[numberOfTiles];
        Arrays.fill(gotFirstFrame,false);
        decoderDirty = new boolean[numberOfTiles];
        Arrays.fill(decoderDirty,false);
        hasDroppedFrame = new boolean[numberOfTiles];
        Arrays.fill(hasDroppedFrame,true);
        bufferReady = new boolean[bufferLength][numberOfTiles];
        for (int i = 0; i < bufferLength; i ++){
            Arrays.fill(bufferReady[i],false);
        }
        renderedTimestamp = new long[bufferLength][numberOfTiles];
        for (int i = 0; i < bufferLength; i ++){
            Arrays.fill(renderedTimestamp[i],0);
        }
    }
    //call this to init for each decoder.
    public VrVideoSync(){
        frameId = numberOfTilesRegistered;
        numberOfTilesRegistered++;
    }
    public void disable(boolean shouldDisable){
        DISABLED = shouldDisable;
    }
    //this should be called when the decoder renders a frame, should tell GL thread to render that.
    public void decoderRendered(long timestampTarget){ //call this after a buffer has been rendered.
//        Log.d("DD","decoder: " + frameId + " indecoderRendered: " + timestampTarget);
//        if (decoderDirty[frameId]){
//            Log.e("AA","DOUBLE DIRTY!");
//        }
//        Log.d("JOSH","Rendered ts: " + timestampTarget);
//        if (timestampTarget == dropTimestamp){
//            return;
//        }
        for (int i = 0; i < bufferLength; i++){
            int bufferIndex = (i + nextFrame) % bufferLength;
            if(!bufferReady[bufferIndex][frameId]){
                bufferInUse[bufferIndex] = true;
                renderedToBuffer[frameId] = bufferIndex;
//                Log.d("DD","Fame " + frameId + " Rendered to buffer: " + bufferIndex);
                renderedTimestamp[bufferIndex][frameId] = timestampTarget;
                doubleDirty[frameId] = false;
                bufferTimestampTarget[frameId] = timestampTarget;
//                Log.d("DD","frame: " + frameId + " buff:" + bufferIndex + " target: " + timestampTarget);
                decoderDirty[frameId] = true;
                if (mostRecentTimestamp[frameId] != timestampTarget){
                    Log.e("JOSH","TIMESTAMPS DON'T MATCH!");
                }
                return;
            }
        }
        //Dropped.
        Log.e("AA","DROPPED FRAME!");
        decoderDirty[frameId] = false;

    }

    public void markDecoderStarted(){
        decoderStarted[frameId] = true;
    }

    public boolean areDecodersReadyToStart(){
        for (int i = 0; i < numberOfTiles; i++){
            if (!decoderStarted[frameId]){
                return false;
            }
        }
        return true;
    }
    //this should be called when GL thread writes a frame to the renderTexture.
    static boolean isDroppingFrames = false;

    public boolean shouldDropFrame(boolean shouldDrop, long timestamp){ //called if a decoder gets too far behind.
//        return false;
        if (DISABLED) {
            return shouldDrop;
        }
        if (shouldDrop && (timestamp > dropGaurd)){
            if (timestamp <= newestTimestamp){
                return false;
            }
            else if (timestamp > dropGaurd ){
                for (int i = 0; i < numberOfTiles; i++){
                    hasDroppedFrame[i] = false;
                }
                dropTimestamp = timestamp;
                dropGaurd = timestamp + DROP_GUARD_INTERVAL;
                isDroppingFrames = true;
            }
        }
        if ((timestamp >= dropTimestamp) && (!hasDroppedFrame[frameId])) {
//            Log.d("JOSH","DROPPED FRAME: " + timestamp + " frame: " + frameId);
            framesDropped++;
            hasDroppedFrame[frameId] = true;
            return true;
        }
        return false;
    }
    //this is called by rendering thread to check if it should render a frame (if there's a spot in the buffer for it)
    public int shouldRender(long timestampRenderTarget){
//        Log.d("DD","decoder: " + frameId + " inshouldRender: " + timestampRenderTarget);
        if(DISABLED) {
            return 1;
        }
//        gotFirstFrame[frameId] = true;
//        for (int i = 0; i < numberOfTiles; i++){
//            if (!gotFirstFrame[i]){
//                return true;
//            }
//        }
//        if (numberOfTilesRegistered != numberOfTiles){
//            return true;
//        }
//        if (!hasDroppedFrame[frameId]){
//            hasDroppedFrame[frameId] = true;
//            return 2;
//        }
        //ENABLE
        if (timestampRenderTarget < lastRenderedTimestamp){
            return 2;
        }
        if (mostRecentTimestamp[frameId] > timestampRenderTarget){
            Log.e("JOSH-DEBUG","Tried to render earlier timestamp!!");
        }
        if (timestampRenderTarget > newestTimestamp){
            newestTimestamp = timestampRenderTarget;
        }
        mostRecentTimestamp[frameId] = timestampRenderTarget;
        if (decoderDirty[frameId]){
            return 0;
        }
        if( timestampRenderTarget == dropTimestamp){
            return 2;
        }
        for (int i = 0; i < bufferLength; i++){
            int bufferIndex = (i + nextFrame) % bufferLength;
            if(!bufferReady[bufferIndex][frameId]){
                if(!bufferInUse[bufferIndex] && requestDrop){
                    requestDrop = false;
                    dropGaurd = timestampRenderTarget + DROP_GUARD_INTERVAL;
                    dropTimestamp = timestampRenderTarget;
                    return 0;
                }
                if (doubleDirty[frameId]){
                    Log.d("AA","DOUBLE DIRTY!");
                }
                doubleDirty[frameId] = true;
                return 1;
            }
        }
        //none available
        return 0;
    }

    //Renders the buffer we want to render to.
    public int getBufferToRenderTo(int frameId){
        if (decoderDirty[frameId]){
            //            Log.d("AA","Frame: " + frameId + " Timestamp: " + bufferTimestampTarget[nextFrame] + " Buffer: " + renderedToBuffer[frameId]);
            return renderedToBuffer[frameId];
        }
        return -1;
    }

    public void renderedToTexture(int frameId){
//        Log.d("DD","Frame: " + frameId + " in renderedToTexture");
        bufferReady[renderedToBuffer[frameId]][frameId] = true;
        renderedToBuffer[frameId] = -1;
        decoderDirty[frameId] = false;
    }


    public int getReadyBuffer(){
        if (DISABLED){
            return nextFrame;
        }

        for (int i = 0; i < numberOfTiles; i++){
            if (!bufferReady[nextFrame][i]){
                return -1;
            }
        }

        return nextFrame;

//        return -1;
    }

    public void bufferRendered(int bufferId){
//        Log.d("DD","in buffer rendered: " + bufferId);
        for (int i = 0; i < numberOfTiles; i++){
//            if (!bufferReady[bufferId][i]){
//                hasDroppedFrame[i] = true;
//            }
            bufferReady[bufferId][i] = false;
        }
        bufferInUse[bufferId] = false;
        nextFrame = (nextFrame + 1)%bufferLength;
        lastRenderedTimestamp = 0;
        String allTimestamps = "";
        for (int i = 0; i < numberOfTiles; i++){
            allTimestamps = allTimestamps + renderedTimestamp[bufferId][i] + ",";
            if (renderedTimestamp[bufferId][i] > lastRenderedTimestamp){
                lastRenderedTimestamp = renderedTimestamp[bufferId][i];
            }
        }
//        for (int i = 0; i < numberOfTiles; i++){
//            if (renderedTimestamp[bufferId][i] < lastRenderedTimestamp){
//                hasDroppedFrame[i] = false;
//            }
//        }
        long curTime = System.nanoTime();
        long elapsedTime = curTime - lastRender; //|| elapsedTime > 24
        lastRender = curTime;

        Log.d("JOSH-METRICS","FRAME: " + buffersRendered + "," + curTime + "," + framesDropped + "," + allTimestamps);
        buffersRendered++;



    }

}
