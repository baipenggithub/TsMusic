package com.ts.music.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.ts.music.constants.MusicConstants;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * BaseFragment.
 */
public abstract class BaseFragment<V extends ViewDataBinding, VMT extends BaseViewModel>
        extends Fragment implements IBaseView {
    protected V mBinding;
    protected VMT mViewModel;
    private int mViewModelId;
    private boolean mIsNavigationViewInit = false;
    private View mLastView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mLastView == null) {
            mBinding = DataBindingUtil.inflate(inflater, initContentView(inflater,
                    container, savedInstanceState), container, false);
            mBinding.setLifecycleOwner(this);
            mLastView = mBinding.getRoot();
        }
        return mLastView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            initViewDataBinding();
            initLayout();
            initData();
            initViewObservable();
    }

    private void initViewDataBinding() {
        mViewModelId = initVariableId();
        mViewModel = initViewModel();
        if (mViewModel == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[1];
            } else {
                modelClass = BaseViewModel.class;
            }
            mViewModel = (VMT) createViewModel(this, modelClass);
        }
        mBinding.setVariable(mViewModelId, mViewModel);
        getLifecycle().addObserver(mViewModel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBinding != null) {
            mBinding.unbind();
        }
    }

    /**
     * Refresh layout.
     */
    public void refreshLayout() {
        if (mViewModel != null) {
            mBinding.setVariable(mViewModelId, mViewModel);
        }
    }

    @Override
    public void initParam() {
        //TODO: initParam Reserved reference
    }

    /**
     * Initialize the root layout.
     *
     * @return layout id
     */
    public abstract int initContentView(LayoutInflater inflater, @Nullable ViewGroup container,
                                        @Nullable Bundle savedInstanceState);

    /**
     * Initialize the id of the ViewModel.
     *
     * @return BR id
     */
    public abstract int initVariableId();

    /**
     * Initialize ViewModel.
     *
     * @return ViewModel inheriting BaseViewModel
     */
    public VMT initViewModel() {
        return null;
    }

    @Override
    public void initData() {
        //TODO: initData Initialization
    }

    public void initLayout() {
        //TODO: initLayout Initialization
    }

    @Override
    public void initViewObservable() {
        //TODO: initViewObservable
    }

    /**
     * Create ViewModel.
     */
    public <T extends ViewModel> T createViewModel(Fragment fragment, Class<T> cls) {
        return ViewModelProviders.of(fragment).get(cls);
    }

    /**
     * Hide soft keyboard.
     */
    public void hideInput() {
        if (null != getActivity()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(),
                    MusicConstants.SOFT_INPUT_FLAGS);
        }
    }

    /**
     * Display soft keyboard.
     *
     * @param editText edit
     */
    public void showInput(EditText editText) {
        if (null != getActivity()) {
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, MusicConstants.SOFT_INPUT_FLAGS);
        }
    }

    /**
     * No data view.
     *
     * @return view
     */

    public View getEmptyView(int layoutRes) {
        View emptyView = getLayoutInflater().inflate(layoutRes, null);
        return emptyView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Nothing to do.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLastView = null;
    }
}