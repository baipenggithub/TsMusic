package com.ts.music.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseRecyclerAdapter<T, K extends BaseRecyclerHolder>
        extends RecyclerView.Adapter<K> {
    private FrameLayout mEmptyLayout;
    private boolean mIsUseEmpty = true;
    private int mLayoutResId;
    private LayoutInflater mLayoutInflater;
    private List<T> mData;
    private static final int DEF_EMPTY_VIEW_COUNT = 0;
    private static final int DEF_VIEW_COUNT = 1;
    private static final int EMPTY_VIEW = 2;

    /**
     * Initialization data.
     */
    public BaseRecyclerAdapter(@LayoutRes int layoutResId, @Nullable List<T> data) {
        mData = data == null ? new ArrayList<>() : data;
        if (layoutResId != DEF_EMPTY_VIEW_COUNT) {
            mLayoutResId = layoutResId;
        }
    }

    /**
     * Setting up a new instance to data.
     */
    public void setNewData(@Nullable List<T> data) {
        mData = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    /**
     * Add one new data.
     */
    public void addData(@NonNull T data) {
        mData.add(data);
        notifyItemInserted(mData.size());
        notifyDataSetChanged();
    }

    /**
     * Add list data.
     */
    public void addListData(@Nullable List<T> data) {
        int startPosition = mData.size();
        if (data != null) {
            mData.addAll(data);
            notifyItemRangeInserted(startPosition, data.size());
        }
    }

    /**
     * Use data to replace all item in mData.
     */
    public void replaceData(@NonNull Collection<? extends T> data) {
        if (data != mData) {
            mData.clear();
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * Clear mData.
     */
    public void clearDate() {
        if (mData != null) {
            mData.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * Get the data of list.
     */
    @NonNull
    public List<T> getData() {
        return mData;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     */
    @Nullable
    public T getItem(int position) {
        if (position >= 0 && position < mData.size()) {
            return mData.get(position);
        } else {
            return null;
        }
    }

    /**
     * If show empty view will be return 1 or not will be return 0.
     */
    private int getEmptyViewCount() {
        if (mEmptyLayout == null || mEmptyLayout.getChildCount() == DEF_EMPTY_VIEW_COUNT) {
            return DEF_EMPTY_VIEW_COUNT;
        }
        if (!mIsUseEmpty) {
            return DEF_EMPTY_VIEW_COUNT;
        }
        if (mData.size() != DEF_EMPTY_VIEW_COUNT) {
            return DEF_EMPTY_VIEW_COUNT;
        }
        return DEF_VIEW_COUNT;
    }

    @Override
    public int getItemCount() {
        int count;
        if (getEmptyViewCount() == DEF_VIEW_COUNT) {
            count = DEF_VIEW_COUNT;

        } else {
            count = +mData.size();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == DEF_VIEW_COUNT) {
            return EMPTY_VIEW;
        } else {
            return getDefItemViewType(position);
        }
    }

    private int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public K onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        K holder;
        mLayoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == EMPTY_VIEW) {
            holder = createBaseViewHolder(mEmptyLayout);
        } else {
            holder = onCreateDefViewHolder(parent);
        }
        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull K holder, int position, @NonNull List<Object> payloads) {
        if (null != payloads && payloads.size() > 0) {
            convert(holder, getItem(position), payloads);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    /**
     * To bind different types of holder and solve different the bind events.
     */
    @Override
    public void onBindViewHolder(@NonNull K holder, int position) {
        int viewType = holder.getItemViewType();
        if (viewType != EMPTY_VIEW) {
            convert(holder, getItem(position));
        }
    }

    private K onCreateDefViewHolder(ViewGroup parent) {
        int layoutId = mLayoutResId;
        return createBaseViewHolder(parent, layoutId);
    }

    private K createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return createBaseViewHolder(getItemView(layoutResId, parent));
    }

    /**
     * Create Generic K instance.
     */
    private K createBaseViewHolder(View view) {
        Class temp = getClass();
        Class cl = null;
        while (cl == null && null != temp) {
            cl = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        K holder;
        if (cl == null) {
            holder = (K) new BaseRecyclerHolder(view);
        } else {
            holder = createGenericKInstance(cl, view);
        }
        return holder != null ? holder : (K) new BaseRecyclerHolder(view);
    }

    /**
     * Create Generic K instance.
     */
    private K createGenericKInstance(Class cl, View view) {
        try {
            Constructor constructor;
            if (cl.isMemberClass() && !Modifier.isStatic(cl.getModifiers())) {
                constructor = cl.getDeclaredConstructor(getClass(), View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(this, view);
            } else {
                constructor = cl.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * get generic parameter.
     */
    private Class getInstancedGenericKClass(Class cl) {
        Type type = cl.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseRecyclerHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                } else if (temp instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) temp).getRawType();
                    if (rawType instanceof Class && BaseRecyclerHolder
                            .class.isAssignableFrom((Class<?>) rawType)) {
                        return (Class<?>) rawType;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Set empty view.
     */
    public void setEmptyView(View emptyView) {
        boolean insert = false;
        if (mEmptyLayout == null) {
            mEmptyLayout = new FrameLayout(emptyView.getContext());
            final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            final ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if (lp != null) {
                layoutParams.width = lp.width;
                layoutParams.height = lp.height;
            }
            mEmptyLayout.setLayoutParams(layoutParams);
            insert = true;
        }
        mEmptyLayout.removeAllViews();
        mEmptyLayout.addView(emptyView);
        mIsUseEmpty = true;
        if (insert) {
            if (getEmptyViewCount() == DEF_VIEW_COUNT) {
                int position = 0;
                notifyItemInserted(position);
            }
        }
    }

    /**
     * Clear adapter empty view.
     */
    public void clearEmptyView() {
        if (mEmptyLayout != null) {
            mEmptyLayout.removeAllViews();
            mIsUseEmpty = false;
        }
    }

    /**
     * ID for an XML layout resource to load.
     */
    private View getItemView(@LayoutRes int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    /**
     * Get the row id associated with the specified position in the list.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     */
    protected abstract void convert(K helper, T item);

    protected abstract void convert(K helper, T item, List<Object> payloads);
}
