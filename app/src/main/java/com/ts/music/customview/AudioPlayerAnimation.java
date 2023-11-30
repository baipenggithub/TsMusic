package com.ts.music.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ts.music.R;
import com.ts.music.utils.DensityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AudioPlayerAnimation extends View {
    private Paint mPaint;
    private List<Pointer> mPointers;
    private int mPointerNum;
    private float mBasePointX;
    private float mBasePointY;
    private float mPointerPadding;
    private float mPointerWidth;
    private int mPointerColor = Color.RED;
    private boolean mIsPlaying = false;
    private int mPosition = -1;
    private Thread mThread;
    private int mPointerSpeed;
    public static final int POINTER_NUM = 4;
    public static final int POINTER_WIDTH = 5;
    public static final float POINTER_HEIGHT = 0.1f;
    public static final int POINTER_SPEED = 40;
    public static final int RANDOM_NEXT_INT = 10;
    public static final int PADDING_BOTTOM = 1;
    public static final float BASE_POINT_X = 0f;
    public static final int SEND_EMPTY_MESSAGE = 0;

    /**
     * Initializes the collection of pens and pointers.
     */
    public AudioPlayerAnimation(Context context) {
        super(context);
        init();
    }

    /**
     * Initializes the collection of pens and pointers.
     */
    public AudioPlayerAnimation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.AudioPlayerAnimation);
        mPointerColor = typedArray.getColor(R.styleable.AudioPlayerAnimation_pointer_color,
                Color.RED);
        mPointerNum = typedArray.getInt(R.styleable.AudioPlayerAnimation_pointer_num,
                POINTER_NUM);
        mPointerWidth = DensityUtils.dp2px(getContext(),
                typedArray.getFloat(R.styleable.AudioPlayerAnimation_pointer_width,
                        POINTER_WIDTH));
        mPointerSpeed = typedArray.getInt(R.styleable.AudioPlayerAnimation_pointer_speed,
                POINTER_SPEED);
        typedArray.recycle();
        init();
    }

    /**
     * Initializes the collection of pens and pointers.
     */
    public AudioPlayerAnimation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.AudioPlayerAnimation);
        mPointerColor = typedArray.getColor(R.styleable.AudioPlayerAnimation_pointer_color,
                Color.RED);
        mPointerNum = typedArray.getInt(R.styleable.AudioPlayerAnimation_pointer_num,
                POINTER_NUM);
        mPointerWidth = DensityUtils.dp2px(getContext(),
                typedArray.getFloat(R.styleable.AudioPlayerAnimation_pointer_width,
                        POINTER_WIDTH));
        mPointerSpeed = typedArray.getInt(R.styleable.AudioPlayerAnimation_pointer_speed,
                POINTER_SPEED);
        typedArray.recycle();
        init();
    }

    /**
     * Initializes the collection of pens and pointers.
     */
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mPointerColor);
        mPointers = new ArrayList<>();
        Random random = new Random();
        if (mPointers != null) {
            mPointers.clear();
        }
        for (int i = 0; i < mPointerNum; i++) {
            Pointer pointer = new Pointer((POINTER_HEIGHT
                    * (random.nextInt(RANDOM_NEXT_INT)
                    + PADDING_BOTTOM)
                    * (getHeight()
                    - getPaddingBottom()
                    - getPaddingTop())));
            mPointers.add(pointer);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mBasePointY = getHeight() - getPaddingBottom();
        mPointerPadding = (getWidth() - getPaddingLeft() - getPaddingRight()
                - mPointerWidth * mPointerNum) / (mPointerNum - PADDING_BOTTOM);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBasePointX = BASE_POINT_X + getPaddingLeft();
        for (int i = 0; i < mPointers.size(); i++) {
            canvas.drawRect(mBasePointX,
                    mBasePointY - mPointers.get(i).getHeight(),
                    mBasePointX + mPointerWidth,
                    mBasePointY,
                    mPaint);
            mBasePointX += (mPointerPadding + mPointerWidth);
        }
    }

    /**
     * Start playing.
     */
    public void start(int position) {
        mPosition = position;
        if (!mIsPlaying) {
            if (mThread == null) {
                mThread = new Thread(new MyRunnable(position));
                mThread.start();
            }
            mIsPlaying = true;
        }
    }

    /**
     * Stop the sub thread and refresh the canvas.
     */
    public void stop() {
        mIsPlaying = false;
        mThread = null;
        invalidate();
    }

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
        }
    };

    /**
     * Sub thread, loop to change the height of each pointer.
     */
    public class MyRunnable implements Runnable {
        private int rPosition;

        public MyRunnable(int position) {
            super();
            rPosition = position;
        }

        @Override
        public void run() {
            for (float i = 0; i < Integer.MAX_VALUE; ) {
                try {
                    for (int j = 0; j < mPointers.size(); j++) {
                        float rate = (float) Math.abs(Math.sin(i + j));
                        mPointers.get(j).setHeight((mBasePointY - getPaddingTop()) * rate);
                    }
                    Thread.sleep(mPointerSpeed);
                    if (mIsPlaying && rPosition == mPosition) {
                        myHandler.sendEmptyMessage(SEND_EMPTY_MESSAGE);
                        i += POINTER_HEIGHT;
                    } else {
                        break;
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Pointer class.
     */
    public static class Pointer {
        private float mHeight;

        Pointer(float height) {
            mHeight = height;
        }

        public float getHeight() {
            return mHeight;
        }

        public void setHeight(float height) {
            mHeight = height;
        }
    }
}
