package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.VmsDataCenterStatus;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsAisleSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsDataCenterSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关高级设置页面
 */
public class TcpVmsAdvancedSettingsFragment extends BaseTcpConnectFragment {
    private static final int VMS_REBOOT = 0x1000;
    private static final int VMS_RESET = 0x1001;

    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    @BindView(R.id.tv_data_center_three)
    TextView mTvDataCenterThree;

    @BindView(R.id.tv_data_center_four)
    TextView mTvDataCenterFour;


    public static TcpVmsAdvancedSettingsFragment newInstance() {
        return new TcpVmsAdvancedSettingsFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_advanced_settings_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
//        startProgressRunnable("刷新数据...", QUERY_CMD_DELAY_MILLIS);
        getDataCenterStatus(ServerNumber.NUMBER_ONE);
    }

    @OnClick({R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.dataCenterFourLayout, R.id.vmsAisleOneLayout, R.id.vmsAisleTwoLayout, R.id.vmsAisleThreeLayout, R.id.vmsRebootLayout, R.id.vmsResetLayout})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!tcpShareViewModel.getConnectStatus()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }

        int id = view.getId();
        if (id == R.id.dataCenterOneLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());

        } else if (id == R.id.dataCenterThreeLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_THREE, mTvDataCenterThree.getText().toString());

        } else if (id == R.id.dataCenterFourLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_FOUR, mTvDataCenterFour.getText().toString());

        } else if (id == R.id.vmsAisleOneLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_ONE);

        } else if (id == R.id.vmsAisleTwoLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_TWO);

        } else if (id == R.id.vmsAisleThreeLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_THREE);

        } else if (id == R.id.vmsRebootLayout) {
            showWarnDialog("确定重启网关吗？", VMS_REBOOT);
        } else if (id == R.id.vmsResetLayout) {
            ToastUtils.show("正在研发中,敬请期待...");
//            showWarnDialog("确定恢复出厂设置吗？");
        }
    }

    /**
     * 获取网关数据中心状态
     */
    private void getDataCenterStatus(ServerNumber serverNumber) {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_DATA_CENTER_STATUS, serverNumberEntity);
        sendCommand(command);
    }

    /**
     * 网关重启指令
     */
    private void rebootGateWay() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
        sendCommand(command);
    }

    /**
     * 网关恢复出厂设置指令
     */
    private void resetGateWay() {
//        TerminalSNEntity entity = new TerminalSNEntity(terminalBean.getSn());
//        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET, entity);
//        sendCommand(command);
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
                            case VMS_REBOOT:
                                rebootGateWay();
                                break;

                            case VMS_RESET:
                                resetGateWay();
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
            case VMS_MD_GET_DATA_CENTER_STATUS: {//获取Vms数据中心状态
                IOTCommandResult<VmsDataCenterStatus> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopProgressRunnable();
                    String errMsg = String.format("%s %s", "查询网关数据中心状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsDataCenterStatus centerStatus = commandResult.getResult();
                if (centerStatus.getCenterid() == 1) {
                    mTvDataCenterOne.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterOne.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                    getDataCenterStatus(ServerNumber.NUMBER_TWO);
                } else if (centerStatus.getCenterid() == 2) {
                    mTvDataCenterTwo.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterTwo.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                    getDataCenterStatus(ServerNumber.NUMBER_THREE);
                } else if (centerStatus.getCenterid() == 3) {
                    mTvDataCenterThree.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterThree.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                    getDataCenterStatus(ServerNumber.NUMBER_FOUR);
                } else if (centerStatus.getCenterid() == 4) {
                    stopProgressRunnable();
                    mTvDataCenterFour.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterFour.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                }
            }
            break;

            case REBOOT: {//重启网关
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = "发送重启指令失败!";
                    Timber.e("%s%s", errMsg, cmdResult.getReason());
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("发送重启指令成功,网关稍后将重启");
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private String getStatusTextById(String statusId) {
        String status = "未知状态";
        if (statusId.equals("0")) {
            status = "未开启";
        } else if (statusId.equals("1")) {
            status = "已上线";
        } else if (statusId.equals("2")) {
            status = "未上线";
        }

        return status;
    }

    private int getStatusColorResId(String statusId) {
        int resId = R.color.text_color_666666;
        if (statusId.equals("0")) {
            resId = R.color.sub_title_text_color;
        } else if (statusId.equals("1")) {
            resId = R.color.text_color_3AD094;
        } else if (statusId.equals("2")) {
            resId = R.color.red;
        }

        return resId;
    }

}