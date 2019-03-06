package com.research.jtabor.tilesynctest;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.VrRenderersFactory2;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.dash.DashMediaSource;
import com.google.android.exoplayer2.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.VrTileTrackSelection;
import com.google.android.exoplayer2.trackselection.VrTileTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import exoaartest.research.josht.exoplayerlibrarytest.SurfaceTest;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener{
//    SurfaceView sv1;
//    SurfaceView sv2;
//    SurfaceView sv3;
//    SurfaceView sv4;
    private static final boolean USING_GL = false;
    private GLSurfaceView glView;


    static final int numberOfTiles = 4;

    TextureView[] tvs = new TextureView[numberOfTiles];

    int numberReady = 0;

    RelativeLayout rl1;
    RelativeLayout rl2;
    RelativeLayout rl3;
    RelativeLayout rl4;
    Display d;
    SurfaceTest st;
    boolean isStartedOnce = false;
    private String urlPrefix = "http://pages.cs.wisc.edu/~tabor/";
   // private String[] videoUrls = {urlPrefix + "newV3a-tiles.mpd"};
           // urlPrefix + "newV3b-tiles.mpd"};
//    private String[] videoUrls = {urlPrefix + "bunny_test_4k_60-tiles.mpd"};
//private String[] videoUrls = {urlPrefix + "bunny_test_1080_60.mpd"};
   private String[] videoUrls = {urlPrefix + "bunny_test_1080_60-tiles.mpd"};
    int currentVideo = 0;
    boolean playFromRouter = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (USING_GL){
            glView = new GLBufferedRendererView(this);
            setContentView(glView);
        }
        else {
            setContentView(R.layout.activity_main);
            d = getWindowManager().getDefaultDisplay();


            rl1 = (RelativeLayout) findViewById(R.id.container1);
            rl2 = (RelativeLayout) findViewById(R.id.container2);
            rl3 = (RelativeLayout) findViewById(R.id.container3);
            rl4 = (RelativeLayout) findViewById(R.id.container4);


            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);

            tvs[0] = new TextureView(this);
            tvs[0].setSurfaceTextureListener(this);
            rl1.addView(tvs[0]);
            tvs[1] = new TextureView(this);
            tvs[1].setSurfaceTextureListener(this);
            rl2.addView(tvs[1]);
            tvs[2] = new TextureView(this);
            tvs[2].setSurfaceTextureListener(this);
            rl3.addView(tvs[2]);
            tvs[3] = new TextureView(this);
            tvs[3].setSurfaceTextureListener(this);
            rl4.addView(tvs[3]);
        }
    }

    public void onClick(View v){
        Log.d("JOSH-METRICS","START OF VIDEO PLAYBACK");
        Display display = ((WindowManager) getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay();
        float refreshRating = display.getRefreshRate();
        Log.d("JOSH-DEBUG","refresh Rate: " + refreshRating);
        //        if (isStartedOnce){
//            st.releasePlayer();
//            st.init(videoUrls[currentVideo]);
//            currentVideo++;
//            st.setTextures(tvs);
//            st.initExoplayer();
//            st.startVideo();
//        }
//        isStartedOnce = true;
        st.startVideo();
    }
    public void startExoplayer(){
        st = new SurfaceTest(4,this,1920,1080,false,playFromRouter);
        st.init(videoUrls[currentVideo]);
        currentVideo++;
        st.setTextures(tvs);
        st.initExoplayer();
//        st.startVideo();
     //   st.bindToTCPClientService(videoUrls[currentVideo]);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("JOSH","TextureView Available! " + numberReady);
        numberReady++;
        if (numberReady==numberOfTiles){
            //ready to go.
            startExoplayer();
        }
    }

    private int makeExternalTexture(){

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.d("JOSH","surface updated?  (Frame rendered)");
    }
}
