package com.ts.music.ui.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentPlaylistBinding;
import com.ts.music.entity.Au;
import com.ts.music.ui.adapter.RadioCurrentlyListAdapter;
import com.ts.music.ui.viewmodel.PlayListViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlayListFragment extends BaseFragment<FragmentPlaylistBinding , PlayListViewModel> {
    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    private RadioCurrentlyListAdapter radioListAdapter;
    private List<Au> list;
    private Fragment currentFragment;
    private FragmentTransaction ft;
    private List<Fragment> fragmentList;
    private FmFragment fmFragment;
    private AmFragment amFragment;
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_playlist;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        list = new ArrayList<>();
        radioListAdapter = new RadioCurrentlyListAdapter();
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
        fmFragment = FmFragment.newInstance();
        amFragment = AmFragment.newInstance();
        fragmentList = new ArrayList<>();
        fragmentList.add(0,fmFragment);
        fragmentList.add(1,amFragment);

        switchFragment(0);
        mBinding.rgToolbar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_allChannel:
                        switchFragment(0);
                        break;
                    case R.id.rb_Favorite:
                        switchFragment(1);
                        break;
                }
            }
        });
    }

    private void  switchFragment(Integer index) {
        Log.e("00000000", "switchFragment: "+index);
        setViewAnimator(index);
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
        if (!fragment.isAdded() && TextUtils.isEmpty(fragment.getTag())) {
            ft.add(R.id.frameLayout2, fragment, fragment.getClass().getName());
        } else {
            ft.show(fragment);
        }
        ft.commitAllowingStateLoss();
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
                animator = ObjectAnimator.ofFloat(mBinding.ivLeft, "translationX", 0f);
                animator.setDuration(300);
                animator.start();
                break;

            case 1 :
                animator = ObjectAnimator.ofFloat(mBinding.ivLeft, "translationX", 140f);
                animator.setDuration(300);
                animator.start();
                break;
        }
    }
}
