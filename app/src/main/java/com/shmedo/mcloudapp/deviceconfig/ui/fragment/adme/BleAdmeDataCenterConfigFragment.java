package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;

public class BleAdmeDataCenterConfigFragment extends BaseBleIotCommunicateFragment {

    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;

    public static BleAdmeDataCenterConfigFragment newInstance(int configMethod) {
        BleAdmeDataCenterConfigFragment fragment = new BleAdmeDataCenterConfigFragment();
        Bundle args = new Bundle();
        args.putInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            configMethod = getArguments().getInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_data_center_config_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}