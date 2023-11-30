package com.ts.music.base;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

public class BaseRecyclerHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;
    private View mConvertView;

    /**
     * Constructor.
     */
    public BaseRecyclerHolder(final View view) {
        super(view);
        mViews = new SparseArray<>();
        mConvertView = view;
    }

    /**
     * Use itemView instead.
     */
    @Deprecated
    public View getConvertView() {
        return mConvertView;
    }

    /**
     * Will set the text of a TextView.
     */
    public BaseRecyclerHolder setText(@IdRes int viewId, CharSequence value) {
        TextView view = getView(viewId);
        view.setText(value);
        return this;
    }

    /**
     * Will set the text of a TextView.
     */
    public BaseRecyclerHolder setText(@IdRes int viewId, @StringRes int strId) {
        TextView view = getView(viewId);
        view.setText(strId);
        return this;
    }

    /**
     * Will set the image of an ImageView from a resource id.
     */
    public BaseRecyclerHolder setImageResource(@IdRes int viewId, @DrawableRes int imageResId) {
        ImageView view = getView(viewId);
        view.setImageResource(imageResId);
        return this;
    }

    /**
     * Will set background color of a view.
     */
    public BaseRecyclerHolder setBackgroundColor(@IdRes int viewId, @ColorInt int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    /**
     * Will set background of a view.
     */
    public BaseRecyclerHolder setBackgroundRes(@IdRes int viewId, @DrawableRes int backgroundRes) {
        View view = getView(viewId);
        view.setBackgroundResource(backgroundRes);
        return this;
    }

    /**
     * Will set text color of a TextView.
     */
    public BaseRecyclerHolder setTextColor(@IdRes int viewId, @ColorInt int textColor) {
        TextView view = getView(viewId);
        view.setTextColor(textColor);
        return this;
    }

    /**
     * Will set the image of an ImageView from a drawable.
     */
    public BaseRecyclerHolder setImageDrawable(@IdRes int viewId, Drawable drawable) {
        ImageView view = getView(viewId);
        view.setImageDrawable(drawable);
        return this;
    }

    /**
     * Add an action to set the image of an image view. Can be called multiple times.
     */
    public BaseRecyclerHolder setImageBitmap(@IdRes int viewId, Bitmap bitmap) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    /**
     * Set a view visibility to VISIBLE (true) or GONE (false).
     */
    public BaseRecyclerHolder setGone(@IdRes int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    /**
     * Set a view visibility to VISIBLE (true) or INVISIBLE (false).
     */
    public BaseRecyclerHolder setVisible(@IdRes int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        return this;
    }

    /**
     * Sets the on click listener of the view.
     */
    @Deprecated
    public BaseRecyclerHolder setOnClickListener(@IdRes int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    /**
     * Sets the listview  item click listener of the view.
     */
    @Deprecated
    public BaseRecyclerHolder setOnItemClickListener(@IdRes int viewId,
                                                     AdapterView.OnItemClickListener listener) {
        AdapterView view = getView(viewId);
        view.setOnItemClickListener(listener);
        return this;
    }

    /**
     * Sets the tag of the view.
     */
    public BaseRecyclerHolder setTag(@IdRes int viewId, Object tag) {
        View view = getView(viewId);
        view.setTag(tag);
        return this;
    }

    /**
     * Sets the tag of the view.
     */
    public BaseRecyclerHolder setTag(@IdRes int viewId, int key, Object tag) {
        View view = getView(viewId);
        view.setTag(key, tag);
        return this;
    }

    /**
     * Get the tag of the view.
     */
    public <T extends View> T getView(@IdRes int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }
}
