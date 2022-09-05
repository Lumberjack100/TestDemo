package com.shmedo.mcloudapp.deviceconfig.ui.fragment.lr200;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.lr200.LR200PositionEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.lr200.LR200PositionInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CustomCommandLogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.SyncInstallationLocationDialog;

import org.jetbrains.annotations.NotNull;

import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/24 <br/>
 * 描述：     LR200 蓝牙设置页面
 */
public class BleLR200AdvancedSettingFragment extends BaseUSRBleIotCommunicateFragment {
    private static final int RESET = 0x0001;

    private String installLocation;


    public static BleLR200AdvancedSettingFragment newInstance() {
        return new BleLR200AdvancedSettingFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_lr200_advanced_setting_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryInstallLocation();
    }

    /**
     * 查询设备安装位置
     */
    private void queryInstallLocation() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_LOCATION);
        sendCommand(command);
    }

    /**
     * 设置设备安装位置
     */
    private void setInstallLocation() {
        String[] strs = installLocation.split(",");
        try {
            LR200PositionEntity entity = new LR200PositionEntity();
            entity.setLng(strs[0]);
            entity.setLat(strs[1]);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_LOCATION, entity);
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            sendCommand(command);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 恢复出厂设置指令
     */
    private void resetDevice() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
        sendCommand(command);
    }

    @OnClick({R.id.syncInstallLocationLayout, R.id.firmwareUpgradeLayout, R.id.cmdDebugLogLayout, R.id.resetLayout, R.id.remoteDebuggingLayout})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
            return;
        }
        int id = v.getId();
        if (id == R.id.syncInstallLocationLayout) {
            SyncInstallationLocationDialog newFragment = new SyncInstallationLocationDialog(mActivity, installLocation);
            newFragment.setDialogFragmentClickListener(LocationFragmentClickListener);
            newFragment.show(getChildFragmentManager(), "dialog");

        } else if (id == R.id.firmwareUpgradeLayout) {//固件升级


        } else if (id == R.id.cmdDebugLogLayout) {
            CustomCommandLogPrintActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.LR200);

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);
        } else if (id == R.id.remoteDebuggingLayout) {
            getDeviceLogin();
        }
    }

    private BaseDialogFragment.DialogFragmentClickListener LocationFragmentClickListener = new BaseDialogFragment.DialogFragmentClickListener<String>() {
        @Override
        public boolean onPositiveClick(View view, String location) {
            if (!TextUtils.isEmpty(location)) {
                installLocation = location;
                setInstallLocation();
            }
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
            case MD_GET_LOCATION: {
                IOTCommandResult<LR200PositionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询经纬度出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                LR200PositionInfo positionInfo = commandResult.getResult();
                installLocation = positionInfo.getLng() + "," + positionInfo.getLat();
            }
            break;

            case MD_SET_LOCATION: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置经纬度出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("设置完成");
            }
            break;

            case RESET: {//恢复出厂设置
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", StringUtils.getString(R.string.reset_failed), cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show(StringUtils.getString(R.string.device_reset_tip));
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
