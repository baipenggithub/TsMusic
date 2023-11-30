package com.ts.music.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;


import com.ts.music.R;
import com.ts.music.constants.MusicConstants;
import com.ts.music.entity.AudioLrcBean;
import com.ts.music.utils.DensityUtils;
import com.ts.music.utils.MusicUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Music Lrc view.
 */
public class MusicLrcView extends View {
    private List<AudioLrcBean> mLrcData;
    private TextPaint mTextPaint;
    private Paint mScrollPaint;
    private String mDefaultContent;
    private int mCurrentLine;
    private float mOffset;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mScaledTouchSlop;
    private OverScroller mOverScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private float mLrcTextSize;
    private float mLrcLineSpaceHeight;
    private int mTouchDelay;
    private int mNormalColor;
    private int mCurrentPlayLineColor;
    private float mNoLrcTextSize;
    private int mNoLrcTextColor;
    private boolean mIsDragging;
    private boolean mIsUserScroll;
    private boolean mIsAutoAdjustPosition = true;
    private Drawable mPlayDrawable;
    private boolean mIsShowTimeIndicator;
    private Rect mPlayRect;
    private Paint mIndicatorPaint;
    private float mIndicatorLineWidth;
    private float mIndicatorTextSize;
    private int mCurrentIndicateLineTextColor;
    private int mIndicatorLineColor;
    private float mIndicatorMargin;
    private float mIconLineGap;
    private float mIconWidth;
    private float mIconHeight;
    private boolean mIsEnableShowIndicator = true;
    private int mIndicatorTextColor;
    private int mIndicatorTouchDelay;
    private boolean mIsCurrentTextBold;
    private boolean mIsLrcIndicatorTextBold;
    private OnPlayIndicatorLineListener mOnPlayIndicatorLineListener;
    private HashMap<String, StaticLayout> mStaticLayoutHashMap = new HashMap<>();
    private HashMap<String, StaticLayout> mLrcMap = new HashMap<>();

    private static final int LINE_STOP_MARGIN_X = 32;
    private static final int LRC_STOP_MARGIN_X = 30;
    private static final int SCROLL_VIEW_HEIGHT = 492;
    private static final int SCROLL_VIEW_MARGIN_START = 1182;
    private static final int SCROLL_HEIGHT = 92;
    private static final int SCROLL_WIDTH = 10;
    private static final int LRC_BEGIN = 0;

    public MusicLrcView(Context context) {
        this(context, null);
    }

