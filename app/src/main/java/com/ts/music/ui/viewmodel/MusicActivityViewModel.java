package com.ts.music.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.ts.sdk.media.contractinterface.IMediaServiceListener;
import com.ts.sdk.media.mananger.BaseManager;
import com.ts.sdk.media.mananger.MediaPlayControlManager;
import com.ts.music.action.BindingCommand;
import com.ts.music.action.SingleLiveEvent;
import com.ts.music.base.BaseViewModel;
import com.ts.music.utils.LogUtils;


/**
 * MusicActivityViewModel.
 */
public class MusicActivityViewModel extends BaseViewModel {

    private MediaPlayControlManager mMediaPlayControlManager;
    private static final String TAG = "MusicActivityViewModel";
    private int mTabPosition = -1;

    /**
     * Constructor.
     */
    public MusicActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public UiChangeObservable mUiChangeObservable = new UiChangeObservable();

    public static class UiChangeObservable {
        public SingleLiveEvent btMusicLivEvent = new SingleLiveEvent<>();
        public SingleLiveEvent onlineMusicLivEvent = new SingleLiveEvent<>();
        public SingleLiveEvent usbMusicLivEvent = new SingleLiveEvent<>();
    }

    // bt music
    public BindingCommand btMusic = new BindingCommand(() -> {
        LogUtils.logD(TAG, "btMusicLivEvent :: invoke ");
        mUiChangeObservable.btMusicLivEvent.call();
    });

    // online music
    public BindingCommand onlineMusic = new BindingCommand(() -> {
        LogUtils.logD(TAG, "onlineMusic :: invoke ");
        mUiChangeObservable.onlineMusicLivEvent.call();
    });

    // usb music
    public BindingCommand usbMusic = new BindingCommand(() -> {
        LogUtils.logD(TAG, "usbMusic :: invoke ");
        mUiChangeObservable.usbMusicLivEvent.call();
    });

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.logD(TAG, "onServiceConnected: setCurrentTabPosition: " + mTabPosition);
        mMediaPlayControlManager = MediaPlayControlManager.getInstance(getApplication()
                .getApplicationContext(), new IMediaServiceListener() {
                    @Override
                    public void onServiceConnected(BaseManager baseManager) {
                        mMediaPlayControlManager = (MediaPlayControlManager) baseManager;
                        if (mTabPosition != -1) {
                            LogUtils.logD(TAG, "onServiceConnected: setCurrentTabPosition: "
                                    + mTabPosition);
                            mMediaPlayControlManager.setCurrentTabPosition(mTabPosition);
                            mTabPosition = -1;
                        }
                    }

                    @Override
                    public void onServiceDisconnected() {
                        // TODO nothing
                        LogUtils.logD(TAG, "onServiceDisconnected: setCurrentTabPosition: "
                                + mTabPosition);
                    }
                });
    }

    /**
     * Update Music Current Tab.
     */
    public void updateCurrentTab(int position) {
        if (mMediaPlayControlManager != null) {
            LogUtils.logD(TAG, "updateCurrentTab: not null, position " + position);
            mMediaPlayControlManager.setCurrentTabPosition(position);
        }
        mTabPosition = position;
    }
}
