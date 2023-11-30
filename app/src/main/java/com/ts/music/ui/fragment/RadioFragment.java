package com.ts.music.ui.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ts.music.BR;
import com.ts.music.R;
import com.ts.music.base.BaseFragment;
import com.ts.music.databinding.FragmentRadioBinding;
import com.ts.music.ui.viewmodel.RadioViewModel;

import java.util.ArrayList;
import java.util.List;


public class RadioFragment extends BaseFragment<FragmentRadioBinding,RadioViewModel>{
    private Fragment currentFragment;
    private FragmentTransaction ft;
    private List<Fragment> fragmentList;
    private AllChannelFragment allChannelFragment;
    private FavoriteFragment favoriteFragment;
    private PlayListFragment playListFragment;
    public static RadioFragment newInstance() {
        return new RadioFragment();
    }

    @Override
    public int initContentView(LayoutInflater inflater,
                               @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_radio;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        allChannelFragment = AllChannelFragment.newInstance();
        favoriteFragment = FavoriteFragment.newInstance();
        playListFragment = PlayListFragment.newInstance();
        fragmentList = new ArrayList<>();
        fragmentList.add(0,allChannelFragment);
        fragmentList.add(1,favoriteFragment);
        fragmentList.add(2,playListFragment);
        switchFragment(0,false);
        mBinding.rgToolbar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_allChannel:
                        switchFragment(0,false);
                        break;
                    case R.id.rb_Favorite:
                        switchFragment(1,false);
                        break;
                }
            }
        });

        mBinding.imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.imageView3.setImageResource(R.drawable.icon_favorite_like);
            }
        });

        mBinding.radioItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.frameLayout3.setVisibility(View.VISIBLE);
                mBinding.rlTop.setVisibility(View.INVISIBLE);
                mBinding.imageView.setVisibility(View.INVISIBLE);
                mBinding.llLabel.setVisibility(View.INVISIBLE);
                switchFragment(2,true);
            }
        });

    }

    private void  switchFragment(Integer index , Boolean isShow) {
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
        if (isShow){
            if (!fragment.isAdded() && TextUtils.isEmpty(fragment.getTag())) {
                ft.add(R.id.frameLayout3, fragment, fragment.getClass().getName());
            } else {
                ft.show(fragment);
            }
        }else {
            if (!fragment.isAdded() && TextUtils.isEmpty(fragment.getTag())) {
                ft.add(R.id.frameLayout, fragment, fragment.getClass().getName());
            } else {
                ft.show(fragment);
            }
        }

        ft.commitAllowingStateLoss();
    }

    private void setViewAnimator(Integer index) {
        if (index == 2){
            return;
        }

        if (index == 0) {
            mBinding.rgToolbar.check(R.id.rb_allChannel);
        } else {
            mBinding.rgToolbar.check(R.id.rb_Favorite);
        }
        ObjectAnimator animator;

        switch (index) {
            case 0 :
                ViewGroup.LayoutParams layoutParams = mBinding.ivLeft.getLayoutParams();
                layoutParams.width = 160;
                mBinding.ivLeft.setLayoutParams(layoutParams);
                animator = ObjectAnimator.ofFloat(mBinding.ivLeft, "translationX", 0f);
                animator.setDuration(300);
                animator.start();
                break;

            case 1 :
                ViewGroup.LayoutParams layoutParams1 = mBinding.ivLeft.getLayoutParams();
                layoutParams1.width = 116;
                mBinding.ivLeft.setLayoutParams(layoutParams1);
                animator = ObjectAnimator.ofFloat(mBinding.ivLeft, "translationX", 255f);
                animator.setDuration(300);
                animator.start();
                break;
        }
    }
}
