package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

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
    private static final String TAG = "NotBtFragment";

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
    }
}
