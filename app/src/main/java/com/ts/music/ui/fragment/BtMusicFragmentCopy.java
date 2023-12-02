package com.ts.music.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.AudioState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentBtMusicCopyBinding;
import com.ts.music.ui.viewmodel.BtMusicViewModel;
import com.ts.music.utils.CommonUtils;
import com.ts.music.utils.LogUtils;


/**
 * BtMusic Fragment.
 */
public class BtMusicFragmentCopy extends BaseFragment<FragmentBtMusicCopyBinding, BtMusicViewModel> {
    private static final String TAG = "BtMusicFragmentCopy";
    private final int Duration = 600;  // 动画时长
    private boolean flag = false;  //标记，控制唱片旋转

    public static BtMusicFragmentCopy newInstance() {
        return new BtMusicFragmentCopy();
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_bt_music_copy;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        mBinding.imageView10.getDrawable().setLevel(0);
        new MyThread().start();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        mViewModel.mAudioData.observe(this, audioInfoBeans -> {
            LogUtils.logD(TAG, "bt music size: " + audioInfoBeans.size());
            // mBtSongListAdapter.setNewData(audioInfoBeans);
        });
        mViewModel.getBtConnectShowState().observe(this, isConnected -> {
            LogUtils.logD(TAG, "initViewObservable: show connect: " + isConnected);
            if (!isConnected) {
                // shrinkSongList();
            }
        });
        mViewModel.getAudioCoverDrawable().observe(this, drawable -> {
            if (null == getActivity()) {
                return;
            }
            // mBinding.imageView2.setImageDrawable(drawable);
            //mBinding.imageView10.setImageDrawable(drawable);

            // mBinding.imageView2.setCornerTopLeftRadius(10);
            // mBinding.imageView2.setCornerBottomLeftRadius(10);

            // mBinding.imageView10.isCircle(true);

        });
        //  mViewModel.setMusicPlayStateListener(this);

        mViewModel.getIsPlay().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                LogUtils.logD(TAG, "getIsPlay: " + aBoolean);
                if (aBoolean) {
                    flag = true;
                    start();
                } else {
                    flag = false;
                    stop();
                }
            }
        });
    }

    /**
     * 开始动画
     */
    private void start() {
        ValueAnimator animator = ValueAnimator.ofInt(0, 10000);
        animator.addUpdateListener(animation -> {
            int level = (int) animation.getAnimatedValue();
            mBinding.rotateHander.getDrawable().setLevel(level);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        animator.setDuration(Duration);
        animator.start();
    }

    /**
     * 暂停动画
     */
    private void pause() {
        ValueAnimator animator01 = ValueAnimator.ofInt(10000, 0);
        animator01.addUpdateListener(animation -> {
            int level = (int) animation.getAnimatedValue();
            mBinding.rotateHander.getDrawable().setLevel(level);
        });

        animator01.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                //state = AnimatorState.State_Playing;
                // audioPause();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // state = AnimatorState.State_Stop;
            }
        });
        animator01.setDuration(Duration);
        animator01.start();
    }

    /**
     * 停止动画 ， 主要用于切歌
     */
    private void stop() {
        ValueAnimator animator01 = ValueAnimator.ofInt(10000, 0);
        animator01.addUpdateListener(animation -> {
            int level = (int) animation.getAnimatedValue();
            mBinding.rotateHander.getDrawable().setLevel(level);
        });

        animator01.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                // audioStop();
                mBinding.imageView10.getDrawable().setLevel(0);
                // state = AnimatorState.State_Playing;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //state = AnimatorState.State_Stop;
            }
        });
        animator01.setDuration(Duration);
        animator01.start();
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    Thread.sleep(50);
                    if (flag) {
                        //只有在flag==true的情况下才会对唱片进行旋转操作
                        handler.sendMessage(new Message());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int level = mBinding.imageView10.getDrawable().getLevel();
            level = level + 200;
            if (level > 10000) {
                level = level - 10000;
            }
            mBinding.imageView10.getDrawable().setLevel(level);
        }
    };
}