    public MusicLrcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicLrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicLrcView);
        mLrcTextSize = typedArray.getDimension(R.styleable.MusicLrcView_lrcTextSize,
                DensityUtils.dp2px(context, MusicConstants.LRC_TEXT_SIZE));
        mLrcLineSpaceHeight = typedArray.getDimension(R.styleable.MusicLrcView_lrcLineSpaceSize,
                DensityUtils.dp2px(context, MusicConstants.LRC_TEXT_SIZE));
        mTouchDelay = typedArray.getInt(R.styleable.MusicLrcView_lrcTouchDelay,
                MusicConstants.INDICATOR_TOUCH_DELAY);
        mIndicatorTouchDelay = typedArray.getInt(R.styleable.MusicLrcView_indicatorTouchDelay,
                MusicConstants.INDICATOR_TOUCH_DELAY);
        mNormalColor = typedArray.getColor(R.styleable.MusicLrcView_lrcNormalTextColor, Color.GRAY);
        mCurrentPlayLineColor = typedArray.getColor(R.styleable.MusicLrcView_lrcCurrentTextColor,
                Color.WHITE);
        mNoLrcTextSize = typedArray.getDimension(R.styleable.MusicLrcView_noLrcTextSize,
                DensityUtils.dp2px(context, MusicConstants.LRC_TEXT_SIZE));
        mNoLrcTextColor = typedArray.getColor(R.styleable.MusicLrcView_noLrcTextColor, Color.BLACK);
        mIndicatorLineWidth = typedArray.getDimension(R.styleable.MusicLrcView_indicatorLineHeight,
                DensityUtils.dp2px(context, MusicConstants.INDICATOR_LINE_WIDTH));
        mIndicatorTextSize = typedArray.getDimension(R.styleable.MusicLrcView_indicatorTextSize,
                DensityUtils.dp2px(context, MusicConstants.LRC_TEXT_SIZE));
        mIndicatorTextColor = typedArray.getColor(R.styleable.MusicLrcView_indicatorTextColor,
                Color.GRAY);
        mCurrentIndicateLineTextColor = typedArray.getColor(
                R.styleable.MusicLrcView_currentIndicateLrcColor, Color.GRAY);
        mIndicatorLineColor = typedArray.getColor(R.styleable.MusicLrcView_indicatorLineColor,
                Color.GRAY);
        mIndicatorMargin = typedArray.getDimension(R.styleable.MusicLrcView_indicatorStartEndMargin,
                DensityUtils.dp2px(context, MusicConstants.ICON_LINE_GAP));
        mIconLineGap = typedArray.getDimension(R.styleable.MusicLrcView_iconLineGap,
                DensityUtils.dp2px(context, MusicConstants.ICON_LINE_GAP));
        mIconWidth = typedArray.getDimension(R.styleable.MusicLrcView_playIconWidth,
                DensityUtils.dp2px(context, MusicConstants.LRC_TEXT_SIZE));
        mIconHeight = typedArray.getDimension(R.styleable.MusicLrcView_playIconHeight,
                DensityUtils.dp2px(context, MusicConstants.LRC_TEXT_SIZE));
        mPlayDrawable = typedArray.getDrawable(R.styleable.MusicLrcView_playIcon);
        mPlayDrawable = mPlayDrawable == null ? ContextCompat.getDrawable(context,
                R.drawable.music_ic_lyrics) : mPlayDrawable;
        mIsCurrentTextBold = typedArray.getBoolean(R.styleable.MusicLrcView_isLrcCurrentTextBold,
                false);
        mIsLrcIndicatorTextBold = typedArray.getBoolean(
                R.styleable.MusicLrcView_isLrcIndicatorTextBold, false);
        typedArray.recycle();
        setupConfigs(context);
    }

    private void setupConfigs(Context context) {
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        mOverScroller = new OverScroller(context, new DecelerateInterpolator());
        mOverScroller.setFriction(MusicConstants.OVER_SCROLLER);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mLrcTextSize);
        mDefaultContent = getResources().getString(R.string.music_text_lrc_empty);

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setAntiAlias(true);
        mIndicatorPaint.setStrokeWidth(mIndicatorLineWidth);
        mIndicatorPaint.setColor(mIndicatorLineColor);
        mPlayRect = new Rect();
        mIndicatorPaint.setTextSize(mIndicatorTextSize);

        mScrollPaint = new Paint();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mPlayRect.left = (int) mIndicatorMargin;
            mPlayRect.top = (int) (getHeight() / MusicConstants.PLAY_RECT_TOP
                    - mIconHeight / MusicConstants.PLAY_RECT_TOP);
            mPlayRect.right = (int) (mPlayRect.left + mIconWidth);
            mPlayRect.bottom = (int) (mPlayRect.top + mIconHeight);
            mPlayDrawable.setBounds(mPlayRect);
        }
    }

    private int getLrcWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getLrcHeight() {
        return getHeight();
    }

    private boolean isLrcEmpty() {
        return mLrcData == null || getLrcCount() == 0;
    }

    private int getLrcCount() {
        return mLrcData.size();
    }

    /**
     * Set lrc data.
     *
     * @param lrcData Res
     */
    public void setLrcData(List<AudioLrcBean> lrcData) {
        resetView(getResources().getString(R.string.music_text_lrc_empty));
        mLrcData = lrcData;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isLrcEmpty()) {
            drawEmptyText(canvas);
            return;
        }
        int indicatePosition = getIndicatePosition();
        mTextPaint.setTextSize(mLrcTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        float lrcWidth = getLrcWidth() / MusicConstants.LRC_HEIGHT + getPaddingLeft();
        float lrcHeight = getLrcHeight() / MusicConstants.LRC_HEIGHT;
        for (int i = 0; i < getLrcCount(); i++) {
            if (i > 0) {
                lrcHeight += (getTextHeight(i - MusicConstants.LINE_POSITION)
                        + getTextHeight(i)) / MusicConstants.LRC_HEIGHT + mLrcLineSpaceHeight;
            }
            if (mCurrentLine == i) {
                mTextPaint.setColor(mCurrentPlayLineColor);
                mTextPaint.setFakeBoldText(mIsCurrentTextBold);
            } else if (indicatePosition == i && mIsShowTimeIndicator) {
                mTextPaint.setFakeBoldText(mIsLrcIndicatorTextBold);
                mTextPaint.setColor(mCurrentIndicateLineTextColor);
            } else {
                mTextPaint.setFakeBoldText(false);
                mTextPaint.setColor(mNormalColor);
            }
            drawLrc(canvas, lrcWidth, lrcHeight, i);
        }

        if (mIsShowTimeIndicator) {
            mPlayDrawable.draw(canvas);
            long time = mLrcData.get(indicatePosition).getTime();
            float timeWidth = mIndicatorPaint.measureText(
                    MusicUtils.getInstance().stringForAudioTime(time));
            mIndicatorPaint.setColor(mIndicatorLineColor);
            canvas.drawLine(mPlayRect.right + mIconLineGap, getHeight()
                    / MusicConstants.LRC_HEIGHT, getWidth() - timeWidth
                    * MusicConstants.TIME_WIDTH - LINE_STOP_MARGIN_X,
                    getHeight() / MusicConstants.LRC_HEIGHT, mIndicatorPaint);
            int baseX = (int) (getWidth() - timeWidth * MusicConstants.BASE_X);
            float baseline = getHeight() / MusicConstants.LRC_HEIGHT - (mIndicatorPaint.descent()
                    - mIndicatorPaint.ascent()) / MusicConstants.PLAY_RECT_TOP
                    - mIndicatorPaint.ascent();
            mIndicatorPaint.setColor(mIndicatorTextColor);
            canvas.drawText(MusicUtils.getInstance().stringForAudioTime(time),
                    baseX - LRC_STOP_MARGIN_X, baseline, mIndicatorPaint);
        }

        mScrollPaint.setStrokeWidth(SCROLL_WIDTH);
        mScrollPaint.setStrokeCap(Paint.Cap.ROUND);
        mScrollPaint.setColor(getResources().getColor(R.color.lrc_scroll));
        int scrollStartY = indicatePosition * SCROLL_VIEW_HEIGHT / mLrcData.size();
        if (scrollStartY == LRC_BEGIN) {
            scrollStartY = SCROLL_VIEW_HEIGHT / mLrcData.size();
        }
        int scrollStopY = scrollStartY + SCROLL_HEIGHT;
        canvas.drawLine(SCROLL_VIEW_MARGIN_START, scrollStartY,
                SCROLL_VIEW_MARGIN_START, scrollStopY, mScrollPaint);
    }

    private void drawLrc(Canvas canvas, float x, float y, int i) {
        String text = mLrcData.get(i).getContent();
        StaticLayout staticLayout = mLrcMap.get(text);
        if (staticLayout == null) {
            mTextPaint.setTextSize(mLrcTextSize);
            staticLayout = new StaticLayout(text, mTextPaint, getLrcWidth(),
                    Layout.Alignment.ALIGN_NORMAL, MusicConstants.STATIC_LAYOUT_SPACING_X,
                    MusicConstants.STATIC_LAYOUT_SPACING_Y, false);
            mLrcMap.put(text, staticLayout);
        }
        canvas.save();
        canvas.translate(x, y - staticLayout.getHeight() / MusicConstants.LRC_HEIGHT - mOffset);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private void drawEmptyText(Canvas canvas) {
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mNoLrcTextColor);
        mTextPaint.setTextSize(mNoLrcTextSize);
        canvas.save();
        StaticLayout staticLayout = new StaticLayout(mDefaultContent, mTextPaint, getLrcWidth(),
                Layout.Alignment.ALIGN_NORMAL, MusicConstants.STATIC_LAYOUT_SPACING_X,
                MusicConstants.STATIC_LAYOUT_SPACING_Y, false);
        canvas.translate(getLrcWidth() / MusicConstants.LRC_HEIGHT + getPaddingLeft(),
                getLrcHeight() / MusicConstants.LRC_HEIGHT);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * Update lrc time.
     *
     * @param time Time
     */
    public void updateTime(long time) {
        if (isLrcEmpty()) {
            return;
        }
        int linePosition = getUpdateTimeLinePosition(time);
        if (mCurrentLine != linePosition) {
            mCurrentLine = linePosition;
            if (mIsUserScroll) {
                invalidateView();
                return;
            }
            ViewCompat.postOnAnimation(this, mScrollRunnable);
        }
    }

    private int getUpdateTimeLinePosition(long time) {
        int linePos = 0;
        for (int i = 0; i < getLrcCount(); i++) {
            AudioLrcBean lrc = mLrcData.get(i);
            if (time >= lrc.getTime()) {
                if (i == getLrcCount() - MusicConstants.LRC_COUNT_INDEX) {
                    linePos = getLrcCount() - MusicConstants.LRC_COUNT_INDEX;
                } else if (time < mLrcData.get(i + MusicConstants.LRC_COUNT_INDEX).getTime()) {
                    linePos = i;
                    break;
                }
            }
        }
        return linePos;
    }

    private Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            mIsUserScroll = false;
            scrollToPosition(mCurrentLine);
        }
    };

    private Runnable mHideIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            mIsShowTimeIndicator = false;
            invalidateView();
        }
    };

    private void scrollToPosition(int linePosition) {
        float scrollY = getItemOffsetY(linePosition);
        final ValueAnimator animator = ValueAnimator.ofFloat(mOffset, scrollY);
        animator.addUpdateListener(animation -> {
            mOffset = (float) animation.getAnimatedValue();
            invalidateView();
        });
        animator.setDuration(MusicConstants.LRC_ANIMATOR_DURATION);
        animator.start();
    }

    /**
     * Get indicate position.
     *
     * @return indicate position
     */
    public int getIndicatePosition() {
        int pos = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i < mLrcData.size(); i++) {
            float offsetY = getItemOffsetY(i);
            float abs = Math.abs(offsetY - mOffset);
            if (abs < min) {
                min = abs;
                pos = i;
            }
        }
        return pos;
    }

    private float getItemOffsetY(int linePosition) {
        float tempY = 0;
        for (int i = 1; i <= linePosition; i++) {
            tempY += (getTextHeight(i - MusicConstants.LRC_COUNT_INDEX)
                    + getTextHeight(i)) / MusicConstants.PLAY_RECT_TOP + mLrcLineSpaceHeight;
        }
        return tempY;
    }

    private float getTextHeight(int linePosition) {
        String text = mLrcData.get(linePosition).getContent();
        StaticLayout staticLayout = mStaticLayoutHashMap.get(text);
        if (staticLayout == null) {
            mTextPaint.setTextSize(mLrcTextSize);
            staticLayout = new StaticLayout(text, mTextPaint, getLrcWidth(),
                    Layout.Alignment.ALIGN_NORMAL, MusicConstants.STATIC_LAYOUT_SPACING_X,
                    MusicConstants.STATIC_LAYOUT_SPACING_Y, false);
            mStaticLayoutHashMap.put(text, staticLayout);
        }
        return staticLayout.getHeight();
    }

    private boolean overScrolled() {
        return mOffset > getItemOffsetY(getLrcCount() - MusicConstants.LRC_COUNT_INDEX)
                || mOffset < 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLrcEmpty()) {
            return super.onTouchEvent(event);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(mScrollRunnable);
                removeCallbacks(mHideIndicatorRunnable);
                if (!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mIsUserScroll = true;
                mIsDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float moveY = event.getY() - mLastMotionY;
                if (Math.abs(moveY) > mScaledTouchSlop) {
                    mIsDragging = true;
                    mIsShowTimeIndicator = mIsEnableShowIndicator;
                    mIsAutoAdjustPosition = true;
                    invalidateView();
                }
                if (mIsDragging) {
                    float maxHeight = getItemOffsetY(getLrcCount()
                            - MusicConstants.LRC_COUNT_INDEX);
                    if (mOffset < 0 || mOffset > maxHeight) {
                        moveY /= MusicConstants.LRC_MOVE_Y;
                    }
                    mOffset -= moveY;
                    mLastMotionY = event.getY();
                    invalidateView();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsDragging && (!mIsShowTimeIndicator || !onClickPlayButton(event))) {
                    mIsShowTimeIndicator = false;
                    invalidateView();
                    performClick();
                }
                handleActionUp(event);
                break;
            default:
                break;
        }
        return true;
    }

    private void handleActionUp(MotionEvent event) {
        if (mIsEnableShowIndicator) {
            ViewCompat.postOnAnimationDelayed(this, mHideIndicatorRunnable, mIndicatorTouchDelay);
        }
        if (mIsShowTimeIndicator && mPlayRect != null && onClickPlayButton(event)) {
            mIsShowTimeIndicator = false;
            invalidateView();
            if (mOnPlayIndicatorLineListener != null) {
                mOnPlayIndicatorLineListener.onPlay(mLrcData.get(getIndicatePosition()).getTime(),
                        mLrcData.get(getIndicatePosition()).getContent());
            }
        }
        if (overScrolled() && mOffset < 0) {
            scrollToPosition(MusicConstants.SCROLL_TO_POSITION);
            if (mIsAutoAdjustPosition) {
                ViewCompat.postOnAnimationDelayed(this, mScrollRunnable, mTouchDelay);
            }
            return;
        }

        if (overScrolled() && mOffset > getItemOffsetY(getLrcCount()
                - MusicConstants.LRC_COUNT_INDEX)) {
            scrollToPosition(getLrcCount() - MusicConstants.LRC_COUNT_INDEX);
            if (mIsAutoAdjustPosition) {
                ViewCompat.postOnAnimationDelayed(this, mScrollRunnable, mTouchDelay);
            }
            return;
        }
        mVelocityTracker.computeCurrentVelocity(
                MusicConstants.FORMATTER_SECOND,
                mMaximumFlingVelocity);
        float velocity = mVelocityTracker.getYVelocity();
        float absVelocity = Math.abs(velocity);
        if (absVelocity > mMinimumFlingVelocity) {
            mOverScroller.fling(MusicConstants.SCROLL_TO_POSITION,
                    (int) mOffset, MusicConstants.SCROLL_TO_POSITION,
                    (int) (-velocity),
                    MusicConstants.SCROLL_TO_POSITION,
                    MusicConstants.SCROLL_TO_POSITION, MusicConstants.SCROLL_TO_POSITION,
                    (int) getItemOffsetY(getLrcCount() - MusicConstants.LRC_COUNT_INDEX),
                    MusicConstants.SCROLL_TO_POSITION,
                    (int) getTextHeight(MusicConstants.SCROLL_TO_POSITION));
            invalidateView();
        }
        releaseVelocityTracker();
        if (mIsAutoAdjustPosition) {
            ViewCompat.postOnAnimationDelayed(this, mScrollRunnable, mTouchDelay);
        }
    }

    private boolean onClickPlayButton(MotionEvent event) {
        float left = mPlayRect.left;
        float right = mPlayRect.right;
        float top = mPlayRect.top;
        float bottom = mPlayRect.bottom;
        float motionX = event.getX();
        float motionY = event.getY();
        return mLastMotionX > left && mLastMotionX < right && mLastMotionY > top
                && mLastMotionY < bottom && motionX > left && motionX < right
                && motionY > top && motionY < bottom;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mOverScroller.computeScrollOffset()) {
            mOffset = mOverScroller.getCurrY();
            invalidateView();
        }
    }

    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * Reset.
     *
     * @param defaultContent Lrc content
     */
    public void resetView(String defaultContent) {
        if (mLrcData != null) {
            mLrcData.clear();
        }
        mLrcMap.clear();
        mStaticLayoutHashMap.clear();
        mCurrentLine = MusicConstants.SCROLL_TO_POSITION;
        mOffset = MusicConstants.SCROLL_TO_POSITION;
        mIsUserScroll = false;
        mIsDragging = false;
        mDefaultContent = defaultContent;
        removeCallbacks(mScrollRunnable);
        invalidate();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Pause (after manually sliding the lyrics,
     * it will no longer automatically roll back to the current playing position).
     */
    public void pause() {
        mIsAutoAdjustPosition = false;
        invalidateView();
    }

    /**
     * Restore (continue automatic rollback).
     */
    public void resume() {
        mIsAutoAdjustPosition = true;
        ViewCompat.postOnAnimationDelayed(this, mScrollRunnable, mTouchDelay);
        invalidateView();
    }

    private void invalidateView() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * Play monitoring.
     *
     * @param onPlayIndicatorLineListener OnPlayIndicatorLineListener
     */
    public void setOnPlayIndicatorLineListener(OnPlayIndicatorLineListener
                                                       onPlayIndicatorLineListener) {
        mOnPlayIndicatorLineListener = onPlayIndicatorLineListener;
    }

    /**
     * Play monitor interface.
     */
    public interface OnPlayIndicatorLineListener {
        void onPlay(long time, String content);
    }
}