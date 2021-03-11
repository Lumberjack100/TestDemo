package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalExternalSensorHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalParamSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.UniversalNetConfigHomeFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.Arrays;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/11/21 <br/>
 * 描述：      Vms 网关挂载的终端设备主页面
 */
public class NetVmsTerminalHomeFragment extends UniversalNetConfigHomeFragment {
    private static final String TERMINAL_INFO = "terminal_info";
    private static final int REBOOT_TERMINAL = 0x0002;

    private VmsTerminalInfo vmsTerminalInfo;


    public static NetVmsTerminalHomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo, VmsTerminalInfo vmsTerminalInfo) {
        NetVmsTerminalHomeFragment fragment = new NetVmsTerminalHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, projectDeviceInfo);
        args.putParcelable(TERMINAL_INFO, vmsTerminalInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vmsTerminalInfo = getArguments().getParcelable(TERMINAL_INFO);
        }
    }

    @Override
    protected void setHeadInfo() {
        if (vmsTerminalInfo != null) {
            mTvDeviceName.setText("VMS终端");
            mTvDeviceSn.setText(String.format("设备编号：%s", vmsTerminalInfo.getSn()));
            mTvProductModel.setText(String.format("固件版本：%s", ""));
            mTvFirmwareVersion.setText(String.format("接入时间：%s", vmsTerminalInfo.getLogintime()));
            mTvDeviceState.setVisibility(View.VISIBLE);
            if (vmsTerminalInfo.getStatus() != 0) {
                mTvDeviceState.setText("在线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceState.setText("离线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
        mTvPlatformCommunicationState.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.GONE);
    }

    @Override
    protected void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "传感器配置", "传感器参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "终端配置", "终端参数配置");
        configModuleList.add(configModule);
    }

    @Override
    protected void processItemClick() {
        BaseDialogFragment newFragment = null;
        switch (selectedConfigModule.getName()) {
            case "状态":
                newFragment = VmsTerminalCurrentStateDialog.newInstance(vmsTerminalInfo);
                newFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "遥测":
                sampleTerminal();
                break;

            case "重启":
                showWarnDialog("温馨提示", "确定重启终端设备吗？", REBOOT_TERMINAL);
                break;

            case "传感器配置":
                VmsTerminalExternalSensorHomeActivity.startActivity(mActivity, projectDeviceInfo, vmsTerminalInfo.getSn());
                break;

            case "终端配置":
                VmsTerminalParamSettingActivity.startActivity(mActivity, projectDeviceInfo, vmsTerminalInfo);
                break;
        }
    }

    /**
     * 遥测终端
     */
    private void sampleTerminal() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_TERMINAL_QUERY_SAMPLE, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 重启终端指令
     */
    private void rebootTerminal() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_REBOOT_TERMINAL, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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
                            case REBOOT_TERMINAL:
                                rebootTerminal();
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
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_TERMINAL_QUERY_SAMPLE:
            case VMS_MD_REBOOT_TERMINAL:
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
            case VMS_TERMINAL_QUERY_SAMPLE:
                dismissProgressDialog();
                newFragment = new TelemetryDialog("遥测", msgIDList);
                break;

            case VMS_MD_REBOOT_TERMINAL:
                dismissProgressDialog();
                newFragment = new CommonCmdDialog("重新启动", "正在重启中...", "预计耗时三分钟,请耐心等待", msgIDList);
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }

    @Override
    protected void setResultData(QueryCmdResult queryCmdResult) {

    }
}
