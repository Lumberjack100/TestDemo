package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.m20.M20BaseInfo;
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
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryCurrentStateDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.UniversalNetConfigHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：    M20设备网络模式配置主页面
 */
public class NetM20HomeFragment extends UniversalNetConfigHomeFragment {
    private NetM20SetupWizardDialogFragment setupWizardDialogFragment;


    public static NetM20HomeFragment newInstance(DeviceInfo deviceInfo) {
        NetM20HomeFragment fragment = new NetM20HomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_setup_wizard, "设置向导", "一键配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    @Override
    protected void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "设置向导":
                setupWizardDialogFragment = NetM20SetupWizardDialogFragment.newInstance();
                setupWizardDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "状态":
//                showWaitDialog("处理中...");
//                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
//                doCommonDispatchRawCmd(command);
                DeviceCurrentStateActivity.startActivity(mActivity, deviceInfo, AppContants.DeviceType.M20);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.M20, deviceInfo, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, deviceInfo, AppContants.DeviceType.M20);
                break;
        }
    }

    /**
     * 获取设备的基本信息
     */
    private void queryBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_GET_BASE_INFO);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 水平初始化
     */
    public void setLevelInitial() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_LEVEL_INITIAL);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
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
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS:
                ToastUtils.show("下发指令失败");
                break;

            case M20_MD_LEVEL_INITIAL:
                if (setupWizardDialogFragment != null && setupWizardDialogFragment.isVisible()) {
                    setupWizardDialogFragment.updateDispatchCmdResult(false, null);
                }
                break;

            case M20_MD_GET_BASE_INFO:
                ToastUtils.show("下发指令失败");
                break;
        }
    }

    /**
     * 指令下发成功处理
     */
    @Override
    protected void doDispatchSuccess(String cmdStr) {
        BaseDispatchCmdDialog newFragment = null;
        //下发指令成功，弹出对话框开始轮询查询指令响应
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS:
                dismissWaitDialog();
                newFragment = new QueryCurrentStateDialog("运行状态", msgIDList);
                ((QueryCurrentStateDialog) newFragment).setOnSeeDetailClickListener(new QueryCurrentStateDialog.OnSeeDetailClickListener() {
                    @Override
                    public void onSeeDetailClick(DevcieCurrentState devcieCurrentState) {
                        DeviceCurrentStateActivity.startActivity(mActivity, deviceInfo, AppContants.DeviceType.M20);
                    }
                });
                break;

            case M20_MD_LEVEL_INITIAL:
                dismissWaitDialog();
                if (setupWizardDialogFragment != null && setupWizardDialogFragment.isVisible()) {
                    setupWizardDialogFragment.updateDispatchCmdResult(true, msgIDList);
                }
                break;

            case M20_MD_GET_BASE_INFO: {//获取设备基本信息
                if (msgIDList != null && msgIDList.size() > 0) {
                    startQueryCmdResponse();
                }
            }
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
            case M20_MD_GET_BASE_INFO: {//获取设备的基本信息
                IOTCommandResult<M20BaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
//                m20BaseInfo = commandResult.getResult();
                updateHeadInfo();
            }
            break;

            default:
                break;
        }
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo() {
//        try {
//            if (m20BaseInfo != null) {
//                mTvFirmwareVersion.setText(String.format("固件版本：%s", m20BaseInfo.getFirversion()));
//            } else {
//                mTvFirmwareVersion.setText("固件版本：--");
//            }
//            mTvPlatformCommunicationState.setText("米度平台连接状态：--");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }
}