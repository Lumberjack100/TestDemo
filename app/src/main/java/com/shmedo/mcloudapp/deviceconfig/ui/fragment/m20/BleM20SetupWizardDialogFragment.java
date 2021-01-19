package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/19/21 <br/>
 * 描述：    M20 设置向导弹窗
 */
public class BleM20SetupWizardDialogFragment extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_close)
    ImageView mIvClose;

    @BindView(R.id.tv_content)
    TextView mTvContent;

    @BindView(R.id.dispatchCmdFailedView)
    View dispatchCmdFailedView;

    @BindView(R.id.tv_description)
    TextView mTvErrorDesc;//错误描述信息

    @BindView(R.id.tv_left)
    TextView mTvLeft;

    @BindView(R.id.tv_right)
    TextView mTvRight;

    private BaseGOCBleIotCommunicateFragment baseGOCBleIotCommunicateFragment;

    private Handler uiHander = new Handler();

    private ProgressRunnable progressRunnable;//常规任务

    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            progressRunnable = null;
            updateState(false);
        }
    }

    protected void startProgressRunnable(long delayMillis) {
        if (progressRunnable == null) {
            progressRunnable = new ProgressRunnable();
            uiHander.postDelayed(progressRunnable, delayMillis);
        }
    }

    protected void stopProgressRunnable() {
        uiHander.removeCallbacksAndMessages(null);
        progressRunnable = null;
    }

    public static BleM20SetupWizardDialogFragment newInstance() {
        return new BleM20SetupWizardDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_m20_setup_wizard_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        this.setCancelable(false);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.8f);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baseGOCBleIotCommunicateFragment = (BaseGOCBleIotCommunicateFragment) getParentFragment();
        initView();
    }

    private void initView() {
        mTvTitle.setText("设置向导");
        mTvContent.setText("是否水平初始化？");
        mTvErrorDesc.setText("水平初始化失败");
        mTvContent.setVisibility(View.VISIBLE);
        dispatchCmdFailedView.setVisibility(View.GONE);
        mTvLeft.setText("跳过");
        mTvRight.setText("是");
    }

    @OnClick({R.id.iv_close, R.id.tv_left, R.id.tv_right})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        int id = v.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.tv_left) {
            dismiss();
            DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DataCenterConfigMethod.BASIC_CONFIG, true);

            ToastUtils.show("跳过");
        } else if (id == R.id.tv_right) {
            if (!baseGOCBleIotCommunicateFragment.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            mTvContent.setText("正在水平初始化...");
            setLevelInitial();
            startProgressRunnable(7000);
            disableTouch();
        }
    }

    /**
     * 水平初始化
     */
    private void setLevelInitial() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_LEVEL_INITIAL);
        baseGOCBleIotCommunicateFragment.sendCommand(command);
    }

    private void disableTouch() {
        //TODO android:clickable="true" 和 android:focusable="true" 已经实现了禁止触摸遮罩层下面的 View,
        // 防止点击未遮住的ToolBar，添加下面代码禁用窗体触摸
//        getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mIvClose.setEnabled(false);
        mTvLeft.setEnabled(false);
        mTvRight.setEnabled(false);
    }

    private void enableTouch() {
        //get user interaction back
//        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mIvClose.setEnabled(true);
        mTvLeft.setEnabled(true);
        mTvRight.setEnabled(true);
    }

    public void updateState(boolean isLevelInitSucc) {
        if (mTvContent == null)
            return;
        stopProgressRunnable();
        enableTouch();

        if (isLevelInitSucc) {
            dismiss();
            DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DataCenterConfigMethod.BASIC_CONFIG, true);

        } else {
            mTvContent.setVisibility(View.GONE);
            dispatchCmdFailedView.setVisibility(View.VISIBLE);
            mTvRight.setText("重新尝试");
        }
    }
}