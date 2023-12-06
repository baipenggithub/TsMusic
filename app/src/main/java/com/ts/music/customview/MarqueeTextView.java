package com.ts.music.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.ts.music.utils.LogUtils;

public class MarqueeTextView extends AppCompatTextView {
    private static final String TAG = "MarqueeTextView";
    private static final long LOOP_CYCLE = 16;
    private static final float DEFAULT_OFF_X = 0f;
    private static final int DEFAULT_START_TEXT_BOUNDS = 0;
    private static final int RECT_TOP = 0;
    private static final float STEP = 1.5f;
    private static final int RUN_TASK = 1;
    private volatile String mContent;
    private float mOffX = 0f;
    private Rect mRect = new Rect();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mTextChanged = false;
    private Handler mHandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            LogUtils.logD(TAG, "handleMessage :: INVOKE");
            if (null != message) {
                if (message.what == RUN_TASK) {
                    LogUtils.logD(TAG, "handleMessage :: do");
                    invalidate();
                }
            }
            return false;
        }
    });

    public MarqueeTextView(Context context) {
        super(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtils.logD(TAG, "MarqueeTextView");
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        LogUtils.logD(TAG, "onTextChanged :: invoke");
        LogUtils.logD(TAG, "onTextChanged :: text :: " + text.toString());
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        invalidate();
        mOffX = DEFAULT_OFF_X;
        mTextChanged = true;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mContent = getText().toString();
        mPaint.setColor(getCurrentTextColor());
        mPaint.setTextSize(getTextSize());
        mPaint.setTextAlign(Paint.Align.RIGHT);
        mPaint.getTextBounds(mContent, DEFAULT_START_TEXT_BOUNDS, mContent.length(), mRect);
        LogUtils.logD(TAG, "mContent : " + mContent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTextChanged) {
            mContent = getText().toString();
            mPaint.getTextBounds(mContent, DEFAULT_START_TEXT_BOUNDS, mContent.length(), mRect);
            mTextChanged = false;
        }

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        LogUtils.logD(TAG, "width : " + width + ",height : " + height);

        mRect.top = RECT_TOP;
        mRect.bottom = height;

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom;

        float startX = -mOffX;
        float startY = mRect.centerY() + distance;

        float textWidth = mPaint.measureText(mContent);
        LogUtils.logD(TAG, "startX : " + startX + ",startY : " + startY
                + ",textWidth : " + textWidth);
        if (textWidth <= width) {
            String info = mContent;
            canvas.drawText(info, width / 2f, startY, mPaint);
        } else {
            if (mOffX > mRect.width()) {
                mOffX = DEFAULT_OFF_X;
                mHandle.removeMessages(RUN_TASK);
                String info = mContent;
                LogUtils.logD(TAG, "info : " + info);
                if (width < textWidth) {
                    info = TextUtils.ellipsize(mContent, new TextPaint(mPaint), width,
                            TextUtils.TruncateAt.END).toString();
                    canvas.drawText(info, width / 2f, startY, mPaint);
                }
            } else {
                canvas.drawText(mContent, startX + (mRect.width() / 2f), startY, mPaint);
                mOffX += STEP;
                mHandle.sendEmptyMessageDelayed(RUN_TASK, LOOP_CYCLE);
            }
        }
    }
}
