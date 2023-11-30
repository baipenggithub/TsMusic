package com.ts.music.entity;

import com.ts.sdk.media.bean.AudioInfoBean;

import java.util.List;

public class UsbMusicListBean {
    private String mCurrentPath;
    private List<AudioInfoBean> mFolderList;
    private List<AudioInfoBean> mAudioList;

    public String getCurrentPath() {
        return mCurrentPath;
    }

    public void setCurrentPath(String currentPath) {
        mCurrentPath = currentPath;
    }

    public List<AudioInfoBean> getFolderList() {
        return mFolderList;
    }

    public void setFolderList(List<AudioInfoBean> folderList) {
        mFolderList = folderList;
    }

    public List<AudioInfoBean> getAudioList() {
        return mAudioList;
    }

    public void setAudioList(List<AudioInfoBean> audioList) {
        mAudioList = audioList;
    }
}
