package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLowEnergyModelEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeLowEnergyModeInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/10/14 <br/>
 * 描述：     TODO
 */
public class BleAdmeLowEnergyModeFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.lowEnableSBtn)
    SwitchButton mSbEnable;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    public static BleAdmeLowEnergyModeFragment newInstance() {
        return new BleAdmeLowEnergyModeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_low_energy_mode_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSwitchViewListener();
        queryParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    private void setSwitchViewListener() {
        mSbEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                enableOrDisableLowEnergy(isChecked);
            }
        });
    }

    /**
     * 继电器低功耗使能状态
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE);
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_5000_MILLIS);
        sendCommand(command);
    }

    /**
     * 继电器低功耗使能
     */
    private void enableOrDisableLowEnergy(boolean isOpen) {
        AdmeLowEnergyModelEntity entity = new AdmeLowEnergyModelEntity();
        entity.setMode(isOpen ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOW_ENERGY_MODE, entity);
        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_5000_MILLIS);
        sendCommand(command);
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (msg.what == AppContants.MsgWhat.MSG_DEFAULT) {
            ToastUtils.show("响应超时,请稍后尝试");
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_LOW_ENERGY_MODE: {
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeLowEnergyModeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询低功耗使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeLowEnergyModeInfo admeLowEnergyModeInfo = commandResult.getResult();
                if (admeLowEnergyModeInfo != null) {
                    if (admeLowEnergyModeInfo.getMode().trim().equals("0")) {
                        mSbEnable.setCheckedImmediatelyNoEvent(false);
                    } else {
                        mSbEnable.setCheckedImmediatelyNoEvent(true);
                    }
                }
            }
            break;

            case ADME_MD_SET_LOW_ENERGY_MODE: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置低功耗使能出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                saveConfigInfo();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存指令出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            break;

            default:
                break;
        }
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        maskLayerLayout.setVisibility(isEditable ? View.GONE : View.VISIBLE);
    }
}
