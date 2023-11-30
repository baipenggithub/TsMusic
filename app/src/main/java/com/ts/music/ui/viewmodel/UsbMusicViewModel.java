package com.ts.music.ui.viewmodel;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ts.sdk.media.bean.AudioInfoBean;

import com.ts.music.R;
import com.ts.music.action.BindingCommand;
import com.ts.music.action.SingleLiveEvent;
import com.ts.music.base.BaseApplication;
import com.ts.music.base.BaseViewModel;
import com.ts.music.constants.MusicConstants;
import com.ts.music.entity.RecordAudioInfo;
import com.ts.music.manager.SearchAudioInfoManager;
import com.ts.music.preferences.SharedPreferencesHelps;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicClickControl;
import com.ts.music.utils.MusicUtils;
import com.ts.music.utils.ToastUtils;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;
import com.ts.sdk.media.callback.IMusicPlayerEventListener;
import com.ts.sdk.media.callback.IMusicPlayerInfoListener;
import com.ts.sdk.media.callback.IUsbDevicesListener;
import com.ts.sdk.media.callback.IUsbMusicCallback;
import com.ts.sdk.media.contractinterface.IMediaServiceListener;
import com.ts.sdk.media.mananger.BaseManager;
import com.ts.sdk.media.mananger.UsbMusicManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Player Fragment ViewModel.
 */
public class UsbMusicViewModel extends BaseViewModel {

    private static final String TAG = UsbMusicViewModel.class.getSimpleName();

    public UsbMusicManager mUsbMusicManager;
    private MusicClickControl mMusicClickControl;
    private RecordAudioInfo mRecordAudioInfo;

    public SingleLiveEvent<List<AudioInfoBean>> mAudioData = new SingleLiveEvent<>();
    public SingleLiveEvent<List<UsbDevicesInfoBean>> mUsbDevices = new SingleLiveEvent<>();
    private SingleLiveEvent<Boolean> mIsConnected = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> mNotifyDataSetChanged = new SingleLiveEvent();
    public SingleLiveEvent<Boolean> mInitLrcView = new SingleLiveEvent();
    public SingleLiveEvent<String> mAlbumCoverDrawable = new SingleLiveEvent<>();
    public SingleLiveEvent mIsLrcViewPause = new SingleLiveEvent();
    public SingleLiveEvent<Long> mLrcViewUpdate = new SingleLiveEvent();
    public SingleLiveEvent<Boolean> mIsFolder = new SingleLiveEvent();

    public ObservableBoolean mIsDataAvailable = new ObservableBoolean(false);
    public ObservableBoolean mIsLryAvailable = new ObservableBoolean(false);
    public SingleLiveEvent<Boolean> mIsPlayInfoEnable = new SingleLiveEvent();
    public SingleLiveEvent<Boolean> mIsDataEnable = new SingleLiveEvent();
    public ObservableBoolean mIsUsbLabelAvailable = new ObservableBoolean(false);
    public ObservableBoolean mIsCheckedFirstUsbLabel = new ObservableBoolean(true);
    public ObservableBoolean mIsCheckedSecondUsbLabel = new ObservableBoolean(false);
    public ObservableBoolean mIsPlayInfoAvailable = new ObservableBoolean(false);
    public ObservableField<String> mAudioName = new ObservableField<>(
            getString(R.string.no_music));
    public ObservableField<String> mAudioNicName = new ObservableField<>(
            getString(R.string.nick_name_default));
    public SingleLiveEvent<String> mAudioSum = new SingleLiveEvent<>();
    public SingleLiveEvent<String> mListTittle = new SingleLiveEvent<>();
    public ObservableField<String> mCurrentTime = new ObservableField(
            getString(R.string.player_time_default));
    public ObservableField<String> mTotalTime = new ObservableField(
            getString(R.string.player_time_default));
    public ObservableField<Drawable> mPlayDrawable = new ObservableField(
            getDrawable(R.drawable.play));
    public ObservableField<Drawable> mLyricDrawable = new ObservableField(
            getDrawable(R.drawable.sheet));
    public ObservableField<Drawable> mModelDrawable = new ObservableField(
            getDrawable(R.drawable.list_loop_click));
    public ObservableInt mProgress = new ObservableInt();
    public ObservableInt mMaxProgress = new ObservableInt(MusicConstants.MAX_PROGRESS);
    public ObservableBoolean mIsTouchSeekBar = new ObservableBoolean(false);
    public SingleLiveEvent<Integer> mUsbIndex = new SingleLiveEvent();
    private int mIndex = -1;
    public ObservableField<String> mSeekTip =
            new ObservableField(getString(R.string.player_time_default));
    public SingleLiveEvent<String> mFirstCurrentPath = new SingleLiveEvent();
    public SingleLiveEvent<String> mSecondCurrentPath = new SingleLiveEvent();
    public SingleLiveEvent<Boolean> mNeedInit = new SingleLiveEvent();
    public SingleLiveEvent<String> mToastMessage = new SingleLiveEvent();
    private AudioInfoBean mLastAudio;
    private int mLastPosition = -1;
    private boolean mNeedAddListener = false;
    private IMediaServiceListener mListener;
    public boolean mIsInitQuerying = false;

