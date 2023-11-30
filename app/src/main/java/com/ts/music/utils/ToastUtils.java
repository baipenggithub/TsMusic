package com.ts.music.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;

import com.ts.music.R;
import com.ts.music.base.BaseApplication;


/**
 * Toast utils.
 */
public final class ToastUtils {

    private static final int COLOR_DEFAULT = 0xFEFFFFFF;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private static Toast sToast;
    private static int sGravity = -1;
    private static int sXOffset = -1;
    private static int sYOffset = -1;
    private static int sBgColor = COLOR_DEFAULT;
    private static int sBgResource = -1;
    private static int sMsgColor = COLOR_DEFAULT;

    private ToastUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Set background color.
     *
     * @param backgroundColor background color
     */
    public static void setBgColor(@ColorInt final int backgroundColor) {
        sBgColor = backgroundColor;
    }

    /**
     * Set background resources.
     *
     * @param bgResource background resources
     */
    public static void setBgResource(@DrawableRes final int bgResource) {
        sBgResource = bgResource;
    }

    /**
     * Set message color.
     *
     * @param msgColor color
     */
    public static void setMsgColor(@ColorInt final int msgColor) {
        sMsgColor = msgColor;
    }

    /**
     * Show short toast safely.
     *
     * @param text text
     */
    public static void showShort(@NonNull final CharSequence text) {
        show(text, Toast.LENGTH_SHORT);
    }

    /**
     * Show short toast safely.
     *
     * @param resId resource ID
     */
    public static void showShort(@StringRes final int resId) {
        show(resId, Toast.LENGTH_SHORT);
    }

    /**
     * Show short toast safely.
     *
     * @param resId resource id
     * @param args  parameter
     */
    public static void showShort(@StringRes final int resId, final Object... args) {
        if (args != null && args.length == 0) {
            show(resId, Toast.LENGTH_SHORT);
        } else {
            show(resId, Toast.LENGTH_SHORT, args);
        }
    }

    /**
     * Show short toast safely.
     *
     * @param format format
     * @param args   format
     */
    public static void showShort(final String format, final Object... args) {
        if (args != null && args.length == 0) {
            show(format, Toast.LENGTH_SHORT);
        } else {
            show(format, Toast.LENGTH_SHORT, args);
        }
    }

    /**
     * Safe display of long-term toast.
     *
     * @param text text
     */
    public static void showLong(@NonNull final CharSequence text) {
        show(text, Toast.LENGTH_LONG);
    }

    /**
     * Safe display of long-term toast.
     *
     * @param resId resource Id
     */
    public static void showLong(@StringRes final int resId) {
        show(resId, Toast.LENGTH_LONG);
    }

    /**
     * Safe display of long-term toast.
     *
     * @param resId resource Id
     * @param args  parameter
     */
    public static void showLong(@StringRes final int resId, final Object... args) {
        if (args != null && args.length == 0) {
            show(resId, Toast.LENGTH_SHORT);
        } else {
            show(resId, Toast.LENGTH_LONG, args);
        }
    }

    /**
     * Safe display of long-term toast.
     *
     * @param format format
     * @param args   parameter
     */
    public static void showLong(final String format, final Object... args) {
        if (args != null && args.length == 0) {
            show(format, Toast.LENGTH_SHORT);
        } else {
            show(format, Toast.LENGTH_LONG, args);
        }
    }

    /**
     * Safely display short custom toast.
     */
    public static View showCustomShort(@LayoutRes final int layoutId) {
        final View view = getView(layoutId);
        show(view, Toast.LENGTH_SHORT);
        return view;
    }

    /**
     * Safely display long custom toast.
     */
    public static View showCustomLong(@LayoutRes final int layoutId) {
        final View view = getView(layoutId);
        show(view, Toast.LENGTH_LONG);
        return view;
    }

    /**
     * Cancel toast display.
     */
    public static void cancel() {
        if (sToast != null) {
            sToast.cancel();
            sToast = null;
        }
    }

