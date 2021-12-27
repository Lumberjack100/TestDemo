package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.DataCenterCommunicateProtoclEntity;
import com.shmedo.configlibrary.ble.cmd.entity.InstallLocationEntity;
import com.shmedo.configlibrary.ble.cmd.entity.RegistrationPlatformSelectionEntity;
import com.shmedo.configlibrary.ble.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CustomCommandLogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.SyncInstallationLocationDialog;
import com.shmedo.mcloudapp.util.LocationUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;

import butterknife.OnClick;
import timber.log.Timber;

/**
 * 蓝牙模式高级设置
 */
public class BleDasAdvancedSettingFragment extends BaseBleCommunicateFragment {
    private static final int RESET = 0x0001;

    private String installLocation;

    private String serverNumber;
    private LinkedList<String> commandItems = new LinkedList<>();


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_das_advanced_setting;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryInstallLocation();
    }

    /**
     * 恢复出厂设置指令
     */
    private void reset() {
        String command = CommandManager.getInstance().getCommand(CommandType.RESTORE_FACTORY_SETTING);
        sendCommand(command);
        Timber.d("发送恢复出厂设置指令===%s", command);
    }

    /**
     * 查询设备安装位置
     */
    private void queryInstallLocation() {
        InstallLocationEntity installLocationEntity = new InstallLocationEntity(2);
        String command = CommandManager.getInstance().getCommand(CommandType.INSTALL_LOCATION, installLocationEntity);
        sendCommand(command);
        Timber.i("查询安装位置：%s", command);
    }

    private void setChongQingRegisterPlatform(int number) {
        commandItems.clear();

        //网络中心通讯协议
        DataCenterCommunicateProtoclEntity communicateProtoclEntity = new DataCenterCommunicateProtoclEntity(number, 1);
        String command = CommandManager.getInstance().getCommand(CommandType.NET_LINK_COMMUN_PROTOCOL, communicateProtoclEntity);
        commandItems.add(command);

        //选择注册平台
        RegistrationPlatformSelectionEntity platformSelectionEntity = new RegistrationPlatformSelectionEntity(number, 4);
        command = CommandManager.getInstance().getCommand(CommandType.AUTO_REGISTRATION_PLATFORM, platformSelectionEntity);
        commandItems.add(command);

        //关闭服务器
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(number);
        command = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, serverNumberEntity);
        commandItems.add(command);

        command = "##00644" + number + "\r\n";
        commandItems.add(command);

        command = "##0061" + "\r\n";
        commandItems.add(command);

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        sendCommand(commandItems.getFirst());
        Timber.d("设置重庆地灾平台指令===%s", commandItems.getFirst());
    }

    @OnClick({R.id.resetLayout, R.id.syncInstallLocationLayout, R.id.customCommandLogPrintLayout, R.id.chongQingTestLayout})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }
        int id = view.getId();
        if (id == R.id.resetLayout) {
            showWarnDialog("温馨提示", "确定恢复出厂设置吗？", RESET);
        } else if (id == R.id.syncInstallLocationLayout) {
            SyncInstallationLocationDialog newFragment = new SyncInstallationLocationDialog(mActivity, installLocation);
            newFragment.setDialogFragmentClickListener(LocationFragmentClickListener);
            newFragment.show(getChildFragmentManager(), "dialog");

        } else if (id == R.id.customCommandLogPrintLayout) {
            CustomCommandLogPrintActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);

        } else if (id == R.id.chongQingTestLayout) {
            showRegisterPlatformDialog();
        }
    }

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String title, String content, int operateType) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title(title)
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
                        switch (operateType) {
                            case RESET:
                                reset();
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private BaseDialogFragment.DialogFragmentClickListener LocationFragmentClickListener = new BaseDialogFragment.DialogFragmentClickListener<String>() {
        @Override
        public boolean onPositiveClick(View view, String location) {
            if (!TextUtils.isEmpty(location)) {
                installLocation = location;
                startDefaultProgress("指令下发中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
                String command = "##9161" + location + "\r\n";
                sendCommand(command);
                Timber.i("同步安装位置指令：%s", command);
            }
            return true;
        }

        @Override
        public void onNegativeClick(View view) {

        }
    };

    private void showRegisterPlatformDialog() {
        String[] numbers = new String[]{"1", "2"};
        int pos = Arrays.asList(numbers).indexOf(serverNumber);
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("选择数据中心", numbers,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                serverNumber = text;
                                setChongQingRegisterPlatform(position + 1);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case INSTALL_LOCATION: {
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    String msg;
                    if (tempStr.charAt(3) == '1') {
                        msg = "同步安装位置出错!";
                    } else {
                        msg = "查询安装位置出错!";
                    }
                    Timber.e(msg);
                    ToastUtils.show(msg);
                    return;
                }
                if (tempStr.charAt(3) == '1') {
                    ToastUtils.show("同步安装位置成功!");
                    saveConfigInfoNoReboot();
                } else {
                    installLocation = ResultParserUtil.getEntityObject(cmdStr);
                }
            }
            break;

            case RESTORE_FACTORY_SETTING:
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                ToastUtils.show("设备开始恢复出厂设置...");
                break;

            case NET_LINK_COMMUN_PROTOCOL://设置通讯协议应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("网络中心通讯协议配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;

            case AUTO_REGISTRATION_PLATFORM:// MQTT 自动注册设置通选择注册平台时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("选择平台配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;

            case SET_SERVER_ADDRESS_PORT://设置数据服务器地址、端口应答
//                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据服务器地址、端口配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;

            case SAVE_CONFIG_INFO://设置数据服务器地址、端口应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存配置信息错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                ToastUtils.show("配置成功");
                break;

            default:
                if (commandItems.size() > 0)
                    commandItems.removeFirst();

                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;
        }
    }


    private void doAfterSetting() {
//        ToastUtils.show("配置成功");
        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
        saveConfigInfoNoReboot();
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationUtils.getInstance().stopLocalService();
    }
}
