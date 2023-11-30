package com.ts.music.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;

import com.ts.music.R;


public final class SwitchAudioInDialog extends AbstractCustomDialogFragment {
    private View mView;

    @Override
    public View onCreateDialogView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.switch_audioin, null);
        mView.findViewById(R.id.switch_audio_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mView.findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO switch audioInput to BlueTooth AudioSource
            }
        });
        mView.findViewById(R.id.button_deny).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO cancel switch and return to original interface
            }
        });
        return mView;
    }
}

