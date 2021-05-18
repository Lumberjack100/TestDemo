package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40SerialPortParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.BaseTcpIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/4/21 <br/>
 * 描述：     E40 TCP 模式设置页面
 */
public class TcpE40AdvancedSettingFragment extends BaseTcpIotCommunicateFragment {
    private static final int REBOOT = 0x1002;
    private static final int RESET = 0x1003;


    public static TcpE40AdvancedSettingFragment newInstance() {
        TcpE40AdvancedSettingFragment fragment = new TcpE40AdvancedSettingFragment();
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

    @OnClick({R.id.resetLayout, R.id.serialPortLayout, R.id.fileDownloadLayout})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (!tcpViewModel.getConnectStatus()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }

        int id = v.getId();
        if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);

        } else if (id == R.id.serialPortLayout) {//RTK模式
            E40SerialPortParamActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT);

        } else if (id == R.id.fileDownloadLayout) {//下载文件

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
                                sendCommand(command);
                            }
                            break;

                            case RESET: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
                                sendCommand(command);
                            }
                            break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case REBOOT: {//重启
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送重启指令失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("发送指令成功,设备稍后将重启,请稍候重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tcpViewModel.disconnect();
                    }
                }, 3000);
            }
            break;

            case RESET: {//恢复出厂设置
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送恢复出厂设置指令失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("发送指令成功,设备稍后将重启,请稍候重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tcpViewModel.disconnect();
                    }
                }, 3000);
            }
            break;

            default:
                break;
        }
    }
}
