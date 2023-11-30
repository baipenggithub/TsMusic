package com.ts.music.utils;

import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionValues;

import androidx.annotation.IdRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class ConstraintUtil {

    private static final int TRANSITION_TIME = 100;
    private ConstraintLayout mConstraintLayout;
    private ConstraintBegin mBegin;
    private ConstraintSet mApplyConstraintSet = new ConstraintSet();
    private ConstraintSet mResetConstraintSet = new ConstraintSet();

    public ConstraintUtil(ConstraintLayout constraintLayout) {
        mConstraintLayout = constraintLayout;
        mResetConstraintSet.clone(constraintLayout);
    }

    /**
     * Start modification.
     */
    public ConstraintBegin begin() {
        synchronized (ConstraintBegin.class) {
            if (mBegin == null) {
                mBegin = new ConstraintBegin();
            }
        }
        mApplyConstraintSet.clone(mConstraintLayout);
        return mBegin;
    }

    /**
     * Animated modification.
     */
    public ConstraintBegin beginWithAnim() {
        AutoTransition transition = new AutoTransition();
        transition.setDuration(TRANSITION_TIME);
        TransitionManager.beginDelayedTransition(mConstraintLayout, transition);
        return begin();
    }

    /**
     * Animated reset.
     */
    public void reSetWidthAnim() {
        Transition transition = new Transition() {
            @Override
            public void captureStartValues(TransitionValues transitionValues) {
                // TODO nothing
            }

            @Override
            public void captureEndValues(TransitionValues transitionValues) {
                // TODO nothing
            }
        };
        transition.setDuration(TRANSITION_TIME);
        TransitionManager.beginDelayedTransition(mConstraintLayout, transition);
        mApplyConstraintSet.applyTo(mConstraintLayout);
    }

    public class ConstraintBegin {

        /**
         * Set margin.
         * @param viewId1 view
         * @param position1 position
         * @param viewId2 view
         * @param position2 position
         */
        public ConstraintBegin setMarginToZero(@IdRes int viewId1, int position1,
                                               int viewId2, int position2) {
            mResetConstraintSet.connect(viewId1, position1, viewId2, position2, 0);
            return this;
        }

        /**
         * Submit modification to take effect.
         */
        public void commit() {
            mResetConstraintSet.applyTo(mConstraintLayout);
        }
    }

}
