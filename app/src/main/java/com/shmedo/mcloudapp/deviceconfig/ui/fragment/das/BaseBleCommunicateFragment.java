package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.AuthenticationConfigEntity;
import com.shmedo.configlibrary.ble.cmd.entity.LowEnergyModelEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SaveConfigInfoEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.LowEnergyModel;
import com.shmedo.configlibrary.ble.enums.SaveConfigMode;
import com.shmedo.configlibrary.ble.utils.DesUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.profile.BleViewModel;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：     TODO #gh#
 */
public abstract class BaseBleCommunicateFragment extends BaseFragment {
    protected static final int DELAY_5000_MILLIS = 5000;
    protected static final int DELAY_10000_MILLIS = 10000;
    protected static final int DELAY_15000_MILLIS = 15000;//蓝牙连接超时时间
    protected static final int DELAY_20000_MILLIS = 20000;//发送配置参数指令超时时间
    protected static final int DELAY_30000_MILLIS = 30000;//发送配置参数指令超时时间
    protected static final int DELAY_40000_MILLIS = 40000;

    protected BleViewModel bleViewModel;

    private String SN = MCloudApp.getCurDeviceToken();

    public boolean isExitMode = false;

    protected String errMsg;

    protected LinkedList<String> commandItems = new LinkedList<>();

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);
    private final HeartHandler mHeartHandler = new HeartHandler(this);

    /**
     * 发送心跳包任务
     */
    private static final class HeartHandler extends WeakHandler<BaseBleCommunicateFragment> {
        private HeartHandler(BaseBleCommunicateFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BaseBleCommunicateFragment fragment) {
            if (fragment.isConnected()) {
                fragment.sendHeartData();
            }
        }
    }

    protected void startHeart() {
        mHeartHandler.sendEmptyMessageDelayed(AppContants.MsgWhat.MSG_HEART, DELAY_40000_MILLIS);
    }

    protected void stopHeart() {
        mHeartHandler.removeCallbacksAndMessages(null);
    }

    protected void customHandleMessage(@NonNull @NotNull Message msg) {
    }

    private static final class DefaultHandler extends WeakHandler<BaseBleCommunicateFragment> {
        private DefaultHandler(BaseBleCommunicateFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BaseBleCommunicateFragment fragment) {
            fragment.dismissProgressDialog();
            Lifecycle.State currentState = fragment.getLifecycle().getCurrentState();
            if (currentState == DESTROYED) {
                return;
            }
            fragment.customHandleMessage(msg);
        }
    }

    protected void startDefaultProgress(String dialogContent, int what, long delayMillis) {
        if (!TextUtils.isEmpty(dialogContent)) {
            showProgressDialog(dialogContent);
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
    public void onPause() {
        super.onPause();
        stopAllProgress();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bleViewModel = getApplicationScopeViewModel(BleViewModel.class);
        bleViewModel.getResponseMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String result) {
                if (!result.startsWith("$$")) {
                    if (bleViewModel.getLogOutputMode().getValue() == null || !bleViewModel.getLogOutputMode().getValue()) {
                        return;
                    }
                }
                handleResponseMessage(result);
            }
        });
    }

    private void handleResponseMessage(final String cmdStr) {
        try {
            String cmdArray[] = cmdStr.replace("\r\n", "").split(",");
            if (cmdStr.startsWith("$$224")) {//认证方式
                if (cmdStr.replace("\r\n", "").endsWith(CommandResult.ERROR_END)) {
                    setAuthenticateWay();//重新认证
                    return;
                }
                sendAuthenticateCodeCmd(cmdArray[3]);

            } else if (cmdStr.startsWith("$$223")) {//设备登录验证结果指令
                Timber.d("设备登录验证状态===%s", cmdArray[1].contains("1"));
                if (cmdArray[1].contains("1")) {
                    onAuthenticateResult(true);
                } else {
                    ToastUtils.show("设备认证失败!");
                    disconnectDevice();
                    onAuthenticateResult(false);
                }

            } else if (cmdStr.contains("Please verify the equipment.\r\n")) {
                ToastUtils.show("设备认证失败!");
                disconnectDevice();
                onAuthenticateResult(false);

            } else if (cmdStr.contains("Equipment Verify OK.\r\n")) {
                onAuthenticateResult(true);

            } else {
                if (cmdStr.startsWith("$$888")) {
                    startHeart();
                    return;
                }
                parseResponseMessage(cmdStr);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
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
    protected final boolean isConnected() {
        return bleViewModel.isConnected();
    }

    protected void clearDevice() {
        bleViewModel.clearDevice();
    }

    protected void onAuthenticateResult(boolean isSuccess) {
    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {

    }

    /**
     * 蓝牙连接成功,发送认证方式
     */
    protected void setAuthenticateWay() {
        AuthenticationConfigEntity configEntity = new AuthenticationConfigEntity(SN, 0);
        String command = CommandManager.getInstance().getCommand(CommandType.AUTHENTICATION_CONFIG, configEntity);
        Timber.d("设置认证类型指令===%s", command);
        sendCommand("\r\n" + command);
    }

    /**
     * 开始认证流程
     */
    private void sendAuthenticateCodeCmd(String authenticateParam) {
//        Timber.d("解密前:%s", authenticateParam);
        byte[] resultData = StringUtil.hexStringToBytes(authenticateParam);
        try {
            String deskey = "12345678";
            //解密后认证码
            String strDecrypt = new String(DesUtil.decrypt(resultData, deskey), StandardCharsets.UTF_8);
//            Timber.d("解密后:%s", strDecrypt);

            if (!TextUtils.isEmpty(strDecrypt)) {
                //反转6位随机码
                String reverseRandomCode = StringUtil.reverseString(strDecrypt.substring(0, 6));
                byte[] byteEncryt = DesUtil.encrypt((reverseRandomCode + deskey).getBytes(), deskey);
                //加密后认证码
                String strEncryt = StringUtil.bytesToHexString(byteEncryt);
                String cmd = "##222," + SN + ",0," + strEncryt.toUpperCase() + "\r\n";
                Timber.d("设备登录验证指令===%s", cmd);
                sendCommand(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送心跳数据(未定义的指令)
     */
    protected void sendHeartData() {
        String command = CommandManager.getInstance().getCommand(CommandType.HEARTBEAT);
        Timber.d("发送心跳数据：%s", command);
        sendCommand(command);
    }

    /**
     * 查询 DAS 设备的配置参数信息
     */
    protected void queryDASConfigInfoCmd() {
        //获取基础配置信息  ##000
        String command = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        Timber.d("获取基础配置信息指令===%s", command);
        sendCommand(command);
    }

    protected void queryDeviceVersionInfo() {
        String command = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE);
        Timber.d("查询设备版本信息：%s", command);
        sendCommand(command);
    }

    /**
     * 打开/关闭设备低功耗模式
     */
    protected void setLowEnergyModel(boolean isOpen) {
        LowEnergyModelEntity entity = new LowEnergyModelEntity(isOpen ? LowEnergyModel.ACTIVATE.toInt() : LowEnergyModel.STANDBY.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.LOW_ENERGY, entity);
        sendCommand(command);
        Timber.d("打开/关闭设备低功耗模式指令===%s", command);
    }

    /**
     * 保存配置信息重启设备指令
     */
    protected void saveConfigInfo() {
        SaveConfigInfoEntity saveConfigInfoEntity = new SaveConfigInfoEntity(SaveConfigMode.SAVE_REBOOT.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.SAVE_CONFIG_INFO, saveConfigInfoEntity);
        sendCommand(command);
        Timber.d("发送保存配置重启设备指令===%s", command);
    }

    /**
     * 保存配置信息，但不会重启设备指令
     */
    protected void saveConfigInfoNoReboot() {
        SaveConfigInfoEntity saveConfigInfoEntity = new SaveConfigInfoEntity(SaveConfigMode.SAVE_NO_REBOOT.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.SAVE_CONFIG_INFO, saveConfigInfoEntity);
        sendCommand(command);
        Timber.d("发送保存配置不重启设备指令===%s", command);
    }

    protected void sendCommand(String cmdStr) {
        bleViewModel.sendIOTProtocolCommand(cmdStr);
    }

    /**
     * 发送指令队列中的第一条指令
     */
    protected void sendCommandFromCmdList(boolean isSave) {
        if (commandItems.size() > 0) {
            String command = commandItems.getFirst();
            sendCommand(command);
            commandItems.removeFirst();
        } else {
            stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
            if (isSave)
                saveConfigInfoNoReboot();
        }
    }

    /**
     * 发送指令队列中的第一条指令
     */
    protected void sendCommandFromCmdList(String dialogContent, int what, long delayMillis) {
        if (commandItems.size() > 0) {
            String command = commandItems.getFirst();
            sendCommand(command);
            commandItems.removeFirst();
            if (!TextUtils.isEmpty(dialogContent) && delayMillis != 0)
                startDefaultProgress(dialogContent, what, delayMillis);
        } else {
            stopDefaultProgress(what);
        }
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
