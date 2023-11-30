package com.ts.music.ui.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public abstract class AbstractCustomDialogFragment extends DialogFragment {
    private static final int SCALE_RATE = 20;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog alertDialog = new Dialog(getContext());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View customView = onCreateDialogView();
        alertDialog.setContentView(customView);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = alertDialog.getWindow().getAttributes();
        windowParams.width = (int) (getResources().getDisplayMetrics().widthPixels
                - getResources().getDisplayMetrics().density * SCALE_RATE);
        alertDialog.getWindow().setAttributes(windowParams);

        return alertDialog;
    }

    //Provide an Interface to custom dialog view
    public abstract View onCreateDialogView();
}
