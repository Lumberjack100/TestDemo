package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.util.XPermissionUtils;

import butterknife.ButterKnife;
import timber.log.Timber;


/**
 *
 */
public abstract class BaseDialogFragment extends DialogFragment {
    protected abstract int initContentView();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setWindowStyle();

        View rootView = inflater.inflate(initContentView(), container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }


    private void setWindowStyle() {
        Dialog mDialog=getDialog();
        Window window = mDialog.getWindow();
        //无标题  必须放在setContextView之前调用
        window.requestFeature(Window.FEATURE_NO_TITLE);
        //Sets whether this dialog is canceled when touched outside the window's bounds.
        mDialog.setCanceledOnTouchOutside(true);
        //Sets whether this dialog is cancelable with the BACK key.
        setCancelable(false);
        window.setWindowAnimations(R.style.share_animation);
        //window外可以点击,不拦截窗口外的事件
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // 设置宽度为屏宽、靠近屏幕底部。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    protected void doScanButtonClick() {
        XPermissionUtils.requestPermissionsResult(getActivity(), 200, new String[]{
                        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        ScanActivity.startActivityForResultByFragment(BaseDialogFragment.this, XPermissionUtils.REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onPermissionDenied() {
                        XPermissionUtils.showRefusePermissionDialog(getActivity(),
                                getActivity().getResources().getString(R.string.permission_request_camera_external_storage));
                    }
                });
    }

    protected void scanResult(String content)
    {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case XPermissionUtils.REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                        Timber.d("扫描结果为：" + content);
                        scanResult(content);
                    }
                }
                break;
        }
    }

    public interface DialogFragmentClickListener {

        boolean onPositiveClick(View view);

        void onNegativeClick(View view);
    }
}
