package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentBtMusicCopyBinding;
import com.ts.music.ui.adapter.BtSongListAdapter;
import com.ts.music.ui.viewmodel.BtMusicViewModel;
import com.ts.music.utils.ConstraintUtil;
import com.ts.music.utils.LogUtils;

import java.util.List;


/**
 * BtMusic Fragment.
 */
public class BtMusicFragment extends BaseFragment<FragmentBtMusicCopyBinding, BtMusicViewModel>
        implements BtMusicViewModel.OnMusicPlayStateListener {
    private static final String TAG = BtMusicFragment.class.getSimpleName();
    private BtSongListAdapter mBtSongListAdapter;
    private static Handler mHandler = new Handler();
    private int mPosition = -1;
    private boolean mIsSongListUp = false;
    private ConstraintUtil mConstraintUtil;

    private Fragment currentFragment;
    private FragmentTransaction ft;
    private List<Fragment> fragmentList;

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
        return R.layout.fragment_bt_music_copy;
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
//        notBtFragment = NotBtFragment.newInstance();
//        fragmentList = new ArrayList<>();
//        fragmentList.add(0,notBtFragment);
//        mBinding.suerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBinding.btConstraintLayout.setVisibility(View.GONE);
//                switchFragment(0);
//            }
//        });
    }

    private void  switchFragment(Integer index) {
        ft= getChildFragmentManager().beginTransaction();
//        ft.setCustomAnimations(R.anim.fade_in, R.anim.exit_anim)
        //判断当前的Fragment是否为空，不为空则隐藏
        if (null != currentFragment) {
            ft.hide(currentFragment);
        }
        //先根据Tag从FragmentTransaction事物获取之前添加的Fragment
        Fragment fragment = getChildFragmentManager().findFragmentByTag(
                fragmentList.get(index).getClass().getSimpleName());
        if (null == fragment) {
            //如fragment为空，则之前未添加此Fragment。便从集合中取出
            fragment = fragmentList.get(index);
        }
        currentFragment = fragment;
        //判断此Fragment是否已经添加到FragmentTransaction事物中
//        if (!fragment.isAdded() && TextUtils.isEmpty(fragment.getTag())) {
//            ft.add(R.id.bt_frameLayout, fragment, fragment.getClass().getName());
//        } else {
//            ft.show(fragment);
//        }
        ft.commitAllowingStateLoss();
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
