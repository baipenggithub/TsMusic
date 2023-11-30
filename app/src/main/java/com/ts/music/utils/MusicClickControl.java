package com.ts.music.utils;

import android.os.SystemClock;

public class MusicClickControl {
    //Set the number of times the trigger is allowed in the time
    private int mCounts = 0;
    //Set time milliseconds
    private int mMillisSeconds = 0;
    //Number of times triggered
    private int mCurrentCounts = 0;
    //Time of first trigger in current period
    private long mFirstTriggerTime = 0;

    /**
     * Initialization.
     *
     * @param counts        Set the number of times the trigger is allowed in the time.
     * @param millisSeconds Set time milliseconds
     */
    public void init(int counts, int millisSeconds) {
        mCounts = counts;
        mMillisSeconds = millisSeconds;
        mCurrentCounts = 0;
        mFirstTriggerTime = 0;
    }

    /**
     * Clickable or not.
     */
    public boolean isCanTrigger() {
        long time = SystemClock.elapsedRealtime();
        if (mFirstTriggerTime == 0 || time - mFirstTriggerTime > mMillisSeconds) {
            mFirstTriggerTime = time;
            mCurrentCounts = 0;
        }
        if (mCurrentCounts >= mCounts) {
            return false;
        }
        ++mCurrentCounts;
        return true;
    }
}
