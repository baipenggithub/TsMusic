package com.ts.music.ui.viewmodel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;


import com.ts.music.base.BaseApplication;
import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.callback.IBtMusicCallback;
import com.ts.sdk.media.constants.BtConstants;
import com.ts.sdk.media.contractinterface.IMediaServiceListener;
import com.ts.sdk.media.mananger.BaseManager;
import com.ts.sdk.media.mananger.BtMusicManager;
import com.ts.music.R;
import com.ts.music.action.BindingCommand;
import com.ts.music.action.SingleLiveEvent;
import com.ts.music.base.BaseRecyclerHolder;
import com.ts.music.base.BaseViewModel;
import com.ts.music.constants.MusicConstants;
import com.ts.music.ui.adapter.BtSongListAdapter;
import com.ts.music.utils.CommonUtils;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicClickControl;
import com.ts.music.utils.ToastUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Player Fragment ViewModel.
 */
public class BtMusicViewModel extends BaseViewModel implements BtSongListAdapter.OnClickListener {

    private static final String TAG = "BtMusicViewModel";
    private static final int TIME_ERROR_CODE = -1;
    private static final int MAX_SEEKBAR_PROGRESS = 100;
    private static final int MIN_SEEKBAR_PROGRESS = 0;
    private static final String TIME_FORMAT = "%02d:%02d";
    private MusicClickControl mMusicClickControl;
    private MediaController.TransportControls mTransportControls;
    private Context mContext;
    private Resources mResources;
    private BtMusicManager mBtMusicManager;
    private IMediaServiceListener mBtListener;
    private OnMusicPlayStateListener mListener;

    private int mAudioPlayTime = TIME_ERROR_CODE;
    private final MutableLiveData<String> mAudioName = new MutableLiveData<>();
    private final MutableLiveData<String> mNickName = new MutableLiveData<>();
    private final MutableLiveData<String> mAlbumName = new MutableLiveData<>();
    private final MutableLiveData<String> mTotalTime = new MutableLiveData<>();
    private final MutableLiveData<String> mCurrentTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> mTextColor = new MutableLiveData<>();
    private final MutableLiveData<Integer> mSeekBarProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsPlay = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsValidAudioName = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mSheetShowState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mListHasData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mBtConnectShowState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAAConnectState = new MutableLiveData<>();
    private final MutableLiveData<Drawable> mPlayModeDrawable = new MutableLiveData<>();
    private final MutableLiveData<String> mSumSongs = new MutableLiveData<>();
    private final MutableLiveData<String> mDeviceName = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsA2dpConnected = new MutableLiveData<>();
    private final MutableLiveData<Drawable> mAlbumCoverDrawable = new MutableLiveData<>();
    public SingleLiveEvent<List<AudioInfoBean>> mAudioData = new SingleLiveEvent<>();
    private boolean mCurrentPlayState = false;
    private boolean mIsAAConnected = false;
    private List<AudioInfoBean> mCurrentPlayList = new ArrayList<>();
    private AudioInfoBean mCurrentAudioInfo = null;

