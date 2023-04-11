package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeAnthropomorphicMovementEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeBrakePadControlEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLowEnergyModelEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeStepperMotorEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeAnthropomorphicMovementInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeBrakePadControlInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeLowEnergyModeInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class NetIntelligentControlFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.positiveAndNegativeEnableSBtn)
    SwitchButton positiveAndNegativeEnableSBtn;//正反测使能

    @BindView(R.id.lowPowerEnableSBtn)
    SwitchButton lowPowerEnableSBtn;//力矩电机继电器低功耗使能

    @BindView(R.id.anthropomorphicEnableSBtn)
    SwitchButton anthropomorphicEnableSBtn;//拟人运动使能

    @BindView(R.id.tv_brake_pad_control)
    TextView mTvBrakePadControl;//刹车片控制

    @BindView(R.id.rl_positive_and_negative_test)
    ViewGroup positiveAndNegativeTestLayout;

    @BindView(R.id.rl_low_power)
    ViewGroup lowPowerLayout;

    @BindView(R.id.rl_anthropomorphic_movement)
    ViewGroup anthropomorphicMovementLayout;

    @BindView(R.id.ll_brake_pad_control)
    ViewGroup brakePadControlLayout;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private final String[] brakePadControls = new String[]{"手动", "自动"};

    public static NetIntelligentControlFragment newInstance(DeviceInfo deviceInfo) {
        NetIntelligentControlFragment fragment = new NetIntelligentControlFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
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
        mTvBrakePadControl.setText(brakePadControls[1]);
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
        intQueryCommands();
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

    private void intQueryCommands() {
        commandItems.clear();

        //获取设备的步进电机正反测使能信息
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_STEPPER_MOTOR);
        commandItems.add(command);

        //获取低功耗使能信息
        command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE);
        commandItems.add(command);

        //获取拟人运动使能信息
        command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE);
        commandItems.add(command);

        //获取刹车片控制方式信息
        command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_BRAKE_PAD_CONTROL);
        commandItems.add(command);

        showWaitDialog("加载中...");
        sendCommandFromCmdList();
    }

    /**
     * 正反测使能</br>
     */
    private void enableOrDisableStepperMotorParam(boolean isOpen) {
        AdmeStepperMotorEntity entity = new AdmeStepperMotorEntity();
        entity.setPosnegtest(isOpen ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
        showWaitDialog("处理中...");
    }

    /**
     * 力矩电机继电器低功耗使能
     */
    private void enableOrDisableLowEnergy(boolean isOpen) {
        AdmeLowEnergyModelEntity entity = new AdmeLowEnergyModelEntity();
        entity.setMode(isOpen ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOW_ENERGY_MODE, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
        showWaitDialog("处理中...");
    }

    /**
     * 拟人运动使能</br>
     */
    private void enableOrDisableAnthropomorphicMovement(boolean isOpen) {
        AdmeAnthropomorphicMovementEntity entity = new AdmeAnthropomorphicMovementEntity();
        entity.setMode(isOpen ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
        showWaitDialog("处理中...");
    }

    /**
     * 力矩电机断电重启指令
     */
    private void torqueMotorReboot() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.TORQUE_MOTOR_REBOOT);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
        showWaitDialog("处理中...");
    }

    /**
     * 刹车片控制</br>
     */
    private void setBrakePadControl(String mode) {
        AdmeBrakePadControlEntity entity = new AdmeBrakePadControlEntity();
        entity.setMode(mode);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_BRAKE_PAD_CONTROL, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
        showWaitDialog("处理中...");
    }

    private void showWarnDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示")
                .content("确定重启力矩电机吗?")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        torqueMotorReboot();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @OnClick({R.id.iv_reboot, R.id.ll_brake_pad_control})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_reboot) {
            showWarnDialog();
        } else if (id == R.id.ll_brake_pad_control) {
            showBrakePadControlDialog();
        }
    }

    /**
     * 选择刹车片控制方式
     */
    public void showBrakePadControlDialog() {
        int pos = Arrays.asList(brakePadControls).indexOf(String.valueOf(mTvBrakePadControl.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", brakePadControls,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvBrakePadControl.setText(text);
                                setBrakePadControl(String.valueOf(position));
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
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
                sendCommandFromCmdList();
                IOTCommandResult<AdmeStepperMotorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询正反测使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    if (!commandResult.getMessage().contains("unsupported"))
                        ToastUtils.show(errMsg);
                    positiveAndNegativeTestLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.GONE : View.VISIBLE);
                    return;
                }
                AdmeStepperMotorInfo admeStepperMotorInfo = commandResult.getResult();
                if (admeStepperMotorInfo == null) {
                    Timber.e("AdmeStepperMotorInfo is Null!");
                    return;
                }
                if ("0".equals(admeStepperMotorInfo.getPosnegtest())) {
                    positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(false);
                } else {
                    positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(true);
                }
            }
            break;

            case ADME_MD_GET_LOW_ENERGY_MODE: {
                sendCommandFromCmdList();
                IOTCommandResult<AdmeLowEnergyModeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询低功耗使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    if (!commandResult.getMessage().contains("unsupported"))
                        ToastUtils.show(errMsg);
                    lowPowerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.GONE : View.VISIBLE);
                    return;
                }
                AdmeLowEnergyModeInfo admeLowEnergyModeInfo = commandResult.getResult();
                if (admeLowEnergyModeInfo == null) {
                    Timber.e("AdmeLowEnergyModeInfo is Null!");
                    return;
                }
                if ("0".equals(admeLowEnergyModeInfo.getMode())) {
                    lowPowerEnableSBtn.setCheckedImmediatelyNoEvent(false);
                } else {
                    lowPowerEnableSBtn.setCheckedImmediatelyNoEvent(true);
                }
            }
            break;

            case ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE: {
                sendCommandFromCmdList();
                IOTCommandResult<AdmeAnthropomorphicMovementInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询拟人运动使能状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    if (!commandResult.getMessage().contains("unsupported"))
                        ToastUtils.show(errMsg);
                    anthropomorphicMovementLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.GONE : View.VISIBLE);
                    return;
                }
                AdmeAnthropomorphicMovementInfo anthropomorphicMovementInfo = commandResult.getResult();
                if (anthropomorphicMovementInfo != null) {
                    if ("0".equals(anthropomorphicMovementInfo.getMode())) {
                        anthropomorphicEnableSBtn.setCheckedImmediatelyNoEvent(false);
                    } else {
                        anthropomorphicEnableSBtn.setCheckedImmediatelyNoEvent(true);
                    }
                }
            }
            break;

            case ADME_MD_GET_BRAKE_PAD_CONTROL: {
                sendCommandFromCmdList();
                IOTCommandResult<AdmeBrakePadControlInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询刹车片控制方式出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    if (!commandResult.getMessage().contains("unsupported"))
                        ToastUtils.show(errMsg);
                    brakePadControlLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.GONE : View.VISIBLE);
                    return;
                }
                brakePadControlLayout.setVisibility(View.VISIBLE);
                AdmeBrakePadControlInfo padControlInfo = commandResult.getResult();
                if (padControlInfo != null) {
                    if ("0".equals(padControlInfo.getMode())) {
                        mTvBrakePadControl.setText(brakePadControls[0]);
                    } else {
                        mTvBrakePadControl.setText(brakePadControls[1]);
                    }
                }
            }
            break;

            case ADME_MD_SET_STEPPER_MOTOR: {//设置ADME的步进电机配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置正反测使能出错!", cmdResult.getReason());
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

            case TORQUE_MOTOR_REBOOT: {//力矩电机断电重启
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", StringUtils.getString(R.string.reboot_failed), cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("力矩电机即将重启");
            }
            break;

            case ADME_MD_SET_BRAKE_PAD_CONTROL: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置刹车片控制方式出错!", cmdResult.getReason());
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