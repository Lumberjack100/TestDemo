package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dms;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.enums.ChannelNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CommonSensorListActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import butterknife.OnClick;

public class BleCommonSensorHomeFragment extends BaseUSRBleIotCommunicateFragment {

    public static BleCommonSensorHomeFragment newInstance() {
        return new BleCommonSensorHomeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_common_sensor_home;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick({R.id.channelOneLayout, R.id.channelTwoLayout, R.id.channelThreeLayout, R.id.channelFourLayout})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
            return;
        }
        int id = view.getId();
        if (id == R.id.channelOneLayout) {
            CommonSensorListActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ChannelNumber.NUMBER_ONE);

        } else if (id == R.id.channelTwoLayout) {
            CommonSensorListActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ChannelNumber.NUMBER_TWO);
        } else if (id == R.id.channelThreeLayout) {
            CommonSensorListActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ChannelNumber.NUMBER_THREE);
        } else if (id == R.id.channelFourLayout) {
            CommonSensorListActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ChannelNumber.NUMBER_FOUR);
        }
    }

}