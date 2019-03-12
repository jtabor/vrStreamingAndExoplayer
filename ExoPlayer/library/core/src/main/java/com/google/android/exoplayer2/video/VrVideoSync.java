package com.google.android.exoplayer2.video;

import android.os.Build;
import android.util.Log;
import android.view.Choreographer;

/**
 * Created by josht on 2/27/2019.
 */

public class VrVideoSync {
    static int totalNumberOfTiles = 0;
    static int numberOfTiles = 0;
    static boolean[] readyToRender;

    static boolean writeToFirstBuffer = true;
    static long renderTime;
    static boolean firstTime = false;
    static boolean dynamicResize = false;
    public int frameId;
    int unAckedFrames = 0;
    static Choreographer.FrameCallback[] frameCallbacks = new Choreographer.FrameCallback[4];

    public VrVideoSync(Choreographer.FrameCallback callback){
        dynamicResize = true;
        frameId = numberOfTiles;
        frameCallbacks[frameId] = callback;
        boolean[] oldReady = new boolean[numberOfTiles];
        System.arraycopy(readyToRender,0,oldReady,0,oldReady.length);
        numberOfTiles++;
        readyToRender = new boolean[numberOfTiles];
        System.arraycopy(oldReady,0,readyToRender,0,oldReady.length);
    }
    public VrVideoSync(int totalNumTiles, Choreographer.FrameCallback callback){
        if (!dynamicResize){
            dynamicResize  = true;
            readyToRender = new boolean[totalNumTiles];
            onFirstBuffer = new boolean[totalNumTiles];
            for (int i = 0; i < onFirstBuffer.length; i++){
                onFirstBuffer[i] = true;
            }
            frameCallbacks = new Choreographer.FrameCallback[totalNumTiles];
        }
        frameId = numberOfTiles;
        frameCallbacks[frameId] = callback;
        numberOfTiles++;
        dynamicResize = true;
    }
    public void incrementFrameNumber(){
        unAckedFrames++;
    }
    public boolean sholdRender(long timeStamp){
        return true;
//        boolean retval = false;
//        if (unAckedFrames>0){
//            unAckedFrames--;
//            retval = true;
//        }
//        if (numberOfTiles < totalNumberOfTiles && !dynamicResize){
//            return false;
//        }
//        readyToRender[frameId] = true;
//
//        boolean allReady = true;
//        for (int i = 0; i < readyToRender.length; i++){
//            if (readyToRender[i] ==false){
//                allReady = false;
//                break;
//            }
//        }
//        if (firstTime && allReady){
//            renderTime = timeStamp;
//        }
//        if (timeStamp < renderTime){
//            return true;
//        }
//        if (allReady){
//            for (int i = 0; i < numberOfTiles; i++){
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    if (frameCallbacks[i]!=null) {
//
//                        //Choreographer.getInstance().postFrameCallback(frameCallbacks[i]);
//                    }
//                }
//            }
//        }
//
//        return allReady;
    }


}
