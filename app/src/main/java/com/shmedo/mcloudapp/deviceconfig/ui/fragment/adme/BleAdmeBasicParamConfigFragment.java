package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLockedRotorDetectionEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicConfigInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeBasicParamConfigView;
import com.shmedo.mcloudapp.util.KeyBordUtils;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        startProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, WRITE_TIME_OUT_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_BASIC);
        sendCommand(command);
    }

    /**
     * 获取堵转检测参数
     */
    private void queryLockRotorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION);
        sendCommand(command);
    }

    private void setSwitchViewListener() {
        admeBasicParamConfigView.mSbDecentralizedEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    admeBasicParamConfigView.mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (admeBasicParamConfigView.lockedRotorDetectionInfo == null) {
                    admeBasicParamConfigView.mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                setLockRotorInfo(isChecked);
            }
        });
    }

    private void setLockRotorInfo(boolean isChecked) {
        AdmeLockedRotorDetectionEntity entity = new AdmeLockedRotorDetectionEntity();
        entity.setLowtbtss(isChecked ? "1" : "0");
        entity.setNumpput(admeBasicParamConfigView.lockedRotorDetectionInfo.getNumpput());
        entity.setPdajtime(admeBasicParamConfigView.lockedRotorDetectionInfo.getPdajtime());
        entity.setDetintiona(admeBasicParamConfigView.lockedRotorDetectionInfo.getDetintiona());
        entity.setDetintionb(admeBasicParamConfigView.lockedRotorDetectionInfo.getDetintionb());
        entity.setLowtorblothr(admeBasicParamConfigView.lockedRotorDetectionInfo.getLowtorblothr());
        entity.setLowtordetime(admeBasicParamConfigView.lockedRotorDetectionInfo.getLowtordetime());
        entity.setLowsusrana(admeBasicParamConfigView.lockedRotorDetectionInfo.getLowsusrana());
        entity.setLowsusranb(admeBasicParamConfigView.lockedRotorDetectionInfo.getLowsusranb());

        entity.setUptbtss(admeBasicParamConfigView.lockedRotorDetectionInfo.getUptbtss());
        entity.setUptorblothr(admeBasicParamConfigView.lockedRotorDetectionInfo.getUptorblothr());
        entity.setUptordetime(admeBasicParamConfigView.lockedRotorDetectionInfo.getUptordetime());
        entity.setUpsusrana(admeBasicParamConfigView.lockedRotorDetectionInfo.getUpsusrana());
        entity.setUpsusranb(admeBasicParamConfigView.lockedRotorDetectionInfo.getUpsusranb());

//        startProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, WRITE_TIME_OUT_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
        sendCommand(command);
    }

    @OnClick({R.id.ll_inclinometer_type, R.id.ll_data_settlement_method, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_inclinometer_type) {
            admeBasicParamConfigView.showInclinometerTypeDialog(mActivity);

        } else if (id == R.id.ll_data_settlement_method) {
            admeBasicParamConfigView.showDataSettlementMethodDialog(mActivity);

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!admeBasicParamConfigView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = admeBasicParamConfigView.getSetCommand();
            if (!TextUtils.isEmpty(command)) {
                startProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, WRITE_TIME_OUT_MILLIS);
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
//                stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeBasicConfigInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基础配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeBasicParamConfigView.basicConfigParam = commandResult.getResult();
                admeBasicParamConfigView.initParamConfigInfo();
                queryLockRotorInfo();
            }
            break;

            case ADME_MD_GET_LOCKED_ROTOR_DETECTION: {
                stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeLockedRotorDetectionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询堵转参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBasicParamConfigView.lockedRotorDetectionInfo = commandResult.getResult();
                admeBasicParamConfigView.initLockedRotorDetectionInfo();
            }
            break;

            case ADME_MD_SET_LOCKED_ROTOR_DETECTION: {
                stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
//                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置堵转参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
//                doAfterSetting();
            }
            break;

            case ADME_MD_SET_BASIC: {//设置设备的基础配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "保存基础配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBasicParamConfigView.doAfterSetting();
                saveConfigInfo();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
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