package com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.ConfigPageViewModel;
import com.shmedo.mcloudapp.profile.GOCBleViewModel;

import java.util.UUID;

import timber.log.Timber;
/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/20/21 <br/>
 * 描述：    与深圳市顾凯信息技术有限公司GOC-MD-400蓝牙模块通讯的页面基类
 */
public abstract class BaseGOCBleIotCommunicateFragment extends BaseFragment {
    public static final int WRITE_TIME_OUT_SECOND = 10000;//发送指令超时时间

    //自定义心跳包指令
    private final String heartBeat = IOTCommandManager.getInstance().getCommand(IOTCommandType.HEART_BEAT)
            + "&apikey=b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9"
            + "&msgid=" + UUID.randomUUID().toString().substring(30);

    protected GOCBleViewModel bleViewModel;

    protected ConfigPageViewModel configPageViewModel;

    public boolean isExitMode = false;//是否退出页面标志

    protected String errMsg;

    private Handler uiHander = new Handler();

    private ProgressRunnable progressRunnable;//常规任务

    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            dismissProgressDialog();
            progressRunnable = null;
            doProgressRun();
        }
    }

    protected void doProgressRun() {
        if (!TextUtils.isEmpty(errMsg)) {
            ToastUtils.show(errMsg);
        }
    }

    protected void startProgressRunnable(String dialogContent, long delayMillis) {
        showProgressDialog(dialogContent, null, null);
        if (progressRunnable == null) {
            progressRunnable = new ProgressRunnable();
            uiHander.postDelayed(progressRunnable, delayMillis);
        }
    }

    protected void stopProgressRunnable() {
        dismissProgressDialog();
        uiHander.removeCallbacksAndMessages(null);
        progressRunnable = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bleViewModel = getApplicationScopeViewModel(GOCBleViewModel.class);
        bleViewModel.getResponseMsg().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String result) {
                if (!result.startsWith("$cmd=")) {
                    if (bleViewModel.getLogOutputMode().getValue() == null || !bleViewModel.getLogOutputMode().getValue()) {
                        return;
                    }
                }
                try {
                    parseResponseMessage(result);
                } catch (Exception ex) {
                    Timber.e(ex);
                }
            }
        });
        configPageViewModel = getActivityScopeViewModel(ConfigPageViewModel.class);
        configPageViewModel.configPageEditableChanged.observeInFragment(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEditable) {
                onEditableChanged(isEditable);
            }
        });
    }

    protected void onEditableChanged(boolean isEditable) {
    }

    @Override
    public void onStop() {
        super.onStop();
        stopProgressRunnable();
    }

    /**
     * ble 建立连接
     */
    protected void connectDevice(BluetoothDevice device) {
        bleViewModel.connect(device);
    }

    /**
     * ble 取消连接
     */
    protected void disconnectDevice() {
        Timber.d("disconnectDevice()调用");
        bleViewModel.disconnect();
    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    public final boolean isConnected() {
        return bleViewModel.isConnected();
    }

    protected void clearDevice() {
        bleViewModel.clearDevice();
    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {

    }

    public void sendCommand(String cmdStr) {
        if (!isConnected()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (!TextUtils.isEmpty(bleViewModel.getDeviceApiKey().getValue())) {
            apiKey = bleViewModel.getDeviceApiKey().getValue();
        }
        cmdStr += "&apikey=" + apiKey
                + "&msgid=" + UUID.randomUUID().toString();

        bleViewModel.sendIOTProtocolCommand(cmdStr);
    }

    protected void showDisconnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        disconnectDevice();
                        if (isExitMode) {
                            mActivity.finish();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    protected void warnNotYetSettingBeforeLeavePage() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content("您已经修改了参数，还未配置到设备，确定离开页面吗？")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mActivity.finish();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

}