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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeAnthropomorphicMovementEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLowEnergyModelEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeStepperMotorEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeAnthropomorphicMovementInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeLowEnergyModeInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import timber.log.Timber;

public class BleIntelligentControlFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.positiveAndNegativeEnableSBtn)
    SwitchButton positiveAndNegativeEnableSBtn;

    @BindView(R.id.lowPowerEnableSBtn)
    SwitchButton lowPowerEnableSBtn;

    @BindView(R.id.anthropomorphicEnableSBtn)
    SwitchButton anthropomorphicEnableSBtn;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;


    public static BleIntelligentControlFragment newInstance() {
        return new BleIntelligentControlFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.intelligent_control_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSwitchViewListener();
        queryPositiveAndNegativeParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }
    private void setSwitchViewListener() {
        positiveAndNegativeEnableSBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                enableOrDisableStepperMotorParam(isChecked);
            }
        });
        lowPowerEnableSBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    lowPowerEnableSBtn.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                enableOrDisableLowEnergy(isChecked);
            }
        });
        anthropomorphicEnableSBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    anthropomorphicEnableSBtn.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                enableOrDisableAnthropomorphicMovement(isChecked);
            }
        });
    }
    /**
     * 获取设备的步进电机正反测使能信息
     */
    private void queryPositiveAndNegativeParamInfo() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_STEPPER_MOTOR);
        sendCommand(command);
    }

    /**
     * 获取低功耗使能信息
     */
    private void queryLowPowerParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE);
        sendCommand(command);
    }

    /**
     * 获取拟人运动使能信息
     */
    private void queryAnthropomorphicParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE);
        sendCommand(command);
    }

    /**
     * 正反测使能
     */
    private void enableOrDisableStepperMotorParam(boolean isOpen) {
        AdmeStepperMotorEntity entity = new AdmeStepperMotorEntity();
        entity.setPosnegtest(isOpen ? "1" : "0");

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_5000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR, entity);
        sendCommand(command);
    }

    /**
     * 继电器低功耗使能
     */
    private void enableOrDisableLowEnergy(boolean isOpen) {
        AdmeLowEnergyModelEntity entity = new AdmeLowEnergyModelEntity();
        entity.setMode(isOpen ? "1" : "0");

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_5000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOW_ENERGY_MODE, entity);
        sendCommand(command);
    }

    /**
     * 拟人运动使能</br>
     */
    private void enableOrDisableAnthropomorphicMovement(boolean isOpen) {
        AdmeAnthropomorphicMovementEntity entity = new AdmeAnthropomorphicMovementEntity();
        entity.setMode(isOpen ? "1" : "0");

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_5000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE, entity);
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
            case ADME_MD_GET_STEPPER_MOTOR: {//获取ADME的步进电机配置参数
                IOTCommandResult<AdmeStepperMotorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "查询步进电机参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeStepperMotorInfo  admeStepperMotorInfo = commandResult.getResult();
                if (admeStepperMotorInfo == null) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    Timber.e("AdmeStepperMotorInfo is Null!");
                    return;
                }
                if (admeStepperMotorInfo.getPosnegtest().trim().equals("0")) {
                    positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(false);
                } else {
                    positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(true);
                }
                queryLowPowerParamInfo();
            }
            break;

            case ADME_MD_GET_LOW_ENERGY_MODE: {
                IOTCommandResult<AdmeLowEnergyModeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "查询低功耗使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeLowEnergyModeInfo admeLowEnergyModeInfo = commandResult.getResult();
                if (admeLowEnergyModeInfo == null) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    Timber.e("AdmeLowEnergyModeInfo is Null!");
                    return;
                }
                if (admeLowEnergyModeInfo.getMode().trim().equals("0")) {
                    lowPowerEnableSBtn.setCheckedImmediatelyNoEvent(false);
                } else {
                    lowPowerEnableSBtn.setCheckedImmediatelyNoEvent(true);
                }
                queryAnthropomorphicParamInfo();
            }
            break;

            case ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE: {
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeAnthropomorphicMovementInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询拟人运动使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeAnthropomorphicMovementInfo anthropomorphicMovementInfo = commandResult.getResult();
                if (anthropomorphicMovementInfo != null) {
                    if (anthropomorphicMovementInfo.getMode().trim().equals("0")) {
                        anthropomorphicEnableSBtn.setCheckedImmediatelyNoEvent(false);
                    } else {
                        anthropomorphicEnableSBtn.setCheckedImmediatelyNoEvent(true);
                    }
                }
            }
            break;

            case ADME_MD_SET_STEPPER_MOTOR: {//设置ADME的步进电机配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置步进电机参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                saveConfigInfo();
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

            case ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置拟人运动使能出错!", cmdResult.getReason());
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