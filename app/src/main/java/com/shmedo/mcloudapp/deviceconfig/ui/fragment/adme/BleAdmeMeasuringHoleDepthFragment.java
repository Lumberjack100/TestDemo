package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeAutoMeasuringHoleDepthEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLockedRotorDetectionEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeasuringHoleDepthEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeasuringHoleDepthInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionDistanceInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/6/21 <br/>
 * 描述：     ADME 测量孔深
 */
public class BleAdmeMeasuringHoleDepthFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.tv_measure_mode)
    TextView mTvMeasureMode;

    @BindView(R.id.decentralizedEnableSBtn)
    SwitchButton mSbDecentralizedEnable;

    /**
     * 手动测孔深模式
     */
    @BindView(R.id.tv_motion_way)
    TextView mTvMotionWay;

    @BindView(R.id.et_motor_movement_speed)
    ClearEditText mEtMovementSpeed;

    @BindView(R.id.motionDistanceEt)
    ClearEditText mEtMotionDistance;

    @BindView(R.id.radio_auto_control)
    RadioButton rbAutoControl;

    @BindView(R.id.radio_manual_control)
    RadioButton rbManualControl;

    /**
     * 自动测孔深模式
     */
    @BindView(R.id.et_down_speed)
    ClearEditText mEtDownSpeed;//下放速度

    @BindView(R.id.et_safe_distance)
    ClearEditText mEtSafeDistance;//安全距离补偿

    @BindView(R.id.tv_hole_depth)
    TextView mTvHoleDepth;//测孔深度

    @BindView(R.id.ll_manual_measure_mode)
    ViewGroup manualMeasureModeLayout;

    @BindView(R.id.ll_auto_measure_mode)
    ViewGroup autoMeasureModeLayout;

    @BindView(R.id.ll_clear_motion_data)
    ViewGroup clearMotionDataLayout;

    @BindView(R.id.ll_motion_data_clear_complete)
    ViewGroup motionDataClearCompleteLayout;

    @BindView(R.id.btn_run)
    Button mBtnRun;

    private boolean isManualMeasureMode = true;//是否手动测量模式

    private String motionWay;//  运动方式
    private String movementSpeed;// 电机运动速度(r/min)
    private String totalDistanceGoal;//  运动距离
    private String lastDistance;//当前距离

    private String downSpeed;// 电机下放速度(r/min)
    private String safeDistance;// 安全距离补偿
    private String holeDepth;// 测量孔深

    private AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo;
    private AdmeLockedRotorDetectionInfo lockedRotorDetectionInfo;
    private BleAdmeMotorMotionDistanceFragment motorMotionDistanceFragment;

    private DecimalFormat decimalFormat = new DecimalFormat();

    public static BleAdmeMeasuringHoleDepthFragment newInstance() {
        return new BleAdmeMeasuringHoleDepthFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_measuring_hole_depth_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setSwitchViewListener();
        setRadioButtonListener();
        queryLockRotorInfo();
    }

    private void setView() {
        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMovementSpeed.setHint("1-180");
        mEtMotionDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        rbAutoControl.setChecked(true);
        rbManualControl.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));

        mEtDownSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDownSpeed.setHint("1-120");
        mEtSafeDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSafeDistance.setHint("0-10");

        manualMeasureModeLayout.setVisibility(View.VISIBLE);
        autoMeasureModeLayout.setVisibility(View.GONE);
        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }

    private void setSwitchViewListener() {
        mSbDecentralizedEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                setLockRotorInfo(isChecked);
            }
        });
    }

    /**
     * 单选框事件
     */
    private void setRadioButtonListener() {
        //自动单选按钮
        rbAutoControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbAutoControl.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    mEtMotionDistance.setEnabled(true);
                    rbManualControl.setChecked(false);
                } else {
                    rbAutoControl.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    mEtMotionDistance.setEnabled(false);
                }
            }
        });

        //手动单选按钮
        rbManualControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbManualControl.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rbAutoControl.setChecked(false);
                } else {
                    rbManualControl.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                }
            }
        });
    }

    /**
     * 获取堵转检测参数
     */
    private void queryLockRotorInfo() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION);
        sendCommand(command);
    }

    /**
     * 获取电机运动配置参数
     */
    private void queryMotorMotionConfig() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH);
        sendCommand(command);
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离
     */
    private void getMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE);
        sendCommand(command);
    }

    /**
     * 清空电机运动数据记录
     */
    private void clearMotorMotionData() {
        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA);
        sendCommand(command);
    }

    /**
     * ADME的电机运动堵转检测使能
     */
    private void setLockRotorInfo(boolean isChecked) {
        AdmeLockedRotorDetectionEntity entity = new AdmeLockedRotorDetectionEntity();
        entity.setLowtbtss(isChecked ? "1" : "0");
        entity.setNumpput(lockedRotorDetectionInfo.getNumpput());
        entity.setPdajtime(lockedRotorDetectionInfo.getPdajtime());
        entity.setDetintiona(lockedRotorDetectionInfo.getDetintiona());
        entity.setDetintionb(lockedRotorDetectionInfo.getDetintionb());
        entity.setLowtorblothr(lockedRotorDetectionInfo.getLowtorblothr());
        entity.setLowtordetime(lockedRotorDetectionInfo.getLowtordetime());
        entity.setLowsusrana(lockedRotorDetectionInfo.getLowsusrana());
        entity.setLowsusranb(lockedRotorDetectionInfo.getLowsusranb());

        entity.setUptbtss(lockedRotorDetectionInfo.getUptbtss());
        entity.setUptorblothr(lockedRotorDetectionInfo.getUptorblothr());
        entity.setUptordetime(lockedRotorDetectionInfo.getUptordetime());
        entity.setUpsusrana(lockedRotorDetectionInfo.getUpsusrana());
        entity.setUpsusranb(lockedRotorDetectionInfo.getUpsusranb());

//        startProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, WRITE_TIME_OUT_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
        sendCommand(command);
    }

    /**
     * 设置ADME的测量孔深配置参数
     */
    private void setMeasuringHoledepth() {
        try {
            AdmeMeasuringHoleDepthEntity entity = new AdmeMeasuringHoleDepthEntity();
            entity.setMovementway(motionWay);
            entity.setMotorspeed(movementSpeed);
            decimalFormat.applyPattern("#.###");
            totalDistanceGoal = decimalFormat.format(Double.parseDouble(totalDistanceGoal));
            entity.setMovedistance(totalDistanceGoal);

            mBtnRun.setEnabled(false);
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 自动测量孔深配置参数
     */
    private void autoMeasuringHoledepth() {
        try {
            AdmeAutoMeasuringHoleDepthEntity entity = new AdmeAutoMeasuringHoleDepthEntity();
            entity.setMotorspeed(downSpeed);
            decimalFormat.applyPattern("#.###");
            safeDistance = decimalFormat.format(Double.parseDouble(safeDistance));
            entity.setSafedistance(safeDistance);

            mTvHoleDepth.setText("0");
            mBtnRun.setEnabled(false);
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnClick({R.id.ll_measure_mode, R.id.ll_motion_type, R.id.btn_run, R.id.ll_clear_motion_data})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (id == R.id.ll_measure_mode) {
            showMeasureModeDialog();

        } else if (id == R.id.ll_motion_type) {
            showMotionTypeDialog();

        } else if (id == R.id.btn_run) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("配置参数错误!");
                return;
            }
            if (isManualMeasureMode)
                setMeasuringHoledepth();
            else
                autoMeasuringHoledepth();

        } else if (id == R.id.ll_clear_motion_data) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            showClearWarnDialog("确认清除设备运动记录数据吗？");
        }
    }

    /**
     * 选择测量模式
     */
    private void showMeasureModeDialog() {
        final String[] values = new String[]{"手动测孔深模式", "自动测孔深模式"};
        int pos = Arrays.asList(values).indexOf(String.valueOf(mTvMeasureMode.getText()));

        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", values,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMeasureMode.setText(text);
                                isManualMeasureMode = (position == 0);
                                mSbDecentralizedEnable.setEnabled(position == 0);
                                manualMeasureModeLayout.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                                autoMeasureModeLayout.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 选择运动方式
     */
    private void showMotionTypeDialog() {
        final String[] values = new String[]{"上拉", "下放"};
        int pos = Arrays.asList(values).indexOf(String.valueOf(mTvMotionWay.getText()));

        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", values,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMotionWay.setText(text);
                                if (position == 0) {
                                    motionWay = "0";
                                } else {
                                    motionWay = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        if (isManualMeasureMode) {
            movementSpeed = mEtMovementSpeed.getText().toString().trim();
            totalDistanceGoal = mEtMotionDistance.getText().toString().trim();
            if (TextUtils.isEmpty(movementSpeed)) {
                ToastUtils.show("请输入电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(movementSpeed);
                if (port < 1 || port > 180) {
                    ToastUtils.show("请输入正确的电机运动速度!");
                    mEtMovementSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
            if (rbAutoControl.isChecked()) {
                if (TextUtils.isEmpty(totalDistanceGoal)) {
                    ToastUtils.show("请输入运动距离!");
                    mEtMotionDistance.requestFocus();
                    return false;
                }
                try {
                    double value = Double.parseDouble(totalDistanceGoal);
                    if (value <= 0) {
                        ToastUtils.show("请输入正确的运动距离!");
                        mEtMotionDistance.requestFocus();
                        return false;
                    }
                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的运动距离!");
                    mEtMotionDistance.requestFocus();
                    return false;
                }
            } else {
                totalDistanceGoal = "99999";
            }
        } else {
            downSpeed = mEtDownSpeed.getText().toString().trim();
            safeDistance = mEtSafeDistance.getText().toString().trim();
            if (TextUtils.isEmpty(downSpeed)) {
                ToastUtils.show("请输入下放速度!");
                mEtDownSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(downSpeed);
                if (port < 1 || port > 120) {
                    ToastUtils.show("请输入正确的下放速度!");
                    mEtDownSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放速度!");
                mEtDownSpeed.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(safeDistance)) {
                ToastUtils.show("请输入安全距离补偿!");
                mEtSafeDistance.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(safeDistance);
                if (value < 0 || value > 10) {
                    ToastUtils.show("请输入正确的安全距离补偿!");
                    mEtSafeDistance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的安全距离补偿!");
                mEtSafeDistance.requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * 清空数据提醒
     *
     * @param content
     */
    protected void showClearWarnDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content(content)
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
                        clearMotorMotionData();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
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
            case ADME_MD_GET_LOCKED_ROTOR_DETECTION: {//获取ADME的堵转参数
                IOTCommandResult<AdmeLockedRotorDetectionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "查询堵转参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                lockedRotorDetectionInfo = commandResult.getResult();
                if (lockedRotorDetectionInfo.getLowtbtss().equals("0")) {
                    mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(false);
                } else {
                    mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(true);
                }
                queryMotorMotionConfig();
            }
            break;

            case ADME_MD_GET_MEASURING_HOLEDEPTH: {//获取ADME的测量孔深配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeMeasuringHoleDepthInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取测量孔深配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                measuringHoleDepthInfo = commandResult.getResult();
                if (motorMotionDistanceFragment != null && motorMotionDistanceFragment.isVisible()) {
                    motorMotionDistanceFragment.processMotorMotionState(measuringHoleDepthInfo);
                } else {
                    initParamConfigInfo();
                    //查询ADME测孔深运动的脉冲数、运动距离
                    getMotorMotionData();
                }
            }
            break;

            case ADME_MD_SET_LOCKED_ROTOR_DETECTION: {//电机运动堵转检测使能
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置堵转参数出错!", cmdResult.getReason());
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
                    String errMsg = String.format("%s %s", "发送保存指令出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            break;

            case ADME_MD_SET_MEASURING_HOLEDEPTH: {//设置ADME的测量孔深配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置测量孔深配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnRun.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            case ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH: {//设置ADME的自动测量孔深参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "自动测量孔深出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnRun.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            case ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE: {//查询ADME测孔深运动的脉冲数、运动距离
                IOTCommandResult<AdmeMotorMotionDistanceInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机的实时运动数据出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeMotorMotionDistanceInfo motorMotionDistanceInfo = commandResult.getResult();
                if (motorMotionDistanceInfo != null) {
                    lastDistance = motorMotionDistanceInfo.getRealmovedistance();
                    holeDepth = motorMotionDistanceInfo.getRealholedepth();
                    try {
                        decimalFormat.applyPattern("#.###");
                        if (!TextUtils.isEmpty(holeDepth) && !decimalFormat.format(Double.parseDouble(holeDepth)).equals("-2")) {
                            mTvHoleDepth.setText(holeDepth);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (motorMotionDistanceFragment != null && motorMotionDistanceFragment.isVisible()) {
                    motorMotionDistanceFragment.updateMotionData(motorMotionDistanceInfo);
                    return;
                }
            }
            break;

            case ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA: {//ADME测量孔深清空
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "清空数据出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                clearMotionDataLayout.setVisibility(View.GONE);
                motionDataClearCompleteLayout.setVisibility(View.VISIBLE);
                getMotorMotionData();
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void initParamConfigInfo() {
        if (measuringHoleDepthInfo == null) {
            Timber.e("AdmeMeasuringHoleDepthInfo is Null!");
            measuringHoleDepthInfo = new AdmeMeasuringHoleDepthInfo();
            return;
        }
        motionWay = measuringHoleDepthInfo.getMovementway().trim();
        movementSpeed = measuringHoleDepthInfo.getMotorspeed().trim();
        totalDistanceGoal = measuringHoleDepthInfo.getMovedistance().trim();
        if (motionWay.equals("0")) {
            mTvMotionWay.setText("上拉");
        } else {
            mTvMotionWay.setText("下放");
        }

        try {
            mEtMovementSpeed.setText(movementSpeed);
            decimalFormat.applyPattern("#.###");
            totalDistanceGoal = decimalFormat.format(Double.parseDouble(totalDistanceGoal));
            mEtMotionDistance.setText(totalDistanceGoal);
        } catch (Exception ex) {
            ex.printStackTrace();
            totalDistanceGoal = "0";
            mEtMotionDistance.setText(totalDistanceGoal);
        }
        //电机处于运动状态，弹出底部运行数据展示框
        if (measuringHoleDepthInfo.getMorunstate().trim().equals("1")) {
            showMotorMotionDialog();
        }
    }

    private void doAfterSetting() {
        mBtnRun.setEnabled(true);
        showMotorMotionDialog();
    }

    /**
     * 打开数据运行弹框
     */
    private void showMotorMotionDialog() {
        //数据运行弹框已经显示了
        if (motorMotionDistanceFragment != null && motorMotionDistanceFragment.isVisible()) {
            motorMotionDistanceFragment.processContinueMotorMotion();
            return;
        }
        Timber.d("start Motion: lastDistance=%s,totalDistanceGoal=%s", lastDistance, totalDistanceGoal);
        motorMotionDistanceFragment = BleAdmeMotorMotionDistanceFragment.newInstance(motionWay, lastDistance, totalDistanceGoal);
        motorMotionDistanceFragment.show(getChildFragmentManager(), "dialog");

        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }
}