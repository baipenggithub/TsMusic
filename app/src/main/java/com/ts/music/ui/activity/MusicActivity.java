package com.ts.music.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.ts.music.R;
import com.ts.music.constants.MusicConstants;
import com.ts.music.databinding.ActivityMainBinding;
import com.ts.music.ui.fragment.BtMusicFragment;
import com.ts.music.ui.fragment.RadioFragment;
import com.ts.music.ui.fragment.UsbMusicFragment;
import com.ts.music.ui.viewmodel.MusicActivityViewModel;
import com.ts.music.utils.LogUtils;


/**
 * MusicActivity.
 */

public class MusicActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "MusicActivity";
    private static final String INTENT_KEY = "module";
    private static final String INTENT_BT_VALUE = "bt";
    private static final String INTENT_USB_VALUE = "usb";
    private static final String INTENT_RADIO_VALUE = "online";
    private static final String INTENT_SAVE_VALUE = "module";
    private static final int SWITCH_FRAGMENT = 0;

    private UsbMusicFragment mUsbPlayerFragment;
    private BtMusicFragment mBtMusicMainFragment;
    private RadioFragment mRadioFragment;
    private FragmentManager mFragmentManager;

    private ActivityMainBinding mBinding;
    private MusicActivityViewModel mViewModel;
    private int mFragmentType = -1;
    private boolean mIsInit = false;

    private final Handler mHandler = new Handler(msg -> {
        LogUtils.logD(TAG, "position : " + msg.arg1);
        switchFragment(msg.arg1);
        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logD(TAG, "onCreate :: invoke");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        if (null != savedInstanceState) {
            mFragmentType = savedInstanceState.getInt(INTENT_SAVE_VALUE);
            LogUtils.logD(TAG, "onCreate :: type" + mFragmentType);
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MusicActivityViewModel.class);
        getLifecycle().addObserver(mViewModel);
        mFragmentManager = getSupportFragmentManager();
        mBinding.topTabBar.setOnCheckedChangeListener(this);
        initLayout();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.logD(TAG, "onNewIntent");
        if (mIsInit && mFragmentType != -1) {
            chooseFragment(intent, true);
        } else {
            chooseFragment(intent, false);
        }
        mIsInit = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(SWITCH_FRAGMENT);
    }

    /**
     * initLayout.
     */
    public void initLayout() {
        mIsInit = true;
        Intent intent = getIntent();
        chooseFragment(intent, true);
    }

    private void chooseFragment(Intent intent, boolean needNull) {
        LogUtils.logD(TAG, "chooseFragment :: invoke :: needNull " + needNull);
        if (intent == null) {
            LogUtils.logD(TAG, "chooseFragment :: intent  null ");
            if (needNull) {
                if (mFragmentType != -1) {
                    selectRadioBt(mFragmentType);
                } else {
                    selectRadioBt(MusicConstants.BT_MUSIC);
                }
            }
        } else {
            String module = intent.getStringExtra(INTENT_KEY);
            if (!TextUtils.isEmpty(module)) {
                LogUtils.logD(TAG, "chooseFragment :: module   " + module);
                switch (module) {
                    case INTENT_USB_VALUE:
                        selectRadioBt(MusicConstants.USB_MUSIC);
                        break;
                    case INTENT_RADIO_VALUE:
                        selectRadioBt(MusicConstants.RADIO_MUSIC);
                        break;
                    case INTENT_BT_VALUE:
                    default:
                        selectRadioBt(MusicConstants.BT_MUSIC);
                        break;
                }
            } else {
                LogUtils.logD(TAG, "chooseFragment :: module  empty ");
                if (mFragmentType != -1) {
                    selectRadioBt(mFragmentType);
                } else {
                    if (mBinding.rbRadio.isChecked()) {
//                        startOnlineMusic();
                    } else {
                        selectRadioBt(MusicConstants.BT_MUSIC);
                    }
                }
            }
        }
    }

    private void setTabBarBac(boolean btMusic, boolean onLineMusic, boolean usbMusic) {
        LogUtils.logD(TAG, "setTabBarBac btMusic : " + btMusic
                + "  onLineMusic : " + onLineMusic
                + "  usbMusic : " + usbMusic);
        mBinding.rbBtMusic.setBackground(btMusic ? getResources()
                .getDrawable(R.drawable.bg_selector_rb) : getResources()
                .getDrawable(R.color.color_transparent));
        mBinding.rbBtMusic.setTextColor(btMusic ? getResources()
                .getColor(R.color.music_toast_color) : getResources()
                .getColor(R.color.bg_rb_tv_color));
        mBinding.rbRadio.setBackground(onLineMusic ? getResources()
                .getDrawable(R.drawable.bg_selector_rb) : getResources()
                .getDrawable(R.color.color_transparent));
        mBinding.rbRadio.setTextColor(onLineMusic ? getResources()
                .getColor(R.color.music_toast_color) : getResources()
                .getColor(R.color.bg_rb_tv_color));
        mBinding.rbUsbMusic.setBackground(usbMusic ? getResources()
                .getDrawable(R.drawable.bg_selector_rb) : getResources()
                .getDrawable(R.color.color_transparent));
        mBinding.rbUsbMusic.setTextColor(usbMusic ? getResources()
                .getColor(R.color.music_toast_color) : getResources()
                .getColor(R.color.bg_rb_tv_color));
        mBinding.rbRadio.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.selector_radio)
                , null, onLineMusic ? getResources()
                        .getDrawable(R.drawable.icon_selector_rb) : null, null);
        mBinding.rbUsbMusic.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.selector_usb)
                , null, usbMusic ? getResources()
                        .getDrawable(R.drawable.icon_selector_rb) : null, null);

        mBinding.rbBtMusic.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.selector_bluetooth)
                , null, btMusic ? getResources()
                        .getDrawable(R.drawable.icon_selector_rb) : null, null);
    }

    private void switchFragment(int position) {
        LogUtils.logD(TAG, "switchFragment :: invoke :: position :: " + position);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        hindFragment(transaction);
        mViewModel.updateCurrentTab(position);
        mFragmentType = position;
        Intent intent = getIntent();
        switch (position) {
            case MusicConstants.BT_MUSIC:
                LogUtils.logD(TAG, " switch BT_MUSIC");
                setTabBarBac(true, false, false);
                if (null == mBtMusicMainFragment) {
                    LogUtils.logD(TAG, "switchFragment :: mBtMusicMainFragment");
                    mBtMusicMainFragment = BtMusicFragment.newInstance();
                    transaction.add(R.id.content_frame_layout, mBtMusicMainFragment);
                } else {
                    transaction.show(mBtMusicMainFragment);
                }
                intent.putExtra(INTENT_KEY, INTENT_BT_VALUE);
                setIntent(intent);
                break;
            case MusicConstants.RADIO_MUSIC:
                LogUtils.logD(TAG, " switch ONLINE_MUSIC");
                setTabBarBac(false, true, false);
//                startOnlineMusic();
                if (null == mRadioFragment) {
                    LogUtils.logD(TAG, "switchFragment :: mRadioFragment");
                    mRadioFragment = RadioFragment.newInstance();
                    transaction.add(R.id.content_frame_layout, mRadioFragment);
                } else {
                    transaction.show(mRadioFragment);
                }
                intent.putExtra(INTENT_KEY, INTENT_RADIO_VALUE);
                setIntent(intent);
                break;
            case MusicConstants.USB_MUSIC:
                LogUtils.logD(TAG, " switch USB_MUSIC");
                setTabBarBac(false, false, true);
                if (null == mUsbPlayerFragment) {
                    LogUtils.logD(TAG, "switchFragment :: mUsbPlayerFragment");
                    mUsbPlayerFragment = UsbMusicFragment.getInstance();
                    transaction.add(R.id.content_frame_layout, mUsbPlayerFragment);
                } else {
                    transaction.show(mUsbPlayerFragment);
                }
                intent.putExtra(INTENT_KEY, INTENT_USB_VALUE);
                setIntent(intent);
                break;
            default:
                break;

        }
        transaction.commit();
    }

    private void selectRadioBt(int type) {
        LogUtils.logD(TAG, "selectRadioBt type : " + type);
        switch (type) {
            case MusicConstants.BT_MUSIC:
                mBinding.topTabBar.check(R.id.rbBtMusic);
                break;
            case MusicConstants.RADIO_MUSIC:
                mBinding.topTabBar.check(R.id.rbRadio);
//                startOnlineMusic();
                break;
            case MusicConstants.USB_MUSIC:
                mBinding.topTabBar.check(R.id.rbUsbMusic);
                break;
            default:
                break;
        }
    }

    private void hindFragment(FragmentTransaction transaction) {
        mFragmentManager.popBackStack(null, MusicConstants.FLAGS);
        if (null != mBtMusicMainFragment) {
            transaction.hide(mBtMusicMainFragment);
        }
        if (null != mUsbPlayerFragment) {
            transaction.hide(mUsbPlayerFragment);
        }
        if (null != mRadioFragment) {
            transaction.hide(mRadioFragment);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(INTENT_SAVE_VALUE, mFragmentType);
        LogUtils.logD(TAG, "onSaveInstanceState :: mFragmentType :: " + mFragmentType);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        LogUtils.logD(TAG, "onCheckedChanged checkedId : " + checkedId);
        switch (checkedId) {
            case R.id.rbBtMusic:
                sendMessage(MusicConstants.BT_MUSIC);
                break;
            case R.id.rbRadio:
                sendMessage(MusicConstants.RADIO_MUSIC);
                break;
            case R.id.rbUsbMusic:
                sendMessage(MusicConstants.USB_MUSIC);
                break;
            default:
                break;
        }
    }

    private void sendMessage(int position) {
        LogUtils.logD(TAG, "sendMessage position : " + position);
        mHandler.removeMessages(SWITCH_FRAGMENT);
        Message message = new Message();
        message.arg1 = position;
        message.what = SWITCH_FRAGMENT;
        mHandler.sendMessage(message);
    }

//    private void startOnlineMusic() {
//        CommonUtils.startApp(MusicConstants.ONLINE_MUSIC_PACKAGE_NAME,
//                MusicConstants.ONLINE_MUSIC_MAIN_ACTIVITY, null, this);
//    }

}