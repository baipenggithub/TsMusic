package com.ts.music.ui.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.callback.IUpdateAudioInfoCallback;
import com.ts.music.constants.MusicConstants;
import com.ts.music.databinding.FragmentUsbMusicPlayerBindingImpl;
import com.ts.music.manager.SearchAudioInfoManager;
import com.ts.music.ui.adapter.USBListAdapter;
import com.ts.music.ui.adapter.UsbCurrentlyListAdapter;
import com.ts.music.ui.viewmodel.UsbMusicViewModel;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicUtils;
import com.ts.music.utils.SortUtils;
import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Player Fragment.
 */
public class UsbMusicFragmentCopy extends BaseFragment<FragmentUsbMusicPlayerBindingImpl, UsbMusicViewModel> implements IUpdateAudioInfoCallback {
    private static final String TAG = UsbMusicFragmentCopy.class.getSimpleName();
    private USBListAdapter mSongListAdapter;
    private UsbCurrentlyListAdapter usbCurrentlyListAdapter;
    private int currentPosition;

    public static UsbMusicFragmentCopy getInstance() {
        return new UsbMusicFragmentCopy();
    }

    @Override
    public void initData() {
        super.initData();
        try {
            mViewModel.getAllSongList();
        } catch (Exception exception) {

        }

    }

