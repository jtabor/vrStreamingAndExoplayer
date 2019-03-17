package exoaartest.research.josht.exoplayerlibrarytest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.VrRenderersFactory2;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.dash.DashMediaSource;
import com.google.android.exoplayer2.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.VrTileTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import static com.google.android.exoplayer2.upstream.DataSource.*;


/**
 * Created by josht on 2/25/2018.
 */

public class SurfaceTest implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener, SimpleExoPlayer.EventListener {
    boolean frameReady[];
    int numberOfTiles = 1;
    TextureView[] tvs;
    SurfaceTexture[] surfaceTextures;
    int[] textureIds;
    int tileWidth = 800;
    int tileHeight = 800;

    int state = -1;
    boolean doesCreateTextures = false;
    static TextureView tv;
    Context currentContext;
    SimpleExoPlayer player;
    boolean isPlaying = false;
    String url = "https://storage.googleapis.com/vr-paradrop/fusion-test/dash-tile-test/VIDEO_0065-tile1.mpd";
    String exoReplaceString = "http://storage.googleapis.com/video_vr/final-videos/";
    String exoPreUrl = "http://172.17.0.2/";
    VrRenderersFactory2 vrf;
    VideoRendererEventListener rendererListener[];
    VrTileTrackSelector trackSelector;

//    private static String SERVER_IP = "192.168.1.111";
//    private static int SERVER_PORT = 8028;
    private static String SERVER_IP = "172.17.0.2";
    private static int SERVER_PORT = 21;
//    private String mpdFileUrls=
//            "https://storage.googleapis.com/vr-paradrop/fusion-test/dash-tile-test/VIDEO_0065-tile1.mpd,"
//                    + "https://storage.googleapis.com/vr-paradrop/fusion-test/dash-tile-test/VIDEO_0065-tile2.mpd";
    private String mpdFileUrls = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";
    TCPClientService tcpClientService;
    private Handler mHandler;
    boolean mBound;
    boolean goThroughRouter = false;

    Handler mainHandler;

    public SurfaceTest(int tilesInVideo, Context currentUnityContext,int widthOfTile, int heightOfTile,boolean createTextures, boolean playFromRouter){
        numberOfTiles = tilesInVideo;
        currentContext = currentUnityContext;
        tileWidth = widthOfTile;
        tileHeight = heightOfTile;
        doesCreateTextures = createTextures;
        goThroughRouter = playFromRouter;
    }
    public void init(String manifestUrl){
        mainHandler = new Handler();
        url = manifestUrl;
        rendererListener = new VideoRendererEventListener[numberOfTiles];
        surfaceTextures = new SurfaceTexture[numberOfTiles];
        tvs = new TextureView[numberOfTiles];
        textureIds = new int[numberOfTiles];
        frameReady = new boolean[numberOfTiles];
        for (int i = 0; i < frameReady.length; i++){
            frameReady[i] = false;
        }
        if (doesCreateTextures) {
            createTextures();
            createTextureViews();
            if(goThroughRouter) {
                bindToTCPClientService(url);
            }
//            Log.d("JOSH","Tried the binding");
        }
    }
    public void releasePlayer(){
        player.release();
    }
    public int getState(){
        return state;
    }
    public void startVideo(){
//        if (state == Player.STATE_READY){
            player.setPlayWhenReady(true);
//        }
    }
    public void bindToTCPClientService(String urlToDownload) {
        mHandler = new Handler();
        currentContext.registerReceiver(tcpMessageListener,new IntentFilter("tcp-message"));
//        LocalBroadcastManager.getInstance(currentContext).registerReceiver(
//                tcpMessageListener,
//                new IntentFilter("tcp-message")
//        );

        Intent intent = new Intent(currentContext, TCPClientService.class);
        intent.putExtra("serverIp", SERVER_IP);
        intent.putExtra("serverPort", SERVER_PORT);
        intent.putExtra("mpdUrls", urlToDownload);
        currentContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("JOSH", "Binding to service");
    }

    public int[] getTextureIds(){
        return textureIds;
    }

    private void createTextures() {
        int textures[] = new int[numberOfTiles];
        GLES20.glGenTextures(numberOfTiles, textures, 0);

        for (int i = 0; i < numberOfTiles; i++){

            int textureId = textures[i];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            textureIds[i] = textureId;
        }
    }

    public void setTextures(TextureView[] textureViews){
        tvs = textureViews;
    }
    private void createTextureViews(){

        for (int i = 0; i < numberOfTiles; i++){
            surfaceTextures[i] = new SurfaceTexture(textureIds[i]);
            surfaceTextures[i].setDefaultBufferSize(tileWidth,tileHeight);
            tvs[i] = new TextureView(currentContext);
            tvs[i].setSurfaceTextureListener(this);
            tvs[i].setSurfaceTexture(surfaceTextures[i]);
        }
        surfaceTextures[0].setOnFrameAvailableListener(this);

    }

