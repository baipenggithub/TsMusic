package com.ts.music.utils;

import android.util.Log;

public class LogUtils {
    private static final String LOG_PREFIX = "MUSIC_HMI : ";

    private static final boolean DEBUG;
    private static final String BUILD_TYPE = "ro.build.type";
    private static final String DEFAULT_BUILD_TYPE = "userdebug";
    private static final String USER_TYPE = "user";

    static {
        // DEBUG = !TextUtils.equals(USER_TYPE, SystemProperties
        // .get(BUILD_TYPE, DEFAULT_BUILD_TYPE));
        DEBUG = true;
    }

    /**
     * Print debug log info.
     *
     * @param tag  title
     * @param info description
     */
    public static void logD(String tag, String info) {
        if (DEBUG) {
            Log.i(LOG_PREFIX + tag, info);
        }
    }

    /**
     * Print verbose log info.
     *
     * @param tag  title
     * @param info description
     */
    public static void logV(String tag, String info) {
        if (DEBUG) {
            Log.i(LOG_PREFIX + tag, info);
        }
    }

    /**
     * Print info log info.
     *
     * @param tag  title
     * @param info description
     */
    public static void logI(String tag, String info) {
        if (DEBUG) {
            Log.i(LOG_PREFIX + tag, info);
        }
    }

    /**
     * Print warn log info.
     *
     * @param tag  title
     * @param info description
     */
    public static void logW(String tag, String info) {
        if (DEBUG) {
            Log.i(LOG_PREFIX + tag, info);
        }
    }

    /**
     * Print error log info.
     *
     * @param tag  title
     * @param info description
     */
    public static void logE(String tag, String info) {
        if (DEBUG) {
            Log.i(LOG_PREFIX + tag, info);
        }
    }
}
