package com.ts.music.entity;

/**
 * Music Lyrics.
 */

public class AudioLrcBean {
    private String mContent;
    private String mTimeText;
    private long mTime;

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getTimeText() {
        return mTimeText;
    }

    public void setTimeText(String timeText) {
        mTimeText = timeText;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }
}