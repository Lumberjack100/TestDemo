package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

import com.shmedo.core.model.CollectorSensorParamsInfo;
import com.shmedo.mcloudapp.R;

import butterknife.ButterKnife;


/**
 *
 */
public abstract class BaseDialogFragment extends DialogFragment {
    protected CollectorSensorParamsInfo collectorSensorParamsInfo;

    protected String selectedChannelNumber = "";

    protected abstract int initContentView();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setWindowStyle(Gravity.BOTTOM);

        View rootView = inflater.inflate(initContentView(), container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }


    protected void setWindowStyle(int gravity) {
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        //无标题  必须放在setContextView之前调用
        window.requestFeature(Window.FEATURE_NO_TITLE);
        //Sets whether this dialog is canceled when touched outside the window's bounds.
        mDialog.setCanceledOnTouchOutside(true);
        //Sets whether this dialog is cancelable with the BACK key.
        mDialog.setCancelable(false);
        window.setWindowAnimations(R.style.DialogFragmentAnimation);
        //window外可以点击,不拦截窗口外的事件
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // 设置宽度为屏宽、靠近屏幕底部。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = gravity;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }


    public interface DialogFragmentClickListener<T> {

        boolean onPositiveClick(View view, T data);

        void onNegativeClick(View view);
    }
}
