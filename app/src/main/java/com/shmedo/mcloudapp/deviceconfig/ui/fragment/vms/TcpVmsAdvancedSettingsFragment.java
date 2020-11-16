package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

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
    public void onClick(View v) {
        int id = v.getId();
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

        }

    }


    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        if (!isActive) {
            return;
        }
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