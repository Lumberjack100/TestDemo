package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ScreenUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/19/21 <br/>
 * 描述：    蓝牙模式M20 设置向导弹窗
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

    @BindView(R.id.tv_dispatch_cmd_failed_desc)
    TextView mTvErrorDesc;//错误描述信息

    @BindView(R.id.tv_left)
    TextView mTvLeft;

    @BindView(R.id.tv_right)
    TextView mTvRight;

    private BaseUSRBleIotCommunicateFragment baseUSRBleIotCommunicateFragment;


    private final InnerHandler mInnerHandler = new InnerHandler(this);

    private static class InnerHandler extends Handler {
        private final WeakReference<BleM20SetupWizardDialogFragment> fragmentWeakReference;

        public InnerHandler(BleM20SetupWizardDialogFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            BleM20SetupWizardDialogFragment fragment = fragmentWeakReference.get();
            if (fragment != null) {
                fragment.updateState(false);
            }
        }
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
        wlp.width = (int) (ScreenUtils.getScreenWidth() * 0.8f);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        baseUSRBleIotCommunicateFragment = (BaseUSRBleIotCommunicateFragment) getParentFragment();
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
            DataCenterHomeActivity.startActivity(mActivity, ProductType.M20, AppContants.CommunicationWay.BLE_CONNECT, true);

        } else if (id == R.id.tv_right) {
            if (!baseUSRBleIotCommunicateFragment.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            mInnerHandler.sendEmptyMessageDelayed(1, 10000);
            mTvContent.setText("正在水平初始化...");
            setLevelInitial();
            disableTouch();
        }
    }

    /**
     * 水平初始化
     */
    private void setLevelInitial() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_LEVEL_INITIAL);
        baseUSRBleIotCommunicateFragment.sendCommand(command);
    }

    private void disableTouch() {
        mIvClose.setEnabled(false);
        mTvLeft.setEnabled(false);
        mTvRight.setEnabled(false);
    }

    private void enableTouch() {
        mIvClose.setEnabled(true);
        mTvLeft.setEnabled(true);
        mTvRight.setEnabled(true);
    }

    public void updateState(boolean isLevelInitSucc) {
        if (mTvContent == null) {
            return;
        }
        mInnerHandler.removeCallbacksAndMessages(null);
        enableTouch();

        if (isLevelInitSucc) {
            mTvContent.setText("初始化完成");
            mInnerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    DataCenterHomeActivity.startActivity(mActivity, ProductType.M20, AppContants.CommunicationWay.BLE_CONNECT, true);
                }
            }, 1500);
        } else {
            mTvContent.setVisibility(View.GONE);
            dispatchCmdFailedView.setVisibility(View.VISIBLE);
            mTvRight.setText("重新尝试");
        }
    }

    @Override
    public void onStop() {
        mInnerHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }
}