package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CustomCommandLogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.SyncInstallationLocationDialog;

import butterknife.OnClick;

/**
 * 蓝牙模式高级设置
 */
public class BleAdvancedSettingFragment extends BaseBleConnectFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_advanced_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick({R.id.resetLayout, R.id.workModeLayout, R.id.productRegisterLayout, R.id.modifyAuthCodeLayout, R.id.syncInstallLocationLayout, R.id.customCommandLogPrintLayout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resetLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.workModeLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.productRegisterLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.modifyAuthCodeLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.syncInstallLocationLayout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                SyncInstallationLocationDialog newFragment = new SyncInstallationLocationDialog(mActivity);
//                newFragment.setDialogFragmentClickListener(firmWareSelectListener);
                newFragment.show(getChildFragmentManager(), "dialog");
                break;

            case R.id.customCommandLogPrintLayout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                CustomCommandLogPrintActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;
        }
    }
}