    private static void show(@StringRes final int resId, final int duration) {
        show(BaseApplication.getApplication().getResources().getText(resId).toString(),
                duration);
    }

    private static void show(@StringRes final int resId, final int duration, final Object... args) {
        show(String.format(BaseApplication.getApplication().getResources().getString(resId), args),
                duration);
    }

    private static void show(final String format, final int duration, final Object... args) {
        show(String.format(format, args), duration);
    }

    private static void show(final CharSequence text, final int duration) {
        sHandler.post(() -> {
            cancel();
            sToast = Toast.makeText(BaseApplication.getApplication(), text, duration);
            TextView tvMessage = sToast.getView().findViewById(android.R.id.message);
            int msgColor = tvMessage.getCurrentTextColor();
            //it solve the font of toast
            TextViewCompat.setTextAppearance(tvMessage, android.R.style.TextAppearance);
            if (sMsgColor != COLOR_DEFAULT) {
                tvMessage.setTextColor(sMsgColor);
            } else {
                tvMessage.setTextColor(msgColor);
            }
            if (sGravity != -1 || sXOffset != -1 || sYOffset != -1) {
                sToast.setGravity(sGravity, sXOffset, sYOffset);
            }
            setBg(tvMessage);
            sToast.show();
        });
    }

    private static void show(final View view, final int duration) {
        sHandler.post(() -> {
            cancel();
            sToast = new Toast(BaseApplication.getApplication());
            sToast.setView(view);
            sToast.setDuration(duration);
            if (sGravity != -1 || sXOffset != -1 || sYOffset != -1) {
                sToast.setGravity(sGravity, sXOffset, sYOffset);
            }
            setBg();
            sToast.show();
        });
    }

    private static void setBg() {
        View toastView = sToast.getView();
        if (sBgResource != -1) {
            toastView.setBackgroundResource(sBgResource);
        } else if (sBgColor != COLOR_DEFAULT) {
            Drawable background = toastView.getBackground();
            if (background != null) {
                background.setColorFilter(
                        new PorterDuffColorFilter(sBgColor, PorterDuff.Mode.SRC_IN)
                );
            } else {
                ViewCompat.setBackground(toastView, new ColorDrawable(sBgColor));
            }
        }
    }

    private static void setBg(final TextView tvMsg) {
        View toastView = sToast.getView();
        if (sBgResource != -1) {
            toastView.setBackgroundResource(sBgResource);
            tvMsg.setBackgroundColor(Color.TRANSPARENT);
        } else if (sBgColor != COLOR_DEFAULT) {
            Drawable tvBg = toastView.getBackground();
            Drawable msgBg = tvMsg.getBackground();
            if (tvBg != null && msgBg != null) {
                tvBg.setColorFilter(new PorterDuffColorFilter(sBgColor, PorterDuff.Mode.SRC_IN));
                tvMsg.setBackgroundColor(Color.TRANSPARENT);
            } else if (tvBg != null) {
                tvBg.setColorFilter(new PorterDuffColorFilter(sBgColor, PorterDuff.Mode.SRC_IN));
            } else if (msgBg != null) {
                msgBg.setColorFilter(new PorterDuffColorFilter(sBgColor, PorterDuff.Mode.SRC_IN));
            } else {
                toastView.setBackgroundColor(sBgColor);
            }
        }
    }

    private static View getView(@LayoutRes final int layoutId) {
        LayoutInflater inflate =
                (LayoutInflater) BaseApplication.getApplication().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        return inflate != null ? inflate.inflate(layoutId, null) : null;
    }

    /**
     * Show fragment search toast.
     *
     * @param msg the message to show
     */
    public static void showToast(String msg) {
        cancel();
        sToast = new Toast(BaseApplication.getApplication());
        View view = getView(R.layout.usb_search_music_toast);
        if (null != view) {
            ((TextView) view.findViewById(R.id.message)).setText(msg);
            sToast.setView(view);
            sToast.setGravity(Gravity.CENTER, 0, 0);
            sToast.setDuration(Toast.LENGTH_SHORT);
            sToast.show();
        }
    }
}

