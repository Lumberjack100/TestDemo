package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40GpsWorkParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40RtkParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40SerialPortParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40SocketDataDebugActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：    E40网络模式设置页面
 */
public class NetE40AdvancedSettingFragment extends BaseNetIotCommunicateFragment {
    private static final int REBOOT = 0x1002;
    private static final int RESET = 0x1003;


    public static NetE40AdvancedSettingFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40AdvancedSettingFragment fragment = new NetE40AdvancedSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.e40_advanced_setting_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick({R.id.resetLayout, R.id.serialPortLayout, R.id.gpsWorkParamLayout, R.id.rtkParamLayout, R.id.fileDownloadLayout, R.id.dataDebugLayout})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        int id = v.getId();
        if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);

        } else if (id == R.id.serialPortLayout) {//串口参数
            E40SerialPortParamActivity.startActivity(mActivity, projectDeviceInfo);

        } else if (id == R.id.gpsWorkParamLayout) {//GPS工作参数
            E40GpsWorkParamActivity.startActivity(mActivity, projectDeviceInfo);

        } else if (id == R.id.rtkParamLayout) {//RTK参数
            E40RtkParamActivity.startActivity(mActivity, projectDeviceInfo);

        } else if (id == R.id.fileDownloadLayout) {//下载文件

        } else if (id == R.id.dataDebugLayout) {//下载文件
            E40SocketDataDebugActivity.startActivity(mActivity, projectDeviceInfo);
        }
    }

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String content, int operateType) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示")
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
                                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
                            }
                            break;

                            case RESET: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
                                showWaitDialog("处理中...");
                                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
                            }
                            break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
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
     * 指令下发失败弹框
     */
    private void doDispatchFailed(String cmdStr) {
        String title = "";
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
//            case MD_UPGRADE:
//                title = "固件升级";
//                break;

//            case REBOOT:
//                title = "重新启动";
//                break;

            case RESET:
                title = "恢复出厂设置";
                break;

            default:
                break;
        }
        BaseDispatchCmdDialog newFragment = new DispatchCmdFailedDialog(title);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 指令下发成功弹框
     */
    private void doDispatchSuccess(String cmdStr) {
        BaseDispatchCmdDialog newFragment = null;
        //下发指令成功，弹出对话框开始轮询查询指令响应
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
//            case MD_UPGRADE:
//                dismissWaitDialog();
//                newFragment = new CommonCmdDialog("固件升级", "固件升级中...", "此过程耗时较长,请耐心等待", msgIDList);
//                break;
//
//            case REBOOT:
//                dismissWaitDialog();
//                newFragment = new CommonCmdDialog("重新启动", "正在重启中...", "预计耗时三分钟,请耐心等待", msgIDList);
//                break;

            case RESET:
                dismissWaitDialog();
                newFragment = new CommonCmdDialog("恢复出厂设置", "设备开始恢复出厂设置...", "此过程耗时较长,请耐心等待", msgIDList);
                break;

            default:
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }
}