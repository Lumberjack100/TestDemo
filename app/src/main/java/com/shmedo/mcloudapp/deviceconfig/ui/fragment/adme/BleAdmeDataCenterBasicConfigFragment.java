package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;

public class BleAdmeDataCenterBasicConfigFragment extends BaseBleIotCommunicateFragment {

    private ServerNumber serverNumber;
    private String serverStatus;
    private DataCenterInfo dataCenterInfo;


    public static BleAdmeDataCenterBasicConfigFragment newInstance(ServerNumber serverNumber, String status) {
        BleAdmeDataCenterBasicConfigFragment fragment = new BleAdmeDataCenterBasicConfigFragment();
        Bundle args = new Bundle();
        args.putSerializable(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        args.putSerializable(AppContants.Extras.DATA_SERVER_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverNumber = (ServerNumber) getArguments().getSerializable(AppContants.Extras.DATA_SERVER_NUMBER);
            serverStatus = getArguments().getString(AppContants.Extras.DATA_SERVER_STATUS);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_data_center_basic_config_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}