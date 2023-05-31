package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.MessageDialog;
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
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

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

    @BindView(R.id.positiveAndNegativeEnableSBtn)
    SwitchButton positiveAndNegativeEnableSBtn;//正反测使能

    @BindView(R.id.ll_decentralized)
    RelativeLayout decentralizedLayout;

    @BindView(R.id.rl_positive_and_negative_test)
    RelativeLayout positiveAndNegativeTestLayout;

    /**
     * 自动测孔深模式
     */
    @BindView(R.id.et_down_speed)
    ClearEditText mEtDownSpeed;//下放速度

    @BindView(R.id.et_bottom_safe_distance)
    ClearEditText mEtBottomSafeDistance;//管底补偿距离

    @BindView(R.id.tv_real_hole_depth)
    TextView mTvRealHoleDepth;//实测测斜管孔深

    @BindView(R.id.tv_recommended_hole_depth)
    TextView mTvRecommendHoleDepth;//推荐测斜管孔深

    /**
     * 手动测孔深模式
     */
    @BindView(R.id.tv_movement_way)
    TextView mTvMovementWay;//运动方式 上拉  下放

    @BindView(R.id.et_motor_movement_speed)
    ClearEditText mEtMovementSpeed;//电机运动速度

    @BindView(R.id.goalMovementDistanceEt)
    ClearEditText mEtGoalMovementDistance;//设定运动距离

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

    private String downSpeed;// 电机下放速度(r/min)
    private String safeDistance;// 安全距离补偿

    private String movementway;// 运动方式
    private String movementSpeed;// 电机运动速度(r/min)
    private String totalDistanceGoal;//  运动距离
    private String lastDistance;//当前距离


    private AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo;
    private BleAdmeManualMeasuringHoleDepthBottomDialog manualMeasuringHoleDepthBottomDialog;
    private BleAdmeAutoMeasuringHoleDepthBottomDialog autoMeasuringHoleDepthBottomDialog;

    private DecimalFormat decimalFormat = new DecimalFormat();

    private final String[] measureWays = new String[]{"自动测孔深", "手动测孔深"};
    private final String[] movementWays = new String[]{"上拉", "下放"};

    private AdmeLockedRotorDetectionInfo lockedRotorDetectionInfo = new AdmeLockedRotorDetectionInfo();

    public static BleAdmeMeasuringHoleDepthFragment newInstance() {
        return new BleAdmeMeasuringHoleDepthFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_measuring_hole_depth_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        setSwitchViewListener();
        //进入页面默认自动测孔深，需要打开堵转检测，先查询是否打开
        queryLockRotorInfo();
        loadAutoLastHistoryData();
    }

    private void setView() {
        mEtDownSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDownSpeed.setHint("1-100");
        mEtBottomSafeDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtBottomSafeDistance.setHint("0-10");

        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMovementSpeed.setHint("1-100");
        mEtGoalMovementDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        //进入页面默认自动测孔深，需要打开堵转检测
        mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(true);

        //默认自动测量模式
        mTvMeasureMode.setText(measureWays[0]);
        decentralizedLayout.setVisibility(View.GONE);
        positiveAndNegativeTestLayout.setVisibility(View.VISIBLE);
        autoMeasureModeLayout.setVisibility(View.VISIBLE);
        manualMeasureModeLayout.setVisibility(View.GONE);
        mTvMovementWay.setText("上拉");
        movementway = "0";

        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);

        mEtMovementSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(movementway) && movementway.equals("0")) {
                    try {
                        int port = Integer.parseInt(text.toString().trim());
                        if (port > 10) {
                            MessageDialog.show("提示", "上拉触发磁开关最大速度为 10！", "我已知晓");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setSwitchViewListener() {
        mSbDecentralizedEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                setLockRotorInfo(isChecked);
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
        if (lockedRotorDetectionInfo == null)
            return;
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

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
        sendCommand(command);
    }

    /**
     * 手动测孔深配置参数
     */
    private void setManualMeasuringHoledepth() {
        try {
            //持久化保存用户数据到SharedPreferences文件中
            SPStaticUtils.put(movementway.equals("0") ? AppContants.ADME.MANUAL_LAST_MOTOR_PULL_UP_SPEED : AppContants.ADME.MANUAL_LAST_MOTOR_DROP_SPEED, movementSpeed);
            SPStaticUtils.put(movementway.equals("0") ? AppContants.ADME.MANUAL_LAST_MOTOR_PULL_UP_DISTANCE : AppContants.ADME.MANUAL_LAST_MOTOR_DROP_DISTANCE, totalDistanceGoal);

            AdmeMeasuringHoleDepthEntity entity = new AdmeMeasuringHoleDepthEntity();
            entity.setMovementway(movementway);
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
     * 自动测孔深配置参数
     */
    private void setAutoMeasuringHoledepth() {
        try {
            //持久化保存用户数据到SharedPreferences文件中
            SPStaticUtils.put(AppContants.ADME.AUTO_LAST_MOTOR_DROP_SPEED, downSpeed);
            SPStaticUtils.put(AppContants.ADME.AUTO_LAST_BOTTOM_SAFE_DISTANCE, safeDistance);

            AdmeAutoMeasuringHoleDepthEntity entity = new AdmeAutoMeasuringHoleDepthEntity();
            entity.setMotorspeed(downSpeed);
            decimalFormat.applyPattern("#.###");
            safeDistance = decimalFormat.format(Double.parseDouble(safeDistance));
            entity.setSafedistance(safeDistance);

            mTvRealHoleDepth.setText("0");
            mTvRecommendHoleDepth.setText("0");
            mBtnRun.setEnabled(false);
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnClick({R.id.ll_measure_mode, R.id.ll_movement_way, R.id.btn_run, R.id.ll_clear_motion_data})
    public void onClick(View view) {
        int id = view.getId();
        if (!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        if (id == R.id.ll_measure_mode) {
            showMeasureModeDialog();

        } else if (id == R.id.ll_movement_way) {
            showMotionTypeDialog();

        } else if (id == R.id.btn_run) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("配置参数错误!");
                return;
            }
            if (mTvMeasureMode.getText().toString().equals(measureWays[1]))
                setManualMeasuringHoledepth();
            else
                setAutoMeasuringHoledepth();

        } else if (id == R.id.ll_clear_motion_data) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                return;
            }
            showClearWarnDialog("确认清除设备运动记录数据吗？");
        }
    }

    /**
     * 选择测量模式
     */
    private void showMeasureModeDialog() {
        int pos = Arrays.asList(measureWays).indexOf(String.valueOf(mTvMeasureMode.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", measureWays,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMeasureMode.setText(text);
                                decentralizedLayout.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                                positiveAndNegativeTestLayout.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                                autoMeasureModeLayout.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                                manualMeasureModeLayout.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

                                if (position == 0)
                                    loadAutoLastHistoryData();
                                else
                                    loadManualLastHistoryData(true);

                                //自动测量孔深模式，需要打开堵转检测
                                if (position == 0) {
                                    if (lockedRotorDetectionInfo != null && lockedRotorDetectionInfo.getLowtbtss().equals("0")) {
                                        mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(true);
                                        setLockRotorInfo(true);
                                    }
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }


    /**
     * 选择运动方式
     */
    private void showMotionTypeDialog() {
        int pos = Arrays.asList(movementWays).indexOf(String.valueOf(mTvMovementWay.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", movementWays,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMovementWay.setText(text);
                                if (position == 0) {
//                                    MessageDialog.show("提示", "上拉触发磁开关最大速度为 10！", "我已知晓");
                                    movementway = "0";
                                    loadManualLastHistoryData(true);
                                } else {
                                    movementway = "1";
                                    loadManualLastHistoryData(false);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 自动测孔深模式加载本地缓存的参数
     */
    private void loadAutoLastHistoryData() {
        String speed = SPStaticUtils.getString(AppContants.ADME.AUTO_LAST_MOTOR_DROP_SPEED, "");
        String distance = SPStaticUtils.getString(AppContants.ADME.AUTO_LAST_BOTTOM_SAFE_DISTANCE, "");
        mEtDownSpeed.setText(speed);
        mEtBottomSafeDistance.setText(distance);
    }

    /**
     * 手动测孔深模式加载本地缓存的参数
     */
    private void loadManualLastHistoryData(boolean isPullUp) {
        String speed = SPStaticUtils.getString(isPullUp ? AppContants.ADME.MANUAL_LAST_MOTOR_PULL_UP_SPEED : AppContants.ADME.MANUAL_LAST_MOTOR_DROP_SPEED, "");
        String distance = SPStaticUtils.getString(isPullUp ? AppContants.ADME.MANUAL_LAST_MOTOR_PULL_UP_DISTANCE : AppContants.ADME.MANUAL_LAST_MOTOR_DROP_DISTANCE, "");
        mEtMovementSpeed.setText(speed);
        mEtGoalMovementDistance.setText(distance);
    }

    private boolean checkValueIsValid() {
        if (mTvMeasureMode.getText().toString().equals(measureWays[1])) {
            movementSpeed = mEtMovementSpeed.getText().toString().trim();
            totalDistanceGoal = mEtGoalMovementDistance.getText().toString().trim();
            if (TextUtils.isEmpty(movementSpeed)) {
                ToastUtils.show("请输入电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(movementSpeed);
                if (port < 1 || port > 100) {
                    ToastUtils.show("请输入正确的电机运动速度!");
                    mEtMovementSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(totalDistanceGoal)) {
                ToastUtils.show("请输入运动距离!");
                mEtGoalMovementDistance.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(totalDistanceGoal);
                if (value <= 0) {
                    ToastUtils.show("请输入正确的运动距离!");
                    mEtGoalMovementDistance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的运动距离!");
                mEtGoalMovementDistance.requestFocus();
                return false;
            }

        } else {
            downSpeed = mEtDownSpeed.getText().toString().trim();
            safeDistance = mEtBottomSafeDistance.getText().toString().trim();
            if (TextUtils.isEmpty(downSpeed)) {
                ToastUtils.show("请输入下放速度!");
                mEtDownSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(downSpeed);
                if (port < 1 || port > 100) {
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
                mEtBottomSafeDistance.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(safeDistance);
                if (value < 0 || value > 10) {
                    ToastUtils.show("请输入正确的安全距离补偿!");
                    mEtBottomSafeDistance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的安全距离补偿!");
                mEtBottomSafeDistance.requestFocus();
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
                //进入页面默认自动测量孔深模式，需要打开堵转检测
                if (lockedRotorDetectionInfo.getLowtbtss().equals("0")) {
                    setLockRotorInfo(true);
                } else {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
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

            case ADME_MD_SET_MEASURING_HOLEDEPTH: {//设置手动测量孔深配置参数
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

            case ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH: {//设置自动测量孔深参数
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
                //初次进入页面，初始化测量孔深配置参数
                if (manualMeasuringHoleDepthBottomDialog == null && autoMeasuringHoleDepthBottomDialog == null) {
                    return;
                }
                //轮询 N 次电机脉冲数据不变化时，查询电机运动状态进行后续处理
                if (mTvMeasureMode.getText().toString().equals(measureWays[1])) {
                    if (manualMeasuringHoleDepthBottomDialog.isVisible()) {
                        manualMeasuringHoleDepthBottomDialog.processMotorMotionState(measuringHoleDepthInfo);
                    }
                } else {
                    if (autoMeasuringHoleDepthBottomDialog.isVisible()) {
                        autoMeasuringHoleDepthBottomDialog.processMotorMotionState(measuringHoleDepthInfo);
                    }
                }
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
                    String holeDepth = motorMotionDistanceInfo.getRealholedepth();
                    String recDepth = motorMotionDistanceInfo.getRecoholedepth();
                    if (!TextUtils.isEmpty(holeDepth) && !TextUtils.isEmpty(safeDistance)) {
                        try {
                            double holeValue = Math.abs(Double.parseDouble(holeDepth));
                            double safeValue = Math.abs(Double.parseDouble(safeDistance));
                            //测孔深值不等于安全补偿距离表示测孔深值有效
                            if (holeValue != safeValue) {
                                mTvRealHoleDepth.setText(holeDepth);
                                mTvRecommendHoleDepth.setText(recDepth);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                if (manualMeasuringHoleDepthBottomDialog == null && autoMeasuringHoleDepthBottomDialog == null)
                    return;

                if (mTvMeasureMode.getText().toString().equals(measureWays[1])) {
                    if (manualMeasuringHoleDepthBottomDialog.isVisible())
                        manualMeasuringHoleDepthBottomDialog.updateMotionData(motorMotionDistanceInfo);
                } else {
                    if (autoMeasuringHoleDepthBottomDialog.isVisible())
                        autoMeasuringHoleDepthBottomDialog.updateMotionData(motorMotionDistanceInfo);
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

    private void doAfterSetting() {
        mBtnRun.setEnabled(true);
        showMotorMotionDialog();
    }

    /**
     * 打开数据运行弹框
     */
    private void showMotorMotionDialog() {
        if (mTvMeasureMode.getText().toString().equals(measureWays[1])) {
            //数据运行弹框已经显示了
            if (manualMeasuringHoleDepthBottomDialog != null && manualMeasuringHoleDepthBottomDialog.isVisible()) {
                manualMeasuringHoleDepthBottomDialog.processContinueMotorMotion();
                return;
            }
            Timber.d("start Motion: lastDistance=%s,totalDistanceGoal=%s", lastDistance, totalDistanceGoal);
            manualMeasuringHoleDepthBottomDialog = BleAdmeManualMeasuringHoleDepthBottomDialog.newInstance(movementway, lastDistance, totalDistanceGoal);
            manualMeasuringHoleDepthBottomDialog.show(getChildFragmentManager(), "dialog");
        } else {
            autoMeasuringHoleDepthBottomDialog = BleAdmeAutoMeasuringHoleDepthBottomDialog.newInstance();
            autoMeasuringHoleDepthBottomDialog.show(getChildFragmentManager(), "dialog");
        }
        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }
}