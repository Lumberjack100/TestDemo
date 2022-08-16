package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeVoltageConfigInfo;
import com.shmedo.configlibrary.iot.model.hac.HacWarningValue;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.hac.AdmeHacThresholdConfigView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/19 <br/>
 * 描述：      ADME HAC 电压、预警阈值配置页面
 */
public class BleAdmeHacThresholdConfigFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.hacThresholdConfigView)
    AdmeHacThresholdConfigView hacThresholdConfigView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    public static BleAdmeHacThresholdConfigFragment newInstance() {
        return new BleAdmeHacThresholdConfigFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_hac_threshold_config_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO #gh# 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
        intQueryCommands();
    }

    private void intQueryCommands() {
        commandItems.clear();

        //获取电压阈值参数
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_VOLTAGE);
        commandItems.add(command);

        //获取预警级别参数
        command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_GET_WARN);
        commandItems.add(command);

        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        sendCommandFromCmdList();
    }

    @OnClick({R.id.btn_add_second_level, R.id.btn_add_third_level, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_add_second_level) {
            hacThresholdConfigView.secondLevelWarningLayout.setVisibility(View.VISIBLE);

        } else if (id == R.id.btn_add_third_level) {
            hacThresholdConfigView.thirdLevelWarningLayout.setVisibility(View.VISIBLE);

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!hacThresholdConfigView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            commandItems.clear();
            String command = hacThresholdConfigView.getVoltageThresholdConfigCommand();
            if (!TextUtils.isEmpty(command))
                commandItems.add(command);

            command = hacThresholdConfigView.getWarningThresholdConfigCommand();
            if (!TextUtils.isEmpty(command))
                commandItems.add(command);

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM);
            commandItems.add(command);

            if (commandItems.size() > 1) {
                startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
                sendCommandFromCmdList();
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
            case ADME_MD_GET_VOLTAGE: {//获取ADME的电压阈值参数
                sendCommandFromCmdList();
                IOTCommandResult<AdmeVoltageConfigInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "获取设备的电压阈值参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                hacThresholdConfigView.voltageConfigInfo = commandResult.getResult();
                hacThresholdConfigView.initVoltageThresholdInfo();
            }
            break;

            case ADME_HAC_MD_GET_WARN: {//获取HAC的预警级别阈值参数
                sendCommandFromCmdList();
                IOTCommandResult<HacWarningValue> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "获取设备的预警级别阈值参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    if (commandResult.getMessage().contains("unsupported")) {
                        hacThresholdConfigView.firstLevelWarningLayout.setVisibility(View.VISIBLE);
                        hacThresholdConfigView.secondLevelWarningLayout.setVisibility(View.GONE);
                        hacThresholdConfigView.thirdLevelWarningLayout.setVisibility(View.GONE);
                    } else
                        ToastUtils.show(errMsg);
                    return;
                }
                hacThresholdConfigView.warningValue = commandResult.getResult();
                hacThresholdConfigView.initWarningThresholdInfo();
            }
            break;

            case ADME_MD_SET_VOLTAGE: {//设置ADME的电压阈值参数
                sendCommandFromCmdList();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "保存电压阈值参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            break;

            case ADME_HAC_MD_SET_WARN: {//设置HAC的预警级别阈值参数
                sendCommandFromCmdList();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "保存预警级别阈值参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
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
                ToastUtils.show("保存成功");
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        hacThresholdConfigView.onEditableChanged(isEditable);
    }

}
