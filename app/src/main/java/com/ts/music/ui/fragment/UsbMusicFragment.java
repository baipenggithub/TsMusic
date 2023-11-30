package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;
import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.callback.IUpdateAudioInfoCallback;
import com.ts.music.constants.MusicConstants;
import com.ts.music.databinding.FragmentUsbMusicPlayerBinding;
import com.ts.music.entity.AudioLrcBean;
import com.ts.music.entity.UsbMusicListBean;
import com.ts.music.manager.SearchAudioInfoManager;
import com.ts.music.ui.adapter.SongListAdapter;
import com.ts.music.ui.viewmodel.UsbMusicViewModel;
import com.ts.music.utils.ConstraintUtil;
import com.ts.music.utils.FolderRecordStack;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicLrcRowParser;
import com.ts.music.utils.MusicUtils;
import com.ts.music.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Player Fragment.
 */
public class UsbMusicFragment extends BaseFragment<FragmentUsbMusicPlayerBinding,
        UsbMusicViewModel> implements IUpdateAudioInfoCallback {
    private static final String TAG = UsbMusicFragment.class.getSimpleName();
    private static volatile UsbMusicFragment sInstance;
    private static final int ADAPATER_ITEM_HEIGHT = 157;
    private SongListAdapter mFirstUsbSongListAdapter;
    private SongListAdapter mSecondUsbSongListAdapter;
    private boolean mIsShowLyric = false;
    private String mUsbOnePath = "";
    private String mUsbTwoPath = "";
    private List<AudioInfoBean> mUsbAudioInfoBeans = new ArrayList<>();
    private FolderRecordStack mFolderRecordStack = new FolderRecordStack();
    private String mUsbOneUuid = "";
    private String mUsbTwoUuid = "";
    private Animation mLoadingAnimation;
    private int mScannerSum = 0;
    private int mFristScanSum = 0;
    private int mSecondScanSum = 0;
    private int mFistUsbAudioSum = 0;
    private int mSecondUsbAudioSum = 0;
    private int mShowListDelayed = -1;
    private boolean mWhetherToPlayAll = true; // true:Play all  false：Folder directory
    private UsbMusicListBean mFirstAllAudioInfo;
    private UsbMusicListBean mSecondAllAudioInfo;
    private List<AudioInfoBean> mCurrentPlayList;
    private Map<String, UsbMusicListBean> mBufferAudioMap;
    private UsbDevicesInfoBean mFristUsb;
    private UsbDevicesInfoBean mSecondUsb;
    private boolean mFristIsPlayAll = true;
    private boolean mSecondIsPlayAll = true;
    private boolean mUsbFirstHasData = false;
    private boolean mUsbSecondHasData = false;
    private boolean mIsShowLoading = false;
    private boolean mIsShowScanningComplete = false;
    private boolean mIsUsbOneListUp = false;
    private boolean mIsUsbTwoListUp = false;
    private int mUsbDataQuerying;
    Handler mHandler = new Handler();
    private ConstraintUtil mConstraintUtil;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayerFragment.
     */
    public static UsbMusicFragment getInstance() {
        return new UsbMusicFragment();
    }

    private UsbMusicFragment() {
        LogUtils.logD(TAG, "UsbMusicFragment :: invoke");
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        LogUtils.logD(TAG, "initContentView :: invoke");
        return R.layout.fragment_usb_music_player;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        LogUtils.logD(TAG, "initLayout :: invoke");
        super.initLayout();
        mLoadingAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_loading);
        initRecyclerView();
        setSeekBarChangeListener();
        SearchAudioInfoManager.getInstance().addUpdateAudioInfoCallback(this);
    }

    @Override
    public void onResume() {
        LogUtils.logD(TAG, "onResume :: invoke");
        super.onResume();
        mViewModel.mInitLrcView.postValue(true);
    }

    @Override
    public void onPause() {
        LogUtils.logD(TAG, "onPause :: invoke ");
        super.onPause();
    }

    private void initRecyclerView() {
        LogUtils.logD(TAG, "initRecyclerView :: invoke");
        mBinding.fragmentSongList.firstUsbRlv.setLayoutManager(new LinearLayoutManager(
                getActivity()));
        MusicUtils.getInstance().setRecyclerViewDivider(mBinding.fragmentSongList.firstUsbRlv);
        mFirstUsbSongListAdapter = new SongListAdapter(R.layout.fragment_song_list_item,
                null);
        mBinding.fragmentSongList.firstUsbRlv.setAdapter(mFirstUsbSongListAdapter);
        mBinding.fragmentSongList.firstUsbRlv.setItemAnimator(null);

        mBinding.fragmentSongList.secondUsbRlv.setLayoutManager(new LinearLayoutManager(
                getActivity()));
        MusicUtils.getInstance().setRecyclerViewDivider(mBinding.fragmentSongList.secondUsbRlv);
        mSecondUsbSongListAdapter = new SongListAdapter(R.layout.fragment_song_list_item,
                null);
        mBinding.fragmentSongList.secondUsbRlv.setAdapter(mSecondUsbSongListAdapter);
        mBinding.fragmentSongList.secondUsbRlv.setItemAnimator(null);
        mBinding.songListShrinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shrinkSongList();
            }
        });

        mBinding.fragmentSongList.firstUsbRlv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mFristUsb != null
                        && event.getAction() == MotionEvent.ACTION_MOVE
                        && !mIsUsbOneListUp) {
                    if (mConstraintUtil == null) {
                        mConstraintUtil = new ConstraintUtil((ConstraintLayout) mBinding.getRoot()
                                .findViewById(R.id.usb_music_fragment));
                    }
                    mBinding.songListSearch.getRoot().setVisibility(View.GONE);
                    mBinding.songSearchGroup.setVisibility(View.GONE);
                    mBinding.usbLabel.getRoot().setVisibility(View.GONE);
                    mViewModel.mIsUsbLabelAvailable.set(false);
                    mBinding.fragmentPlayer.getRoot().setVisibility(View.GONE);
                    ConstraintUtil.ConstraintBegin begin = mConstraintUtil.beginWithAnim();
                    begin.setMarginToZero(R.id.usb_music_back_folder, ConstraintSet.TOP,
                            R.id.song_list_shrink_btn, ConstraintSet.BOTTOM);
                    begin.commit();
                    mBinding.songListShrinkBtn.setVisibility(View.VISIBLE);
                    mBinding.songListBg.setVisibility(View.VISIBLE);
                    mIsUsbOneListUp = true;
                }
                return false;
            }
        });

        mBinding.fragmentSongList.secondUsbRlv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSecondUsb != null
                        && event.getAction() == MotionEvent.ACTION_MOVE
                        && !mIsUsbTwoListUp) {
                    if (mConstraintUtil == null) {
                        mConstraintUtil = new ConstraintUtil((ConstraintLayout) mBinding.getRoot()
                                .findViewById(R.id.usb_music_fragment));
                    }
                    mBinding.songListSearch.getRoot().setVisibility(View.GONE);
                    mBinding.songSearchGroup.setVisibility(View.GONE);
                    mBinding.usbLabel.getRoot().setVisibility(View.GONE);
                    mViewModel.mIsUsbLabelAvailable.set(false);
                    mBinding.fragmentPlayer.getRoot().setVisibility(View.GONE);
                    ConstraintUtil.ConstraintBegin begin = mConstraintUtil.beginWithAnim();
                    begin.setMarginToZero(R.id.usb_music_back_folder, ConstraintSet.TOP,
                            R.id.song_list_shrink_btn, ConstraintSet.BOTTOM);
                    begin.commit();
                    mBinding.songListShrinkBtn.setVisibility(View.VISIBLE);
                    mBinding.songListBg.setVisibility(View.VISIBLE);
                    mIsUsbTwoListUp = true;
                }
                return false;
            }
        });
    }

    @Override
    public void initViewObservable() {
        LogUtils.logD(TAG, "initViewObservable :: invoke");
        super.initViewObservable();
        mViewModel.mToastMessage.observe(this, message -> {
            LogUtils.logD(TAG, "mToastMessage--> message: " + message);
            if (!TextUtils.isEmpty(message)) {
                switch (message) {
                    case MusicConstants.ErrorCode.UNKNOWN_ERROR:
                        ToastUtils.showToast(getString(R.string.music_file_problem));
                        break;
                    default:
                        break;
                }
            }
        });
        mViewModel.mFirstCurrentPath.observe(this, path -> {
            LogUtils.logD(TAG, "mFirstCurrentPath--> path: " + path);
            if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() != View.VISIBLE
                    || TextUtils.isEmpty(path)) {
                LogUtils.logD(TAG, "firstUsbRlv  :: getVisibility : GONE ");
                return;
            }
            int depth = path.split("/").length;
            if (depth <= MusicConstants.MIN_DEPTH) {
                mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
                if (!mFristIsPlayAll) {
                    LogUtils.logD(TAG, "mFristIsPlayAll  false ");
                    setListTitle(MusicConstants.PLAY_ALL);
                    UsbMusicListBean usbMusicListBean = MusicUtils.getInstance()
                            .filterAudio(mUsbAudioInfoBeans, path);
                    if (null != usbMusicListBean && null != usbMusicListBean.getAudioList()
                            && usbMusicListBean.getAudioList().size() > 0) {
                        mFistUsbAudioSum = usbMusicListBean.getAudioList().size();
                    } else {
                        mFistUsbAudioSum = 0;
                    }
                    setScannerAudioSum(mFistUsbAudioSum);
                } else {
                    LogUtils.logD(TAG, "mFristIsPlayAll  true ");
                }
            } else {
                mBinding.usbMusicBackFolder.tvFolderPath.setText(
                        MusicUtils.getInstance().match(path));
                updateScrollLocation();
                mBinding.usbMusicBackFolderGroup.setVisibility(View.VISIBLE);
                setListTitle(MusicConstants.PLAY_CURRENT_FOLDER);
                UsbMusicListBean usbMusicListBean = getUsbMusicList(path);
                if (null != usbMusicListBean && null != usbMusicListBean.getAudioList()) {
                    setScannerAudioSum(usbMusicListBean.getAudioList().size());
                } else {
                    setScannerAudioSum(0);
                }
            }
        });
        mViewModel.mSecondCurrentPath.observe(this, path -> {
            LogUtils.logD(TAG, "mSecondCurrentPath--> path: " + path);
            if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() != View.VISIBLE
                    || TextUtils.isEmpty(path)) {
                LogUtils.logD(TAG, "secondUsbRlv  :: getVisibility : GONE ");
                return;
            }
            int depth = path.split("/").length;
            if (depth <= MusicConstants.MIN_DEPTH) {
                mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
                if (!mSecondIsPlayAll) {
                    LogUtils.logD(TAG, "mSecondIsPlayAll  false");
                    setListTitle(MusicConstants.PLAY_ALL);
                    UsbMusicListBean usbMusicListBean = MusicUtils.getInstance()
                            .filterAudio(mUsbAudioInfoBeans, path);
                    if (null != usbMusicListBean && null != usbMusicListBean.getAudioList()
                            && usbMusicListBean.getAudioList().size() > 0) {
                        mSecondUsbAudioSum = usbMusicListBean.getAudioList().size();
                    } else {
                        mSecondUsbAudioSum = 0;
                    }
                    setScannerAudioSum(mSecondUsbAudioSum);
                } else {
                    LogUtils.logD(TAG, "mSecondIsPlayAll  true");
                }
            } else {
                mBinding.usbMusicBackFolder.tvFolderPath.setText(
                        MusicUtils.getInstance().match(path));
                updateScrollLocation();
                mBinding.usbMusicBackFolderGroup.setVisibility(View.VISIBLE);
                setListTitle(MusicConstants.PLAY_CURRENT_FOLDER);
                UsbMusicListBean usbMusicListBean = getUsbMusicList(path);
                if (null != usbMusicListBean && null != usbMusicListBean.getAudioList()) {
                    setScannerAudioSum(usbMusicListBean.getAudioList().size());
                } else {
                    setScannerAudioSum(0);
                }
            }
        });
        mViewModel.mAudioData.observe(this, audioInfoBeans -> {
            stopLoadingAnimation();
            if (mUsbDataQuerying != 0) {
                mUsbAudioInfoBeans = audioInfoBeans;
                queryComplete();
                return;
            }
            LogUtils.logD(TAG, "mAudioData--> invoke ");
            if (null != audioInfoBeans) {
                LogUtils.logD(TAG, "mAudioData--> size: " + audioInfoBeans.size());
                mUsbAudioInfoBeans = audioInfoBeans;
                autoUpdateBufferMap();
                setInitAdapter(MusicConstants.UPDATE_ADAPTER_ALL);
            }
        });

        mViewModel.mIsLrcViewPause.observe(this, ob -> {
            LogUtils.logD(TAG, "mIsLrcViewPause--> ob: " + ob);
            if ((Boolean) ob) {
                mBinding.musicLrcView.lrcView.pause();
            } else {
                mBinding.musicLrcView.lrcView.resume();
            }
        });

        mViewModel.mLrcViewUpdate.observe(this, currentTime -> {
            LogUtils.logD(TAG, "mLrcViewUpdate--> currentTime: " + currentTime);
            if (null != currentTime) {
                mBinding.musicLrcView.lrcView.updateTime(currentTime);
            }
        });

        mViewModel.uiChangeObservable.playerLyricEvent.observe(this, ob -> {
            LogUtils.logD(TAG, "playerLyricEvent--> ob: " + ob);
            if (!mIsShowLyric) {
                showLrcView();
            } else {
                hideLrcView();
            }
        });

        mViewModel.mInitLrcView.observe(this, bool -> {
            LogUtils.logD(TAG, "mInitLrcView--> bool: " + bool);
            if (bool) {
                initLrc();
            }
        });

        mViewModel.uiChangeObservable.songSearchEvent.observe(this, ob -> {
            LogUtils.logD(TAG, "songSearchEvent--> ob: " + ob);
            showMusicSearchFragment();
        });

        mViewModel.mUsbDevices.observe(this, this::usbDevicesChange);

        mViewModel.mNotifyDataSetChanged.observe(this, aBoolean -> {
            LogUtils.logD(TAG, "mNotifyDataSetChanged--> aBoolean: " + aBoolean);
            if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                setCurrentPlayerInfo(mFirstUsbSongListAdapter, mBinding.fragmentSongList
                        .firstUsbRlv);
            } else {
                setCurrentPlayerInfo(mSecondUsbSongListAdapter, mBinding.fragmentSongList
                        .secondUsbRlv);
            }
        });

        mViewModel.mUsbIndex.observe(this, integer -> {
            LogUtils.logD(TAG, "mUsbIndex--> integer: " + integer);
            switch (integer) {
                case MusicConstants.USB_1_INDEX:
                    isVisibleRecyclerView(View.VISIBLE, View.GONE);
                    if (mUsbFirstHasData && !mIsShowLyric
                            && mFristUsb.getScanState() == MusicConstants.MEDIA_SCANNER_DATA) {
                        setSearchBarVisible(true);
                    } else {
                        setSearchBarVisible(false);
                    }
                    mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
                    if (!mIsShowLyric) {
                        showInitLoading(mFristUsb);
                    }
                    if (mFristIsPlayAll) {
                        setListTitle(MusicConstants.BACK_USB_FOLDER);
                    } else {
                        if (!TextUtils.isEmpty(mUsbOnePath)) {
                            mViewModel.mFirstCurrentPath
                                    .postValue(mViewModel.mFirstCurrentPath.getValue());
                        } else {
                            mViewModel.mFirstCurrentPath.postValue("/");
                        }
                    }
                    mViewModel.mNotifyDataSetChanged.postValue(true);
                    LogUtils.logD(TAG, "mUsbIndex-->Usb1");
                    break;
                case MusicConstants.USB_2_INDEX:
                    isVisibleRecyclerView(View.GONE, View.VISIBLE);
                    if (mUsbSecondHasData && !mIsShowLyric
                            && mSecondUsb.getScanState() == MusicConstants.MEDIA_SCANNER_DATA) {
                        setSearchBarVisible(true);
                    } else {
                        setSearchBarVisible(false);
                    }
                    mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
                    if (!mIsShowLyric) {
                        showInitLoading(mSecondUsb);
                    }
                    if (mSecondIsPlayAll) {
                        setListTitle(MusicConstants.BACK_USB_FOLDER);
                    } else {
                        if (!TextUtils.isEmpty(mUsbTwoPath)) {
                            mViewModel.mSecondCurrentPath
                                    .postValue(mViewModel.mSecondCurrentPath.getValue());
                        } else {
                            mViewModel.mSecondCurrentPath.postValue("/");
                        }
                    }
                    mViewModel.mNotifyDataSetChanged.postValue(true);
                    LogUtils.logD(TAG, "mUsbIndex-->Usb2");
                    break;
                default:
                    break;
            }
        });

        mViewModel.mAlbumCoverDrawable.observe(this, album -> {
            LogUtils.logD(TAG, "mAlbumCoverDrawable--> album: " + album);
            if (null != album) {
                mBinding.fragmentPlayer.albumCover.setImageBitmap(MusicUtils.getInstance()
                        .getAlbumArt(album, false));
            }
        });

        mViewModel.uiChangeObservable.playerPlayOrPauseEvent.observe(this, ob -> {
            LogUtils.logD(TAG, "playerPlayOrPauseEvent--> ob: " + ob);
            playOrPause();
        });

        mViewModel.uiChangeObservable.playAllEvent.observe(this, ob -> {
            LogUtils.logD(TAG, "playAllEvent ...");
            String type = mViewModel.mListTittle.getValue();
            if (TextUtils.isEmpty(type)) {
                return;
            }
            // Play all
            if (type.equals(getString(R.string.music_song_list_sum))) {
                LogUtils.logD(TAG, " Play all ...");
                setListTitle(MusicConstants.BACK_USB_FOLDER);
                mWhetherToPlayAll = false;
                List<AudioInfoBean> firstUsbAudioInfo = MusicUtils.getInstance().getAllAudio(
                        mUsbAudioInfoBeans, mUsbOnePath);
                List<AudioInfoBean> secondUsbAudioInfo = MusicUtils.getInstance().getAllAudio(
                        mUsbAudioInfoBeans, mUsbTwoPath);

                if (firstUsbAudioInfo != null && firstUsbAudioInfo.size() > 0) {
                    mFirstUsbSongListAdapter.setNewData(firstUsbAudioInfo);
                    setOnItemClick(mFirstUsbSongListAdapter);
                }

                if (secondUsbAudioInfo != null && secondUsbAudioInfo.size() > 0) {
                    mSecondUsbSongListAdapter.setNewData(secondUsbAudioInfo);
                    setOnItemClick(mSecondUsbSongListAdapter);
                }

                if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                    mFristIsPlayAll = true;
                    mViewModel.startPlayAudio(mFirstUsbSongListAdapter.getData(), MusicConstants
                            .DEFAULT_PLAYER_INDEX, true);
                } else {
                    mSecondIsPlayAll = true;
                    mViewModel.startPlayAudio(mSecondUsbSongListAdapter.getData(), MusicConstants
                            .DEFAULT_PLAYER_INDEX, true);
                }
            } else if (type.equals(getString(R.string.usb_disk_folder))) {
                LogUtils.logD(TAG, " Play folder ...");
                if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                    if (!TextUtils.isEmpty(mUsbOnePath)) {
                        mFristIsPlayAll = false;
                        setListDataByAudioPath(mUsbOnePath);
                    }
                } else if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() == View.VISIBLE) {
                    if (!TextUtils.isEmpty(mUsbTwoPath)) {
                        mSecondIsPlayAll = false;
                        setListDataByAudioPath(mUsbTwoPath);
                    }
                }
                mWhetherToPlayAll = true;
            } else if (type.equals(getString(R.string.music_play_current_folder))) {
                LogUtils.logD(TAG, " Play current folder ...");
                if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                    mCurrentPlayList = getAudioList(mFirstUsbSongListAdapter.getData());
                    if (mCurrentPlayList.size() > 0) {
                        mViewModel.setAudioInfo(mCurrentPlayList
                                .get(MusicConstants.DEFAULT_PLAYER_INDEX));
                        mViewModel.startPlayAudio(mCurrentPlayList,
                                MusicConstants.DEFAULT_PLAYER_INDEX, true);
                    }
                } else {
                    mCurrentPlayList = getAudioList(mSecondUsbSongListAdapter.getData());
                    if (mCurrentPlayList.size() > 0) {
                        mViewModel.setAudioInfo(mCurrentPlayList
                                .get(MusicConstants.DEFAULT_PLAYER_INDEX));
                        mViewModel.startPlayAudio(mCurrentPlayList,
                                MusicConstants.DEFAULT_PLAYER_INDEX, true);
                    }
                }
            }
        });

        // Sub file return event monitoring
        mViewModel.uiChangeObservable.folderBackEvent.observe(this, ob -> {
            LogUtils.logD(TAG, "folderBackEvent :: invoke");
            String path = "";
            if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                path = mViewModel.mFirstCurrentPath.getValue();
            } else {
                path = mViewModel.mSecondCurrentPath.getValue();
            }
            path = new StringBuffer(path).substring(0, path.lastIndexOf("/"));
            LogUtils.logD(TAG, "folderBackEvent :: path ::" + path);
            int depth = path.split("/").length;
            if (depth < MusicConstants.MIN_DEPTH) {
                return;
            } else if (depth == MusicConstants.MIN_DEPTH) {
                LogUtils.logD(TAG, "folderBackEvent :: depth == 3");
                //  show play all button
                UsbMusicListBean usbMusicListBean = getUsbMusicList(path);
                if (null != usbMusicListBean && null != usbMusicListBean.getAudioList()) {
                    setScannerAudioSum(usbMusicListBean.getAudioList().size());
                } else {
                    setScannerAudioSum(0);
                }
                if (!TextUtils.isEmpty(mUsbOnePath) && path.contains(mUsbOnePath)) {
                    LogUtils.logD(TAG, "folderBackEvent :: depth == 3 :: mUsbOnePath :: "
                            + mUsbOnePath + "path :: " + path);
                    updateFolderListAdapter(mFirstUsbSongListAdapter, path, false);
                    mViewModel.mFirstCurrentPath.setValue(path);
                } else if (!TextUtils.isEmpty(mUsbTwoPath) && path.contains(mUsbTwoPath)) {
                    LogUtils.logD(TAG, "folderBackEvent :: depth == 3 :: mUsbTwoPath :: "
                            + mUsbTwoPath + "path :: " + path);
                    updateFolderListAdapter(mSecondUsbSongListAdapter, path, false);
                    mViewModel.mSecondCurrentPath.setValue(path);
                }
            } else {
                if (!TextUtils.isEmpty(mUsbOnePath) && path.contains(mUsbOnePath)) {
                    LogUtils.logD(TAG, "folderBackEvent :: depth > 3 :: mUsbOnePath :: "
                            + mUsbOnePath + "path :: " + path);
                    updateFolderListAdapter(mFirstUsbSongListAdapter, path, true);
                    mViewModel.mFirstCurrentPath.setValue(path);
                } else if (!TextUtils.isEmpty(mUsbTwoPath) && path.contains(mUsbTwoPath)) {
                    LogUtils.logD(TAG, "folderBackEvent :: depth > 3 :: mUsbTwoPath :: "
                            + mUsbTwoPath + "path :: " + path);
                    updateFolderListAdapter(mSecondUsbSongListAdapter, path, true);
                    mViewModel.mSecondCurrentPath.setValue(path);
                }
            }
        });

        mViewModel.mIsPlayInfoEnable.observeForever(ob -> {
            LogUtils.logD(TAG, "mIsPlayInfoEnable :: ob:: " + ob);
            mBinding.fragmentPlayer.play.setEnabled(ob);
            mBinding.fragmentPlayer.playNext.setEnabled(ob);
            mBinding.fragmentPlayer.playPrevious.setEnabled(ob);
            mBinding.fragmentPlayer.mode.setEnabled(ob);
            mBinding.fragmentPlayer.musicSeekBar.setEnabled(ob);
        });

        mViewModel.mIsDataEnable.observeForever(ob -> {
            LogUtils.logD(TAG, "mIsDataEnable :: ob:: " + ob);
            mBinding.fragmentPlayer.lyricOrSheet.setEnabled(ob);
        });
    }

    private List<AudioInfoBean> getAudioList(List<AudioInfoBean> audioInfoBeanList) {
        List<AudioInfoBean> list = new ArrayList<>();
        if (null != audioInfoBeanList) {
            for (AudioInfoBean audioInfoBean : audioInfoBeanList) {
                if (audioInfoBean.getAudioId() != MusicConstants.AUDIO_FOLDER_ID) {
                    list.add(audioInfoBean);
                }
            }
        }
        return list;
    }

    /**
     * Get UsbMusicListBean from mBufferAudioMap.
     */
    public UsbMusicListBean getUsbMusicList(String path) {
        LogUtils.logD(TAG, "getUsbMusicList :: invoke :: path ::" + path);
        UsbMusicListBean usbMusicListBean;
        UsbMusicListBean buffer;
        if (null != mUsbAudioInfoBeans) {
            LogUtils.logD(TAG, "getUsbMusicList :: mUsbAudioInfoBeans size::"
                    + mUsbAudioInfoBeans.size());
            if (null != mBufferAudioMap) {
                if (mBufferAudioMap.containsKey(path)) {
                    LogUtils.logD(TAG, "getUsbMusicList :: containsKey  path ::" + path);
                    buffer = mBufferAudioMap.get(path);
                } else {
                    LogUtils.logD(TAG, "getUsbMusicList :: not containsKey  path ::");
                    buffer = MusicUtils.getInstance().filterFolderAudio(mUsbAudioInfoBeans, path);
                    if (null != buffer) {
                        mBufferAudioMap.put(path, buffer);
                    }
                }
            } else {
                LogUtils.logD(TAG, "getUsbMusicList :: not mBufferAudioMap");
                mBufferAudioMap = new HashMap<>();
                buffer = MusicUtils.getInstance().filterFolderAudio(mUsbAudioInfoBeans, path);
                if (null != buffer) {
                    mBufferAudioMap.put(path, buffer);
                }
            }
            usbMusicListBean = cloneBean(buffer);
            return usbMusicListBean;
        }
        return null;
    }

    private UsbMusicListBean cloneBean(UsbMusicListBean bean) {
        UsbMusicListBean usbMusicListBean = new UsbMusicListBean();
        List<AudioInfoBean> audioList = new ArrayList<>();
        List<AudioInfoBean> folderList = new ArrayList<>();
        if (null != bean) {
            if (null != bean.getFolderList() && bean.getFolderList().size() > 0) {
                LogUtils.logD(TAG, "getUsbMusicList :: getFolderList size  ::"
                        + bean.getFolderList().size());
                for (AudioInfoBean audioInfoBean : bean.getFolderList()) {
                    folderList.add(audioInfoBean);
                }
            }
            if (null != bean.getAudioList() && bean.getAudioList().size() > 0) {
                LogUtils.logD(TAG, "getUsbMusicList :: getAudioList size  ::"
                        + bean.getAudioList().size());
                for (AudioInfoBean audioInfoBean : bean.getAudioList()) {
                    audioList.add(audioInfoBean);
                }
            }
            usbMusicListBean.setAudioList(audioList);
            usbMusicListBean.setFolderList(folderList);
            usbMusicListBean.setCurrentPath(bean.getCurrentPath());
            return usbMusicListBean;
        }
        return null;
    }

    /**
     * Set UsbMusicListBean into mBufferAudioMap.
     */
    private void autoUpdateBufferMap() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (null != mUsbAudioInfoBeans && mUsbAudioInfoBeans.size() > 0) {
                    mBufferAudioMap = new HashMap<>();
                    for (AudioInfoBean audioInfoBean : mUsbAudioInfoBeans) {
                        LogUtils.logD(TAG, "autoUpdateBufferMap :: path :: "
                                + audioInfoBean.getAudioPath()
                                + "getAudioName ::  " + audioInfoBean.getAudioName()
                                + "::" + mUsbAudioInfoBeans.size());
                        String path = audioInfoBean.getAudioPath();
                        path = new StringBuffer(path).substring(0, path.lastIndexOf("/"));
                        do {
                            if (!mBufferAudioMap.containsKey(path)) {
                                LogUtils.logD(TAG, "mBufferAudioMap :: put path :: " + path);
                                mBufferAudioMap.put(path, MusicUtils.getInstance()
                                        .filterFolderAudio(mUsbAudioInfoBeans, path));
                            }
                            path = new StringBuffer(path).substring(0, path.lastIndexOf("/"));
                        } while (path.split("/").length >= MusicConstants.MIN_DEPTH);
                    }
                }
            }
        });
    }

    /**
     * Update list based on path.
     */
    public void setListDataByAudioPath(String path) {
        LogUtils.logD(TAG, "setListDataByAudioPath :: invoke :: path::" + path);
        if (!TextUtils.isEmpty(mUsbOnePath) && path.contains(mUsbOnePath)) {
            LogUtils.logD(TAG, "setListDataByAudioPath :: mFirstCurrentPath :: " + path);
            mViewModel.mFirstCurrentPath.setValue(path);
            mFristIsPlayAll = false;
            if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                LogUtils.logD(TAG, "firstUsbRlv :: getVisibility  VISIBLE ");
                updateFolderListAdapter(mFirstUsbSongListAdapter, path, true);
            } else {
                LogUtils.logD(TAG, "firstUsbRlv :: getVisibility  GONE ");
                updateFolderListAdapter(mFirstUsbSongListAdapter, path, false);
            }
        } else if (!TextUtils.isEmpty(mUsbTwoPath) && path.contains(mUsbTwoPath)) {
            LogUtils.logD(TAG, "setListDataByAudioPath :: mSecondCurrentPath ::" + path);
            mViewModel.mSecondCurrentPath.setValue(path);
            mSecondIsPlayAll = false;
            if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() == View.VISIBLE) {
                LogUtils.logD(TAG, "secondUsbRlv :: getVisibility  VISIBLE ");
                updateFolderListAdapter(mSecondUsbSongListAdapter, path, true);
            } else {
                LogUtils.logD(TAG, "secondUsbRlv :: getVisibility  GONE ");
                updateFolderListAdapter(mSecondUsbSongListAdapter, path, false);
            }
        }
    }

    private void updateScrollLocation() {
        LogUtils.logD(TAG, "updateScrollLocation :: invoke ");
        mBinding.usbMusicBackFolder.tvFolderPathContainer.post(new Runnable() {
            @Override
            public void run() {
                mBinding.usbMusicBackFolder.tvFolderPathContainer
                        .fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
    }

    private void setScannerAudioSum(int fistUsbAudioSum) {
        LogUtils.logD(TAG, "setScannerAudioSum :: invoke  :: fistUsbAudioSum " + fistUsbAudioSum);
        mViewModel.mAudioSum.postValue(String.format(getString(R.string.sum_song),
                fistUsbAudioSum));
    }

    private void setListTitle(int type) {
        LogUtils.logD(TAG, "setListTitle :: invoke  :: type " + type);
        switch (type) {
            case MusicConstants.PLAY_ALL:
                mViewModel.mListTittle.postValue(getString(R.string.music_song_list_sum));
                mViewModel.mIsFolder.setValue(true);
                break;
            case MusicConstants.PLAY_CURRENT_FOLDER:
                mViewModel.mListTittle.postValue(getString(R.string.music_play_current_folder));
                mViewModel.mIsFolder.setValue(true);
                break;
            case MusicConstants.BACK_USB_FOLDER:
                mViewModel.mListTittle.postValue(getString(R.string.usb_disk_folder));
                mViewModel.mIsFolder.setValue(false);
                break;
            default:
                break;
        }
    }

    private void showInitLoading(UsbDevicesInfoBean usb) {
        LogUtils.logD(TAG, "showInitLoading :: invoke ");
        if (null != usb) {
            LogUtils.logD(TAG, " showInitLoading :: not  null ::  getPort" + usb.getPort());
            if (usb.getPort().equals(MusicConstants.USB_1_PORT)) {
                LogUtils.logD(TAG, "showInitLoading :: getScanState" + usb.getScanState());
                if (usb.getScanState() == MusicConstants.MEDIA_SCANNER_STARTED) {
                    LogUtils.logD(TAG, "showInitLoading :: USB_1_PORT :: MEDIA_SCANNER_STARTED ");
                    if (mFristScanSum > 1) {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_songs), mFristScanSum));
                    } else {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_song), mFristScanSum));
                    }
                    mFirstUsbSongListAdapter.clearEmptyView();
                    if (mFirstUsbSongListAdapter.getData().size() == 0) {
                        showLoadingAnimation();
                    }
                } else if (usb.getScanState() == MusicConstants.MEDIA_SCANNER_FINISHED) {
                    LogUtils.logD(TAG, "showInitLoading :: USB_1_PORT :: MEDIA_SCANNER_FINISHED ");
                    stopLoadingAnimation();
                    mIsShowScanningComplete = true;
                    new Handler().postDelayed(() -> {
                        mIsShowScanningComplete = false;
                        if (null != mFristUsb && mFristUsb
                                .getScanState() == MusicConstants.MEDIA_SCANNER_FINISHED) {
                            mFristUsb.setScanState(MusicConstants.MEDIA_SCANNER_DATA);
                        }
                    }, MusicConstants.DELAY_HIDDEN_TIME);
                } else if (usb.getScanState() == MusicConstants.MEDIA_SCANNER_DATA) {
                    LogUtils.logD(TAG, "showInitLoading :: USB_1_PORT :: MEDIA_SCANNER_DATA ");
                    stopLoadingAnimation();
                }
            } else if (usb.getPort().equals(MusicConstants.USB_2_PORT)) {
                LogUtils.logD(TAG, "showInitLoading :: getScanState" + usb.getScanState());
                if (usb.getScanState() == MusicConstants.MEDIA_SCANNER_STARTED) {
                    LogUtils.logD(TAG, "showInitLoading :: USB_2_PORT :: MEDIA_SCANNER_STARTED ");
                    if (mSecondScanSum > 1) {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_songs), mSecondScanSum));
                    } else {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_song), mSecondScanSum));
                    }
                    mSecondUsbSongListAdapter.clearEmptyView();
                    if (mSecondUsbSongListAdapter.getData().size() == 0) {
                        showLoadingAnimation();
                    }
                } else if (usb.getScanState() == MusicConstants.MEDIA_SCANNER_FINISHED) {
                    LogUtils.logD(TAG, "showInitLoading :: USB_2_PORT :: MEDIA_SCANNER_FINISHED ");
                    stopLoadingAnimation();
                    mIsShowScanningComplete = false;
                    new Handler().postDelayed(() -> {
                        mIsShowScanningComplete = false;
                        if (null != mSecondUsb && mSecondUsb
                                .getScanState() == MusicConstants.MEDIA_SCANNER_FINISHED) {
                            mSecondUsb.setScanState(MusicConstants.MEDIA_SCANNER_DATA);
                        }
                    }, MusicConstants.DELAY_HIDDEN_TIME);
                } else if (usb.getScanState() == MusicConstants.MEDIA_SCANNER_DATA) {
                    LogUtils.logD(TAG, "showInitLoading :: USB_2_PORT :: MEDIA_SCANNER_DATA ");
                    stopLoadingAnimation();
                }
            }
        } else {
            LogUtils.logD(TAG, "usb null ");
        }
    }

    private void isVisibleRecyclerView(int visible, int gone) {
        LogUtils.logD(TAG, "isVisibleRecyclerView :: invoke ");
        mBinding.fragmentSongList.firstUsbRlv.setVisibility(visible);
        mBinding.fragmentSongList.secondUsbRlv.setVisibility(gone);
    }

    private void hideLrcView() {
        LogUtils.logD(TAG, "hideLrcView :: invoke ");
        mBinding.fragmentSongListGroup.setVisibility(View.VISIBLE);
        if (mUsbOnePath.isEmpty() || mUsbTwoPath.isEmpty()) {
            mViewModel.mIsUsbLabelAvailable.set(false);
        } else {
            mViewModel.mIsUsbLabelAvailable.set(true);
        }
        mBinding.musicLrcGroup.setVisibility(View.GONE);
        if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
            if (mUsbFirstHasData && mFristUsb.getScanState()
                    > MusicConstants.MEDIA_SCANNER_STARTED) {
                setSearchBarVisible(true);
            } else {
                setSearchBarVisible(false);
            }
            mViewModel.mFirstCurrentPath.postValue(mViewModel.mFirstCurrentPath.getValue());
        } else if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() == View.VISIBLE) {
            if (mUsbSecondHasData && mSecondUsb.getScanState()
                    > MusicConstants.MEDIA_SCANNER_STARTED) {
                setSearchBarVisible(true);
            } else {
                setSearchBarVisible(false);
            }
            mViewModel.mSecondCurrentPath.postValue(mViewModel.mSecondCurrentPath.getValue());
        }
        mIsShowLyric = false;
        if (mShowListDelayed != -1) {
            if (mShowListDelayed == MusicConstants.USB_1_INDEX) {
                mViewModel.mUsbIndex.postValue(MusicConstants.USB_1_INDEX);
            } else {
                mViewModel.mUsbIndex.postValue(MusicConstants.USB_2_INDEX);
            }
            mShowListDelayed = -1;
        }
        if (mIsShowLoading) {
            mBinding.usbMusicLoadingGroup.setVisibility(View.VISIBLE);
        }
    }

    private void showLrcView() {
        LogUtils.logD(TAG, "showLrcView :: invoke ");
        mBinding.fragmentSongListGroup.setVisibility(View.GONE);
        mBinding.songSearchGroup.setVisibility(View.GONE);
        mViewModel.mIsUsbLabelAvailable.set(false);
        mBinding.musicLrcGroup.setVisibility(View.VISIBLE);
        mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
        mIsShowLyric = true;
        if (mIsShowLoading) {
            mBinding.usbMusicLoadingGroup.setVisibility(View.GONE);
        }
    }

    private void playOrPause() {
        LogUtils.logD(TAG, "playOrPause :: invoke ");
        if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
            LogUtils.logD(TAG, "playOrPause :: mFirstUsbSongListAdapter ");
            mViewModel.startPlayAudio(mFirstUsbSongListAdapter.getData(), MusicConstants
                    .DEFAULT_PLAYER_INDEX, false);
        } else {
            LogUtils.logD(TAG, "playOrPause :: mSecondUsbSongListAdapter ");
            mViewModel.startPlayAudio(mSecondUsbSongListAdapter.getData(), MusicConstants
                    .DEFAULT_PLAYER_INDEX, false);
        }
    }

    private void showMusicSearchFragment() {
        LogUtils.logD(TAG, "showMusicSearchFragment :: invoke ");
        if (null != getActivity()) {
            LogUtils.logD(TAG, "showMusicSearchFragment :: do ");
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            UsbMusicSearchFragment musicSearchFragment = UsbMusicSearchFragment.getInstance();
            transaction.add(R.id.content_frame_layout, musicSearchFragment);
            transaction.addToBackStack(UsbMusicSearchFragment.class.getSimpleName());
            transaction.commit();
        }
    }

    private void initLrc() {
        try {
            if (null != mViewModel && null != mViewModel.mUsbMusicManager
                    && null != mViewModel.mUsbMusicManager.getCurrentPlayerMusic()) {
                AudioInfoBean currentAudio = mViewModel.mUsbMusicManager.getCurrentPlayerMusic();
                String currentAudioPath = currentAudio.getAudioPath();
                if (!TextUtils.isEmpty(currentAudioPath)) {
                    String lrcPath = currentAudioPath.substring(MusicConstants.SUB_STRING_INDEX,
                            currentAudioPath.lastIndexOf(".")) + MusicConstants.MUSIC_LRC_FILE_NAME;
                    List<AudioLrcBean> lrcList = MusicLrcRowParser
                            .parseLrcFromFile(new File(lrcPath));
                    mBinding.musicLrcView.lrcView.setLrcData(lrcList);
                    mBinding.musicLrcView.lrcView
                            .setOnPlayIndicatorLineListener((time, content) -> {
                                try {
                                    mViewModel.mUsbMusicManager.seekTo(time);
                                } catch (RemoteException ex) {
                                    ex.printStackTrace();
                                }
                            });
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set song list adapter.
     */
    private void setInitAdapter(int type) {
        LogUtils.logD(TAG, "setAdapter :: invoke");
        List<AudioInfoBean> firstUsbAudioInfo = MusicUtils.getInstance()
                .getAllAudio(mUsbAudioInfoBeans, mUsbOnePath);
        List<AudioInfoBean> secondUsbAudioInfo = MusicUtils.getInstance()
                .getAllAudio(mUsbAudioInfoBeans, mUsbTwoPath);
        if (null != firstUsbAudioInfo && firstUsbAudioInfo.size() > 0) {
            mUsbFirstHasData = true;
            if (type == MusicConstants.UPDATE_ADAPTER_ALL
                    || type == MusicConstants.UPDATE_ADAPTER_FIRST) {
                mFirstUsbSongListAdapter.setNewData(firstUsbAudioInfo);
                setOnItemClick(mFirstUsbSongListAdapter);
                if (mViewModel.mNeedInit.getValue()) {
                    mViewModel.initRecentAudio(mUsbOneUuid, firstUsbAudioInfo,
                            mBinding.fragmentPlayer.musicSeekBar);
                    mViewModel.mNotifyDataSetChanged.postValue(true);
                }
            }
        } else {
            mUsbFirstHasData = false;
        }
        if (null != secondUsbAudioInfo && secondUsbAudioInfo.size() > 0) {
            mUsbSecondHasData = true;
            if (type == MusicConstants.UPDATE_ADAPTER_ALL
                    || type == MusicConstants.UPDATE_ADAPTER_SECOND) {
                mSecondUsbSongListAdapter.setNewData(secondUsbAudioInfo);
                setOnItemClick(mSecondUsbSongListAdapter);
                if (mViewModel.mNeedInit.getValue()) {
                    mViewModel.initRecentAudio(mUsbTwoUuid, secondUsbAudioInfo,
                            mBinding.fragmentPlayer.musicSeekBar);
                    mViewModel.mNotifyDataSetChanged.postValue(true);
                }
            }
        } else {
            mUsbSecondHasData = false;
        }
        if (TextUtils.isEmpty(mUsbOnePath) && TextUtils.isEmpty(mUsbTwoPath)) {
            mViewModel.updateLyricButton(false);
            mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
            setEmptyAdapter(R.layout.usb_exception_view);
        } else {
            LogUtils.logD(TAG, "setInitAdapter: mUsbFirstHasData: " + mUsbFirstHasData
                + "; mUsbSecondHasData: " + mUsbSecondHasData);
            if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                if (mUsbFirstHasData && !mIsShowLyric && mFristUsb.getScanState()
                        > MusicConstants.MEDIA_SCANNER_STARTED) {
                    setSearchBarVisible(true);
                } else {
                    mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
                    setSearchBarVisible(false);
                }
            } else if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() == View.VISIBLE) {
                if (mUsbSecondHasData && !mIsShowLyric && mSecondUsb.getScanState()
                        > MusicConstants.MEDIA_SCANNER_STARTED) {
                    setSearchBarVisible(true);
                } else {
                    mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
                    setSearchBarVisible(false);
                }
            }
            if (!TextUtils.isEmpty(mUsbOnePath) && !mUsbFirstHasData) {
                mFirstUsbSongListAdapter
                        .setEmptyView(getEmptyView(R.layout.usb_music_no_data_view));
            }
            if (!TextUtils.isEmpty(mUsbTwoPath) && !mUsbSecondHasData) {
                mSecondUsbSongListAdapter
                        .setEmptyView(getEmptyView(R.layout.usb_music_no_data_view));
            }
        }
    }

    private void setSearchBarVisible(boolean show) {
        LogUtils.logD(TAG, "setVisiable :: invoke :: " + show);
        if (show) {
            mBinding.songSearchGroup.setVisibility(View.VISIBLE);
        } else {
            mBinding.songSearchGroup.setVisibility(View.GONE);
        }
    }

    private void setOnItemClick(SongListAdapter adapter) {
        adapter.setOnItemClickListener((helper, resultBean, position) -> {
            LogUtils.logD(TAG, "setOnItemClickListener :: invoke");
            if (null != mViewModel.mUsbMusicManager) {
                // Music folder
                if (resultBean.getAudioId() == MusicConstants.AUDIO_FOLDER_ID) {
                    updateFolderListAdapter(adapter, resultBean.getAudioPath(), true);
                    if (!TextUtils.isEmpty(mUsbOnePath)
                            && resultBean.getAudioPath().contains(mUsbOnePath)) {
                        LogUtils.logD(TAG, "resultBean.getAudioPath()" + resultBean.getAudioPath());
                        mViewModel.mFirstCurrentPath.setValue(resultBean.getAudioPath());
                    } else if (!TextUtils.isEmpty(mUsbTwoPath)
                            && resultBean.getAudioPath().contains(mUsbTwoPath)) {
                        mViewModel.mSecondCurrentPath.setValue(resultBean.getAudioPath());
                    }
                    // Normal playback
                } else {
                    try {
                        long currentPlayerId = mViewModel.mUsbMusicManager.getCurrentPlayerId();
                        if (currentPlayerId != resultBean.getAudioId()) {
                            mViewModel.setAudioInfo(resultBean);
                            mViewModel.startPlayAudio(adapter.getData(), position
                                    - MusicUtils.getInstance().getAudioSum(
                                    adapter.getData()), true);
                        }
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Update list with folder data.
     */
    public void updateFolderListAdapter(SongListAdapter adapter, String path, boolean updateSum) {
        LogUtils.logD(TAG, "updateFolderListAdapter::invoke :: path::"
                + path + " updateSum:: " + updateSum);
        if (null != mUsbAudioInfoBeans && null != adapter && null != path) {
            UsbMusicListBean usbMusicListBean = getUsbMusicList(path);
            if (null != usbMusicListBean) {
                LogUtils.logD(TAG, "usbMusicListBean:: not null ");
                if (null != usbMusicListBean.getFolderList()) {
                    List<AudioInfoBean> list = usbMusicListBean.getFolderList();
                    if (null != usbMusicListBean.getAudioList()) {
                        list.addAll(usbMusicListBean.getAudioList());
                    }
                    adapter.setNewData(list);
                    setOnItemClick(adapter);
                } else {
                    adapter.setNewData(usbMusicListBean.getAudioList());
                    setOnItemClick(adapter);
                }
                if (updateSum && path.split("/").length > MusicConstants.MIN_DEPTH) {
                    setScannerAudioSum(usbMusicListBean.getAudioList().size());
                }
                mViewModel.mNotifyDataSetChanged.postValue(true);
            } else {
                LogUtils.logD(TAG, "usbMusicListBean:: null ");
            }
        }
    }

    private void setEmptyAdapter(int layoutRes) {
        if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
            mFirstUsbSongListAdapter.setEmptyView(getEmptyView(layoutRes));
        } else {
            mSecondUsbSongListAdapter.setEmptyView(getEmptyView(layoutRes));
        }
    }

    private void setSeekBarChangeListener() {
        mBinding.fragmentPlayer.musicSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            try {
                                if (null != mViewModel.mUsbMusicManager) {
                                    long duration = mViewModel.mUsbMusicManager.getDurtion();
                                    if (duration > 0) {
                                        updateSeekTipLocation(progress);
                                        String time = MusicUtils.getInstance()
                                                .stringForAudioTime(progress * duration
                                                        / MusicConstants.MAX_PROGRESS);
                                        mViewModel.mCurrentTime.set(time);
                                        mViewModel.mSeekTip.set(time);
                                    }
                                }
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mBinding.getRoot().findViewById(R.id.seek_tip).setVisibility(View.VISIBLE);
                        mViewModel.mSeekTip.set(mViewModel.mCurrentTime.get());
                        mViewModel.mIsTouchSeekBar.set(true);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        try {
                            mBinding.getRoot().findViewById(R.id.seek_tip)
                                    .setVisibility(View.INVISIBLE);
                            mViewModel.mIsTouchSeekBar.set(false);
                            if (null != mViewModel.mUsbMusicManager) {
                                long duration = mViewModel.mUsbMusicManager.getDurtion();
                                if (duration > 0) {
                                    long currentTime = seekBar.getProgress() * duration
                                            / MusicConstants.MAX_PROGRESS;
                                    mViewModel.mUsbMusicManager.seekTo(currentTime);
                                    mViewModel.mLrcViewUpdate.postValue(currentTime);
                                }
                            }
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
    }

    /**
     * update seek tip location  according to  progress.
     */
    public void updateSeekTipLocation(int progress) {
        float seekTipX = MusicConstants.VALID_SEEK_WIDTH
                * (Float.valueOf(progress) / Float.valueOf(MusicConstants.MAX_PROGRESS))
                - MusicConstants.SEEK_TIP_OFFSET;
        mBinding.getRoot().findViewById(R.id.seek_tip).setTranslationX(seekTipX);
    }

    @Override
    public void updateAudioInfo(AudioInfoBean audioInfoBean) {
        LogUtils.logD(TAG, "updateAudioInfo:: invoke");
        if (null != audioInfoBean) {
            String path = MusicUtils.getInstance()
                    .getPreviousLevelPath(audioInfoBean.getAudioPath());
            LogUtils.logD(TAG, "updateAudioInfo:: path :: " + path);
            setListDataByAudioPath(path);
            UsbMusicListBean usbMusicListBean = getUsbMusicList(path);
            if (null != usbMusicListBean && null != usbMusicListBean.getAudioList()
                    && usbMusicListBean.getAudioList().size() > 0) {
                List<AudioInfoBean> list = usbMusicListBean.getAudioList();
                int position = MusicUtils.getInstance()
                        .getCurrentPlayIndex(list,
                                audioInfoBean.getAudioId(), audioInfoBean.getAudioPath());
                mViewModel.startPlayAudio(list, position, true);
                String searchResultPath = list.get(position).getAudioPath();
                if (!mUsbOnePath.isEmpty() && searchResultPath.contains(mUsbOnePath)) {
                    checkedUsbLabel(true, false);
                } else if (!mUsbTwoPath.isEmpty() && searchResultPath.contains(mUsbTwoPath)) {
                    checkedUsbLabel(false, true);
                }
            }
        } else {
            LogUtils.logD(TAG, "updateAudioInfo:: audioInfoBean is null");
        }

    }

    @Override
    public void addSingleAudioInfo(AudioInfoBean audioInfoBean) {
        if (null != getActivity()) {
            getActivity().runOnUiThread(() -> {
                if (!TextUtils.isEmpty(mUsbOnePath)
                        && audioInfoBean.getAudioPath().contains(mUsbOnePath)) {
                    mFristScanSum++;
                    LogUtils.logD(TAG, "mFristScanSum  Size: "
                            + mFristScanSum + " :: mUsbOnePath :: " + mUsbOnePath);
                } else if (!TextUtils.isEmpty(mUsbTwoPath)
                        && audioInfoBean.getAudioPath().contains(mUsbTwoPath)) {
                    mSecondScanSum++;
                    LogUtils.logD(TAG, "mSecondScanSum  Size: "
                            + mSecondScanSum + " :: mUsbTwoPath :: " + mUsbTwoPath);
                }
                if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                    if (mFristScanSum > 1) {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_songs), mFristScanSum));
                    } else {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_song), mFristScanSum));
                    }
                } else if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() == View.VISIBLE) {
                    if (mSecondScanSum > 1) {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_songs), mSecondScanSum));
                    } else {
                        mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                                R.string.scanned_sum_song), mSecondScanSum));
                    }
                }
            });
        }
    }

    @Override
    public void addScanAudioInfo(List<AudioInfoBean> audioInfoBean) {
        if (null != getActivity()) {
            getActivity().runOnUiThread(() -> {
                List<AudioInfoBean> firstUsbAudioInfo = MusicUtils.getInstance().getAllAudio(
                        audioInfoBean, mUsbOnePath);
                List<AudioInfoBean> secondUsbAudioInfo = MusicUtils.getInstance().getAllAudio(
                        audioInfoBean, mUsbTwoPath);
                if (firstUsbAudioInfo != null && firstUsbAudioInfo.size() > 0) {
                    mFirstUsbSongListAdapter.addListData(firstUsbAudioInfo);
                    setOnItemClick(mFirstUsbSongListAdapter);
                    stopLoadingAnimation();
                }
                if (secondUsbAudioInfo != null && secondUsbAudioInfo.size() > 0) {
                    mSecondUsbSongListAdapter.addListData(secondUsbAudioInfo);
                    setOnItemClick(mSecondUsbSongListAdapter);
                    stopLoadingAnimation();
                }
            });
        }
    }

    @Override
    public void onUsbDevicesChanged(List<UsbDevicesInfoBean> usbDevices) {
        if (null != getActivity()) {
            getActivity().runOnUiThread(() -> handleUsbDevicesChanged(usbDevices));
        }
    }

    @Override
    public void onScanStateChanged(int state, String deviceId, int portId) {
        if (null != getActivity()) {
            getActivity().runOnUiThread(() -> handleScanStateChanged(state, deviceId, portId));
        }
    }

    /**
     * Notification interface update.
     *
     * @param usbList U-disk inserted
     */
    private void usbDevicesChange(List<UsbDevicesInfoBean> usbList) {
        LogUtils.logD(TAG, "usbDevicesChange :: invoke");
        handleUsbDevicesChanged(usbList);
    }

    private void handleScanStateChanged(int state, String deviceId, int portId) {
        LogUtils.logD(TAG, "handleScanStateChanged media scanner finished:" + state
                + " ,portId:" + portId);
        if (MusicConstants.MEDIA_SCANNER_STARTED == state) {
            if (null != mFristUsb && mFristUsb.getUuid().equals(deviceId)) {
                mFristUsb.setScanState(MusicConstants.MEDIA_SCANNER_STARTED);
                if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                    mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                            R.string.scanned_sum_song), mFristScanSum));
                    mFirstUsbSongListAdapter.clearEmptyView();
                    if (mFirstUsbSongListAdapter.getData().size() == 0) {
                        showLoadingAnimation();
                    }
                }
            } else if (null != mSecondUsb && mSecondUsb.getUuid().equals(deviceId)) {
                mSecondUsb.setScanState(MusicConstants.MEDIA_SCANNER_STARTED);
                if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() == View.VISIBLE) {
                    mBinding.usbMusicLoading.tvLoadingTitle.setText(String.format(getString(
                            R.string.scanned_sum_song), mSecondScanSum));
                    mSecondUsbSongListAdapter.clearEmptyView();
                    if (mSecondUsbSongListAdapter.getData().size() == 0) {
                        showLoadingAnimation();
                    }
                }
            }
        } else if (MusicConstants.MEDIA_SCANNER_FINISHED == state) {
            if (null != mFristUsb && mFristUsb.getUuid().equals(deviceId)) {
                try {
                    mUsbAudioInfoBeans = mViewModel.mUsbMusicManager.getAllAudio();
                    mUsbDataQuerying = MusicConstants.USB_1_PORT_ID;
                } catch (RemoteException error) {
                    LogUtils.logD(TAG, "getAudioInfo :: error");
                    error.printStackTrace();
                }
            } else if (null != mSecondUsb && mSecondUsb.getUuid().equals(deviceId)) {
                try {
                    mUsbAudioInfoBeans = mViewModel.mUsbMusicManager.getAllAudio();
                    mUsbDataQuerying = MusicConstants.USB_2_PORT_ID;
                } catch (RemoteException error) {
                    LogUtils.logD(TAG, "getAudioInfo :: error");
                    error.printStackTrace();
                }
            }
        }
    }

    private void queryComplete() {
        if (mUsbDataQuerying == MusicConstants.USB_1_PORT_ID && null != mFristUsb) {
            mFristUsb.setScanState(MusicConstants.MEDIA_SCANNER_FINISHED);
            if (mBinding.fragmentSongList.firstUsbRlv.getVisibility() == View.VISIBLE) {
                mIsShowScanningComplete = true;
                stopLoadingAnimation();
            }
            new Handler().postDelayed(() -> {
                LogUtils.logD(TAG, "Handler().postDelayed::mFristScanSum");
                autoUpdateBufferMap();
                setInitAdapter(MusicConstants.UPDATE_ADAPTER_FIRST);
                mIsShowScanningComplete = false;
                if (null != mFristUsb) {
                    mFristUsb.setScanState(MusicConstants.MEDIA_SCANNER_DATA);
                }
                mFristScanSum = 0;

            }, MusicConstants.DELAY_HIDDEN_TIME);
            mUsbDataQuerying = 0;
        } else if (mUsbDataQuerying == MusicConstants.USB_2_PORT_ID && null != mSecondUsb) {
            mSecondUsb.setScanState(MusicConstants.MEDIA_SCANNER_FINISHED);
            if (mBinding.fragmentSongList.secondUsbRlv.getVisibility() == View.VISIBLE) {
                mIsShowScanningComplete = true;
                stopLoadingAnimation();
            }
            new Handler().postDelayed(() -> {
                autoUpdateBufferMap();
                setInitAdapter(MusicConstants.UPDATE_ADAPTER_SECOND);
                mIsShowScanningComplete = false;
                if (null != mSecondUsb) {
                    mSecondUsb.setScanState(MusicConstants.MEDIA_SCANNER_DATA);
                }
                mSecondScanSum = 0;

            }, MusicConstants.DELAY_HIDDEN_TIME);
            mUsbDataQuerying = 0;
        }
    }

    private void handleUsbDevicesChanged(List<UsbDevicesInfoBean> usbList) {
        LogUtils.logD(TAG, "handleUsbDevicesChanged :: invoke ");
        mUsbOnePath = "";
        mUsbTwoPath = "";
        mViewModel.mIsUsbLabelAvailable.set(usbList.size() == MusicConstants.USB_LABEL_MAX_NUMBER
                && !mIsShowLyric);
        for (UsbDevicesInfoBean device : usbList) {
            if (device.getPort().equals(MusicConstants.USB_1_PORT)) {
                if (null != mFristUsb && device.getUuid().equals(mFristUsb.getUuid())) {
                    LogUtils.logD(TAG, "handleUsbDevicesChanged:: mFristUsb same");
                    mUsbOnePath = mFristUsb.getPath();
                    mUsbOneUuid = mFristUsb.getUuid();
                } else {
                    LogUtils.logD(TAG, "handleUsbDevicesChanged:: mFristUsb diff");
                    mFristUsb = device;
                    mUsbOnePath = device.getPath();
                    mUsbOneUuid = device.getUuid();
                    if (mFristUsb.getScanState() == MusicConstants.MEDIA_SCANNER_FINISHED) {
                        mFristUsb.setScanState(MusicConstants.MEDIA_SCANNER_DATA);
                    }
                }
                LogUtils.logD(TAG, "handleUsbDevicesChanged :: USB_1_PORT :: mUsbOnePath"
                        + mUsbOnePath + "mUsbOneUuid ::" + mUsbOneUuid);
                if (null != device.getLabel()) {
                    mBinding.usbLabel.rbUsbLabelFirst.setText(device.getLabel());
                }
            } else if (device.getPort().equals(MusicConstants.USB_2_PORT)) {
                if (null != mSecondUsb && device.getUuid().equals(mSecondUsb.getUuid())) {
                    LogUtils.logD(TAG, "handleUsbDevicesChanged:: mSecondUsb same");
                    mUsbTwoPath = mSecondUsb.getPath();
                    mUsbTwoUuid = mSecondUsb.getUuid();
                } else {
                    LogUtils.logD(TAG, "handleUsbDevicesChanged:: mSecondUsb diff");
                    mSecondUsb = device;
                    mUsbTwoPath = device.getPath();
                    mUsbTwoUuid = device.getUuid();
                    if (mSecondUsb.getScanState() == MusicConstants.MEDIA_SCANNER_FINISHED) {
                        mSecondUsb.setScanState(MusicConstants.MEDIA_SCANNER_DATA);
                    }
                }
                LogUtils.logD(TAG, "handleUsbDevicesChanged :: USB_2_PORT :: mUsbTwoPath"
                        + mUsbTwoPath + "mUsbTwoUuid ::" + mUsbTwoUuid);
                if (null != device.getLabel()) {
                    mBinding.usbLabel.rbUsbLabelSecond.setText(device.getLabel());
                }
            }
        }
        if (!TextUtils.isEmpty(mUsbOnePath) && TextUtils.isEmpty(mUsbTwoPath)) {
            LogUtils.logD(TAG, "handleUsbDevicesChanged: mUsbOnePath has data");
            mSecondUsbSongListAdapter.clearDate();
            mSecondIsPlayAll = true;
            mSecondUsb = null;
            mBinding.usbLabel.rbUsbLabelSecond.setText(getString(R.string.second_usb_label_name));
            if (mIsShowLyric) {
                try {
                    AudioInfoBean bean = mViewModel.mUsbMusicManager.getCurrentPlayerMusic();
                    if (bean != null && bean.getAudioPath().contains(mUsbTwoPath)) {
                        LogUtils.logD(TAG, "handleUsbDevicesChanged: last in 2");
                        initLrc();
                    }
                    mShowListDelayed = MusicConstants.USB_1_INDEX;
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            } else {
                mViewModel.mUsbIndex.postValue(MusicConstants.USB_1_INDEX);
            }
            checkedUsbLabel(true, false);
            if (mIsUsbTwoListUp) {
                shrinkSongList();
            }
        } else if (TextUtils.isEmpty(mUsbOnePath) && !TextUtils.isEmpty(mUsbTwoPath)) {
            LogUtils.logD(TAG, "handleUsbDevicesChanged: mUsbTwoPath has data");
            mFirstUsbSongListAdapter.clearDate();
            mFristIsPlayAll = true;
            mFristUsb = null;
            mBinding.usbLabel.rbUsbLabelFirst.setText(getString(R.string.first_usb_label_name));
            if (mIsShowLyric) {
                try {
                    AudioInfoBean bean = mViewModel.mUsbMusicManager.getCurrentPlayerMusic();
                    if (bean != null && bean.getAudioPath().contains(mUsbOnePath)) {
                        LogUtils.logD(TAG, "handleUsbDevicesChanged: last in 1");
                        initLrc();
                    }
                    mShowListDelayed = MusicConstants.USB_2_INDEX;
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            } else {
                mViewModel.mUsbIndex.postValue(MusicConstants.USB_2_INDEX);
            }
            checkedUsbLabel(false, true);
            if (mIsUsbOneListUp) {
                shrinkSongList();
            }
        } else if (!TextUtils.isEmpty(mUsbOnePath) && !TextUtils.isEmpty(mUsbTwoPath)) {
            LogUtils.logD(TAG, "handleUsbDevicesChanged: both has data");
            try {
                AudioInfoBean bean = mViewModel.mUsbMusicManager.getCurrentPlayerMusic();
                if (bean != null) {
                    if (bean.getAudioPath().contains(mUsbOnePath)) {
                        if (mIsShowLyric) {
                            mShowListDelayed = MusicConstants.USB_1_INDEX;
                        } else {
                            mViewModel.mUsbIndex.postValue(MusicConstants.USB_1_INDEX);
                        }
                        checkedUsbLabel(true, false);
                        stopLoadingAnimation();
                    } else if (bean.getAudioPath().contains(mUsbTwoPath)) {
                        if (mIsShowLyric) {
                            mShowListDelayed = MusicConstants.USB_2_INDEX;
                        } else {
                            mViewModel.mUsbIndex.postValue(MusicConstants.USB_2_INDEX);
                        }
                        checkedUsbLabel(false, true);
                        stopLoadingAnimation();
                    }
                } else {
                    checkedUsbLabel(true, false);
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        if (usbList.size() == 0) {
            shrinkSongList();
            LogUtils.logD(TAG, "handleUsbDevicesChanged ::usbList.size == 0 ");
            mFirstUsbSongListAdapter.clearDate();
            mSecondUsbSongListAdapter.clearDate();
            stopLoadingAnimation();
            setDefaultAudioInfo();
            setEmptyAdapter(R.layout.usb_exception_view);
            mBinding.usbMusicBackFolderGroup.setVisibility(View.GONE);
            mBinding.songSearchGroup.setVisibility(View.GONE);
            mSecondScanSum = 0;
            mFristScanSum = 0;
            mShowListDelayed = -1;
            mFristUsb = null;
            mSecondUsb = null;
            mFristIsPlayAll = true;
            mSecondIsPlayAll = true;
            mUsbFirstHasData = false;
            mUsbSecondHasData = false;
            mIsShowLoading = false;
            mIsShowScanningComplete = false;
            mBinding.usbLabel.rbUsbLabelFirst.setText(getString(R.string.first_usb_label_name));
            mBinding.usbLabel.rbUsbLabelSecond.setText(getString(R.string.second_usb_label_name));
        }
        LogUtils.logD(TAG, "handleUsbDevicesChanged count: " + usbList.size()
                + " ,mUsbOnePath: " + mUsbOnePath + " ,mUsbTwoPath: " + mUsbTwoPath);
    }

    private void checkedUsbLabel(boolean firstLabel, boolean secondLabel) {
        mBinding.usbLabel.rbUsbLabelFirst.setChecked(firstLabel);
        mBinding.usbLabel.rbUsbLabelSecond.setChecked(secondLabel);
        mViewModel.mIsCheckedFirstUsbLabel.set(firstLabel);
        mViewModel.mIsCheckedSecondUsbLabel.set(secondLabel);
    }

    private void setDefaultAudioInfo() {
        LogUtils.logD(TAG, "setDefaultAudioInfo :: invoke ");
        if (null != getActivity()) {
            getActivity().runOnUiThread(() -> {
                LogUtils.logD(TAG, "setDefaultAudioInfo :: do ");
                mViewModel.clear();
                mWhetherToPlayAll = true;
                mViewModel.mAudioName.set(getString(R.string.no_music));
                mViewModel.mAudioNicName.set(getString(R.string.nick_name_default));
                mBinding.fragmentPlayer.albumCover.setImageResource(R.drawable.no_music);
                mViewModel.mCurrentTime.set(getString(R.string.player_time_default));
                mViewModel.mTotalTime.set(getString(R.string.player_time_default));
                mViewModel.mProgress.set(MusicConstants.DEFAULT_PLAYER_INDEX);
                mBinding.fragmentPlayer.musicSeekBar.setProgress(MusicConstants.DEFAULT_PROGRESS);
                mViewModel.mIsDataAvailable.set(false);
                mViewModel.mIsDataEnable.postValue(false);
                mViewModel.mIsPlayInfoAvailable.set(false);
                mViewModel.mIsPlayInfoEnable.postValue(false);
                mViewModel.mIsLryAvailable.set(false);
                mViewModel.mIsTouchSeekBar.set(false);
                mBinding.fragmentPlayer.musicSeekBar.setEnabled(false);
                mFirstUsbSongListAdapter.clearDate();
                mSecondUsbSongListAdapter.clearDate();
                mUsbAudioInfoBeans.clear();
                mViewModel.mNeedInit.postValue(true);
                if (mBinding.musicLrcGroup.getVisibility() == View.VISIBLE) {
                    mBinding.musicLrcGroup.setVisibility(View.GONE);
                    mBinding.fragmentSongListGroup.setVisibility(View.VISIBLE);
                    mIsShowLyric = false;
                }
                mViewModel.mPlayDrawable.set(mViewModel.getDrawable(R.drawable.play));
                if (null != mBufferAudioMap) {
                    mBufferAudioMap.clear();
                    mBufferAudioMap = null;
                }
                mViewModel.mFirstCurrentPath.postValue("");
                mViewModel.mSecondCurrentPath.postValue("");
            });
            LogUtils.logD(TAG, "mProgress:" + mBinding.fragmentPlayer.musicSeekBar
                    .getProgress());
        }
    }

    private void setCurrentPlayerInfo(SongListAdapter listAdapter, RecyclerView rlv) {
        try {
            if (null != mViewModel.mUsbMusicManager
                    && null != mViewModel.mUsbMusicManager.getCurrentPlayerMusic()) {
                AudioInfoBean audioInfoBean = mViewModel.mUsbMusicManager.getCurrentPlayerMusic();
                if (null != listAdapter && null != listAdapter.getData()
                        && listAdapter.getData().size() > 0) {
                    int currentPlayIndex = MusicUtils.getInstance().getCurrentPlayIndex(
                            listAdapter.getData(), audioInfoBean.getAudioId(),
                            audioInfoBean.getAudioPath());
                    LogUtils.logD(TAG, "setCurrentPlayerInfo->musicId："
                            + audioInfoBean.getAudioId() + " ,currentPlayIndex:"
                            + currentPlayIndex + " ,audioName:" + audioInfoBean.getAudioName());
                    listAdapter.setMusicPlayerManager(mViewModel.mUsbMusicManager.isPlaying(),
                            currentPlayIndex, audioInfoBean.getAudioId());
                    if (currentPlayIndex != -1) {
                        recycleScrolltoMiddle(rlv, currentPlayIndex);
                    }
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void recycleScrolltoMiddle(RecyclerView rlv, int position) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rlv.getLayoutManager();
        int offset = (rlv.getHeight() / 2) - (ADAPATER_ITEM_HEIGHT / 2);
        linearLayoutManager.scrollToPositionWithOffset(position, offset);
    }

    /**
     * show loading animation when scan start.
     */
    private void showLoadingAnimation() {
        mBinding.usbMusicLoadingGroup.setVisibility(View.VISIBLE);
        mBinding.usbMusicLoading.ivLoading.startAnimation(mLoadingAnimation);
        mIsShowLoading = true;
    }

    /**
     * stop loading animation when scan finish.
     */
    private void stopLoadingAnimation() {
        if (mViewModel.mIsInitQuerying) {
            return;
        }
        mBinding.usbMusicLoading.ivLoading.clearAnimation();
        mBinding.usbMusicLoadingGroup.setVisibility(View.GONE);
        mIsShowLoading = false;
    }

    private void shrinkSongList() {
        if (mIsUsbOneListUp || mIsUsbTwoListUp) {
            LogUtils.logD(TAG, "shrinkSongList: shrink");
            mConstraintUtil.reSetWidthAnim();
            mConstraintUtil = null;
            updateScrollLocation();
            mBinding.songListShrinkBtn.setVisibility(View.GONE);
            mBinding.songListBg.setVisibility(View.GONE);
            mBinding.fragmentPlayer.getRoot().setVisibility(View.VISIBLE);
            mBinding.songListSearch.getRoot().setVisibility(View.VISIBLE);
            if (mViewModel.mUsbIndex.getValue() == MusicConstants.USB_1_INDEX
                    && mFristUsb.getScanState() == MusicConstants.MEDIA_SCANNER_DATA
                    || mViewModel.mUsbIndex.getValue() == MusicConstants.USB_2_INDEX
                    && mSecondUsb.getScanState() == MusicConstants.MEDIA_SCANNER_DATA) {
                mBinding.songSearchGroup.setVisibility(View.VISIBLE);
            }
            mIsUsbOneListUp = false;
            mIsUsbTwoListUp = false;
            if (mFristUsb != null && mSecondUsb != null) {
                LogUtils.logD(TAG, "shrinkSongList: show usb label");
                mBinding.usbLabel.getRoot().setVisibility(View.VISIBLE);
                mViewModel.mIsUsbLabelAvailable.set(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.logD(TAG, "onDestroy :: invoke ");
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        sInstance = null;
    }
}
