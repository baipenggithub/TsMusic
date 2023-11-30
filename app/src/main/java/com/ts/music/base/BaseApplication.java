package com.ts.music.base;

import android.app.Application;

/**
 * BaseApplication.
 */
public class BaseApplication extends Application {
    private static BaseApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        setApplication(this);
    }

    public static void setApplication(BaseApplication application) {
        sInstance = application;
    }

    public static BaseApplication getApplication() {
        return sInstance;
    }
}
