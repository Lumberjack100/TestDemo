package com.shmedo.mcloudapp.deviceconfig.ui.fragment.rn20;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.rn20.Rn20PositionEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.rn20.Rn20PositionInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
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
 * 创建时间:  2021/8/4 <br/>
 * 描述：     TODO
 */
public class BleRN20AdvancedSettingFragment extends BaseUSRBleIotCommunicateFragment {
    private String installLocation;

    public static BleRN20AdvancedSettingFragment newInstance() {
        return new BleRN20AdvancedSettingFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_rn20_advanced_setting;
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
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RN20_MD_GET_TERMINAL_LOCAL);
        sendCommand(command);
    }

    /**
     * 设置设备安装位置
     */
    private void setInstallLocation() {
        String[] strs = installLocation.split(",");
        try {
            Rn20PositionEntity entity = new Rn20PositionEntity();
            entity.setLongitude(strs[0]);
            entity.setLatitude(strs[1]);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_REBOOT_TERMINAL, entity);
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            sendCommand(command);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    @OnClick({R.id.syncInstallLocationLayout, R.id.customCommandLogPrintLayout})
    public void onClick(View v) {
        if (!DebouncingUtils.isValid(v, 1000)) {
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
        } else if (id == R.id.customCommandLogPrintLayout) {
            CustomCommandLogPrintActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.RN20);
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

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case RN20_MD_GET_TERMINAL_LOCAL: {
                IOTCommandResult<Rn20PositionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询经纬度出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                Rn20PositionInfo rn20PositionInfo = commandResult.getResult();
                installLocation = rn20PositionInfo.getLongitude() + "," + rn20PositionInfo.getLatitude();
            }
            break;

            case RN20_MD_SET_TERMINAL_LOCAL: {
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

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
        }
    }

}
