package com.ts.music.utils;

public class FolderRecordStack {
    private Object[] mElements;
    private int mSize = 0;
    private static final int DEFAULT_CAPACITY = 10;

    public FolderRecordStack() {
        mElements = new Object[DEFAULT_CAPACITY];
    }

    /**
     * Push object to stack.
     */
    public void push(Object object) {
        if (ensureCapacity()) {
            mElements[mSize++] = object;
        }
    }

    /**
     * Pop latest object.
     */
    public Object pop() {
        if (mSize == 0) {
            return null;
        }
        return mElements[--mSize];
    }

    /**
     * Get last element.
     */
    public Object getLastElement() {
        if (mSize == 0) {
            return null;
        }
        return mElements[mSize - 1];
    }

    /**
     * Get size.
     */
    public int getSize() {
        return mSize;
    }

    /**
     * Ensure capacity is enough.
     */
    private boolean ensureCapacity() {
        return mElements.length != mSize;
    }

    /**
     * Clear Stack.
     */
    public void clear() {
        mElements = new Object[DEFAULT_CAPACITY];
        mSize = 0;
    }
}
