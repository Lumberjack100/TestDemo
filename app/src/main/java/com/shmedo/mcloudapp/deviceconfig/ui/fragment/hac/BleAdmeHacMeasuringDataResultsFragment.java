package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.os.Bundle;
import android.view.View;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.hac.AdmeHacMeasuringDataActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/28 <br/>
 * 描述：     测量数据结果展示页面
 */
public class BleAdmeHacMeasuringDataResultsFragment extends BaseUSRBleIotCommunicateFragment {

    public static BleAdmeHacMeasuringDataResultsFragment newInstance() {
        BleAdmeHacMeasuringDataResultsFragment fragment = new BleAdmeHacMeasuringDataResultsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_adme_hac_measuring_data_results;
    }


    @OnClick({R.id.btn_restart, R.id.btn_save})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (id == R.id.btn_restart) {
            AdmeHacMeasuringDataActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);

        } else if (id == R.id.btn_save) {
            DeviceConfigActivity.startActivity(mActivity, null, "ADME_HAC10");
        }
    }
}
