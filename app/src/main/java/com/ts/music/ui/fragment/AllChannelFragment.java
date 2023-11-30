package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentAllchannelBinding;
import com.ts.music.entity.RadioBean;
import com.ts.music.ui.adapter.RadioListAdapter;
import com.ts.music.ui.viewmodel.AllChannelViewModel;
import java.util.ArrayList;
import java.util.List;

public class AllChannelFragment extends BaseFragment<FragmentAllchannelBinding, AllChannelViewModel> {
    public static AllChannelFragment newInstance() {
        return new AllChannelFragment();
    }
    private RadioListAdapter radioListAdapter;
    private List<RadioBean> list;
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_allchannel;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        list = new ArrayList<>();
        radioListAdapter = new RadioListAdapter();
        mBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        for (int i = 1; i < 50; i++) {
            RadioBean radioBean = new RadioBean();
            radioBean.setName("FM103.7");
            if (i<10){
                radioBean.setNum("0"+i);
            }
            else {
                radioBean.setNum(i+"");
            }
            list.add(radioBean);
        }
        radioListAdapter.setDataList(list);
        mBinding.mRecyclerView.setAdapter(radioListAdapter);
    }
}
