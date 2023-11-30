package com.ts.music.ui.viewmodel;

import android.app.Application;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;
import com.ts.sdk.media.callback.IUsbDevicesListener;
import com.ts.sdk.media.contractinterface.IMediaServiceListener;
import com.ts.sdk.media.mananger.BaseManager;
import com.ts.sdk.media.mananger.UsbMusicManager;
import com.ts.music.R;
import com.ts.music.action.BindingCommand;
import com.ts.music.action.SingleLiveEvent;
import com.ts.music.base.BaseApplication;
import com.ts.music.base.BaseViewModel;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.ToastUtils;

import java.util.List;

/**
 * Player Fragment ViewModel.
 */
public class UsbMusicSearchViewModel extends BaseViewModel {

    private static final String TAG = UsbMusicSearchViewModel.class.getSimpleName();
    private IMediaServiceListener mListener;
    public UsbMusicManager mUsbMusicManager;
    public SingleLiveEvent<List<UsbDevicesInfoBean>> mUsbDevices = new SingleLiveEvent<>();
    public SingleLiveEvent<List<AudioInfoBean>> mSearchResultAudio = new SingleLiveEvent<>();
    public SingleLiveEvent<List<UsbDevicesInfoBean>> mUsbDevicesInfoBeans = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> mClearAdapter = new SingleLiveEvent<>();
    private SingleLiveEvent<Boolean> mIsConnected = new SingleLiveEvent<>();
    public ObservableField<String> mKeywordSearch = new ObservableField<>();

    /**
     * Constructor.
     */
    public UsbMusicSearchViewModel(@NonNull Application application) {
        super(application);
        // TODO Initialization
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mListener =  new IMediaServiceListener() {
            @Override
            public void onServiceConnected(BaseManager baseManager) {
                try {
                    LogUtils.logD(TAG, "onServiceConnected :: invoke ");
                    mUsbMusicManager = (UsbMusicManager) baseManager;
                    mUsbMusicManager.registerUsbDevicesStatusObserver(mUsbDevicesListener);
                    mIsConnected.postValue(true);

                } catch (SecurityException | IllegalStateException | RemoteException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected() {
                mIsConnected.postValue(false);
            }
        };
        mUsbMusicManager = UsbMusicManager.getInstance(BaseApplication.getApplication(), mListener);
    }

    public UiChangeObservable uiChangeObservable = new UiChangeObservable();

    public static class UiChangeObservable {
        public SingleLiveEvent backEvent = new SingleLiveEvent<>();
    }

    // Back
    public BindingCommand backCommand = new BindingCommand(() -> {
        uiChangeObservable.backEvent.call();
    });

    // Clear EditText
    public BindingCommand deleteKeywordSearchCommand = new BindingCommand(() -> {
        mKeywordSearch.set("");
        mClearAdapter.postValue(true);
    });

    // Search result
    public BindingCommand keywordSearchResultCommand = new BindingCommand(() -> {
        try {
            if (null != mUsbMusicManager) {
                if (null != mKeywordSearch.get() && !TextUtils.isEmpty(mKeywordSearch.get())) {
                    mUsbDevicesInfoBeans.postValue(mUsbMusicManager.getUsbDevices());
                    mSearchResultAudio.postValue(mUsbMusicManager.getAudioInfo(
                            mKeywordSearch.get()));
                } else {
                    ToastUtils.showToast(BaseApplication.getApplication().getResources()
                            .getString(R.string.search_content_cannot_empty));
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    });

    private IUsbDevicesListener mUsbDevicesListener = new IUsbDevicesListener.Stub() {
        @Override
        public void onUsbDevicesChange(List<UsbDevicesInfoBean> usbDevices) {
            mUsbDevices.postValue(usbDevices);
        }

        @Override
        public void onScanStateChange(int state, String deviceId, int portId) {
            // Do nothing.
        }
    };

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mUsbMusicManager != null) {
            try {
                mUsbMusicManager.unRegisterUsbDevicesStatusObserver(mUsbDevicesListener);
            } catch (RemoteException remote) {
                remote.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUsbMusicManager != null) {
            if (mListener != null) {
                mUsbMusicManager.removeMediaServiceListener(mListener);
            }
            mUsbMusicManager.release();
            mUsbMusicManager = null;
        }
    }
}
