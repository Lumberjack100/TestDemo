package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CustomCommandLogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.SyncInstallationLocationDialog;

import butterknife.OnClick;
import timber.log.Timber;

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
                newFragment.setDialogFragmentClickListener(LocationFragmentClickListener);
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

    private BaseDialogFragment.DialogFragmentClickListener LocationFragmentClickListener = new BaseDialogFragment.DialogFragmentClickListener<String>() {
        @Override
        public boolean onPositiveClick(View view, String location) {
            if (!TextUtils.isEmpty(location)) {
                String[] strs = location.split(",");

                showProgressDialog("指令下发中...");
                String command = "##9161" + strs[0] + "," + strs[1] + "\r\n";
                sendCommonCommandImmediately(command);
                Timber.i("同步安装位置指令：%s", command);
            }

            return true;
        }

        @Override
        public void onNegativeClick(View view) {

        }
    };

    @Override
    protected void parseResponseMessage(String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case INSTALL_LOCATION: {
                dismissProgressDialog();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("同步安装位置指令出错!");
                    ToastUtils.show("同步安装位置指令出错!");
                    return;
                }
                ToastUtils.show("同步安装位置完成!");
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }
}
