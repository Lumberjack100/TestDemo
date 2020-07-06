package com.shmedo.mcloudapp.ui;

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
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/1 <br/>
 * 描述：    隐私权限提示
 */
public class PrivacyTipDialog extends DialogFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setWindowStyle(Gravity.CENTER);
        View rootView = inflater.inflate(R.layout.fragment_privacy_dialog, container, false);
        ButterKnife.bind(this, rootView);
        initView();
        return rootView;
    }

    private void setWindowStyle(int gravity) {
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        //无标题  必须放在setContextView之前调用
        window.requestFeature(Window.FEATURE_NO_TITLE);
        //Sets whether this dialog is canceled when touched outside the window's bounds.
        mDialog.setCanceledOnTouchOutside(false);
        //Sets whether this dialog is cancelable with the BACK key.
        setCancelable(false);
        window.setWindowAnimations(R.style.share_animation);
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

    private void initView() {

    }

    @OnClick({R.id.btn_agree, R.id.btn_deny})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_agree:
                doPositiveClick(view);
                break;

            case R.id.btn_deny:
                doNegativeClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onPositiveClick(view);
        dismiss();
    }

    private void doNegativeClick(View view) {
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }


    public interface DialogFragmentClickListener {
        void onPositiveClick(View view);

        void onNegativeClick(View view);
    }
}
