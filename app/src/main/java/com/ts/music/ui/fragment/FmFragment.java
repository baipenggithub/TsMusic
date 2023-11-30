package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentFmBinding;
import com.ts.music.ui.viewmodel.FmViewModel;

public class FmFragment extends BaseFragment<FragmentFmBinding, FmViewModel> {
    public static FmFragment newInstance() {
        return new FmFragment();
    }
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_fm;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();

    }
}
