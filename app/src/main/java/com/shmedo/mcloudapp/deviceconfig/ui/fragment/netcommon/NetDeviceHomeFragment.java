package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryCurrentStateDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;

import java.util.Arrays;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/9 <br/>
 * 描述：     网络模式通用设备配置主页面
 */
public class NetDeviceHomeFragment extends UniversalNetConfigHomeFragment {
    private static final int REBOOT = 0x0002;

    public static NetDeviceHomeFragment newInstance(DeviceInfo deviceInfo) {
        NetDeviceHomeFragment fragment = new NetDeviceHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initConfigModuleData() {
        configModuleList.clear();

//        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
//        configModuleList.add(configModule);

        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_time, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

//        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "MQTT协议配置");
//        configModuleList.add(configModule);
//
//        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
//        configModuleList.add(configModule);
    }

    @Override
    protected void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态": {
                DeviceCurrentStateActivity.startActivity(mActivity, deviceInfo, ProductType.DAS);
            }
            break;

            case "时间": {
                showWaitDialog("处理中...");
                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_TERMINAL_TIME);
                doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
            }
            break;

            case "遥测": {
                showWaitDialog("处理中...");
                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_SAMPLE);
                doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
            }
            break;

            case "重启":
                showWarnDialog("温馨提示", "确定重启设备吗？", REBOOT);
                break;
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
                            case REBOOT: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
                                showWaitDialog("处理中...");
                                doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
                            }
                            break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    /**
     * 指令下发结果
     *
     * @param dispatchCmdItemList
     * @param cmdStr
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            doDispatchFailed(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        doDispatchSuccess(cmdStr);
    }

    /**
     * 指令下发失败处理
     */
    @Override
    protected void doDispatchFailed(String cmdStr) {
        String title = "";
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS:
                title = "运行状态";
                break;

            case QUERY_TERMINAL_TIME:
                title = "时间";
                break;

            case QUERY_SAMPLE:
                title = "遥测";
                break;

            case REBOOT:
                title = "重新启动";
                break;

            default:
                break;
        }
        BaseDispatchCmdDialog newFragment = new DispatchCmdFailedDialog(title);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 指令下发成功处理
     */
    @Override
    protected void doDispatchSuccess(String cmdStr) {
        //下发指令成功，弹出对话框开始轮询查询指令响应
        BaseDispatchCmdDialog newFragment = null;
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS:
                dismissWaitDialog();
                newFragment = new QueryCurrentStateDialog("运行状态", msgIDList);
                ((QueryCurrentStateDialog) newFragment).setOnSeeDetailClickListener(new QueryCurrentStateDialog.OnSeeDetailClickListener() {
                    @Override
                    public void onSeeDetailClick(DevcieCurrentState devcieCurrentState) {
                        DeviceCurrentStateActivity.startActivity(mActivity, deviceInfo, ProductType.DAS);
                    }
                });
                break;

            case QUERY_TERMINAL_TIME:
                dismissWaitDialog();
                newFragment = new QueryTerminalTimeDialog("终端时间", msgIDList);
                break;

            case QUERY_SAMPLE:
                dismissWaitDialog();
                newFragment = new TelemetryDialog("遥测", msgIDList);
                break;

            case REBOOT:
                dismissWaitDialog();
                newFragment = new CommonCmdDialog("重新启动", "正在重启中...", "预计耗时三分钟,请耐心等待", msgIDList);
                break;

            default:
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }
}
