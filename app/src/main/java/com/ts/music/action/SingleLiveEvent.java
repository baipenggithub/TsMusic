package com.ts.music.action;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SingleLiveEvent.
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T>
            observer) {
        super.observe(owner, mT -> {
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(mT);
            }
        });
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    public void setValue(@Nullable T mt) {
        mPending.set(true);
        super.setValue(mt);
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}