    private UsbMusicFragmentCopy() {
        LogUtils.logD(TAG, "UsbMusicFragment :: invoke");
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_usb_music_player;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        initRecyclerView();
        SearchAudioInfoManager.getInstance().addUpdateAudioInfoCallback(this);
        mBinding.rgToolbar.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.rb_allChannel:
                    //switchFragment(0,false);
                    setViewAnimator(0);
                    mBinding.allSongsRecyclerView.setVisibility(View.VISIBLE);
                    mBinding.folderRecyclerView.setVisibility(View.GONE);
                    break;
                case R.id.rb_Favorite:
                    // switchFragment(1,false);
                    setViewAnimator(1);
//                    mBinding.folderRecyclerView.setVisibility(View.VISIBLE);
//                    mBinding.allSongsRecyclerView.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void onResume() {
        LogUtils.logD(TAG, "onResume :: invoke");
        super.onResume();
        try {
            mViewModel.getAllSongList();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        LogUtils.logD(TAG, "onPause :: invoke ");
        super.onPause();
    }

    private void initRecyclerView() {
        LogUtils.logD(TAG, "initRecyclerView :: invoke");
        //列表
        mBinding.allSongsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSongListAdapter = new USBListAdapter();
        usbCurrentlyListAdapter = new UsbCurrentlyListAdapter();
        mBinding.allSongsRecyclerView.setAdapter(mSongListAdapter);

        mSongListAdapter.setOnItemClickListener(new USBListAdapter.OnClickListener() {
            @Override
            public void play(RecyclerView.ViewHolder helper, AudioInfoBean resultBean, int position) {
                Log.e(TAG, "play: "+position);
                currentPosition = position;
                try {
                    long currentPlayerId = mViewModel.mUsbMusicManager.getCurrentPlayerId();
                    if (currentPlayerId != resultBean.getAudioId()) {
                        mViewModel.setAudioInfo(resultBean);
                        mViewModel.startPlayAudio(mSongListAdapter.getData(), position
                                - MusicUtils.getInstance().getAudioSum(
                                mSongListAdapter.getData()), true);
                        mBinding.textView.setText(resultBean.getAudioName());
                        mBinding.textView5.setText(resultBean.getAudioArtistName());

                        mBinding.rlTop.setVisibility(View.GONE);
                        mBinding.llLabel.setVisibility(View.GONE);
                        mBinding.allSongsRecyclerView.setVisibility(View.GONE);

                        mBinding.usbConstraintLayout.setVisibility(View.VISIBLE);

                        mBinding.mRecyclerView.setAdapter(usbCurrentlyListAdapter);
                        usbCurrentlyListAdapter.setSelect(position);
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        usbCurrentlyListAdapter.setOnItemClickListener(new USBListAdapter.OnClickListener() {
            @Override
            public void play(RecyclerView.ViewHolder helper, AudioInfoBean resultBean, int position) {
                Log.e(TAG, "play: "+position);
                currentPosition = position;
                try {
                    long currentPlayerId = mViewModel.mUsbMusicManager.getCurrentPlayerId();
                    if (currentPlayerId != resultBean.getAudioId()) {
                        mViewModel.setAudioInfo(resultBean);
                        mViewModel.startPlayAudio(usbCurrentlyListAdapter.getData(), position
                                - MusicUtils.getInstance().getAudioSum(
                                usbCurrentlyListAdapter.getData()), true);
                        mBinding.textView.setText(resultBean.getAudioName());
                        mBinding.textView5.setText(resultBean.getAudioArtistName());
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
        setSeekBarChangeListener();
        //文件夹
//        mBinding.folderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    @Override
    public void initViewObservable() {
        LogUtils.logD(TAG, "initViewObservable :: invoke");
        super.initViewObservable();
        mViewModel.mAudioData.observe(this, new Observer<List<AudioInfoBean>>() {
            @Override
            public void onChanged(List<AudioInfoBean> audioInfoBeans) {
                requireActivity().runOnUiThread(() -> {
                    if (null != audioInfoBeans && audioInfoBeans.size() > 0) {
                        SortUtils.sortAudioListData(audioInfoBeans);
                        LogUtils.logD(TAG, "mAudioData.observe :: invoke" + audioInfoBeans.toString());
                        /*myAudioInfoBeans.clear();
                        myAudioInfoBeans.addAll(audioInfoBeans);*/
                        mSongListAdapter.setDataList(audioInfoBeans);
                        usbCurrentlyListAdapter.setDataList(audioInfoBeans);
                        mBinding.imageView5.setEnabled(true);
                        mBinding.imageView4.setEnabled(true);
                        mBinding.imageView6.setEnabled(true);
                        mBinding.musicSeekBar.setEnabled(true);
                    } else {
                        mSongListAdapter.clearData();
                        usbCurrentlyListAdapter.clearData();
                        mBinding.imageView5.setEnabled(false);
                        mBinding.imageView5.setImageResource(R.drawable.icon_usb_player);
                        mBinding.imageView4.setEnabled(false);
                        mBinding.imageView6.setEnabled(false);
                        mBinding.musicSeekBar.setEnabled(false);
                        mBinding.textView.setText(getString(R.string.no_music));
                        mBinding.textView5.setText(getString(R.string.nick_name_default));
                        mBinding.musicSeekBar.setProgress(0);
                        mBinding.currenttime.setText("00:00");
                        mBinding.alltime.setText("00:00");
                    }
                });
            }
        });

        mViewModel.mAlbumCoverDrawable.observe(this, album -> {
            LogUtils.logD(TAG, "mAlbumCoverDrawable--> album: " + album);
            if (null != album) {
                mBinding.imageView2.setImageBitmap(MusicUtils.getInstance()
                        .getAlbumArt(album, false));
//                mBinding.imageView10.setImageBitmap(MusicUtils.getInstance()
//                        .getAlbumArt(album, false));
            }
        });

        mViewModel.uiChangeObservable.playerPlayOrPauseEvent.observe(this, ob -> {
            LogUtils.logD(TAG, "playerPlayOrPauseEvent--> ob: " + ob);
            playOrPause();
        });

        mViewModel.uiChangeObservable.last.observe(this , ob -> {
            Log.e(TAG, "selectitem: ");
            currentPosition--;
            mSongListAdapter.setSelect(currentPosition);
            usbCurrentlyListAdapter.setSelect(currentPosition);
        });

        mViewModel.uiChangeObservable.next.observe(this , ob ->{
            currentPosition++;
            mSongListAdapter.setSelect(currentPosition);
            usbCurrentlyListAdapter.setSelect(currentPosition);
        });

    }

    private void playOrPause() {
        LogUtils.logD(TAG, "playOrPause :: invoke ");
        if (mViewModel.mNotifyDataSetChanged.getValue() != null){
            if (mViewModel.mNotifyDataSetChanged.getValue()){
                mViewModel.startPlayAudio(mSongListAdapter.getData(), MusicConstants
                        .DEFAULT_PLAYER_INDEX, false);
            }else {
                mViewModel.startPlayAudio(mSongListAdapter.getData(), MusicConstants
                        .DEFAULT_PLAYER_INDEX, false);
            }
        }else {
            mBinding.imageView5.setImageResource(R.drawable.icon_usb_player);
        }

    }

    private void setViewAnimator(Integer index) {
        if (index == 0) {
            mBinding.rgToolbar.check(R.id.rb_allChannel);
        } else {
            mBinding.rgToolbar.check(R.id.rb_Favorite);
        }
        ObjectAnimator animator;

        switch (index) {
            case 0 :
                ViewGroup.LayoutParams layoutParams = mBinding.ivLeft.getLayoutParams();
                layoutParams.width = 134;
                mBinding.ivLeft.setLayoutParams(layoutParams);
                animator = ObjectAnimator.ofFloat(mBinding.ivLeft, "translationX", 0f);
                animator.setDuration(300);
                animator.start();
                break;

            case 1 :
                ViewGroup.LayoutParams layoutParams1 = mBinding.ivLeft.getLayoutParams();
                layoutParams1.width = 90;
                mBinding.ivLeft.setLayoutParams(layoutParams1);
                animator = ObjectAnimator.ofFloat(mBinding.ivLeft, "translationX", 230f);
                animator.setDuration(300);
                animator.start();
                break;
        }
    }


    @Override
    public void onDestroy() {
        LogUtils.logD(TAG, "onDestroy :: invoke ");
        super.onDestroy();
    }

    @Override
    public void updateAudioInfo(AudioInfoBean audioInfoBean) {
        LogUtils.logD(TAG, "updateAudioInfo: " + audioInfoBean);
    }

    @Override
    public void addSingleAudioInfo(AudioInfoBean audioInfoBean) {
        LogUtils.logD(TAG, "addSingleAudioInfo: " + audioInfoBean);
    }

    @Override
    public void addScanAudioInfo(List<AudioInfoBean> audios) {
        LogUtils.logD(TAG, "addScanAudioInfo: " + audios);
        if (null == getActivity()) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (null != audios && audios.size() > 0) {
                SortUtils.sortAudioListData(audios);
//                mSongListAdapter.setDataList(audios);
            }
        });
    }

    @Override
    public void onUsbDevicesChanged(List<UsbDevicesInfoBean> usbDevices) {
        LogUtils.logD(TAG, "onUsbDevicesChanged: " + usbDevices);

    }

    @Override
    public void onScanStateChanged(int state, String deviceId, int portId) {
        LogUtils.logD(TAG, "onScanStateChanged: state: " + state + "  ;deviceId: " + deviceId + " ,portId: " + portId);

    }


    private void setSeekBarChangeListener() {
        mBinding.musicSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            try {
                                if (null != mViewModel.mUsbMusicManager) {
                                    long duration = mViewModel.mUsbMusicManager.getDurtion();
                                    if (duration > 0) {
                                        updateSeekTipLocation(progress);
                                        String time = MusicUtils.getInstance()
                                                .stringForAudioTime(progress * duration
                                                        / MusicConstants.MAX_PROGRESS);
                                        mViewModel.mCurrentTime.set(time);
                                        mViewModel.mSeekTip.set(time);
                                    }
                                }
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mBinding.getRoot().findViewById(R.id.currenttime).setVisibility(View.VISIBLE);
                        mViewModel.mSeekTip.set(mViewModel.mCurrentTime.get());
                        mViewModel.mIsTouchSeekBar.set(true);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        try {
                            mBinding.getRoot().findViewById(R.id.currenttime)
                                    .setVisibility(View.INVISIBLE);
                            mViewModel.mIsTouchSeekBar.set(false);
                            if (null != mViewModel.mUsbMusicManager) {
                                long duration = mViewModel.mUsbMusicManager.getDurtion();
                                if (duration > 0) {
                                    long currentTime = seekBar.getProgress() * duration
                                            / MusicConstants.MAX_PROGRESS;
                                    mViewModel.mUsbMusicManager.seekTo(currentTime);
                                    mViewModel.mLrcViewUpdate.postValue(currentTime);
                                }
                            }
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
    }

    public void updateSeekTipLocation(int progress) {
        float seekTipX = MusicConstants.VALID_SEEK_WIDTH
                * (Float.valueOf(progress) / Float.valueOf(MusicConstants.MAX_PROGRESS))
                - MusicConstants.SEEK_TIP_OFFSET;
        mBinding.getRoot().findViewById(R.id.currenttime).setTranslationX(seekTipX);
    }
}
