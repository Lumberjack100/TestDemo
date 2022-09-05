package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicConfigInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeBasicParamConfigView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME 基本参数配置页面
 */
public class BleAdmeBasicParamConfigFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.admeBasicParamConfigView)
    AdmeBasicParamConfigView admeBasicParamConfigView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;


    public static BleAdmeBasicParamConfigFragment newInstance() {
        return new BleAdmeBasicParamConfigFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_basic_param_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSwitchViewListener();
        queryBasicParamConfigInfo();
        //TODO #gh# 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    /**
     * 获取设备的基础配置参数
     */
    private void queryBasicParamConfigInfo() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_BASIC);
        sendCommand(command);
    }

    /**
     * 获取执行机构参数
     */
    private void queryExecutiveAgencyInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY);
        sendCommand(command);
    }

    /**
     * 获取堵转检测参数
     */
    private void queryLockRotorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION);
        sendCommand(command);
    }

    /**
     * 获取设备的步进电机正反测使能信息
     */
    private void queryPositiveAndNegativeParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_STEPPER_MOTOR);
        sendCommand(command);
    }

    private void setSwitchViewListener() {
        admeBasicParamConfigView.mSbDecentralizedEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    admeBasicParamConfigView.mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                String command = admeBasicParamConfigView.getLockRotorCommand(isChecked);
                if (TextUtils.isEmpty(command)) {
                    admeBasicParamConfigView.mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                sendCommand(command);
            }
        });
        admeBasicParamConfigView.positiveAndNegativeEnableSBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    admeBasicParamConfigView.positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                String command = admeBasicParamConfigView.getPositiveAndNegativeCommand(isChecked);
                if (TextUtils.isEmpty(command)) {
                    admeBasicParamConfigView.positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                sendCommand(command);
            }
        });
    }

    @OnClick({R.id.ll_inclinometer_type, R.id.ll_measure_method, R.id.ll_measurement_interval_per_round, R.id.ll_data_settlement_method, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_inclinometer_type) {
            admeBasicParamConfigView.showInclinometerTypeDialog(mActivity);

        } else if (id == R.id.ll_measure_method) {
            admeBasicParamConfigView.showMeasureMethodDialog(mActivity);

        }else if (id == R.id.ll_measurement_interval_per_round) {
            admeBasicParamConfigView.showMeasIntervalPerRoundsDialog(mActivity);

        }  else if (id == R.id.ll_data_settlement_method) {
            admeBasicParamConfigView.showDataSettlementMethodDialog(mActivity);

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!admeBasicParamConfigView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = admeBasicParamConfigView.getBasicCommand();
            if (!TextUtils.isEmpty(command)) {
                startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_15000_MILLIS);
                sendCommand(command);
            }
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

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_BASIC: {//获取设备的基础配置参数
                IOTCommandResult<AdmeBasicConfigInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "获取设备的基础配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeBasicParamConfigView.initBasicConfigInfo(commandResult.getResult());
                queryExecutiveAgencyInfo();
            }
            break;

            case ADME_MD_GET_EXECUTIVE_AGENCY: {//获取ADME的执行机构配置参数
                IOTCommandResult<AdmeExecutiveAgencyInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "查询执行机构参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeBasicParamConfigView.initExecutiveAgencyInfo(commandResult.getResult());
                queryLockRotorInfo();
            }
            break;

            case ADME_MD_GET_LOCKED_ROTOR_DETECTION: {
                IOTCommandResult<AdmeLockedRotorDetectionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "查询堵转参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBasicParamConfigView.initLockedRotorDetectionInfo(commandResult.getResult());
                queryPositiveAndNegativeParamInfo();
            }
            break;

            case ADME_MD_GET_STEPPER_MOTOR: {//获取ADME的步进电机配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeStepperMotorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询步进电机参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBasicParamConfigView.initPositiveAndNegativeInfo(commandResult.getResult());
            }
            break;

            case ADME_MD_SET_LOCKED_ROTOR_DETECTION: {
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置堵转参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
//                saveConfigInfo();
            }
            break;

            case ADME_MD_SET_STEPPER_MOTOR: {//设置ADME的步进电机配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置步进电机参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
//                saveConfigInfo();
            }
            break;

            case ADME_MD_SET_BASIC: {//设置设备的基础配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "保存基础配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBasicParamConfigView.doAfterSetting();
                String command = admeBasicParamConfigView.getExecutiveAgencyCommand();
                if (!TextUtils.isEmpty(command)) {
                    sendCommand(command);
                } else {
                    saveConfigInfo();
                }
            }
            break;

            case ADME_MD_SET_EXECUTIVE_AGENCY: {//设置ADME的执行机构配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置执行机构参数出错!", cmdResult.getReason());
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
            ToastUtils.show("保存成功");
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isConnected()) {
            if (admeBasicParamConfigView.checkValueIsChange(configPageViewModel.configPageEditableChanged.getValue())) {
                warnNotYetSettingBeforeLeavePage();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        admeBasicParamConfigView.onEditableChanged(isEditable);
    }
}