    /**
     * Constructor.
     */
    public BtMusicViewModel(@NonNull Application application) {
        super(application);
        mContext = application;
        mResources = mContext.getResources();
        mMusicClickControl = new MusicClickControl();
        mMusicClickControl.init(MusicConstants.CLICK_COUNTS, MusicConstants.FORMAT_SECOND_BT_MUSIC);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.logD(TAG, "onCreate");
        serviceConnectedInit();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        mContext.registerReceiver(mBtStateReceiver, intentFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtils.logD(TAG, "onStart");
        updateA2dpState();
        setBluetoothDeviceName(CommonUtils.getConnectedDevice());
    }

    @SuppressLint("NewApi")
    private void init() {
        setBluetoothDeviceName(CommonUtils.getConnectedDevice());
        setSeekBarProgress(MIN_SEEKBAR_PROGRESS);
        boolean isA2dpConnected = false;
        boolean isExistPlayList = false;
        if (mBtMusicManager != null) {
            try {
                mCurrentPlayList = mBtMusicManager.getCurrentPlayList();
                mCurrentPlayState = mBtMusicManager.getPlayState();
                if (mCurrentPlayList != null) {
                    isExistPlayList = mCurrentPlayList.size() > 0;
                }
                mCurrentAudioInfo = mBtMusicManager.getMetaData();
                isA2dpConnected = checkBluetoothStatus();
                mIsAAConnected = mBtMusicManager.getAAConnectionState();
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
        }

        if (mCurrentPlayList != null) {
            mAudioData.postValue(mCurrentPlayList);
            mSumSongs.postValue(String.format(mContext.getString(R.string.sum_song), mCurrentPlayList.size()));
        }

        updateA2dpState();
        setIsPlay(mCurrentPlayState);
        setListHasData(isExistPlayList);
        LogUtils.logD(TAG, "mCurrentAudioInfo:" + mCurrentAudioInfo + "/" + "  isA2dpConnected:" + isA2dpConnected + "  isAAConnected : " + mIsAAConnected);
        if (mCurrentAudioInfo == null || !isA2dpConnected) {
            setAudioName(mContext.getResources().getString(R.string.audio_name_default));
            setNickName("");
            setAlbumName("");
            setCurrentTime(mContext.getResources().getString(R.string.player_time_default));
            setTotalTime(mContext.getResources().getString(R.string.player_time_default));
            setAudioCoverDrawable(mContext.getDrawable(R.drawable.icon_songs_bg));
        } else {
            LogUtils.logD(TAG, "artist name : " + mCurrentAudioInfo.getAudioArtistName() + " audio name : " + mCurrentAudioInfo.getAudioName());
            mAudioPlayTime = Integer.parseInt(mCurrentAudioInfo.getAudioPlayTime());

            String audioName = mCurrentAudioInfo.getAudioName();
            String showAudioName = (audioName == null || Objects.equals("", audioName)) ? mContext.getResources().getString(R.string.audio_name_default) : audioName;
            setAudioName(showAudioName);

            String artistName = mCurrentAudioInfo.getAudioArtistName();
            String showArtistName = (artistName == null || Objects.equals("", artistName)) ? mContext.getResources().getString(R.string.nick_name_default) : artistName;
            setNickName(showArtistName);
            String albumName = mCurrentAudioInfo.getAudioAlbumName();
            String showAlbumName = (albumName == null || Objects.equals("", albumName)) ? mContext.getResources().getString(R.string.nick_name_default) : albumName;
            setAlbumName(showAlbumName);

            long progress = mCurrentAudioInfo.getLastPlayTime();
            setCurrentTime(timeFormatConversion(progress));
            setSeekBarProgress(progress);
            setTotalTime(timeFormatConversion(mAudioPlayTime));
            updateAudioCover(mCurrentAudioInfo);
            if (null != mListener) {
                mListener.onMusicPlayStateChanged(getIndexFromPlayList(mCurrentAudioInfo), mCurrentPlayState, showArtistName);
            }
        }
    }

    /**
     * Update audio cover drawable.
     */
    private void updateAudioCover(AudioInfoBean audioInfoBean) {
        LogUtils.logD(TAG, "audioCoverUrl : " + audioInfoBean.getAudioCover());
        if (audioInfoBean.getAudioCover() == null) {
            setAudioCoverDrawable(mContext.getDrawable(R.drawable.icon_songs_bg));
        } else {
            setAudioCoverDrawable(getDrawable(audioInfoBean.getAudioCover()));
        }
    }

    /**
     * Get audio cover drawable.
     */
    private Drawable getDrawable(String url) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(url);
        } catch (FileNotFoundException fileNotFoundException) {
            LogUtils.logD(TAG, "audio cover file not found");
            setAudioCoverDrawable(mContext.getDrawable(R.drawable.icon_songs_bg));
            fileNotFoundException.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Drawable drawable = new BitmapDrawable(mResources, bitmap);
        LogUtils.logD(TAG, "drawable : " + drawable);
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return drawable;
    }

    private void serviceConnectedInit() {
        try {
            mBtListener = new IMediaServiceListener() {
                @Override
                public void onServiceConnected(BaseManager baseManager) {
                    LogUtils.logD(TAG, "onServiceConnected====>" + baseManager);
                    mBtMusicManager = (BtMusicManager) baseManager;
                    if (mBtMusicManager == null) {
                        return;
                    }
                    try {
                        mBtMusicManager.registerAudioCallbackListenser(mBluetoothAudioInterface);
                        init();
                    } catch (RemoteException exception) {
                        exception.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected() {
                    LogUtils.logD(TAG, "====onServiceDisconnected====");
                }

            };
            BtMusicManager.getInstance(BaseApplication.getApplication(), mBtListener);
        } catch (IllegalStateException exception) {
            exception.printStackTrace();
            LogUtils.logD(TAG, "onServiceDisconnected::" + exception.toString());
        }
    }

    // Play the previous song
    public BindingCommand playerLastMusic = new BindingCommand(() -> {
        if (mBtMusicManager == null || !checkBluetoothStatus() || !mMusicClickControl.isCanTrigger() || dataIsEmpty()) {
            LogUtils.logD(TAG, "Not clickable");
            return;
        }
        try {
            LogUtils.logD(TAG, "Play the previous song");
            mBtMusicManager.prev();
        } catch (RemoteException exception) {
            LogUtils.logD(TAG, "Play the previous song exception :"
                    + exception.toString());
            exception.printStackTrace();
        }
    });

    // Start pause
    public BindingCommand playerPlayOrPause = new BindingCommand(this::playOrPause);

    private void playOrPause() {
        LogUtils.logD(TAG, "playOrPause");
        if (mBtMusicManager == null || !checkBluetoothStatus() || !mMusicClickControl.isCanTrigger() || dataIsEmpty()) {
            LogUtils.logD(TAG, "Not clickable");
            return;
        }
        try {
            boolean state = mBtMusicManager.getPlayState();
            LogUtils.logD(TAG, "Play or pause : state ==" + state);
            if (!state) {
                if (mBtMusicManager.play()) {
                    LogUtils.logD(TAG, "Music play");
                } else {
                    LogUtils.logD(TAG, "Music play fail");
                    ToastUtils.showShort(R.string.music_playback_error);
                }
            } else {
                if (mBtMusicManager.pause()) {
                    LogUtils.logD(TAG, "Music pause");
                } else {
                    LogUtils.logD(TAG, "Music pause fail");
                    ToastUtils.showShort(R.string.music_playback_error);
                }
            }
        } catch (RemoteException exception) {
            exception.printStackTrace();
        }
    }

    // Play next
    public BindingCommand playerNext = new BindingCommand(() -> {
        LogUtils.logD(TAG, "Play next");
        if (mBtMusicManager == null || !checkBluetoothStatus() || !mMusicClickControl.isCanTrigger() || dataIsEmpty()) {
            LogUtils.logD(TAG, "Not clickable");
            return;
        }
        try {
            mBtMusicManager.next();
        } catch (RemoteException exception) {
            exception.printStackTrace();
        }
    });

    // Playback btConnect
    public BindingCommand btConnect = new BindingCommand(() -> {
        LogUtils.logD(TAG, "Playback btConnect");
        if (!mMusicClickControl.isCanTrigger()) {
            LogUtils.logD(TAG, "Not clickable");
            return;
        }
        CommonUtils.startSettingBt(mContext);
    });

    private final IBtMusicCallback mBluetoothAudioInterface = new IBtMusicCallback.Stub() {

        @Override
        public void onServiceStateChanged(boolean connected) {
            LogUtils.logD(TAG, "onServiceStateChanged == " + connected);
        }

        @Override
        public void onPlayProgressChanged(long progress, AudioInfoBean audioInfoBean) {
            LogUtils.logD(TAG, "progress== " + progress
                    + "mAudioPlayTime== " + mAudioPlayTime);
            if (progress < 0) {
                LogUtils.logE(TAG, "Play Progress Changed Exception");
                return;
            }
            setSeekBarProgress(progress);
            setCurrentTime(timeFormatConversion(progress));
        }

        @Override
        public void onPlayStateChanged(int state) {
            LogUtils.logD(TAG, "onPlayStateChanged == " + state);
            mCurrentPlayState = state == MusicConstants.PLAYER_STATUS_START;
            setIsPlay(mCurrentPlayState);
            updatePlayList();
        }

        @Override
        public void onMetadataChanged(AudioInfoBean metadata) {
            if (null == metadata) {
                LogUtils.logD(TAG, "metadata is null");
                return;
            }
            mCurrentAudioInfo = metadata;
            LogUtils.logD(TAG, "metadata" + metadata.getAudioName());
            updateUi(metadata);
        }

        @Override
        public void onPlayPositionChanged(int position) {
            LogUtils.logD(TAG, "onPlayPositionChanged == " + position);
        }

        @Override
        public void onConnectionStateChanged(int state) {
            LogUtils.logD(TAG, "onConnectionStateChanged == " + state);
            switch (state) {
                case BluetoothProfile.STATE_CONNECTED:
                    setIsPlay(false);
                    init();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    mCurrentAudioInfo = null;
                    // restore initial value.
                    init();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onMediaItemListRetrieved(int sum, List<AudioInfoBean> list)
                throws RemoteException {
            LogUtils.logD(TAG, "onMediaItemListRetrieved sum : " + sum);
            if (sum <= 0 || list == null || list.size() == 0) {
                setListHasData(false);
            } else {
                LogUtils.logD(TAG, "update play list");
                setListHasData(true);
                mCurrentPlayList = list;
                mSumSongs.postValue(String.format(mContext.getString(R.string.sum_song), sum));
                mAudioData.postValue(list);
                updatePlayList();
            }

        }

        @Override
        public void onPlayerModeChanged(int playMode, boolean random) {
            //Do nothing.
        }

        @Override
        public void onMediaBrowserConnected() {
            LogUtils.logD(TAG, "onMediaBrowserConnected");
            initUi();
        }

        @Override
        public void onAAConnected(int type, boolean state) throws RemoteException {
            LogUtils.logD(TAG, "onAAConnected state : " + state);
            mIsAAConnected = state;
            updateA2dpState();
        }

        private void initUi() {
            if (mBtMusicManager == null) {
                return;
            }
            AudioInfoBean metadataItem = null;
            try {
                metadataItem = mBtMusicManager.getMetaData();
                if (null == metadataItem) {
                    LogUtils.logD(TAG, "metadata is null");
                    return;
                }
                LogUtils.logD(TAG, "initUi" + metadataItem.toString());
                if (metadataItem.getAudioName() == null || metadataItem.getAudioArtistName() == null) {
                    setAudioName(mContext.getResources().getString(R.string.audio_name_default));
                    setNickName(mContext.getResources().getString(R.string.nick_name_default));
                    setAlbumName(mContext.getResources().getString(R.string.nick_name_default));
                    // setmTextColor(mContext.getColor(R.color.light_grey));
                } else {
                    setAudioName(metadataItem.getAudioName());
                    setNickName(metadataItem.getAudioArtistName());
                    setAlbumName(metadataItem.getAudioAlbumName());
                }
                updateAudioCover(metadataItem);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
        }
    };

    private void updatePlayList() {
        LogUtils.logD(TAG, "updatePlayList");
        try {
            if (mCurrentAudioInfo == null && mBtMusicManager != null) {
                mCurrentAudioInfo = mBtMusicManager.getMetaData();
            }

        } catch (RemoteException exception) {
            exception.printStackTrace();
        }

        if (mCurrentAudioInfo != null && null != mListener) {
            mListener.onMusicPlayStateChanged(getIndexFromPlayList(mCurrentAudioInfo), mCurrentPlayState, getNickName().getValue());
        }
    }

    private void updateUi(AudioInfoBean metadata) {
        LogUtils.logD(TAG, "audio name : " + metadata.getAudioName());
        mAudioPlayTime = Integer.parseInt(metadata.getAudioPlayTime()) == TIME_ERROR_CODE
                ? 0 : Integer.parseInt(metadata.getAudioPlayTime());
        updateAudioCover(metadata);
        if (mAudioPlayTime < 0 || metadata.getAudioName() == null) {
            LogUtils.logD(TAG, "metadata is exception");
            return;
        }
        if (!Objects.equals(mAudioName.getValue(), metadata.getAudioName())) {
            setAudioName((metadata.getAudioName() == null || metadata.getAudioName().isEmpty()) ? mResources.getString(R.string.audio_name_default) : metadata.getAudioName());
        }
        LogUtils.logD(TAG, "Artist name : " + metadata.getAudioArtistName()
                + "  Album name : " + metadata.getAudioAlbumName());
        setNickName(((metadata.getAudioArtistName() == null) || metadata.getAudioArtistName().equals("")) ? mContext.getResources().getString(R.string.nick_name_default) : (metadata.getAudioArtistName()));
        setTotalTime(timeFormatConversion(mAudioPlayTime));
        setAlbumName(((metadata.getAudioAlbumName() == null) || metadata.getAudioAlbumName().equals("")) ? mContext.getResources().getString(R.string.nick_name_default) : (metadata.getAudioAlbumName()));
        if (null != mListener) {
            mListener.onMusicPlayStateChanged(getIndexFromPlayList(metadata),
                    mCurrentPlayState, getNickName().getValue());
        }
    }

    private int getIndexFromPlayList(AudioInfoBean metadata) {
        for (AudioInfoBean audioInfoBean : mCurrentPlayList) {
            if (audioInfoBean.getAudioName().equals(metadata.getAudioName())) {
                return mCurrentPlayList.indexOf(audioInfoBean);
            }
        }
        return TIME_ERROR_CODE;
    }

    @SuppressLint("DefaultLocale")
    private String timeFormatConversion(long time) {
        return String.format(TIME_FORMAT, TimeUnit.MILLISECONDS.toMinutes(time), TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    private Boolean checkBluetoothStatus() {
        if (mBtMusicManager == null) {
            return false;
        }
        try {
            int state = mBtMusicManager.getBluetoothState();
            LogUtils.logD(TAG, "checkBluetoothStatus state  " + state);
            if (state == BtConstants.BT_STATE_CONNECTED) {
                LogUtils.logD(TAG, "Bt connected");
                return true;
            } else {
                LogUtils.logD(TAG, "Bt disconnected");
                return false;
            }
        } catch (RemoteException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private Boolean dataIsEmpty() {
        if (getAudioName().getValue() == null || getNickName().getValue() == null) {
            LogUtils.logD(TAG, "data is null");
            return true;
        }
        return getAudioName().getValue().equals(mContext.getResources().getString(R.string.audio_name_default)) || getNickName().equals(mContext.getResources().getString(R.string.nick_name_default));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.logD(TAG, "onDestroy");
        mContext.unregisterReceiver(mBtStateReceiver);
        try {
            if (mBtMusicManager != null) {
                mBtMusicManager.unRegisterAudioCallbackListenser(mBluetoothAudioInterface);
                mBtMusicManager.release();
            }
        } catch (RemoteException exception) {
            exception.printStackTrace();
        }
    }

    //set text color
//    public void setmTextColor(Integer textColor) {
//        mTextColor.postValue(textColor);
//    }

    //get text color
    public MutableLiveData<Integer> getTextColor() {
        return mTextColor;
    }

    // Get audio name valid status.
    public MutableLiveData<Boolean> getIsValidAudioName() {
        return mIsValidAudioName;
    }

    // Set audio name valid status.
    public void setIsValidAudioName(Boolean isValidAudioName) {
        if (!isValidAudioName) {
            setIsPlay(false);
        }
        mIsValidAudioName.postValue(isValidAudioName);
    }

    // Get audio name.
    public MutableLiveData<String> getAudioName() {
        return mAudioName;
    }

    /**
     * Set audio name.
     */
    public void setAudioName(String audioName) {
        LogUtils.logD(TAG, "setAudioName audio name : " + audioName);
        setIsValidAudioName((mResources.getString(
                R.string.audio_name_default).equals(audioName) || mResources.getString(
                R.string.bt_music_not_provided).equals(audioName)) ? false : true);
        mAudioName.postValue(audioName);
    }

    // Get nick name.
    public MutableLiveData<String> getNickName() {
        return mNickName;
    }

    public MutableLiveData<String> getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName.postValue(albumName);
    }

    // Set nick name.
    public void setNickName(String nickName) {
        mNickName.postValue(nickName);
    }

    // Get seekBar progress.
    public MutableLiveData<Integer> getSeekBarProgress() {
        return mSeekBarProgress;
    }

    // Set seekBar progress.
    public void setSeekBarProgress(long seekBarProgress) {
        mSeekBarProgress.postValue((int) (((double) seekBarProgress / (double) mAudioPlayTime)
                * MAX_SEEKBAR_PROGRESS));
    }

    // Get play state.
    public MutableLiveData<Boolean> getIsPlay() {
        return mIsPlay;
    }

    // Set play state.
    public void setIsPlay(Boolean isPlay) {
        mIsPlay.postValue(isPlay);
    }

    // Get total time.
    public MutableLiveData<String> getTotalTime() {
        return mTotalTime;
    }

    // Set total time.
    public void setTotalTime(String totalTime) {
        mTotalTime.postValue(totalTime);
    }

    // Get Current time.
    public MutableLiveData<String> getCurrentTime() {
        return mCurrentTime;
    }

    // Set Current time.
    public void setCurrentTime(String currentTime) {
        mCurrentTime.postValue(currentTime);
    }

    public MutableLiveData<Boolean> getAAConnectionState() {
        return mAAConnectState;
    }

    public void setAAConnectState(boolean aaConnectState) {
        mAAConnectState.postValue(aaConnectState);
    }

    // Get bt connect state.
    public MutableLiveData<Boolean> getBtConnectShowState() {
        return mBtConnectShowState;
    }

    // Set bt connect state.
    public void setBtConnectShowState(boolean state) {
        mBtConnectShowState.postValue(state);
    }

    // Get album cover.
    public MutableLiveData<Drawable> getAudioCoverDrawable() {
        return mAlbumCoverDrawable;
    }

    // Set album cover.
    public void setAudioCoverDrawable(Drawable albumCoverDrawable) {
        mAlbumCoverDrawable.postValue(albumCoverDrawable);
    }

    /**
     * Get a2dp connect state.
     *
     * @return a2dp connect state
     */
    public MutableLiveData<Boolean> getA2dpConnectedState() {
        return mIsA2dpConnected;
    }

    /**
     * Set a2dp connect state.
     */
    public void setA2dpConnectedState(boolean state) {
        mIsA2dpConnected.postValue(state);
    }

    // Get sheet show state.
    public MutableLiveData<Boolean> getSheetShowState() {
        return mSheetShowState;
    }

    // Set sheet show state.
    public void setSheetShowState(Boolean sheetShowState) {
        mSheetShowState.postValue(sheetShowState);
    }

    /**
     * Get bluetooth device name.
     *
     * @return bluetooth device name
     */
    public MutableLiveData<String> getBluetoothDeviceName() {
        return mDeviceName;
    }

    /**
     * Add mutually exclusive for only showing one of play list
     * or a2dp prompt or bt connection prompt.
     */
    private void updateA2dpState() {
        LogUtils.logD(TAG, "updateA2dpState aa connection state : " + mIsAAConnected);
        if (mIsAAConnected) {
            setA2dpConnectedState(false);
            setBtConnectShowState(true);
            setSheetShowState(false);
            setAAConnectState(true);
        } else if (checkBluetoothStatus()) {
            setA2dpConnectedState(false);
            setBtConnectShowState(true);
            setAAConnectState(false);
            setSheetShowState(true);
        } else if (CommonUtils.isExistConnectDevices()) {
            setBtConnectShowState(true);
            setSheetShowState(false);
            setAAConnectState(false);
            setA2dpConnectedState(true);
        } else {
            setA2dpConnectedState(false);
            setSheetShowState(false);
            setAAConnectState(false);
            setBtConnectShowState(false);
        }
    }

    /**
     * Set bluetooth device name.
     *
     * @param connectedDevice connected bluetooth device
     */
    @SuppressLint("MissingPermission")
    public void setBluetoothDeviceName(BluetoothDevice connectedDevice) {
        String deviceName = null;
        if (connectedDevice != null && !connectedDevice.getName().isEmpty()) {
            LogUtils.logD(TAG, "setBluetoothDeviceName :" + connectedDevice.getName());
            deviceName = connectedDevice.getName();
        } else {
            deviceName = mContext.getString(R.string.default_bt_device_name);
        }
        mDeviceName.postValue(deviceName);
    }

    public MutableLiveData<Drawable> getPlayModeDrawable() {
        return mPlayModeDrawable;
    }

    public void setPlayModeDrawable(Drawable playModeDrawable) {
        mPlayModeDrawable.postValue(playModeDrawable);
    }

    public MutableLiveData<String> getSumSongs() {
        return mSumSongs;
    }

    public void setListHasData(boolean isHave) {
        mListHasData.postValue(isHave);
    }

    public MutableLiveData<Boolean> getListHasData() {
        return mListHasData;
    }

    private final BroadcastReceiver mBtStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            LogUtils.logD(TAG, "state======" + state);
            if (state == BluetoothAdapter.STATE_CONNECTED || state == BluetoothAdapter.STATE_DISCONNECTED) {
                setBluetoothDeviceName(device);
                updateA2dpState();
            }
        }
    };

    @Override
    public void play(BaseRecyclerHolder helper, AudioInfoBean resultBean, int position) {
        LogUtils.logD(TAG, "play media id : " + resultBean.getUserId());
        if (mBtMusicManager == null || !checkBluetoothStatus()
                || !mMusicClickControl.isCanTrigger() || dataIsEmpty()) {
            LogUtils.logD(TAG, "Not clickable");
            return;
        }
        String mediaId = resultBean.getUserId();
        if (!Objects.equals(mediaId, null)) {
            try {
                mBtMusicManager.playFromMediaId(mediaId);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void setMusicPlayStateListener(OnMusicPlayStateListener listener) {
        mListener = listener;
    }

    public interface OnMusicPlayStateListener {
        void onMusicPlayStateChanged(int position, boolean isPlaying, String artistName);
    }
}
