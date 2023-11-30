package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;
import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentMusicSearchBinding;
import com.ts.music.manager.SearchAudioInfoManager;
import com.ts.music.ui.adapter.MusicSearchAdapter;
import com.ts.music.ui.viewmodel.UsbMusicSearchViewModel;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicUtils;
import com.ts.music.utils.SortUtils;
import com.ts.music.utils.ToastUtils;

import java.util.List;

public class UsbMusicSearchFragment extends BaseFragment<FragmentMusicSearchBinding,
        UsbMusicSearchViewModel> {
    private MusicSearchAdapter mMusicSearchAdapter;
    private static final String TAG = UsbMusicSearchFragment.class.getSimpleName();
    private static final int BACK_DELAY = 100;
    private List<UsbDevicesInfoBean> mUsbDevicesInfoBeans;

    /**
     * Get instance of VideoSearchFragment.
     *
     * @return the instance of VideoSearchFragment
     */
    public static UsbMusicSearchFragment getInstance() {
        return new UsbMusicSearchFragment();
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_search;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        mBinding.etMusicSearch.requestFocus();
        showInput(mBinding.etMusicSearch);
        initRecyclerView();
        ConstraintLayout root = mBinding.searchRoot;
        root.setOnClickListener(view -> {
            if (view.getId() != R.id.et_music_search) {
                mBinding.etMusicSearch.clearFocus();
                hideInput();
            }
        });
    }

    private void initRecyclerView() {
        mBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MusicUtils.getInstance().setRecyclerViewDivider(mBinding.mRecyclerView);
        mMusicSearchAdapter = new MusicSearchAdapter(R.layout.fragment_search_list_item, null);
        mBinding.mRecyclerView.setAdapter(mMusicSearchAdapter);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        mViewModel.uiChangeObservable.backEvent.observe(this, ob -> fragmentBackStack());
        mViewModel.mSearchResultAudio.observe(this, this::setSearchAdapter);
        mViewModel.mUsbDevicesInfoBeans.observe(this, usbDevicesInfoBeans -> {
            LogUtils.logD(TAG, "mUsbDevicesInfoBeans :: invoke ");
            if (null != usbDevicesInfoBeans && usbDevicesInfoBeans.size() > 0) {
                this.mUsbDevicesInfoBeans = usbDevicesInfoBeans;
            }
        });
        mViewModel.mUsbDevices.observe(this, this::usbDevicesChange);
        mViewModel.mClearAdapter.observe(this, ob -> {
            LogUtils.logD(TAG, "mClearAdapter :: invoke ");
            if (ob) {
                LogUtils.logD(TAG, "mClearAdapter :: do ");
                mMusicSearchAdapter.clearDate();
            }
        });
        initEvents();
    }

    private void initEvents() {
        mBinding.etMusicSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                LogUtils.logD(TAG, "onEditorAction :: invoke :: action " + action);
                if (action == EditorInfo.IME_ACTION_SEARCH) {
                    LogUtils.logD(TAG, "onEditorAction :: IME_ACTION_SEARCH ");
                    mViewModel.mKeywordSearch.set(textView.getText().toString());
                    if (!TextUtils.isEmpty(mViewModel.mKeywordSearch.get())) {
                        try {
                            mViewModel.mUsbDevicesInfoBeans.postValue(
                                    mViewModel.mUsbMusicManager.getUsbDevices());
                            mViewModel.mSearchResultAudio.postValue(
                                    mViewModel.mUsbMusicManager.getAudioInfo(
                                            mViewModel.mKeywordSearch.get()));
                        } catch (RemoteException error) {
                            error.printStackTrace();
                        }
                    } else {
                        ToastUtils.showToast(getResources()
                                .getString(R.string.search_content_cannot_empty));
                        mMusicSearchAdapter.clearDate();
                        mMusicSearchAdapter.notifyDataSetChanged();
                    }
                }
                return true;
            }
        });
    }

    public void setDeviceMap(List<UsbDevicesInfoBean> usbDevicesInfoBeans) {
        mUsbDevicesInfoBeans = usbDevicesInfoBeans;
    }

    private void setSearchAdapter(List<AudioInfoBean> audioInfoBeans) {
        hideInput();
        if (audioInfoBeans.size() > 0) {
            mBinding.tvSearchInfo.setVisibility(View.GONE);
            mMusicSearchAdapter.setSearchKey(mViewModel.mKeywordSearch.get());
            SortUtils.sortSearchResult(audioInfoBeans, mViewModel.mKeywordSearch.get());
            mMusicSearchAdapter.setUsbDevicesInfoBeans(mUsbDevicesInfoBeans);
            mMusicSearchAdapter.setNewData(audioInfoBeans);
            mMusicSearchAdapter.setOnItemClickListener((helper, resultBean, position) -> {
                SearchAudioInfoManager.getInstance().setAudioInfo(resultBean);
                fragmentBackStack();
            });
        } else {
            if (null != getActivity()) {
                mBinding.tvSearchInfo.setText(String.format(getActivity()
                                .getString(R.string.usb_music_search_empty),
                        '"' + mViewModel.mKeywordSearch.get() + '"'));
                mBinding.tvSearchInfo.setVisibility(View.VISIBLE);
            }
            mMusicSearchAdapter.clearDate();
        }
    }

    private void fragmentBackStack() {
        if (null != getActivity()) {
            hideInput();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != getActivity()) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                    }
                }
            }, BACK_DELAY);

        }
    }

    /**
     * Notification interface update.
     *
     * @param usbList U-disk inserted
     */
    private void usbDevicesChange(List<UsbDevicesInfoBean> usbList) {
        if (usbList.size() == 0) {
            mViewModel.mKeywordSearch.set("");
            mMusicSearchAdapter.clearDate();
            hideInput();
            mViewModel.uiChangeObservable.backEvent.call();
        } else {
            mMusicSearchAdapter.setEmptyView(getEmptyView(R.layout.usb_music_no_data_view));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideInput();
    }
}
