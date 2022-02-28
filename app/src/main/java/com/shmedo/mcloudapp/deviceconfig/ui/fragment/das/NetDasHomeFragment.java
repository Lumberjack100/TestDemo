package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasActiveEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasCollectorSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryCurrentStateDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.UniversalNetConfigHomeFragment;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：     网络模式 DAS 设备配置主页面
 */
public class NetDasHomeFragment extends UniversalNetConfigHomeFragment {
    private static final int REBOOT = 0x0002;

    public static NetDasHomeFragment newInstance(DeviceInfo deviceInfo) {
        NetDasHomeFragment fragment = new NetDasHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        // TODO #gh# 定制需求，暂时屏蔽
//        configModule = new ConfigModule(R.drawable.ic_device_reboot, "传感器初始化", "传感器初始化");
//        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "传感器配置", "传感器参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "MQTT协议配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    @Override
    protected void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态": {
//                showWaitDialog("处理中...");
//                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
//                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
                DeviceCurrentStateActivity.startActivity(mActivity, deviceInfo, ProductType.DAS);
            }
            break;

            case "传感器初始化": {
                DasActiveEntity entity = new DasActiveEntity();
                entity.setMode("1");
                showWaitDialog("处理中...");
                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_ACTIVE, entity);
                doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
            }

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

            case "采集器配置":
                DasCollectorSettingActivity.startActivity(mActivity, deviceInfo);
                break;

            case "传感器配置":
                DasSensorConfigActivity.startActivity(mActivity, deviceInfo);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, ProductType.DAS, deviceInfo, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, deviceInfo, ProductType.DAS);
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

            case DAS_MD_SET_ACTIVE:
                title = "传感器初始化";
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

            case DAS_MD_SET_ACTIVE:
                if (msgIDList != null && msgIDList.size() > 0) {
                    startQueryCmdResponse();
                }
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

    @Override
    protected void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case DAS_MD_SET_ACTIVE: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "传感器初始化失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("传感器已初始化,设备即将重启!");
            }
            break;

            default:
                break;
        }
    }

}
