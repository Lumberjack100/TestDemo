package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.PopTip;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeExecutiveAgencyView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME 执行机构配置页面
 */
public class BleAdmeExecutiveAgencyFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.admeExecutiveAgencyView)
    AdmeExecutiveAgencyView admeExecutiveAgencyView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    public static BleAdmeExecutiveAgencyFragment newInstance() {
        return new BleAdmeExecutiveAgencyFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_executive_agency_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    /**
     * 获取执行机构参数
     */
    private void queryParamInfo() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY);
        sendCommand(command);
    }

    @OnClick({R.id.ll_measure_method, R.id.ll_data_settlement_method, R.id.ll_data_response, R.id.ll_measurement_interval_per_round, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_measure_method) {
            admeExecutiveAgencyView.showMeasureMethodDialog(mActivity);

        } else if (id == R.id.ll_data_settlement_method) {
            admeExecutiveAgencyView.showDataSettlementMethodDialog(mActivity);

        } else if (id == R.id.ll_data_response) {
            admeExecutiveAgencyView.showDataResponseDialog(mActivity);

        } else if (id == R.id.ll_measurement_interval_per_round) {
            admeExecutiveAgencyView.showMeasIntervalPerRoundsDialog(mActivity);

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!admeExecutiveAgencyView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = admeExecutiveAgencyView.getSetCommand();
            if (!TextUtils.isEmpty(command)) {
                startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
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
            case ADME_MD_GET_EXECUTIVE_AGENCY: {//获取ADME的执行机构配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeExecutiveAgencyInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : String.format("%s %s", "获取参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    PopTip.show(errMsg).autoDismiss(4500).iconError();
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeExecutiveAgencyView.admeExecutiveAgencyInfo = commandResult.getResult();
                admeExecutiveAgencyView.initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_EXECUTIVE_AGENCY: {//设置ADME的执行机构配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    if (errMsg.contains("time_err"))
                        errMsg = errMsg.replaceFirst("(time_err)(:?)", "一轮测量时间不能少于").concat("小时");
                    PopTip.show(errMsg).autoDismiss(4500).iconError();
                    return;
                }
                admeExecutiveAgencyView.doAfterSetting();
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
//        if (isConnected()) {
//            if (admeExecutiveAgencyView.checkValueIsChange(configPageViewModel.configPageEditableChanged.getValue())) {
//                warnNotYetSettingBeforeLeavePage();
//                return true;
//            } else {
//                return false;
//            }
//        }
        return false;
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        admeExecutiveAgencyView.onEditableChanged(isEditable);
    }
}