package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
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
import com.shmedo.configlibrary.iot.model.adme.AdmeLowEnergyModeInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class NetIntelligentControlFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.positiveAndNegativeEnableSBtn)
    SwitchButton positiveAndNegativeEnableSBtn;

    @BindView(R.id.lowPowerEnableSBtn)
    SwitchButton lowPowerEnableSBtn;

    @BindView(R.id.anthropomorphicEnableSBtn)
    SwitchButton anthropomorphicEnableSBtn;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;


    public static NetIntelligentControlFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetIntelligentControlFragment fragment = new NetIntelligentControlFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
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
                enableOrDisableStepperMotorParam(isChecked);
            }
        });
        lowPowerEnableSBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableOrDisableLowEnergy(isChecked);
            }
        });
        anthropomorphicEnableSBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableOrDisableAnthropomorphicMovement(isChecked);
            }
        });
    }

    /**
     * 获取设备的步进电机正反测使能信息
     */
    private void queryPositiveAndNegativeParamInfo() {
        showWaitDialog("加载中...");
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_STEPPER_MOTOR);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取低功耗使能信息
     */
    private void queryLowPowerParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取拟人运动使能参数
     */
    private void queryAnthropomorphicParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 禁用步进电机参数</br>
     */
    private void enableOrDisableStepperMotorParam(boolean isOpen) {
        AdmeStepperMotorEntity entity = new AdmeStepperMotorEntity();
        entity.setPosnegtest(isOpen ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
        showWaitDialog("处理中...");
    }

    /**
     * 继电器低功耗使能
     */
    private void enableOrDisableLowEnergy(boolean isOpen) {
        AdmeLowEnergyModelEntity entity = new AdmeLowEnergyModelEntity();
        entity.setMode(isOpen ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOW_ENERGY_MODE, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
        showWaitDialog("处理中...");
    }

    /**
     * 拟人运动使能</br>
     */
    private void enableOrDisableAnthropomorphicMovement(boolean isOpen) {
        AdmeAnthropomorphicMovementEntity entity = new AdmeAnthropomorphicMovementEntity();
        entity.setMode(isOpen ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
        showWaitDialog("处理中...");
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        ToastUtils.show("下发指令失败");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case ADME_MD_GET_STEPPER_MOTOR: {//获取ADME的步进电机配置参数
                IOTCommandResult<AdmeStepperMotorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询步进电机参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeStepperMotorInfo  admeStepperMotorInfo = commandResult.getResult();
                if (admeStepperMotorInfo == null) {
                    dismissWaitDialog();
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
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询低功耗使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeLowEnergyModeInfo admeLowEnergyModeInfo = commandResult.getResult();
                if (admeLowEnergyModeInfo == null) {
                    dismissWaitDialog();
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
                dismissWaitDialog();
                IOTCommandResult<AdmeLowEnergyModeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询拟人运动使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeLowEnergyModeInfo admeLowEnergyModeInfo = commandResult.getResult();
                if (admeLowEnergyModeInfo != null) {
                    if (admeLowEnergyModeInfo.getMode().trim().equals("0")) {
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
                    dismissWaitDialog();
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
                    dismissWaitDialog();
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
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置拟人运动使能出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                saveConfigInfo();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                dismissWaitDialog();
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