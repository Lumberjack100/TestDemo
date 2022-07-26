package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPStaticUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.AttachPopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.hac.HacMeasuringHoleDepthInfoEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.hac.HacHoleAreaDepthInfo;
import com.shmedo.configlibrary.iot.model.hac.HacMeasuringHoleDepthInfo;
import com.shmedo.configlibrary.iot.model.hac.HacMotorMotionDistanceInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class BleAdmeHacMeasuringHoleDepthFragment extends BaseUSRBleIotCommunicateFragment implements TextWatcher {
    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddress;

    @BindView(R.id.et_hole_num)
    ClearEditText mEtHoleNum; //孔号

    @BindView(R.id.et_area_num)
    ClearEditText mEtAreaNum;//区号

    @BindView(R.id.tv_measure_mode)
    TextView mTvMeasureMode; //测孔深模式

    /**
     * 自动测孔深模式
     */
    @BindView(R.id.et_down_speed)
    ClearEditText mEtDownSpeed;//下放速度

    @BindView(R.id.et_bottom_safe_distance)
    ClearEditText mEtBottomSafeDistance;//管底补偿距离

    /**
     * 手动测孔深模式
     */
    @BindView(R.id.decentralizedEnableSBtn)
    SwitchButton mSbDecentralizedEnable;//下放堵转检测

    @BindView(R.id.tv_movement_way)
    TextView mTvMovementWay; //运动方式 上拉  下放

    @BindView(R.id.et_motor_movement_speed)
    ClearEditText mEtMovementSpeed; //电机运动速度

    @BindView(R.id.goalMovementDistanceEt)
    ClearEditText mEtGoalMovementDistance; //设定运动距离

    @BindView(R.id.tv_hole_depth)
    TextView mTvHoleDepth;//测斜管孔深

    @BindView(R.id.ll_manual_measure_mode)
    ViewGroup manualMeasureModeLayout;

    @BindView(R.id.ll_auto_measure_mode)
    ViewGroup autoMeasureModeLayout;

    @BindView(R.id.btn_run)
    Button mBtnRun;

    private HacMeasuringHoleDepthInfo measuringHoleDepthInfo;
    private BleAdmeHacManualMeasuringHoleDepthDialog manualMeasuringHoleDepthBottomDialog;
    private BleAdmeHacAutoMeasuringHoleDepthDialog autoMeasuringHoleDepthBottomDialog;

    private String address;//MAC 地址
    private String holeno;//孔号
    private String areano;//区号
    private String motorspeed;//电机速度
    private String measway;//测量模式（0:自动测量，1:手动测量）
    private String safedistance;//管底补偿距离
    private String movementway;//运动方式（0:上拉，1:下放）
    private String movedistance;//设定运动距离
    private String lastDistance;//当前距离

    private DecimalFormat decimalFormat = new DecimalFormat();

    private List<HacHoleAreaDepthInfo> depthInfoArrayList = new ArrayList<>();
    private final String[] measureModes = new String[]{"自动测量孔深", "手动测量孔深"};
    private final String[] movementWays = new String[]{"上拉", "下放"};

    private List<String> holeNumList = new ArrayList<>();

    public static BleAdmeHacMeasuringHoleDepthFragment newInstance() {
        return new BleAdmeHacMeasuringHoleDepthFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_adme_hac_measuring_hole_depth;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        loadLastHistoryData(true);
        queryMeasuringHoleDepthInfoParam();
//        initTestData();
    }

    private void initTestData() {
        for (int i = 1; i <= 100; i++) {
            holeNumList.add(String.valueOf(i));
        }
    }

    private void setView() {
        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mEtMacAddress.setHint("XXXXXXXXXXXX");
        mEtHoleNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtHoleNum.setHint("01-99");
        mEtAreaNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtAreaNum.setHint("01-99");

        mEtDownSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDownSpeed.setHint("1-120");
        mEtBottomSafeDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtBottomSafeDistance.setHint("0-10");

        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMovementSpeed.setHint("1-180");
        mEtGoalMovementDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        mEtHoleNum.addTextChangedListener(this);

        //默认自动测量模式
        measway = "0";
        mTvMeasureMode.setText(measureModes[0]);
        autoMeasureModeLayout.setVisibility(View.VISIBLE);
        manualMeasureModeLayout.setVisibility(View.GONE);
        mTvMovementWay.setText("上拉");
        movementway = "0";
    }

    /**
     * 获取HAC的孔深测量配置参数
     */
    private void queryMeasuringHoleDepthInfoParam() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM);
        sendCommand(command);
    }

    /**
     * 清空电机运动数据记录
     */
    private void clearMotorMotionData() {
        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_15000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA);
        sendCommand(command);
    }

    /**
     * 测孔深配置参数
     */
    private void setMeasuringHoledepthParam() {
        try {
            if (mTvMeasureMode.getText().toString().contains("手动测量")) {
                //上拉
                if (movementway.equals("0")) {
                    //持久化保存用户数据到SharedPreferences文件中
                    SPStaticUtils.put(AppContants.ADME.LAST_MOTOR_PULL_UP_SPEED, motorspeed);
                    SPStaticUtils.put(AppContants.ADME.LAST_MOTOR_PULL_UP_DISTANCE, movedistance);
                } else {
                    SPStaticUtils.put(AppContants.ADME.LAST_MOTOR_DROP_SPEED, motorspeed);
                    SPStaticUtils.put(AppContants.ADME.LAST_MOTOR_DROP_DISTANCE, movedistance);
                }
            }
            HacMeasuringHoleDepthInfoEntity entity = new HacMeasuringHoleDepthInfoEntity();
            entity.setAddress(address);
            entity.setHoleno(holeno);
            entity.setAreano(areano);
            entity.setMotorspeed(motorspeed);
            entity.setMeasway(measway);
            if (measway.equals("0")) {
                //自动测孔深，默认打开下放堵转检测
                entity.setLowtbtss("1");
                entity.setSafedistance(safedistance);
            } else {
                entity.setLowtbtss(mSbDecentralizedEnable.isChecked() ? "1" : "0");
                entity.setMovementway(movementway);
                entity.setMovedistance(movedistance);
            }

            mTvHoleDepth.setText("0");
            mBtnRun.setEnabled(false);
//            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_SET_HOLE_MEASURE_PARAM, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnClick({R.id.iv_show_hole_dropdown, R.id.ll_measure_mode, R.id.ll_movement_way, R.id.btn_run})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (id == R.id.iv_show_hole_dropdown) {
            showHoleDropDownList(view);

        } else if (id == R.id.ll_measure_mode) {
            showMeasureModeDialog();

        } else if (id == R.id.ll_movement_way) {
            showMovementTypeDialog();

        } else if (id == R.id.btn_run) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("配置参数错误!");
                return;
            }
            clearMotorMotionData();
        }
    }

    /**
     * 选择孔号
     *
     * @param v
     */
    private void showHoleDropDownList(View v) {
        if (holeNumList == null || holeNumList.size() == 0) {
            ToastUtils.show("没有可选孔号");
            return;
        }
        String[] holeNums = holeNumList.toArray(new String[0]);
        AttachPopupView attachPopupView = new XPopup.Builder(getContext())
                .hasShadowBg(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .isDarkTheme(false)
                .popupAnimation(PopupAnimation.NoAnimation) //NoAnimation表示禁用动画
                .atView(v)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(holeNums, null, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        mEtHoleNum.setText(text);
                        HacHoleAreaDepthInfo info = depthInfoArrayList.get(position);
                        mEtAreaNum.setText(info != null ? info.getAreano() : "");
                    }
                }, 0, 0);
        attachPopupView.show();
    }

    /**
     * 选择测量模式
     */
    private void showMeasureModeDialog() {
        int pos = Arrays.asList(measureModes).indexOf(String.valueOf(mTvMeasureMode.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", measureModes,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                measway = String.valueOf(position);
                                mTvMeasureMode.setText(text);
                                autoMeasureModeLayout.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                                manualMeasureModeLayout.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择运动方式
     */
    private void showMovementTypeDialog() {
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
                                    ToastUtils.show("触发磁开关最大安全速度为20");
                                    movementway = "0";
                                    loadLastHistoryData(true);
                                } else {
                                    movementway = "1";
                                    loadLastHistoryData(false);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private void loadLastHistoryData(boolean isPullUp) {
        if (isPullUp) {
            String speed = SPStaticUtils.getString(AppContants.ADME.LAST_MOTOR_PULL_UP_SPEED, "");
            String distance = SPStaticUtils.getString(AppContants.ADME.LAST_MOTOR_PULL_UP_DISTANCE, "");
            mEtMovementSpeed.setText(speed);
            mEtGoalMovementDistance.setText(distance);
        } else {
            String speed = SPStaticUtils.getString(AppContants.ADME.LAST_MOTOR_DROP_SPEED, "");
            String distance = SPStaticUtils.getString(AppContants.ADME.LAST_MOTOR_DROP_DISTANCE, "");
            mEtMovementSpeed.setText(speed);
            mEtGoalMovementDistance.setText(distance);
        }
    }

    private boolean checkValueIsValid() {
        address = mEtMacAddress.getText().toString().trim();
        holeno = mEtHoleNum.getText().toString().trim();
        areano = mEtAreaNum.getText().toString().trim();
        motorspeed = measway.equals("0") ? mEtDownSpeed.getText().toString().trim() : mEtMovementSpeed.getText().toString().trim();
        safedistance = mEtBottomSafeDistance.getText().toString().trim();
        movedistance = mEtGoalMovementDistance.getText().toString().trim();

        if (TextUtils.isEmpty(address)) {
            ToastUtils.show("请输入Mac地址!");
            mEtMacAddress.requestFocus();
            return false;
        }
        if (!ValidateUtil.isValidMacAddressNoColon(address)) {
            ToastUtils.show("请输入正确的Mac地址!");
            mEtMacAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(holeno)) {
            ToastUtils.show("请设置孔号!");
            mEtHoleNum.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(areano)) {
            ToastUtils.show("请设置区号!");
            mEtAreaNum.requestFocus();
            return false;
        }

        if (measway.equals("0")) {
            if (TextUtils.isEmpty(motorspeed)) {
                ToastUtils.show("请输入下放速度!");
                mEtDownSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(motorspeed);
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

            if (TextUtils.isEmpty(safedistance)) {
                ToastUtils.show("请输入管底补偿距离!");
                mEtBottomSafeDistance.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(safedistance);
                if (value < 0 || value > 10) {
                    ToastUtils.show("请输入正确的管底补偿距离!");
                    mEtBottomSafeDistance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的管底补偿距离!");
                mEtBottomSafeDistance.requestFocus();
                return false;
            }
        } else {
            if (TextUtils.isEmpty(motorspeed)) {
                ToastUtils.show("请输入电机速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(motorspeed);
                if (port < 1 || port > 180) {
                    ToastUtils.show("请输入正确的电机速度!");
                    mEtMovementSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(movedistance)) {
                ToastUtils.show("请输入设定运动距离!");
                mEtGoalMovementDistance.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(movedistance);
                if (value <= 0) {
                    ToastUtils.show("请输入正确的设定运动距离!");
                    mEtGoalMovementDistance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的设定运动距离!");
                mEtGoalMovementDistance.requestFocus();
                return false;
            }
        }
        return true;
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
            case ADME_HAC_MD_GET_HOLE_MEASURE_PARAM: {//获取HAC的孔深测量配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<HacMeasuringHoleDepthInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取孔深测量参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    return;
                }
                measuringHoleDepthInfo = commandResult.getResult();
                initMeasuringHoleDepthInfoParam();
            }
            break;

            case ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA: {//ADME测量孔深清空
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "清空数据出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                setMeasuringHoledepthParam();
            }
            break;

            case ADME_HAC_MD_SET_HOLE_MEASURE_PARAM: {//设置HAC的孔深测量配置参数
                mBtnRun.setEnabled(true);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置孔深测量参数出错!", cmdResult.getReason());
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
                showMotorMotionDialog();
            }
            break;

            case ADME_HAC_MD_GET_HOLE_MEASURE_PULSE: {//查询ADME测孔深运动的脉冲数、运动距离
                IOTCommandResult<HacMotorMotionDistanceInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机的实时脉冲数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                HacMotorMotionDistanceInfo motorMotionDistanceInfo = commandResult.getResult();
                if (motorMotionDistanceInfo != null) {
                    lastDistance = motorMotionDistanceInfo.getRealmovedistance();
                    String holeDepth = motorMotionDistanceInfo.getRealholedepth();
                    if (!TextUtils.isEmpty(holeDepth) && !TextUtils.isEmpty(safedistance)) {
                        try {
                            double holeValue = Math.abs(Double.parseDouble(holeDepth));
                            double safeValue = Math.abs(Double.parseDouble(safedistance));
                            //测孔深值不等于安全补偿距离表示测孔深值有效
                            if (holeValue != safeValue) {
                                mTvHoleDepth.setText(holeDepth);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                if (manualMeasuringHoleDepthBottomDialog == null && autoMeasuringHoleDepthBottomDialog == null)
                    return;

                if (mTvMeasureMode.getText().toString().equals(measureModes[0])) {
                    if (manualMeasuringHoleDepthBottomDialog.isVisible())
                        manualMeasuringHoleDepthBottomDialog.updateMotionData(motorMotionDistanceInfo);
                } else {
                    if (autoMeasuringHoleDepthBottomDialog.isVisible())
                        autoMeasuringHoleDepthBottomDialog.updateMotionData(motorMotionDistanceInfo);
                }
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void initMeasuringHoleDepthInfoParam() {
        if (measuringHoleDepthInfo == null) {
            Timber.e("HacMeasuringHoleDepthInfo is Null!");
            measuringHoleDepthInfo = new HacMeasuringHoleDepthInfo();
            return;
        }
        address = measuringHoleDepthInfo.getAddress();
        mEtMacAddress.setText(address);

        mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(measuringHoleDepthInfo.getLowtbtss().equals("1"));

        depthInfoArrayList.clear();
        depthInfoArrayList.addAll(measuringHoleDepthInfo.getHolelist());
        if (depthInfoArrayList == null || depthInfoArrayList.size() == 0) {
            return;
        }
        holeNumList.clear();
        for (HacHoleAreaDepthInfo info : depthInfoArrayList) {
            holeNumList.add(info.getHoleno());
        }
    }

    /**
     * 打开数据运行弹框
     */
    private void showMotorMotionDialog() {
        if (mTvMeasureMode.getText().toString().equals(measureModes[0])) {
            //数据运行弹框已经显示了
            if (manualMeasuringHoleDepthBottomDialog != null && manualMeasuringHoleDepthBottomDialog.isVisible()) {
                manualMeasuringHoleDepthBottomDialog.processContinueMotorMotion();
                return;
            }
            Timber.d("start Motion: lastDistance=%s,totalDistanceGoal=%s", lastDistance, movedistance);
            manualMeasuringHoleDepthBottomDialog = BleAdmeHacManualMeasuringHoleDepthDialog.newInstance(movementway, lastDistance, movedistance);
            manualMeasuringHoleDepthBottomDialog.show(getChildFragmentManager(), "dialog");
        } else {
            autoMeasuringHoleDepthBottomDialog = BleAdmeHacAutoMeasuringHoleDepthDialog.newInstance();
            autoMeasuringHoleDepthBottomDialog.show(getChildFragmentManager(), "dialog");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (holeNumList.contains(s.toString())) {
            mBtnRun.setText("重测孔深");
        } else {
            mBtnRun.setText("启动");
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}