    /**
     * Constructor.
     */
    public UsbMusicViewModel(@NonNull Application application) {
        super(application);
        LogUtils.logD(TAG, "UsbMusicViewModel :: invoke");
        mMusicClickControl = new MusicClickControl();
        mMusicClickControl.init(MusicConstants.CLICK_COUNTS, MusicConstants.CURRENT_DURATION);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.logD(TAG, "onCreate :: invoke");
        mNeedInit.postValue(true);
        mListener = new IMediaServiceListener() {
            @Override
            public void onServiceConnected(BaseManager baseManager) {
                try {
                    LogUtils.logD(TAG, "onServiceConnected  ::  invoke");
                    mUsbMusicManager = (UsbMusicManager) baseManager;
                    mIsConnected.postValue(true);
                    mUsbDevices.postValue(mUsbMusicManager.getUsbDevices());
                    mUsbMusicManager.registerUsbDevicesStatusObserver(mUsbDevicesListener);
                    mUsbMusicManager.getAllAudio();
                    mIsInitQuerying = true;
                    mUsbMusicManager.registerVideoStatusObserver(mUsbMusicCallback);
                    int playModel = mUsbMusicManager.getPlayerMode();
                    if (playModel > MusicConstants.PLAYER_STATUS_DESTROY) {
                        mModelDrawable.set(getDrawable(MusicUtils.getInstance()
                                .getPlayerModelToWhiteRes(playModel)));
                    }
                    addPlayerEventListener();
                } catch (RemoteException | SecurityException | IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected() {
                LogUtils.logD(TAG, "onServiceDisconnected :: invoke");
                mIsConnected.postValue(false);
            }
        };
        mUsbMusicManager = UsbMusicManager.getInstance(BaseApplication.getApplication(), mListener);
    }

    public UiChangeObservable uiChangeObservable = new UiChangeObservable();

    public static class UiChangeObservable {
        public SingleLiveEvent playerLyricEvent = new SingleLiveEvent<>();
        public SingleLiveEvent songSearchEvent = new SingleLiveEvent<>();
        public SingleLiveEvent playerPlayOrPauseEvent = new SingleLiveEvent<>();
        public SingleLiveEvent playAllEvent = new SingleLiveEvent<>();
        public SingleLiveEvent folderBackEvent = new SingleLiveEvent<>();
    }

    // Back to folder
    public BindingCommand folderBackCommand = new BindingCommand(() -> {
        LogUtils.logD(TAG, "folderBackCommand :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            LogUtils.logD(TAG, "folderBackCommand :: do");
            uiChangeObservable.folderBackEvent.call();
        }
    });

    // Playback mode
    public BindingCommand playerModel = new BindingCommand(() -> {
        LogUtils.logD(TAG, "playerModel :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            try {
                LogUtils.logD(TAG, "playerModel :: do");
                mUsbMusicManager.changedPlayerPlayMode();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    });

    // Playback lyric
    public BindingCommand playerLyric = new BindingCommand(() -> {
        LogUtils.logD(TAG, "playerLyric :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            LogUtils.logD(TAG, "playerLyric :: do");
            uiChangeObservable.playerLyricEvent.call();
            mIsLryAvailable.set(!mIsLryAvailable.get());
            updateLyricButton(!mIsLryAvailable.get() && mIsDataAvailable.get());
        }
    });

    // Play last
    public BindingCommand playerLastMusic = new BindingCommand(() -> {
        LogUtils.logD(TAG, "playerLastMusic :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            try {
                LogUtils.logD(TAG, "playerLastMusic :: do");
                mUsbMusicManager.playLastMusic();
                mInitLrcView.postValue(true);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    });

    // Play Or Pause
    public BindingCommand playerPlayOrPause = new BindingCommand(() -> {
        LogUtils.logD(TAG, "playerPlayOrPause :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            LogUtils.logD(TAG, "playerPlayOrPause :: do");
            uiChangeObservable.playerPlayOrPauseEvent.call();
        }
    });

    // Play next
    public BindingCommand playerNext = new BindingCommand(() -> {
        LogUtils.logD(TAG, "playerNext :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            try {
                LogUtils.logD(TAG, "playerNext :: do");
                mUsbMusicManager.playNextMusic();
                mInitLrcView.postValue(true);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    });

    // Play all
    public BindingCommand playerAll = new BindingCommand(() -> {
        LogUtils.logD(TAG, "playerAll :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            LogUtils.logD(TAG, "playerAll :: do");
            uiChangeObservable.playAllEvent.call();
        }
    });

    // Play search
    public BindingCommand songSearch = new BindingCommand(() -> {
        LogUtils.logD(TAG, "songSearch :: invoke");
        if (mMusicClickControl.isCanTrigger()) {
            LogUtils.logD(TAG, "songSearch :: do");
            uiChangeObservable.songSearchEvent.call();
        }
    });

    // Switch usb label
    public BindingCommand<Integer> switchUsbLabel = new BindingCommand(value -> {
        LogUtils.logD(TAG, "switchUsbLabel :: invoke:: value" + value);
        if ((int) value == R.id.rb_usb_label_first) {
            mUsbIndex.postValue(MusicConstants.USB_1_INDEX);
            mIsCheckedFirstUsbLabel.set(true);
            mIsCheckedSecondUsbLabel.set(false);
        } else if ((int) value == R.id.rb_usb_label_second) {
            mUsbIndex.postValue(MusicConstants.USB_2_INDEX);
            mIsCheckedFirstUsbLabel.set(false);
            mIsCheckedSecondUsbLabel.set(true);
        }
    });

    public Drawable getDrawable(int res) {
        return BaseApplication.getApplication().getResources().getDrawable(res);
    }

    private String getString(int res) {
        return BaseApplication.getApplication().getResources().getString(res);
    }

    /**
     * Start player info.
     */
    public void startPlayAudio(List<AudioInfoBean> audioInfoBeans, int index, boolean isList) {
        LogUtils.logD(TAG, "startPlayAudio ::invoke ");
        List<AudioInfoBean> playList = getFilterAudioList(audioInfoBeans);
        if ((null == playList || playList.size() == 0) && isList) {
            LogUtils.logD(TAG, "startPlayAudio :: playList：0");
            return;
        }
        try {
            if (null != mUsbMusicManager.getCurrentPlayerMusic()) {
                if (isList) {
                    LogUtils.logD(TAG, "startPlayAudio-->isList：" + isList);
                    mUsbMusicManager.startPlayMusic(playList, index);
                } else {
                    LogUtils.logD(TAG, "startPlayAudio-->isList：" + isList);
                    mUsbMusicManager.playOrPause();
                }
            } else {
                LogUtils.logD(TAG, "startPlayAudio-->");
                addPlayerEventListener();
                mUsbMusicManager.startPlayMusic(playList, index);
            }
            if (mIndex != index) {
                mInitLrcView.postValue(true);
            }
            mIndex = index;
            mNotifyDataSetChanged.postValue(true);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private List<AudioInfoBean> getFilterAudioList(List<AudioInfoBean> audioInfoBeans) {
        List<AudioInfoBean> filterAudioInfo = new ArrayList<>();
        if (null != audioInfoBeans && audioInfoBeans.size() > 0) {
            for (AudioInfoBean infoBean : audioInfoBeans) {
                if (infoBean.getAudioId() != MusicConstants.AUDIO_FOLDER_ID) {
                    filterAudioInfo.add(infoBean);
                }
            }
        }
        return filterAudioInfo;
    }

    private void addPlayerEventListener() throws RemoteException {
        if (null != mUsbMusicManager) {
            mUsbMusicManager.setPlayInfoListener(mInfoListener);
            mUsbMusicManager.addOnPlayerEventListener(mStub);
        }
    }

    /**
     * Clear service playback data.
     */
    public void clear() {
        LogUtils.logD(TAG, "clear :: invoke ");
        try {
            clearData();
            mUsbMusicManager.onDestroy();
        } catch (RemoteException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Destroy.
     */
    public void onDestroy() {
        LogUtils.logD(TAG, "onDestroy :: invoke");
        if (mUsbMusicManager != null) {
            try {
                if (mStub != null) {
                    mUsbMusicManager.removePlayerListener(mStub);
                }
                if (mListener != null) {
                    mUsbMusicManager.removeMediaServiceListener(mListener);
                }
                if (mUsbDevicesListener != null) {
                    mUsbMusicManager.unRegisterUsbDevicesStatusObserver(mUsbDevicesListener);
                }
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
            mUsbMusicManager.release();
            mUsbMusicManager = null;
        }
    }

    private IUsbDevicesListener mUsbDevicesListener = new IUsbDevicesListener.Stub() {
        @Override
        public void onUsbDevicesChange(List<UsbDevicesInfoBean> usbDevices) {
            LogUtils.logD(TAG, "onUsbDevicesChange usb devices count: " + usbDevices.size());
            SearchAudioInfoManager.getInstance().notifyUsbDevicesChanged(usbDevices);
            try {
                if (usbDevices.size() == 0) {
                    LogUtils.logD(TAG, "onUsbDevicesChange removePlayerListener");
                    if (mUsbMusicManager != null) {
                        mUsbMusicManager.removePlayerListener(mStub);
                        mNeedAddListener = true;
                    }
                } else if (usbDevices.size() > 0 && mNeedAddListener) {
                    LogUtils.logD(TAG, "onUsbDevicesChange addOnPlayerEventListener");
                    if (mUsbMusicManager != null) {
                        mUsbMusicManager.addOnPlayerEventListener(mStub);
                        mNeedAddListener = false;
                    }
                }
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onScanStateChange(int state, String deviceId, int portId) {
            LogUtils.logD(TAG, "onScanStateChange state: " + state + " deviceId: "
                    + deviceId + " portId: " + portId);
            SearchAudioInfoManager.getInstance().notifyScanState(state, deviceId, portId);
        }
    };

    private IUsbMusicCallback mUsbMusicCallback = new IUsbMusicCallback.Stub() {
        @Override
        public void onAudioStateChange(AudioInfoBean audioInfoBean) {
            // TODO Audio state change
        }

        @Override
        public void addAudio(AudioInfoBean audioInfoBean) {
            // TODO
        }

        @Override
        public void startPlayMusic(AudioInfoBean audioInfoBean) {
            // TODO Start Play Music
        }

        @Override
        public void onAudioQueryCompleted(List<AudioInfoBean> list) {
            mIsInitQuerying = false;
            mAudioData.postValue(list);
        }

        @Override
        public void onScanListUpdated(List<AudioInfoBean> list) {
            LogUtils.logD(TAG, "onScanListUpdated：" + list.size());
            SearchAudioInfoManager.getInstance().addScanAudioInfo(list);
        }
    };

    private IMusicPlayerInfoListener.Stub mInfoListener = new IMusicPlayerInfoListener.Stub() {
        @Override
        public void onPlayMusicInfo(AudioInfoBean audioInfoBean, int index) {
            mInitLrcView.postValue(true);
            LogUtils.logD(TAG, "audioId：" + audioInfoBean.getAudioId() + ",index：" + index);
        }
    };

    private IMusicPlayerEventListener.Stub mStub = new IMusicPlayerEventListener.Stub() {
        @Override
        public void onMusicPlayerState(int playerState, String message) {
            LogUtils.logD(TAG, "playerState: " + playerState + " ,message: " + message);
            switch (playerState) {
                case MusicConstants.MUSIC_PLAYER_PLAYING:
                    LogUtils.logD(TAG, "IMusicPlayerEventListener--->playing");
                    mNotifyDataSetChanged.postValue(true);
                    mPlayDrawable.set(getDrawable(R.drawable.suspend));
                    mIsLrcViewPause.postValue(true);
                    break;
                case MusicConstants.MUSIC_PLAYER_PAUSE:
                    LogUtils.logD(TAG, "IMusicPlayerEventListener--->pause");
                    mNotifyDataSetChanged.postValue(true);
                    mPlayDrawable.set(getDrawable(R.drawable.play));
                    mIsLrcViewPause.postValue(false);
                    break;
                case MusicConstants.MUSIC_PLAYER_STOP:
                    LogUtils.logD(TAG, "IMusicPlayerEventListener--->stop");
                    mIsLrcViewPause.postValue(false);
                    mNotifyDataSetChanged.postValue(true);
                    mPlayDrawable.set(getDrawable(R.drawable.play));
                    mProgress.set(MusicConstants.DEFAULT_PLAYER_INDEX);
                    mCurrentTime.set(getString(R.string.player_time_default));
                    break;
                default:
                    LogUtils.logD(TAG, "IMusicPlayerEventListener--->default");
                    mPlayDrawable.set(getDrawable(R.drawable.play));
                    break;
            }
        }

        @Override
        public void onPrepared(long totalDuration) {
            // TODO onPrepared
        }

        @Override
        public void onInfo(int event, int extra) {
            // TODO Music info
        }

        @Override
        public void onPlayMusicOnInfo(AudioInfoBean infoBean, int position) {
            LogUtils.logD(TAG, "onPlayMusicOnInfo:: invoke");
            if (null != infoBean) {
                LogUtils.logD(TAG, "onPlayMusicOnInfo:: infoBean :: " + infoBean.toString());
                if (null != mLastAudio && mLastAudio.getAudioId() == infoBean.getAudioId()
                        && !TextUtils.isEmpty(mLastAudio.getAudioPath())
                        && !TextUtils.isEmpty(infoBean.getAudioPath())
                        && mLastAudio.getAudioPath().equals(infoBean.getAudioPath())
                        && position == mLastPosition && checkPlayerInfo(infoBean)) {
                    LogUtils.logD(TAG, "onPlayMusicOnInfo:: infoBean :: same");
                } else {
                    LogUtils.logD(TAG, "onPlayMusicOnInfo:: infoBean :: diff");
                    mLastAudio = infoBean;
                    mLastPosition = position;
                    setAudioInfo(infoBean);
                    mIsPlayInfoAvailable.set(true);
                    mIsPlayInfoEnable.postValue(true);
                    mNotifyDataSetChanged.postValue(true);
                }
            }
        }

        @Override
        public void onMusicPathInvalid(AudioInfoBean mAudioInfoBean, int position) {
            // TODO Music path invalid
        }

        @Override
        public void onTaskRuntime(long totalDuration, long currentDuration, int bufferProgress,
                                  AudioInfoBean audioInfoBean) {
            LogUtils.logD(TAG, "onTaskRuntime :: currentDuration :: "
                    + currentDuration + " : totalDuration :: "
                    + totalDuration + " : audioInfoBean ::"
                    + (audioInfoBean != null ? audioInfoBean.toString() : "null"));
            if (mLastAudio == null && audioInfoBean != null) {
                mLastAudio = audioInfoBean;
                mTotalTime.set(MusicUtils.getInstance()
                        .stringForAudioTime(audioInfoBean.getAddTime()));
                mAlbumCoverDrawable.postValue(audioInfoBean.getAudioPath());
                mAudioName.set(audioInfoBean.getAudioName());
                mAudioNicName.set(audioInfoBean.getAudioArtistName()
                        + MusicConstants.STR_SPLICER
                        + (TextUtils.isEmpty(audioInfoBean.getAudioAlbumName())
                        ? getString(R.string.audio_name_default)
                        : audioInfoBean.getAudioAlbumName()));
            }
            if (totalDuration > MusicConstants.PLAYER_STATUS_DESTROY) {
                if (!mIsTouchSeekBar.get()) {
                    int progress = (int) (((float) currentDuration / totalDuration)
                            * MusicConstants.MAX_PROGRESS);
                    mProgress.set(progress);
                    mLrcViewUpdate.postValue(currentDuration);
                }
            } else {
                mProgress.set(-1);
            }
            if (!mIsTouchSeekBar.get() && totalDuration > MusicConstants.PLAYER_STATUS_DESTROY) {
                if (currentDuration > totalDuration) {
                    mCurrentTime.set(MusicUtils.getInstance().stringForAudioTime(totalDuration));
                } else {
                    mCurrentTime.set(MusicUtils.getInstance().stringForAudioTime(currentDuration));
                }
                mTotalTime.set(MusicUtils.getInstance().stringForAudioTime(totalDuration));
            }
        }

        @Override
        public void onPlayerConfig(int playModel, boolean isToast) {
            if (playModel > MusicConstants.PLAYER_STATUS_DESTROY) {
                mModelDrawable.set(getDrawable(MusicUtils.getInstance()
                        .getPlayerModelToWhiteRes(playModel)));
            }
            if (isToast) {
                ToastUtils.showToast(MusicUtils.getInstance().getPlayerModelToString(
                        BaseApplication.getApplication(), playModel));
            }
        }
    };

    private void saveAudioPlaybackInfo(AudioInfoBean audioInfoBean, int progress) {
        try {
            if (null == mUsbMusicManager || null == mAudioData.getValue()) {
                return;
            }
            RecordAudioInfo recordAudioInfo = new RecordAudioInfo();
            String path = audioInfoBean.getAudioPath();
            // Play mode
            recordAudioInfo.setPlayMode(mUsbMusicManager.getPlayerMode());
            // AudioId
            recordAudioInfo.setAudioId(audioInfoBean.getAudioId());
            // Path
            recordAudioInfo.setPath(path);
            // UuId
            recordAudioInfo.setUuid(MusicUtils.getInstance().getUuidFromPath(path));
            // Player state
            recordAudioInfo.setPlayerState(mUsbMusicManager.getPlayerState());
            // Current progress
            recordAudioInfo.setProgress(progress);
            // Current time
            recordAudioInfo.setCurrentTime(System.currentTimeMillis());
            // Cache info
            MusicUtils.getInstance().saveAudioPlaybackInfo(recordAudioInfo);
            LogUtils.logD(TAG, "cache info=" + recordAudioInfo.toString());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initial memory data.
     */
    public void initRecentAudio(String uid, List<AudioInfoBean> songList, SeekBar seekBar) {
        LogUtils.logD(TAG, "initRecentAudio :: invoke ");
        mNeedInit.postValue(false);
        try {
            AudioInfoBean audioInfoBean = mUsbMusicManager.getCurrentPlayerMusic();
            if (mUsbMusicManager.isPlaying()) {
                LogUtils.logD(TAG, "initRecentAudio :: mUsbMusicManager  "
                        + (audioInfoBean == null ? "null" : audioInfoBean.toString()));
                mPlayDrawable.set(getDrawable(R.drawable.suspend));
                addPlayerEventListener();
                mIsDataAvailable.set(true);
                mIsDataEnable.postValue(true);
                mIsPlayInfoAvailable.set(true);
                updateLyricButton(true);
                mIsPlayInfoEnable.postValue(true);
                seekBar.setEnabled(true);
                mNotifyDataSetChanged.postValue(true);
                setAudioInfo(audioInfoBean);
                return;
            }
            if (null != audioInfoBean) {
                LogUtils.logD(TAG, "initRecentAudio :getCurrentPlayerMusic :: "
                        + audioInfoBean.toString());
                mUsbMusicManager.onCheckedCurrentPlayTask();
            } else {
                if (null != songList && songList.size() > 0) {
                    audioInfoBean = songList.get(MusicConstants.DEFAULT_PLAYER_INDEX);
                    LogUtils.logD(TAG, "initRecentAudio:: Current list first audio "
                            + audioInfoBean.toString());
                } else {
                    audioInfoBean = new AudioInfoBean();
                    audioInfoBean.setAudioName(getString(R.string.audio_name_default));
                    LogUtils.logD(TAG, "initRecentAudio:: Current list no audio "
                            + audioInfoBean.toString());
                }
            }
            mIsDataAvailable.set(true);
            mIsDataEnable.postValue(true);
            mIsPlayInfoAvailable.set(true);
            mIsPlayInfoEnable.postValue(true);
            seekBar.setEnabled(true);
            setAudioInfo(audioInfoBean);
            mNotifyDataSetChanged.postValue(true);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void getJsonData(String uid) {
        LogUtils.logD(TAG, "getJsonData :: invoke");
        try {
            Gson gson = new Gson();
            RecordAudioInfo usbFirst = null;
            RecordAudioInfo usbSecond = null;
            String data = SharedPreferencesHelps.getObjectData(MusicConstants.USB_FIRST_UUID);
            if (!TextUtils.isEmpty(data)) {
                usbFirst = gson.fromJson(data, RecordAudioInfo.class);
            }
            data = SharedPreferencesHelps.getObjectData(MusicConstants.USB_SECOND_UUID);
            if (!TextUtils.isEmpty(data)) {
                usbSecond = gson.fromJson(data, RecordAudioInfo.class);
            }
            if (null != usbFirst && usbFirst.getUuid().equals(uid)) {
                mRecordAudioInfo = usbFirst;
            } else if (null != usbSecond && usbSecond.getUuid().equals(uid)) {
                mRecordAudioInfo = usbSecond;
            }
        } catch (JsonSyntaxException error) {
            error.printStackTrace();
        }
    }

    private boolean checkPlayerInfo(AudioInfoBean audioInfoBean) {
        LogUtils.logD(TAG, "checkPlayerInfo :: invoke ");
        if (null != audioInfoBean && null != mAudioName && null != mAudioNicName) {
            String oldAudioName = mAudioName.get();
            String oldAudioNick = mAudioNicName.get();
            String newAudioName = audioInfoBean.getAudioName();
            String newAudioNick = audioInfoBean.getAudioArtistName()
                    + MusicConstants.STR_SPLICER
                    + (TextUtils.isEmpty(audioInfoBean.getAudioAlbumName())
                    ? getString(R.string.audio_name_default)
                    : audioInfoBean.getAudioAlbumName());

            LogUtils.logD(TAG, "checkPlayerInfo :: oldAudioName "
                    + oldAudioName + ": oldAudioNick : " + oldAudioNick);
            LogUtils.logD(TAG, "checkPlayerInfo :: newAudioName "
                    + newAudioName + ": newAudioNick : " + newAudioNick);
            if (oldAudioName.equals(newAudioName) && oldAudioNick.equals(newAudioNick)) {
                LogUtils.logD(TAG, "checkPlayerInfo ::true ");
                return true;
            }
        }
        return false;
    }


    /**
     * Set current playback information.
     *
     * @param audioInfo AudioInfoBean
     */
    public void setAudioInfo(AudioInfoBean audioInfo) {
        if (null != audioInfo && !checkPlayerInfo(audioInfo)) {
            LogUtils.logD(TAG, "setAudioInfo :: invoke ");
            mTotalTime.set(MusicUtils.getInstance().stringForAudioTime(audioInfo.getAddTime()));
            mAlbumCoverDrawable.postValue(audioInfo.getAudioPath());
            mAudioName.set(audioInfo.getAudioName());
            if (TextUtils.isEmpty(audioInfo.getAudioArtistName())
                    && TextUtils.isEmpty(audioInfo.getAudioAlbumName())) {
                mAudioNicName.set(getString(R.string.nick_name_default));
            } else {
                mAudioNicName.set(audioInfo.getAudioArtistName()
                        + MusicConstants.STR_SPLICER
                        + (TextUtils.isEmpty(audioInfo.getAudioAlbumName())
                        ? getString(R.string.audio_name_default)
                        : audioInfo.getAudioAlbumName()));
            }
        }
    }

    /**
     * Set click toggle icon.
     */
    public void updateLyricButton(boolean visible) {
        mLyricDrawable.set(visible ? getDrawable(R.drawable.sheet)
                : getDrawable(R.drawable.lyric));
    }

    public void clearData() {
        mLastAudio = null;
        mLastPosition = -1;
    }
}
