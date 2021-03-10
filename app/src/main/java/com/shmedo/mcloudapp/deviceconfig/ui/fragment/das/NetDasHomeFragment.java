package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;

import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasCollectorSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryCurrentStateDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.UniversalNetConfigHomeFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.Arrays;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：     网络模式 DAS 设备配置主页面
 */
public class NetDasHomeFragment extends UniversalNetConfigHomeFragment {

    public static NetDasHomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo, int deviceType) {
        NetDasHomeFragment fragment = new NetDasHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, projectDeviceInfo);
        args.putInt(AppContants.Extras.DEVICE_TYPE, deviceType);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "遥测", "远距离测量");
        configModuleList.add(configModule);

        // TODO #gh#(设备暂不支持网络配置)
//        if (projectDeviceInfo.getDeviceTypeID() == 4) {
//            configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
//            configModuleList.add(configModule);
//        }

//        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
//        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    @Override
    protected void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态": {
                showProgressDialog("处理中...");
                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
//                DeviceCurrentStateActivity.startActivity(mActivity, projectDeviceInfo, null, AppContants.DeviceType.E40);
            }
            break;

            case "时间": {
                showProgressDialog("处理中...");
                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_TERMINAL_TIME);
                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
            }
            break;

            case "遥测": {
                showProgressDialog("处理中...");
                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_SAMPLE);
                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
            }
            break;

            case "采集器配置":
                DasCollectorSettingActivity.startActivity(mActivity, projectDeviceInfo.getId());
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.DAS, projectDeviceInfo, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, projectDeviceInfo, AppContants.DeviceType.DAS);
                break;
        }
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
            dismissProgressDialog();
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
        String title;
        BaseDispatchCmdDialog newFragment = null;
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS:
                title = "运行状态";
                newFragment = new DispatchCmdFailedDialog(title);
                break;

            case QUERY_TERMINAL_TIME:
                title = "时间";
                newFragment = new DispatchCmdFailedDialog(title);
                break;

            case QUERY_SAMPLE:
                title = "遥测";
                newFragment = new DispatchCmdFailedDialog(title);
                break;

            default:
                break;
        }
        if (newFragment != null)
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
                dismissProgressDialog();
                newFragment = new QueryCurrentStateDialog("运行状态", msgIDList);
                ((QueryCurrentStateDialog) newFragment).setOnSeeDetailClickListener(new QueryCurrentStateDialog.OnSeeDetailClickListener() {
                    @Override
                    public void onSeeDetailClick(DevcieCurrentState devcieCurrentState) {
                        DeviceCurrentStateActivity.startActivity(mActivity, projectDeviceInfo, devcieCurrentState, AppContants.DeviceType.E40);
                    }
                });
                break;

            case QUERY_TERMINAL_TIME:
                dismissProgressDialog();
                newFragment = new QueryTerminalTimeDialog("终端时间", msgIDList);
                break;

            case QUERY_SAMPLE:
                dismissProgressDialog();
                newFragment = new TelemetryDialog("遥测", msgIDList);
                break;

            default:
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }

    @Override
    protected void setResultData(QueryCmdResult queryCmdResult) {

    }

}
