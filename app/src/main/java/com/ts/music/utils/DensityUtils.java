package com.ts.music.utils;

import android.content.Context;
import android.util.TypedValue;

public class DensityUtils {
    /**
     * dp-> px.
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal,
                context.getResources().getDisplayMetrics());
    }

    /**
     * px-> dp.
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }
}
