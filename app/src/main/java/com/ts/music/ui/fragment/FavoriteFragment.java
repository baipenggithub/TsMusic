package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentFavoriteBinding;
import com.ts.music.entity.Au;
import com.ts.music.ui.adapter.RadioFavoriteListAdapter;
import com.ts.music.ui.viewmodel.AllChannelViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends BaseFragment<FragmentFavoriteBinding, AllChannelViewModel> {
    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }
    private RadioFavoriteListAdapter radioListAdapter;
    private List<Au> list;
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_favorite;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        list = new ArrayList<>();
        radioListAdapter = new RadioFavoriteListAdapter();
        mBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        for (int i = 1; i < 50; i++) {
            Au radioBean = new Au();
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
