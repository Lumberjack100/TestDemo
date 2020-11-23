package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.model.TerminalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/23 <br/>
 * 描述：      Vms终端多项式扩展传感器配置页面
 */
public class TcpVmsTerminalExternalPolynomialSensorFragment extends BaseTcpConnectFragment {

    private TerminalSensorInfo sensorInfo;


    public static TcpVmsTerminalExternalPolynomialSensorFragment newInstance(TerminalSensorInfo sensorInfo) {
        TcpVmsTerminalExternalPolynomialSensorFragment fragment = new TcpVmsTerminalExternalPolynomialSensorFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.SENSOR_PARAM, sensorInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sensorInfo = getArguments().getParcelable(AppContants.Extras.SENSOR_PARAM);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tcp_vms_terminal_external_polynomial_sensor;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }
}