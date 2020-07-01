package com.shmedo.mcloudapp.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/1 <br/>
 * 描述：    隐私权限提示
 */
public class PrivacyTipDialog extends DialogFragment {
    private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

    private boolean finishActivity = false;

    /**
     * Creates a new instance of this dialog and optionally finishes the calling Activity
     * when the 'Ok' button is clicked.
     */
    public static PrivacyTipDialog newInstance(boolean finishActivity) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);

        PrivacyTipDialog dialog = new PrivacyTipDialog();
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        finishActivity = getArguments().getBoolean(ARGUMENT_FINISH_ACTIVITY);

        return new AlertDialog.Builder(getActivity())
                .setMessage("")
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (finishActivity) {
            getActivity().finish();
        }
    }
}
