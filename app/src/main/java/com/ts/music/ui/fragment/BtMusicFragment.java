package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.constants.MusicConstants;
import com.ts.music.databinding.FragmentBtMusicBinding;
import com.ts.music.ui.adapter.BtSongListAdapter;
import com.ts.music.ui.viewmodel.BtMusicViewModel;
import com.ts.music.utils.ConstraintUtil;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicUtils;


/**
 * BtMusic Fragment.
 */
public class BtMusicFragment extends BaseFragment<FragmentBtMusicBinding, BtMusicViewModel>
        implements BtMusicViewModel.OnMusicPlayStateListener {
    private static final String TAG = BtMusicFragment.class.getSimpleName();
    private BtSongListAdapter mBtSongListAdapter;
    private static Handler mHandler = new Handler();
    private int mPosition = -1;
    private boolean mIsSongListUp = false;
    private ConstraintUtil mConstraintUtil;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UsbMusicMainFragment.
     */
    public static BtMusicFragment newInstance() {
        return new BtMusicFragment();
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_bt_music;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        // Disable the dragging function of seekbar
//        mBinding.fragmentBtPlayer.musicSeekBar.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                return true;
//            }
//        });
//        initRecyclerView();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
//        mViewModel.mAudioData.observe(this, audioInfoBeans -> {
//            LogUtils.logD(TAG, "bt music size: " + audioInfoBeans.size());
//            mBtSongListAdapter.setNewData(audioInfoBeans);
//        });
//        mViewModel.getBtConnectShowState().observe(this, isConnected -> {
//            Log.d(TAG, "initViewObservable: show connect: " + isConnected);
//            if (!isConnected) {
//                shrinkSongList();
//            }
//        });
//        mViewModel.getAudioCoverDrawable().observe(this, drawable -> {
//            mBinding.fragmentBtPlayer.albumCover.setImageDrawable(drawable);
//        });
//        mViewModel.setMusicPlayStateListener(this);
    }

//    private void initRecyclerView() {
//        LogUtils.logD(TAG, "initRecyclerView");
//        mBinding.fragmentSongList.btRecyclerView.setLayoutManager(new LinearLayoutManager(
//                getActivity()));
//        MusicUtils.getInstance().setRecyclerViewDivider(mBinding.fragmentSongList.btRecyclerView);
//        mBtSongListAdapter = new BtSongListAdapter(R.layout.bt_song_list_item, null);
//        mBinding.fragmentSongList.btRecyclerView.setAdapter(mBtSongListAdapter);
//        mBtSongListAdapter.setOnItemClickListener((helper, resultBean, position) -> {
//            LogUtils.logD(TAG, "click position : " + position
//                    + ",mSelected : " + mBtSongListAdapter.mSelected);
//            if (mBtSongListAdapter.mSelected != position) {
//                mViewModel.play(helper, resultBean, position);
//            }
//        });
//        mBinding.songListShrinkBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                shrinkSongList();
//            }
//        });
//        mBinding.fragmentSongList.btRecyclerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (mBtSongListAdapter.getData() != null
//                        && event.getAction() == MotionEvent.ACTION_MOVE
//                        && !mIsSongListUp) {
//                    if (mConstraintUtil == null) {
//                        mConstraintUtil = new ConstraintUtil((ConstraintLayout) mBinding.getRoot()
//                                .findViewById(R.id.bt_music_fragment));
//                    }
//                    mBinding.fragmentBtPlayer.getRoot().setVisibility(View.GONE);
//                    ConstraintUtil.ConstraintBegin begin = mConstraintUtil.beginWithAnim();
//                    begin.setMarginToZero(R.id.fragment_song_list, ConstraintSet.TOP,
//                            R.id.song_list_shrink_btn, ConstraintSet.BOTTOM);
//                    begin.commit();
//                    mBinding.songListShrinkBtn.setVisibility(View.VISIBLE);
//                    mBinding.songListBg.setVisibility(View.VISIBLE);
//                    mIsSongListUp = true;
//                }
//                return false;
//            }
//        });
//    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void onMusicPlayStateChanged(int position, boolean isPlaying, String artistName) {
        LogUtils.logD(TAG, "onMusicPlayStateChanged position : "
                + position + " mPosition : " + mPosition + "  isPlaying : " + isPlaying
                + "  artist name : " + artistName);
//        mHandler.post(() -> {
//            mBtSongListAdapter.setMusicPlayerManager(isPlaying, position, artistName);
//            if (mPosition != position) {
//                if (position != MusicConstants.ERROR_CODE) {
//                    mBinding.fragmentSongList.btRecyclerView.scrollToPosition(position);
//                } else {
//                    mBinding.fragmentSongList.btRecyclerView.scrollToPosition(0);
//                }
//            }
//        });
//        mPosition = position;
    }

//    private void shrinkSongList() {
//        if (mIsSongListUp) {
//            mConstraintUtil.reSetWidthAnim();
//            mConstraintUtil = null;
//            mBinding.songListShrinkBtn.setVisibility(View.GONE);
//            mBinding.songListBg.setVisibility(View.GONE);
//            mBinding.fragmentBtPlayer.getRoot().setVisibility(View.VISIBLE);
//            mIsSongListUp = false;
//        }
//    }
}
