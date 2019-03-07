package com.research.jtabor.tilesynctest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * Created by josht on 3/2/2019.
 */

public class GLBufferedRendererView extends GLSurfaceView {

    private final BufferedRenderer renderer;

    public GLBufferedRendererView(Context c){
        super(c);
        setEGLContextClientVersion(2);
        renderer = new BufferedRenderer();
        renderer.setContext(getContext());
        setRenderer(renderer);
    }
}
