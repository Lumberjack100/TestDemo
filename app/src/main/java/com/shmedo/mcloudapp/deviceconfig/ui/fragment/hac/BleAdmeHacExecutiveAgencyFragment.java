package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.hac.HacExecutiveAgencyInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.hac.AdmeHacExecutiveAgencyView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/19<br/>
 * 描述：     ADME HAC 执行机构配置页面
 */
public class BleAdmeHacExecutiveAgencyFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.hacExecutiveAgencyView)
    AdmeHacExecutiveAgencyView hacExecutiveAgencyView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    public static BleAdmeHacExecutiveAgencyFragment newInstance() {
        return new BleAdmeHacExecutiveAgencyFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_adme_hac_executive_agency;
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
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_GET_EXECUTIVE_AGENCY);
        sendCommand(command);
    }

    @OnClick({R.id.ll_data_settlement_method, R.id.btn_confirm})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_data_settlement_method) {
            hacExecutiveAgencyView.showDataSettlementMethodDialog(mActivity);

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!hacExecutiveAgencyView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = hacExecutiveAgencyView.getSetCommand();
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
            case ADME_HAC_MD_GET_EXECUTIVE_AGENCY: {//获取ADME的执行机构配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<HacExecutiveAgencyInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询执行机构参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                hacExecutiveAgencyView.admeExecutiveAgencyInfo = commandResult.getResult();
                hacExecutiveAgencyView.initParamConfigInfo();
            }
            break;

            case ADME_HAC_MD_SET_EXECUTIVE_AGENCY: {//设置ADME的执行机构配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置执行机构参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(cmdResult.getReason().contains("equimodel_err") ? "设备模式错误，无法配置参数" : errMsg);
                    return;
                }
                hacExecutiveAgencyView.doAfterSetting();
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
        return false;
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        hacExecutiveAgencyView.onEditableChanged(isEditable);
    }
}