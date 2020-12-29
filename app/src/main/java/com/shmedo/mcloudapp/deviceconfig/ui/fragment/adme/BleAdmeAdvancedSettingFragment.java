package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeDataCenterHomeActivity;

import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：     ADME 设置页面
 */
public class BleAdmeAdvancedSettingFragment extends BaseBleIotCommunicateFragment {


    public static BleAdmeAdvancedSettingFragment newInstance() {
        return new BleAdmeAdvancedSettingFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_advanced_setting_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

    @OnClick({R.id.dataCenterConfigLayout,R.id.rebootLayout, R.id.resetLayout, R.id.firmwareUpgradeLayout, R.id.workModeLayout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dataCenterConfigLayout:
                AdmeDataCenterHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG);
                break;

            case R.id.rebootLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.resetLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.firmwareUpgradeLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.workModeLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;
        }
    }
}