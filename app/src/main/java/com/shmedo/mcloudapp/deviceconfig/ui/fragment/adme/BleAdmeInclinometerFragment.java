package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

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
import com.shmedo.configlibrary.iot.model.adme.AdmeInclinometerInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeInclinometerView;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME 测斜仪参数配置页面
 */
public class BleAdmeInclinometerFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.admeInclinometerView)
    AdmeInclinometerView admeInclinometerView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;


    public static BleAdmeInclinometerFragment newInstance() {
        return new BleAdmeInclinometerFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_inclinometer_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryParamConfigInfo();
        //TODO #gh# 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    /**
     * 获取ADME的测斜仪参数
     */
    private void queryParamConfigInfo() {
        startProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, WRITE_TIME_OUT_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_INCLINOMETER);
        sendCommand(command);
    }

    @OnClick({R.id.ll_inclinometer_type, R.id.ll_low_power_mode, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_inclinometer_type) {
            admeInclinometerView.showInclinometerTypeDialog(mActivity);

        } else if (id == R.id.ll_low_power_mode) {
            admeInclinometerView.showLowPowerModeDialog(mActivity);

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!admeInclinometerView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = admeInclinometerView.getSetCommand();
            if (!TextUtils.isEmpty(command)) {
                startProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, WRITE_TIME_OUT_MILLIS);
                sendCommand(command);
            }
        }
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
            case ADME_MD_GET_INCLINOMETER: {//获取ADME的测斜仪配置参数
                stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeInclinometerInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的测斜仪参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeInclinometerView.admeInclinometerInfo = commandResult.getResult();
                admeInclinometerView.initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_INCLINOMETER: {//设置ADME的测斜仪配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "保存测斜仪参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeInclinometerView.doAfterSetting();
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
            if (admeInclinometerView.checkValueIsChange(configPageViewModel.configPageEditableChanged.getValue())) {
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
        admeInclinometerView.onEditableChanged(isEditable);
    }
}