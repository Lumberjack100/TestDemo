package com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.AdmeViewModel;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.ConfigPageViewModel;
import com.shmedo.mcloudapp.profile.USRBleViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：    与有人物联网蓝牙模块通讯的页面基类
 */
public abstract class BaseUSRBleIotCommunicateFragment extends BaseFragment {
    protected static final int DELAY_5000_MILLIS = 5000;
    protected static final int DELAY_10000_MILLIS = 10000;//发送指令超时时间
    protected static final int DELAY_15000_MILLIS = 15000;//蓝牙连接超时时间

    //自定义心跳包指令
    private final String heartBeat = IOTCommandManager.getInstance().getCommand(IOTCommandType.HEART_BEAT)
            + "&apikey=b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9"
            + "&msgid=" + UUID.randomUUID().toString().substring(30);

    protected USRBleViewModel usrBleViewModel;

    protected ConfigPageViewModel configPageViewModel;

    protected AdmeViewModel admeViewModel;

    public boolean isExitMode = false;//是否退出页面标志

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);


    protected void customHandleMessage(@NonNull @NotNull Message msg) {

    }

    private static final class DefaultHandler extends WeakHandler<BaseUSRBleIotCommunicateFragment> {
        private DefaultHandler(BaseUSRBleIotCommunicateFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BaseUSRBleIotCommunicateFragment fragment) {
            fragment.dismissProgressDialog();
            fragment.customHandleMessage(msg);
        }
    }

    protected void startDefaultProgress(String dialogContent, int what, long delayMillis) {
        if (!TextUtils.isEmpty(dialogContent)) {
            showProgressDialog(dialogContent, null, null);
        }
        mDefaultHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected void stopDefaultProgress(int what) {
        dismissProgressDialog();
        mDefaultHandler.removeMessages(what);
    }

    protected void stopAllProgress() {
        dismissProgressDialog();
        mDefaultHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAllProgress();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        usrBleViewModel = getApplicationScopeViewModel(USRBleViewModel.class);
        usrBleViewModel.getResponseMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
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
        configPageViewModel.configPageEditableChanged.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEditable) {
                onEditableChanged(isEditable);
            }
        });
        admeViewModel = getApplicationScopeViewModel(AdmeViewModel.class);
    }

    protected void onEditableChanged(boolean isEditable) {
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
        stopAllProgress();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        if (type == IOTCommandType.UNKNOWN_TYPE) {
            Timber.e("未知的命令:%s", cmdStr);
            ToastUtils.show("未知的命令:" + cmdStr);
        }
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
            return;
        }
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (!TextUtils.isEmpty(usrBleViewModel.deviceApiKeyRequest.getDeviceApiKeyLiveData().getValue())) {
            apiKey = usrBleViewModel.deviceApiKeyRequest.getDeviceApiKeyLiveData().getValue();
        }
        if (!cmdStr.contains("&apikey")) {
            cmdStr += "&apikey=" + apiKey
                    + "&msgid=" + UUID.randomUUID().toString().substring(30);
        }

        usrBleViewModel.sendIOTProtocolCommand(cmdStr + "&&");
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
