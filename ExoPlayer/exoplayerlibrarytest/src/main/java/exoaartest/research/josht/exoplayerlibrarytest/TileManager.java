package exoaartest.research.josht.exoplayerlibrarytest;

/**
 * Contains information about which chunks have been downloaded for the given tile and which chunk
 * is being currently played by exoPlayer.
 */

public class TileManager {
    private static final int NUM_LOOKAHEAD_CHUNKS = 2;
    private final int TILE_ID;
    private final String TILE_URL;

    private int lastDownloadedChunkId = 0;
    private int currentlyPlayingChunkId = 0;
    private CallbackHandler callbackHandler;

    TileManager(int tileId, String tileUrl, CallbackHandler iCallbackHandler) {
        TILE_ID = tileId;
        TILE_URL = tileUrl;
        this.callbackHandler = iCallbackHandler;
    }

    public int getTileId() {
        return TILE_ID;
    }

    public String getTileUrl() {
        return TILE_URL;
    }

    public void setLastDownloadedChunkId(int iLastDownloadedChunkId) {
        this.lastDownloadedChunkId = iLastDownloadedChunkId;

        // notify if exo-player can continue playing
        if (lastDownloadedChunkId - currentlyPlayingChunkId >= NUM_LOOKAHEAD_CHUNKS) {
            if (callbackHandler != null) {
                callbackHandler.isEligibleToPlayVideo();
            }
        }
    }

    public void setCurrentlyPlayingChunkId(int iCurrentlyPlayingChunkId) {
        this.currentlyPlayingChunkId = iCurrentlyPlayingChunkId;

        // check if the next 'x' chunks have been downloaded
        if (lastDownloadedChunkId - currentlyPlayingChunkId < NUM_LOOKAHEAD_CHUNKS) {
            if (callbackHandler != null) {
                callbackHandler.shouldPauseVideo();
            }
        }
    }

    public interface CallbackHandler {
        void isEligibleToPlayVideo();
        void shouldPauseVideo();
    }
}