    public void updateTexture(){
//        Log.d("JOSH-METRICS","Render: " + System.currentTimeMillis());
        boolean shouldRender = true;
        for (int i = 0; i < numberOfTiles; i++){
            if (!frameReady[i]){
                shouldRender = false;
//                Log.d("JOSH-METRICS","NotRendered: " + System.currentTimeMillis());
//                return;
            }
        }

        for (int i = 0; i < numberOfTiles; i++) {
            surfaceTextures[i].updateTexImage();
            frameReady[i] = false;
        }
    }
    public int findSurfaceTexture(SurfaceTexture st){
        Log.d("JOSH","equal works? " + st.equals(st));
        for (int i = 0 ; i < surfaceTextures.length; i++){
            if(surfaceTextures[i].equals(st)){
                return i;
            }
        }
        return -1;
    }
    public void updateTexture(int i){
        surfaceTextures[i].updateTexImage();
    }
    public void initExoplayer(){
//        numberOfTiles = 1;
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        //LoadControl lc = new DefaultLoadControl(new DefaultAllocator(true, 5000),50,150,100L,100L);
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new VrTileTrackSelector(numberOfTiles);
        TrackSelector ts = new DefaultTrackSelector(bandwidthMeter);
//        trackSelector.init(this);
// 2. Create the player
        vrf = new VrRenderersFactory2(currentContext,null);
        vrf.setNumberOfRenderers(numberOfTiles,rendererListener);

        LoadControl lc = new DefaultLoadControl(new DefaultAllocator(true,5000),1000,50000,54034L,100L);

        player = ExoPlayerFactory.newSimpleInstance(vrf, trackSelector);
        //player = ExoPlayerFactory.newSimpleInstance(currentContext, ts);
        player.addListener(this);
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bwMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(currentContext,
                Util.getUserAgent(currentContext, "yourApplicationName"), bwMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        Uri uri;
        if (goThroughRouter) {
            uri = Uri.parse(url.replace(exoReplaceString, exoPreUrl));
        }
        else{
            uri = Uri.parse(url);
        }
//        Uri uri = Uri.parse("http://172.17.0.2/VIDEO_0065-tiles.mpd");
        //Uri uri = Uri.parse("https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd");
//TODO: Make these all into 1 URL so that we can do the switching per tile.
        DashMediaSource videoSource = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource2 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource3 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource4 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource5 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource6 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource7 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        DashMediaSource videoSource8 = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);

//        ExtractorMediaSource videoSource = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//        ExtractorMediaSource videoSource2 = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//        ExtractorMediaSource videoSource3 = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//        ExtractorMediaSource videoSource4 = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//        ExtractorMediaSource videoSource5 = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//        ExtractorMediaSource videoSource6 = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//        ExtractorMediaSource videoSource7 = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);
//        ExtractorMediaSource videoSource8 = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);

//        MediaSource videoSource = new DashMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
//        MediaSource videoSource2 = new ExtractorMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
//        MediaSource videoSource3 = new ExtractorMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
//        MediaSource videoSource4 = new ExtractorMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);
        MergingMediaSource vrVideoSource = new MergingMediaSource(videoSource,videoSource2,videoSource3,videoSource4,videoSource5,videoSource6,videoSource7,videoSource8);
// Prepare the player with the source.
//        player.setVideoSurface(s);

        player.setVideoTextureViews(tvs);
        player.prepare(vrVideoSource);
//        player.prepare(videoSource);
        player.setPlayWhenReady(false);
       // mainHandler.postDelayed(startExoplayer, 10000);
    }
    private Runnable startExoplayer = new Runnable(){
        @Override
        public void run() {
            player.setPlayWhenReady(true);
        }
    };
    public void printTimestamp(){
        Log.d("JOSH","time: "+ player.getCurrentPosition());
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("JOSH","surface available!");
//        createPlayerForTextureView(tv);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d("JOSH","Size Changed.");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d("JOSH","Tex Destroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d("JOSH","surface Updated");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d("JOSH","HIT A FRAME!");
        for (int i = 0; i < surfaceTextures.length; i++){
            if (surfaceTexture.equals(surfaceTextures[i])){
                frameReady[i] = true;
                Log.d("JOSH","Frame:" + i + " available!");
                return;
            }
        }
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d("JOSH", "Bound to service");
            TCPClientService.MyBinder binder = (TCPClientService.MyBinder) service;
            tcpClientService = binder.getService();
            mBound = true;
            sendExoplayerStatus.run();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    // send exoplyer status periodically
    Runnable sendExoplayerStatus = new Runnable() {
        @Override
        public void run() {
            try {
                if (player != null && tcpClientService != null) {
                    long position =  player.getCurrentPosition(); //this function can change value of mInterval.
                    tcpClientService.sendMessage("position~" + position);
                    //Log.d(TAG, "Sending: " + position);
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(sendExoplayerStatus, 100);
            }
        }
    };

    private BroadcastReceiver tcpMessageListener = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {

            String data = intent.getStringExtra("message");
            Log.d("JOSH", "Received command: " + data);
            if (data.contains("play")) {
//                createAllSurfaces("http://" + SERVER_IP + "/VIDEO_0065-tile1.mpd");
//                createPlayerForTextureView(new TextureView(currentContext));
                initExoplayer();
                startVideo();
                Log.d("JOSH","Tried to start exoplayer..");
//                startExoplayer();
            } else if (data.contains("pause")) {
                pausePlayer();
            } else if (data.contains("resume")) {
                resumePlayer();
            }
        }
    };

    private void pausePlayer(){
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }
    private void resumePlayer(){
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        state = playbackState;
        switch(playbackState){

            case(Player.STATE_BUFFERING):
                Log.d("JOSH-State","Buffering!");
                break;
            case(Player.STATE_ENDED):
                Log.d("JOSH-State","Ended.");
                break;
            case(Player.STATE_IDLE):
                Log.d("JOSH-State","Idle!");
                break;
            case(Player.STATE_READY):
                Log.d("JOSH-State","Ready!");
                Log.d("JOSH-INFO","duration: " + player.getDuration());
                break;
            default:
                Log.d("JOSH-State","Unkown State");
                break;
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}
