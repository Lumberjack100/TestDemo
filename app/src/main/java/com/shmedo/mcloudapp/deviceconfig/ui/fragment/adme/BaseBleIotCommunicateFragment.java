package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

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
import com.shmedo.mcloudapp.deviceconfig.viewmodels.AdmeViewModel;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.ConfigPageViewModel;
import com.shmedo.mcloudapp.profile.USRBleViewModel;

import java.util.UUID;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：     TODO #gh#
 */
public abstract class BaseBleIotCommunicateFragment extends BaseFragment {
    public static final int WRITE_TIME_OUT_SECOND = 10000;//发送指令超时时间

    //自定义心跳包指令
    private final String heartBeat = IOTCommandManager.getInstance().getCommand(IOTCommandType.HEART_BEAT)
            + "&apikey=b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9"
            + "&msgid=" + UUID.randomUUID().toString().substring(30);

    protected USRBleViewModel usrBleViewModel;

    protected ConfigPageViewModel configPageViewModel;

    protected AdmeViewModel admeViewModel;

    public boolean isExitMode = false;//是否退出页面标志

    protected String errMsg;

    private Handler uiHander = new Handler();

    private Handler heartHander = new Handler();//心跳包处理

    private ProgressRunnable progressRunnable;//常规任务

    private HeartRunnable heartRunnable;//心跳包任务

    /**
     * 发送心跳包任务
     */
    private class HeartRunnable implements Runnable {
        @Override
        public void run() {
            if (isConnected()) {
                sendHeartData();
                heartHander.postDelayed(this, 30000);
            }
        }
    }

    protected void startHeartRunnable() {
        if (heartRunnable == null) {
            heartRunnable = new HeartRunnable();
            heartHander.postDelayed(heartRunnable, 10000);
        }
    }

    protected void stopHeartRunnable() {
        heartHander.removeCallbacksAndMessages(null);
        heartRunnable = null;
    }

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
        usrBleViewModel = getApplicationScopeViewModel(USRBleViewModel.class);
        usrBleViewModel.getResponseMsg().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String result) {
                if (!result.startsWith("$cmd=")) {
                    if (usrBleViewModel.getLogOutputMode().getValue() == null || !usrBleViewModel.getLogOutputMode().getValue()) {
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
        admeViewModel = getApplicationScopeViewModel(AdmeViewModel.class);
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
        usrBleViewModel.connect(device);
    }

    /**
     * ble 取消连接
     */
    protected void disconnectDevice() {
        Timber.d("disconnectDevice()调用");
        usrBleViewModel.disconnect();
    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    protected final boolean isConnected() {
        return usrBleViewModel.isConnected();
    }

    protected void clearDevice() {
        usrBleViewModel.clearDevice();
    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {

    }

    /**
     * 发送心跳数据(未定义的指令)
     */
    protected void sendHeartData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.HEART_BEAT);
        Timber.d("发送心跳数据：%s", command);
        sendCommand(command);
    }

    /**
     * 保存配置信息
     */
    protected void saveConfigInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM);
        sendCommand(command);
    }

    protected void sendCommand(String cmdStr) {
        if (!isConnected()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (!TextUtils.isEmpty(usrBleViewModel.getDeviceApiKey().getValue())) {
            apiKey = usrBleViewModel.getDeviceApiKey().getValue();
        }
        if (!cmdStr.contains("&apikey")) {
            cmdStr += "&apikey=" + apiKey
                    + "&msgid=" + UUID.randomUUID().toString().substring(30);
        }

        usrBleViewModel.sendIOTProtocolCommand(cmdStr);
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
