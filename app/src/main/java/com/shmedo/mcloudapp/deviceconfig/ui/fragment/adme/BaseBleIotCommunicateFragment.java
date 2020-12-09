package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.entity.AuthenticationConfigEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.profile.USRBleViewModel;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：     TODO
 */
public abstract class BaseBleIotCommunicateFragment extends BaseFragment {
    protected USRBleViewModel usrBleViewModel;

    private String SN = MCloudApp.getCurDeviceToken();
    private String macAddress = MCloudApp.getCurDeviceMacAddr();

    public boolean isExitMode = false;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        usrBleViewModel = getApplicationScopeViewModel(USRBleViewModel.class);
        usrBleViewModel.getResponseMsg().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String result) {
                ToastUtils.show(result);
            }
        });
    }

    /**
     * ble 建立连接
     */
    protected void connectDevice(BluetoothDevice device) {
        usrBleViewModel.connect(device);
    }

    /**
     * ble 取消连接
     */
    public void disconnectDevice() {
        Timber.d("disconnectDevice() 调用");
        usrBleViewModel.disconnect();
    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    public final boolean isConnected() {
        return usrBleViewModel.isConnected();
    }

    protected void onConnectionStateChanged(final boolean isConnected) {
        if (isConnected) {
            setAuthenticateWay();
        }
    }

    /**
     * 蓝牙连接成功,发送认证方式
     */
    protected void setAuthenticateWay() {
        AuthenticationConfigEntity configEntity = new AuthenticationConfigEntity(SN, 0);
        String command = CommandManager.getInstance().getCommand(CommandType.AUTHENTICATION_CONFIG, configEntity);
        usrBleViewModel.sendIOTProtocolCommand("\r\n" + command);
        Timber.d("设置认证类型指令===%s", command);
    }

    protected void showDisconnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
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
                        disconnectDevice();
                        if (isExitMode) {
                            mActivity.finish();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    protected void warnNotYetSettingBeforeLeavePage() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content("您已经修改了参数，还未配置到设备，确定离开页面吗？")
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
                        mActivity.finish();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
