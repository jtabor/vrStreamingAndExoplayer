package com.google.android.exoplayer2.trackselection;

import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.TrackGroup;

/**
 * Created by josht on 3/18/2018.
 */

public class VrTileTrackSelection extends BaseTrackSelection {

    private int selectedIndex;
    private int reason;
    private int newSelection = 0;
    private boolean hasNewSelection = false;
    public VrTileTrackSelection(TrackGroup group, int[] tracks, int firstSelection){
        super(group, tracks);
        selectedIndex = firstSelection;
        reason = C.SELECTION_REASON_INITIAL;
    }

    public void setNewSelection(int trackToSelect){
        Log.d("JOSH","Set new Selection!");
        newSelection = trackToSelect;
        hasNewSelection = true;
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public int getSelectionReason() {
        return reason;
    }

    @Override
    public Object getSelectionData() {
        return null;
    }
    public void disable(){
        this.disable();
    }
    public void enable(){
        this.enable();
    }
    @Override
    public void updateSelectedTrack(long playbackPositionUs, long bufferedDurationUs, long availableDurationUs) {
        Log.d("JOSH","Called update selection in vrselection");
        if(hasNewSelection){  //maybe add some stuff in here to only switch if your buffer is good..
            selectedIndex = newSelection;
            hasNewSelection = false;
            reason = C.SELECTION_REASON_ADAPTIVE;
        }
    }
}
