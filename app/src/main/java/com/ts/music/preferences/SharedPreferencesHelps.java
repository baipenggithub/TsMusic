package com.ts.music.preferences;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.music.base.BaseApplication;
import com.ts.music.entity.RecordAudioInfo;

import java.util.List;

/**
 * SharedPreferences Helps.
 */
public class SharedPreferencesHelps {
    private static SharedPreferences sPreferences = null;

    private static SharedPreferences getPreferences() {
        if (sPreferences == null) {
            sPreferences = PreferenceManager
                    .getDefaultSharedPreferences(BaseApplication.getApplication());
        }
        return sPreferences;
    }

    /**
     * Save player mode.
     */
    public static synchronized String getPlayerModel(String key, String defaultValue) {
        return getPreferences().getString(key, defaultValue);
    }

    /**
     * Get player mode.
     */
    public static synchronized void setPlayerModel(String key, String value) {
        getPreferences().edit().putString(key, value).apply();
    }

    /**
     * Set object data.
     *
     * @param key   Key
     * @param value Value
     */
    public static void setObjectData(String key, RecordAudioInfo value) {
        Gson gson = new Gson();
        getPreferences().edit().putString(key, gson.toJson(value)).apply();
    }

    /**
     * Set object list data.
     *
     * @param key   Key
     * @param value Value
     */
    public static void setObjectData(String key, List<AudioInfoBean> value) {
        Gson gson = new Gson();
        getPreferences().edit().putString(key, gson.toJson(value)).apply();
    }

    /**
     * Get object data.
     *
     * @param key Key
     */
    public static String getObjectData(String key) {
        return getPreferences().getString(key, "");
    }
}