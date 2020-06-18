package com.shmedo.mcloudapp.maps.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

import com.shmedo.mcloudapp.R;

import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/18 <br/>
 * 描述：    TODO
 */
public abstract class BaseSearchPoiDialogFragment extends DialogFragment {

    protected abstract int initContentView();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setWindowStyle();

        View rootView = inflater.inflate(initContentView(), container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    private void setWindowStyle() {
        Dialog mDialog = getDialog();
        //Sets whether this dialog is canceled when touched outside the window's bounds.
        mDialog.setCanceledOnTouchOutside(true);
        //Sets whether this dialog is cancelable with the BACK key.
        setCancelable(false);
        Window window = mDialog.getWindow();
        //无标题  必须放在setContextView之前调用
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setWindowAnimations(R.style.share_animation);
        // 设置宽度为屏宽、靠近屏幕底部。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
    }
}
