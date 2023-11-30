package com.ts.music.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.ts.music.base.BaseApplication;
import com.ts.music.constants.MusicConstants;

import java.util.Set;

public class CommonUtils {
    private static final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Jump to other apps.
     *
     * @param packageName APP package name
     * @param className   APP class name
     * @param bundle      APP bundle
     */
    public static void startApp(String packageName, String className, Bundle bundle,
                                Context context) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            if (!TextUtils.isEmpty(packageName)) {
                intent.setComponent(new ComponentName(packageName, className));
            }
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            exception.getMessage();
        }
    }

    /**
     * Jump to Setting bt.
     */
    public static void startSettingBt(Context context) {
        try {
            Intent intent = new Intent();
            intent.putExtra(MusicConstants.SETTING_BT_KEY, MusicConstants.SETTING_BT_VALUE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            if (!TextUtils.isEmpty(MusicConstants.SETTING_PACKAGE_NAME)) {
                intent.setComponent(new ComponentName(MusicConstants.SETTING_PACKAGE_NAME,
                        MusicConstants.SETTING_MAIN_ACTIVITY));
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            exception.getMessage();
        }
    }

    /**
     * Get connected device name.
     *
     * @return name
     */
    @SuppressLint("MissingPermission")
    public static String getConnectedDeviceName() {
        if (isExistConnectDevices()) {
            return getConnectedDevice().getName();
        }
        return null;
    }

    /**
     * Get connected device.
     *
     * @return device
     * 蓝牙绑定状态：
     * BOND_NONE：远程设备未绑定。
     * BOND_BONDING：正在与远程设备进行绑定。
     * BOND_BONDED：远程设备已绑定。
     * --------------------
     * STATE_OFF：表示本地蓝牙适配器已关闭。
     * STATE_TURNING_ON：表示本地蓝牙适配器正在打开。
     * STATE_ON：表示本地蓝牙适配器已开启，并可供使用。
     * STATE_TURNING_OFF：表示本地蓝牙适配器正在关闭。
     */
    @SuppressLint("MissingPermission")
    public static BluetoothDevice getConnectedDevice() {
//        if (ActivityCompat.checkSelfPermission(BaseApplication.getApplication(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            LogUtils.logD("CommonUtils", "No BLUETOOTH_CONNECT permission");
//            return null;
//        }
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            LogUtils.logD("CommonUtils", "getBondState: " + device.getBondState() + " ,getState() :" + mBluetoothAdapter.getState());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                return device;
            }
        }
        return null;
    }

    /**
     * If had connected to other devices.
     *
     * @return true : has connected devices. or don't have.
     */
    @SuppressLint("MissingPermission")
    public static boolean isExistConnectDevices() {
//        if (ActivityCompat.checkSelfPermission(BaseApplication.getApplication(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            LogUtils.logD("CommonUtils", "No BLUETOOTH_CONNECT permission");
//            return false;
//        }
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            LogUtils.logD("CommonUtils", "getBondState: " + device.getBondState() + " ,getState() :" + mBluetoothAdapter.getState());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                return true;
            }
        }
        return false;
    }
}