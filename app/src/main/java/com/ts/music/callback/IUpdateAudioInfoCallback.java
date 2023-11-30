package com.ts.music.callback;

import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;

import java.util.List;

public interface IUpdateAudioInfoCallback {
    /**
     * Update search audio info.
     */
    void updateAudioInfo(AudioInfoBean audioInfoBean);

    /**
     * Add single audio info bean.
     *
     * @param audioInfoBean AudioInfoBean
     */
    void addSingleAudioInfo(AudioInfoBean audioInfoBean);

    /**
     * Add scan result.
     */
    void addScanAudioInfo(List<AudioInfoBean> audios);

    /**
     * Monitor USB device changes.
     *
     * @param usbDevices Usb devices list
     */
    void onUsbDevicesChanged(List<UsbDevicesInfoBean> usbDevices);

    /**
     * Monitor USB device scan state changed.
     *
     * @param state    USB device scan state.
     * @param portId   USB device port.
     * @param deviceId USB device id.
     */
    void onScanStateChanged(int state, String deviceId, int portId);
}
