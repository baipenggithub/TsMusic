package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.callback.IUpdateAudioInfoCallback;
import com.ts.music.databinding.FragmentUsbMusicPlayerBindingImpl;
import com.ts.music.manager.SearchAudioInfoManager;
import com.ts.music.ui.adapter.SongListAdapter;
import com.ts.music.ui.adapter.USBListAdapter;
import com.ts.music.ui.viewmodel.UsbMusicViewModel;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.SortUtils;
import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;

import java.util.List;
import java.util.Objects;

/**
 * Player Fragment.
 */
public class UsbMusicFragmentCopy extends BaseFragment<FragmentUsbMusicPlayerBindingImpl, UsbMusicViewModel> implements IUpdateAudioInfoCallback {
    private static final String TAG = UsbMusicFragmentCopy.class.getSimpleName();
    private USBListAdapter mSongListAdapter;


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
                    mBinding.allSongsRecyclerView.setVisibility(View.VISIBLE);
                    mBinding.folderRecyclerView.setVisibility(View.GONE);
                    break;
                case R.id.rb_Favorite:
                    // switchFragment(1,false);
                    mBinding.folderRecyclerView.setVisibility(View.VISIBLE);
                    mBinding.allSongsRecyclerView.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void onResume() {
        LogUtils.logD(TAG, "onResume :: invoke");
        super.onResume();
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
        mSongListAdapter = new USBListAdapter(R.layout.item_usb_list, null);
        mBinding.allSongsRecyclerView.setAdapter(mSongListAdapter);

        //文件夹
        mBinding.folderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                        mSongListAdapter.addListData(audioInfoBeans);
                    }
                });
            }
        });

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
                mSongListAdapter.replaceData(audios);
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
}