package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsAisleSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsDataCenterSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关高级设置页面
 */
public class TcpVmsAdvancedSettingsFragment extends BaseTcpConnectFragment {
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

    @OnClick({R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.dataCenterFourLayout, R.id.vmsAisleOneLayout, R.id.vmsAisleTwoLayout, R.id.vmsAisleThreeLayout, R.id.vmsResetLayout})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!tcpShareViewModel.getConnectStatus()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }

        int id = view.getId();
        if (id == R.id.dataCenterOneLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_ONE);

        } else if (id == R.id.dataCenterTwoLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_TWO);

        } else if (id == R.id.dataCenterThreeLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_THREE);

        } else if (id == R.id.dataCenterFourLayout) {
            VmsDataCenterSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, ServerNumber.NUMBER_FOUR);

        } else if (id == R.id.vmsAisleOneLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_ONE);

        } else if (id == R.id.vmsAisleTwoLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_TWO);

        } else if (id == R.id.vmsAisleThreeLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_THREE);

        } else if (id == R.id.vmsResetLayout) {
            ToastUtils.show("正在研发中,敬请期待...");
//            showWarnDialog("确定恢复出厂设置吗？");
        }
    }

    /**
     * 恢复出厂设置指令
     */
    private void resetTerminal() {
//        TerminalSNEntity entity = new TerminalSNEntity(terminalBean.getSn());
//        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_REBOOT_TERMINAL, entity);
//        sendCommand(command);
    }

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String content) {
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
                        resetTerminal();
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
            case MD_GET_GATEWAY_BASE: {//获取网关的基本信息
//                IOTCommandResult<GatewayBaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
//                if (!commandResult.isSuccess()) {
//                    stopProgressRunnable();
//                    String errMsg = "查询网关基本信息出错!";
//                    Timber.e("%s%s", errMsg, commandResult.getMessage());
//                    ToastUtils.show(errMsg);
//                    return;
//                }
//                gatewayBaseInfo = commandResult.getResult();
//                setHeadInfo();
//                getGatewayStatus(VmsAisleNumber.NUMBER_ONE);
            }
            break;


            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

}