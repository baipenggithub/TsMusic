package com.ts.music.manager;


import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;
import com.ts.music.callback.IUpdateAudioInfoCallback;

import java.util.List;

public class SearchAudioInfoManager {
    private static volatile SearchAudioInfoManager sSearchAudioInfoManager;
    private IUpdateAudioInfoCallback mCallback;

    /**
     * Initialization.
     */
    public static SearchAudioInfoManager getInstance() {
        if (sSearchAudioInfoManager == null) {
            synchronized (SearchAudioInfoManager.class) {
                if (sSearchAudioInfoManager == null) {
                    sSearchAudioInfoManager = new SearchAudioInfoManager();
                }
                return sSearchAudioInfoManager;
            }
        }
        return sSearchAudioInfoManager;
    }

    /**
     * Set search click data.
     */
    public void setAudioInfo(AudioInfoBean audioInfoBean) {
        if (null == mCallback) {
            return;
        }
        mCallback.updateAudioInfo(audioInfoBean);
    }

    /**
     * Set single data listening.
     *
     * @param audioInfoBeans AudioInfoBean
     */
    public void addSingleAudioInfo(AudioInfoBean audioInfoBeans) {
        if (null == mCallback) {
            return;
        }
        mCallback.addSingleAudioInfo(audioInfoBeans);
    }

    /**
     * Add scan result.
     */
    public void addScanAudioInfo(List<AudioInfoBean> audios) {
        if (null == mCallback) {
            return;
        }
        mCallback.addScanAudioInfo(audios);
    }

    /**
     * Monitor USB devices plugging.
     *
     * @param usbDevices usb devices list.
     */
    public void notifyUsbDevicesChanged(List<UsbDevicesInfoBean> usbDevices) {
        if (null == mCallback) {
            return;
        }
        mCallback.onUsbDevicesChanged(usbDevices);
    }

    /**
     * Notify USB device scan state.
     *
     * @param state    USB device scan state.
     * @param portId   USB device port.
     * @param deviceId USB device id
     */
    public void notifyScanState(int state, String deviceId, int portId) {
        if (null == mCallback) {
            return;
        }
        mCallback.onScanStateChanged(state, deviceId, portId);
    }

    /**
     * Add update audioInfo  callback.
     *
     * @param callback IMapPoiClickCallback.
     */
    public void addUpdateAudioInfoCallback(IUpdateAudioInfoCallback callback) {
        mCallback = callback;
    }
}
