package com.ts.music.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentAmBinding;
import com.ts.music.ui.viewmodel.AmViewModel;

public class AmFragment extends BaseFragment<FragmentAmBinding, AmViewModel> {
    public static AmFragment newInstance() {
        return new AmFragment();
    }
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_am;
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
