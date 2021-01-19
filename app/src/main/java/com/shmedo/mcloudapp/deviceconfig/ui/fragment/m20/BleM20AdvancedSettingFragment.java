package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeInfo;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CustomCommandLogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.FirmWareSelectDialog;
import com.shmedo.mcloudapp.entity.DeviceTypeInfoDao;
import com.shmedo.mcloudapp.util.DaoManager;

import org.jetbrains.annotations.NotNull;

import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/18/21 <br/>
 * 描述：    M20 设置页面
 */
public class BleM20AdvancedSettingFragment extends BaseGOCBleIotCommunicateFragment {
    private static final int REBOOT = 0x1000;
    private static final int RESET = 0x1001;
    private static final int LEVEL_INITIAL = 0x1002;

    private int deviceTypeID;


    public static BleM20AdvancedSettingFragment newInstance() {
        return new BleM20AdvancedSettingFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_m20_advanced_setting_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchDeviceTypeInfo("M20");
    }

    private void searchDeviceTypeInfo(String typeName) {
        DeviceTypeInfo deviceTypeInfo = DaoManager.getInstance().getDaoSession().getDeviceTypeInfoDao().queryBuilder()
                .where(DeviceTypeInfoDao.Properties.DeviceTypeName.like("%" + typeName + "%"))
                .unique();

        if (deviceTypeInfo != null) {
            deviceTypeID = deviceTypeInfo.getId();
        } else {
            deviceTypeID = -1;
        }
    }

    /**
     * 水平初始化
     */
    private void setLevelInitial() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_LEVEL_INITIAL);
        sendCommand(command);
    }

    /**
     * 重启指令
     */
    private void rebootDevice() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
        sendCommand(command);
    }

    /**
     * 恢复出厂设置指令
     */
    private void resetDevice() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
        sendCommand(command);
    }

    @OnClick({R.id.dataCenterConfigLayout, R.id.cmdDebugLogLayout, R.id.firmwareUpgradeLayout, R.id.horizontalInitializationLayout, R.id.rebootLayout, R.id.resetLayout})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }

        int id = v.getId();
        if (id == R.id.dataCenterConfigLayout) {
            DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG);

        } else if (id == R.id.cmdDebugLogLayout) {
            CustomCommandLogPrintActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DeviceType.M20);

        } else if (id == R.id.firmwareUpgradeLayout) {//固件升级
            FirmWareSelectDialog newFragment = new FirmWareSelectDialog(MCloudApp.getCompanyID(), deviceTypeID);
            newFragment.setDialogFragmentClickListener(firmWareSelectListener);
            newFragment.show(getChildFragmentManager(), "dialog");

        } else if (id == R.id.horizontalInitializationLayout) {
            showWarnDialog("确定进行水平初始化吗？", LEVEL_INITIAL);

        } else if (id == R.id.rebootLayout) {//重启
            showWarnDialog("确定重启设备吗？", REBOOT);

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);
        }
    }

    private BaseDialogFragment.DialogFragmentClickListener firmWareSelectListener = new BaseDialogFragment.DialogFragmentClickListener<FirmWareInfo>() {
        @Override
        public boolean onPositiveClick(View view, FirmWareInfo firmWareInfo) {

            return true;
        }

        @Override
        public void onNegativeClick(View view) {

        }
    };

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String content, int operateType) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示")
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
                        switch (operateType) {
                            case LEVEL_INITIAL:
                                setLevelInitial();
                                break;

                            case REBOOT:
                                rebootDevice();
                                break;

                            case RESET:
                                resetDevice();
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case M20_MD_LEVEL_INITIAL: {//M20水平初始化设置
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "水平初始化设置出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("操作完成");
            }
            break;

            case REBOOT: {//重启
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "重启出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("设备稍后将重启,请等待后重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disconnectDevice();
                    }
                }, 3000);
            }
            break;

            case RESET: {//恢复出厂设置
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "恢复出厂设置出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("设备稍后将重启,请等待后重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disconnectDevice();
                    }
                }, 3000);
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

}