package com.research.jtabor.tilesynctest;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.VrRenderersFactory2;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.VrTileTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

public class MainActivity extends Activity implements SurfaceHolder.Callback{
    SurfaceView sv1;
    SurfaceView sv2;
    SurfaceView sv3;
    SurfaceView sv4;

    RelativeLayout rl1;
    RelativeLayout rl2;
    RelativeLayout rl3;
    RelativeLayout rl4;

    Surface surfaces[] = new Surface[4];

    VrRenderersFactory2 vrf;
    VideoRendererEventListener rendererListener[] = new VideoRendererEventListener[4];

    SimpleExoPlayer player;

    int holdersFound = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rl1 = (RelativeLayout)findViewById(R.id.container1);
        rl2 = (RelativeLayout)findViewById(R.id.container2);
        rl3 = (RelativeLayout)findViewById(R.id.container3);
        rl4 = (RelativeLayout)findViewById(R.id.container4);



        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);

        sv1 = new SurfaceView(this);
        sv1.getHolder().addCallback(this);
        sv1.setLayoutParams(lp);
        rl1.addView(sv1);

        sv2 = new SurfaceView(this);
        sv2.getHolder().addCallback(this);
        sv2.setLayoutParams(lp);
        rl2.addView(sv2);

        sv3 = new SurfaceView(this);
        sv3.getHolder().addCallback(this);
        sv3.setLayoutParams(lp);
        rl3.addView(sv3);

        sv4 = new SurfaceView(this);
        sv4.getHolder().addCallback(this);
        sv4.setLayoutParams(lp);
        rl4.addView(sv4);
    }

    public void onClick(View v){
        TrackSelectionArray tsa = player.getCurrentTrackSelections();
        TrackSelection[] tss = tsa.getAll();
        Log.d("JOSH","TS SIZE: " + tss.length);
        for (TrackSelection ts : tss){
            if (ts != null) {
                Log.d("JOSH", "TS: " + ts.toString());
                Log.d("JOSH", "TS: " + ts.getSelectedFormat());
            }
        }
    }
    public void createAllSurfaces(){

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new VrTileTrackSelector();
// 2. Create the player
        vrf = new VrRenderersFactory2(this,null);
        vrf.setNumberOfRenderers(4,rendererListener);
        player = ExoPlayerFactory.newSimpleInstance(vrf, trackSelector);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bwMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bwMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
        Uri uri =  Uri.parse( "https://storage.googleapis.com/vr-paradrop/vr-video1/one.mpd" );

        DashMediaSource videoSource = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource2 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource3 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource4 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);

//        MediaSource videoSource = new DashMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
//        MediaSource videoSource2 = new ExtractorMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
//        MediaSource videoSource3 = new ExtractorMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
//        MediaSource videoSource4 = new ExtractorMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
        MergingMediaSource vrVideoSource = new MergingMediaSource(videoSource,videoSource2,videoSource3,videoSource4);
// Prepare the player with the source.
//        player.setVideoSurface(s);
        player.setVideoSurfaces(surfaces);

        player.prepare(vrVideoSource);
        player.setPlayWhenReady(true);

    }



    public void createPlayerForSurface(Surface s){

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory =
//                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector =
                new VrTileTrackSelector(videoTrackSelectionFactory);
// 2. Create the player

        SimpleExoPlayer player =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bwMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bwMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
        Uri uri =  Uri.parse( "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4" );

        MediaSource videoSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);
// Prepare the player with the source.
        player.setVideoSurface(s);
        player.prepare(videoSource);
        player.setPlayWhenReady(true);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder.equals(sv1.getHolder())){
            Log.d("JOSH","Holder 1!");
            holdersFound++;
            surfaces[0] = holder.getSurface();
//            createPlayerForSurface(holder.getSurface());
        }
        else if (holder.equals(sv2.getHolder())){
            Log.d("JOSH","Holder 2!");
            holdersFound++;
            surfaces[1] = holder.getSurface();
//            createPlayerForSurface(holder.getSurface());
        }
        else if (holder.equals(sv3.getHolder())){
            Log.d("JOSH","Holder 3!");
            holdersFound++;
            surfaces[2] = holder.getSurface();
//            createPlayerForSurface(holder.getSurface());
        }
        else if (holder.equals(sv4.getHolder())){
            Log.d("JOSH","Holder 4!");
            holdersFound++;
            surfaces[3] = holder.getSurface();
//            createPlayerForSurface(holder.getSurface());
        }
        if (holdersFound == 4){
            Log.d("JOSH","All surfaces prepared!");
            createAllSurfaces();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
