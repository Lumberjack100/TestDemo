package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLockedRotorDetectionEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/2/21 <br/>
 * 描述：    ADME 蓝牙模式下电机运动堵转缓停参数配置页面
 */
public class BleAdmeLockedRotorDetectionFragment extends BaseUSRBleIotCommunicateFragment {
    public static final int DOWN_OPERATOR = 0x0001;
    public static final int PULLUP_OPERATOR = 0x0002;

    @BindView(R.id.downEnableSBtn)
    SwitchButton mSbDownEnable;

    @BindView(R.id.et_down_pulses_per_unit_time)
    ClearEditText mEtDownPulsesPerUnitTime;//单位时间脉冲数

    @BindView(R.id.et_down_pulse_detection_time)
    ClearEditText mEtDownPulseDetectionTime;//检测判断时间

    @BindView(R.id.tv_down_slow_start_interval)
    TextView mTvDownSlowStartInterval;//下放缓起区间

    @BindView(R.id.tv_down_slow_stop_interval)
    TextView mTvDownSlowStopInterval;//下放缓停区间

    @BindView(R.id.seekBar_down_slow_stop_interval)
    RangeSeekBar seekBarDownSlowStartStopInterval;//下放缓起缓停区间

    @BindView(R.id.tv_down_stall_detection_interval)
    TextView mTvDownStallDetectionInterval;//下放堵转检测区间

    @BindView(R.id.seekBar_down_stall_detection_interval)
    RangeSeekBar seekBarDownStallDetectionInterval;//下放堵转检测区间

    @BindView(R.id.et_down_torque_stall_threshold)
    ClearEditText mEtDownTorqueStallThreshold;//下放力矩堵转阈值

    @BindView(R.id.et_down_torque_detection_time)
    ClearEditText mEtDownTorqueDetectionTime;//下放力矩检测判断时间


    @BindView(R.id.pullUpEnableSBtn)
    SwitchButton mSbPullUpEnable;

    @BindView(R.id.tv_pull_up_slow_start_interval)
    TextView mTvPullUpSlowStartInterval;//上拉缓起区间

    @BindView(R.id.tv_pull_up_slow_stop_interval)
    TextView mTvPullUpSlowStopInterval;//上拉缓停区间

    @BindView(R.id.seekBar_pull_up_slow_stop_interval)
    RangeSeekBar seekBarPullUpSlowStartStopInterval;//上拉缓起缓停区间

    @BindView(R.id.et_pull_up_torque_stall_threshold)
    ClearEditText mEtPullUpTorqueStallThreshold;//上拉力矩堵转阈值

    @BindView(R.id.et_pull_up_torque_detection_time)
    ClearEditText mEtPullUpTorqueDetectionTime;//上拉力矩检测判断时间

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.downMeterChildLayout)
    ViewGroup downMeterChildLayout;

    @BindView(R.id.pullUpMeterChildLayout)
    ViewGroup pullUpMeterChildLayout;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private AdmeLockedRotorDetectionInfo lockedRotorDetectionInfo;

    private String downPulsesPerUnitTime;//下放单位时间脉冲数
    private String downPulseDetectionTime;//下放脉冲检测判断时间
    private String downSlowStartIntervalEndValue;//下放缓起区间终值(加速阶段)
    private String downSlowStopIntervalStartValue;//下放缓停区间起始值(减速阶段)
    private String downStallDetectionIntervalStartValue;//堵转检测区间起始值
    private String downStallDetectionIntervalEndValue;//堵转检测区间终值
    private String downTorqueStallThreshold;//下放力矩堵转阈值
    private String downTorqueDetectionTime;//下放力矩检测判断时间

    private String pullUpSlowStartIntervalEndValue;//上拉缓起区间终值(加速阶段)
    private String pullUpSlowStopIntervalStartValue;//上拉缓停区间起始值(减速阶段)
    private String pullUpTorqueStallThreshold;//上拉力矩堵转阈值
    private String pullUpTorqueDetectionTime;//上拉力矩检测判断时间

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static BleAdmeLockedRotorDetectionFragment newInstance() {
        return new BleAdmeLockedRotorDetectionFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_locked_rotor_detection_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        setSwitchViewListener();
        setSeekBarListener();
        queryParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    private void setView() {
        mEtDownPulsesPerUnitTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDownPulsesPerUnitTime.setHint("[1,10000]");

        mEtDownPulseDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDownPulseDetectionTime.setHint("[0.1,10.0]");

        mEtDownTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDownTorqueStallThreshold.setHint("[0.00,2.00]");

        mEtDownTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDownTorqueDetectionTime.setHint("[0.01,5.00]");

        mEtPullUpTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtPullUpTorqueStallThreshold.setHint("[1.00,6.00]");

        mEtPullUpTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtPullUpTorqueDetectionTime.setHint("[0.01,5.00]");
    }

    private void setSwitchViewListener() {
        mSbDownEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    mSbDownEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定使下放计米堵转检测不生效？", DOWN_OPERATOR);
                } else {
                    downMeterChildLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mSbPullUpEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    mSbPullUpEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定使上拉计米堵转检测不生效？", PULLUP_OPERATOR);
                } else {
                    pullUpMeterChildLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setSeekBarListener() {
        //下放缓起缓停区间
        seekBarDownSlowStartStopInterval.setIndicatorTextDecimalFormat("0");
        seekBarDownSlowStartStopInterval.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                DecimalFormat indicatorTextDecimalFormat = new DecimalFormat("0");
                downSlowStartIntervalEndValue = indicatorTextDecimalFormat.format(leftValue);
                downSlowStopIntervalStartValue = indicatorTextDecimalFormat.format(rightValue);
                mTvDownSlowStartInterval.setText(String.format("0%%-%s%%", downSlowStartIntervalEndValue));
                mTvDownSlowStopInterval.setText(String.format("%s%%-100%%", downSlowStopIntervalStartValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //start tracking touch
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                float leftValue = view.getLeftSeekBar().getProgress();
                float rightValue = view.getRightSeekBar().getProgress();
                if (leftValue >= 50) {
                    ToastUtils.show("下放缓起区间终值不能大于50%");
                    view.setProgress(49);
                }
                if (rightValue < 50) {
                    ToastUtils.show("下放缓停区间起始值不能小于50%");
                    view.setProgress(leftValue, 50);
                }
            }
        });
        //堵转检测区间
        seekBarDownStallDetectionInterval.setIndicatorTextDecimalFormat("0");
        seekBarDownStallDetectionInterval.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                DecimalFormat indicatorTextDecimalFormat = new DecimalFormat("0");
                downStallDetectionIntervalStartValue = indicatorTextDecimalFormat.format(leftValue);
                downStallDetectionIntervalEndValue = indicatorTextDecimalFormat.format(rightValue);
                mTvDownStallDetectionInterval.setText(String.format("%s%%-%s%%", indicatorTextDecimalFormat.format(leftValue), indicatorTextDecimalFormat.format(rightValue)));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //start tracking touch
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                float leftValue = view.getLeftSeekBar().getProgress();
                float rightValue = view.getRightSeekBar().getProgress();
                if (leftValue >= 50) {
                    ToastUtils.show("下放堵转检测区间起始值不能大于50%");
                    view.setProgress(49);
                }
                if (rightValue < 50) {
                    ToastUtils.show("下放堵转检测区间终值不能小于50%");
                    view.setProgress(leftValue, 50);
                }
            }
        });
        //上拉缓起缓停区间
        seekBarPullUpSlowStartStopInterval.setIndicatorTextDecimalFormat("0");
        seekBarPullUpSlowStartStopInterval.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                DecimalFormat indicatorTextDecimalFormat = new DecimalFormat("0");
                //leftValue 表示上拉缓起区间终值(加速阶段)
                pullUpSlowStartIntervalEndValue = indicatorTextDecimalFormat.format(leftValue);
                //rightValue 表示上拉缓停区间起始值(减速阶段)
                pullUpSlowStopIntervalStartValue = indicatorTextDecimalFormat.format(rightValue);
                mTvPullUpSlowStartInterval.setText(String.format("0%%-%s%%", pullUpSlowStartIntervalEndValue));
                mTvPullUpSlowStopInterval.setText(String.format("%s%%-100%%", pullUpSlowStopIntervalStartValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //start tracking touch
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                float leftValue = view.getLeftSeekBar().getProgress();
                float rightValue = view.getRightSeekBar().getProgress();
                if (leftValue >= 50) {
                    ToastUtils.show("上拉缓起区间终值不能大于50%");
                    view.setProgress(49);
                }
                if (rightValue < 50) {
                    ToastUtils.show("上拉缓停区间起始值不能小于50%");
                    view.setProgress(leftValue, 50);
                }
            }
        });
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content, int type) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
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
                        if (type == DOWN_OPERATOR) {
                            processSave();
                            downMeterChildLayout.setVisibility(View.GONE);
                        } else if (type == PULLUP_OPERATOR) {
                            processSave();
                            pullUpMeterChildLayout.setVisibility(View.GONE);
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (type == DOWN_OPERATOR) {
                            mSbDownEnable.setCheckedImmediatelyNoEvent(true);
                        } else if (type == PULLUP_OPERATOR) {
                            mSbPullUpEnable.setCheckedImmediatelyNoEvent(true);
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 获取参数
     */
    private void queryParamInfo() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION);
        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        downPulsesPerUnitTime = mEtDownPulsesPerUnitTime.getText().toString().trim();
        downPulseDetectionTime = mEtDownPulseDetectionTime.getText().toString().trim();
        downTorqueStallThreshold = mEtDownTorqueStallThreshold.getText().toString().trim();
        downTorqueDetectionTime = mEtDownTorqueDetectionTime.getText().toString().trim();
        pullUpTorqueStallThreshold = mEtPullUpTorqueStallThreshold.getText().toString().trim();
        pullUpTorqueDetectionTime = mEtPullUpTorqueDetectionTime.getText().toString().trim();

        if (mSbDownEnable.isChecked()) {
            if (TextUtils.isEmpty(downPulsesPerUnitTime)) {
                ToastUtils.show("请输入下放单位时间脉冲数!");
                mEtDownPulsesPerUnitTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(downPulsesPerUnitTime);
                if (value < 1 || value > 10000) {
                    ToastUtils.show("请输入正确的下放单位时间脉冲数!");
                    mEtDownPulsesPerUnitTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放单位时间脉冲数!");
                mEtDownPulsesPerUnitTime.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(downPulseDetectionTime)) {
                ToastUtils.show("请输入下放脉冲检测判断时间!");
                mEtDownPulseDetectionTime.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(downPulseDetectionTime);
                if (value < 0.1 || value > 10.0) {
                    ToastUtils.show("请输入正确的下放脉冲检测判断时间!");
                    mEtDownPulseDetectionTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放脉冲检测判断时间!");
                mEtDownPulseDetectionTime.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(downTorqueStallThreshold)) {
            ToastUtils.show("请输入下放力矩堵转阈值!");
            mEtDownTorqueStallThreshold.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(downTorqueStallThreshold);
            if (value < 0.00 || value > 2.00) {
                ToastUtils.show("请输入正确的下放力矩堵转阈值!");
                mEtDownTorqueStallThreshold.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放力矩堵转阈值!");
            mEtDownTorqueStallThreshold.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(downTorqueDetectionTime)) {
            ToastUtils.show("请输入下放力矩检测判断时间!");
            mEtDownTorqueDetectionTime.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(downTorqueDetectionTime);
            if (value < 0.01 || value > 5.00) {
                ToastUtils.show("请输入正确的下放力矩检测判断时间!");
                mEtDownTorqueDetectionTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放力矩检测判断时间!");
            mEtDownTorqueDetectionTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(pullUpTorqueStallThreshold)) {
            ToastUtils.show("请输入上拉力矩堵转阈值!");
            mEtPullUpTorqueStallThreshold.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(pullUpTorqueStallThreshold);
            if (value < 1.00 || value > 6.00) {
                ToastUtils.show("请输入正确的上拉力矩堵转阈值!");
                mEtPullUpTorqueStallThreshold.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的上拉力矩堵转阈值!");
            mEtPullUpTorqueStallThreshold.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(pullUpTorqueDetectionTime)) {
            ToastUtils.show("请输入上拉力矩检测判断时间!");
            mEtPullUpTorqueDetectionTime.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(pullUpTorqueDetectionTime);
            if (value < 0.01 || value > 5.00) {
                ToastUtils.show("请输入正确的上拉力矩检测判断时间!");
                mEtPullUpTorqueDetectionTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的上拉力矩检测判断时间!");
            mEtPullUpTorqueDetectionTime.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        try {
            AdmeLockedRotorDetectionEntity entity = new AdmeLockedRotorDetectionEntity();
            entity.setLowtbtss(mSbDownEnable.isChecked() ? "1" : "0");
            entity.setNumpput(downPulsesPerUnitTime);//下放单位时间脉冲数
            entity.setPdajtime(downPulseDetectionTime);//下放脉冲检测判断时间
            entity.setLowsusranb(downSlowStartIntervalEndValue);//下放缓起区间终值
            entity.setLowsusrana(downSlowStopIntervalStartValue);//下放缓停区间起始值
            entity.setDetintiona(downStallDetectionIntervalStartValue);//堵转检测区间起始值
            entity.setDetintionb(downStallDetectionIntervalEndValue);//堵转检测区间终值
            entity.setLowtorblothr(downTorqueStallThreshold);//下放力矩堵转阈值
            entity.setLowtordetime(downTorqueDetectionTime);//下放力矩检测判断时间

            entity.setUptbtss(mSbPullUpEnable.isChecked() ? "1" : "0");
            entity.setUpsusranb(pullUpSlowStartIntervalEndValue);//上拉缓起区间终值
            entity.setUpsusrana(pullUpSlowStopIntervalStartValue);//上拉缓停区间起始值
            entity.setUptorblothr(pullUpTorqueStallThreshold);//上拉力矩堵转阈值
            entity.setUptordetime(pullUpTorqueDetectionTime);//上拉力矩检测判断时间

            mBtnSave.setEnabled(false);
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
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
            case ADME_MD_GET_LOCKED_ROTOR_DETECTION: {
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeLockedRotorDetectionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询堵转参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                lockedRotorDetectionInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_LOCKED_ROTOR_DETECTION: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "设置堵转参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(cmdResult.getReason().contains("equimodel_err") ? "设备模式错误，无法配置参数" : errMsg);
                    mBtnSave.setEnabled(true);
                    return;
                }
                doAfterSetting();
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
            ToastUtils.show("保存成功");
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void initParamConfigInfo() {
        if (lockedRotorDetectionInfo == null) {
            Timber.e("AdmeLockedRotorDetectionInfo is Null!");
            lockedRotorDetectionInfo = new AdmeLockedRotorDetectionInfo();
            return;
        }
        downPulsesPerUnitTime = lockedRotorDetectionInfo.getNumpput().trim();//下放单位时间脉冲数
        downPulseDetectionTime = lockedRotorDetectionInfo.getPdajtime().trim();//下放脉冲检测判断时间
        downSlowStartIntervalEndValue = lockedRotorDetectionInfo.getLowsusranb().trim();//下放缓起区间终值(加速阶段)
        downSlowStopIntervalStartValue = lockedRotorDetectionInfo.getLowsusrana().trim();//下放缓停区间起始值(减速阶段)
        downStallDetectionIntervalStartValue = lockedRotorDetectionInfo.getDetintiona().trim();//堵转检测区间起始值
        downStallDetectionIntervalEndValue = lockedRotorDetectionInfo.getDetintionb().trim();//堵转检测区间终值
        downTorqueStallThreshold = lockedRotorDetectionInfo.getLowtorblothr().trim();//下放力矩堵转阈值
        downTorqueDetectionTime = lockedRotorDetectionInfo.getLowtordetime().trim();//下放力矩检测判断时间

        pullUpSlowStartIntervalEndValue = lockedRotorDetectionInfo.getUpsusranb().trim();//上拉缓起区间终值(加速阶段)
        pullUpSlowStopIntervalStartValue = lockedRotorDetectionInfo.getUpsusrana().trim();//上拉缓停区间起始值(减速阶段)
        pullUpTorqueStallThreshold = lockedRotorDetectionInfo.getUptorblothr().trim();//上拉力矩堵转阈值
        pullUpTorqueDetectionTime = lockedRotorDetectionInfo.getUptordetime().trim();//上拉力矩检测判断时间

        if (lockedRotorDetectionInfo.getLowtbtss().equals("0")) {
            mSbDownEnable.setCheckedImmediatelyNoEvent(false);
            downMeterChildLayout.setVisibility(View.GONE);
        } else {
            mSbDownEnable.setCheckedImmediatelyNoEvent(true);
            downMeterChildLayout.setVisibility(View.VISIBLE);
        }
        if (lockedRotorDetectionInfo.getUptbtss().equals("0")) {
            mSbPullUpEnable.setCheckedImmediatelyNoEvent(false);
            pullUpMeterChildLayout.setVisibility(View.GONE);
        } else {
            mSbPullUpEnable.setCheckedImmediatelyNoEvent(true);
            pullUpMeterChildLayout.setVisibility(View.VISIBLE);
        }

        try {
            mEtDownPulsesPerUnitTime.setText(downPulsesPerUnitTime);

            decimalFormat.applyPattern("#");
            downPulseDetectionTime = decimalFormat.format(Double.parseDouble(downPulseDetectionTime));
            mEtDownPulseDetectionTime.setText(downPulseDetectionTime);

            seekBarDownSlowStartStopInterval.setProgress(Integer.parseInt(downSlowStartIntervalEndValue), Integer.parseInt(downSlowStopIntervalStartValue));
            seekBarDownStallDetectionInterval.setProgress(Integer.parseInt(downStallDetectionIntervalStartValue), Integer.parseInt(downStallDetectionIntervalEndValue));

            decimalFormat.applyPattern("#.##");
            downTorqueStallThreshold = decimalFormat.format(Double.parseDouble(downTorqueStallThreshold));
            mEtDownTorqueStallThreshold.setText(downTorqueStallThreshold);

            decimalFormat.applyPattern("#");
            downTorqueDetectionTime = decimalFormat.format(Double.parseDouble(downTorqueDetectionTime));
            mEtDownTorqueDetectionTime.setText(downTorqueDetectionTime);

            seekBarPullUpSlowStartStopInterval.setProgress(Integer.parseInt(pullUpSlowStartIntervalEndValue), Integer.parseInt(pullUpSlowStopIntervalStartValue));

            decimalFormat.applyPattern("#.##");
            pullUpTorqueStallThreshold = decimalFormat.format(Double.parseDouble(pullUpTorqueStallThreshold));
            mEtPullUpTorqueStallThreshold.setText(pullUpTorqueStallThreshold);

            decimalFormat.applyPattern("#");
            pullUpTorqueDetectionTime = decimalFormat.format(Double.parseDouble(pullUpTorqueDetectionTime));
            mEtPullUpTorqueDetectionTime.setText(pullUpTorqueDetectionTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doAfterSetting() {
        if (lockedRotorDetectionInfo != null) {
            if (!mSbDownEnable.isChecked()) {
                lockedRotorDetectionInfo.setLowtbtss("0");
            } else {
                lockedRotorDetectionInfo.setLowtbtss("1");
                lockedRotorDetectionInfo.setNumpput(downPulsesPerUnitTime);
                lockedRotorDetectionInfo.setPdajtime(downPulseDetectionTime);
                lockedRotorDetectionInfo.setDetintionb(downStallDetectionIntervalEndValue);
                lockedRotorDetectionInfo.setDetintiona(downStallDetectionIntervalStartValue);
                lockedRotorDetectionInfo.setLowsusrana(downSlowStopIntervalStartValue);
                lockedRotorDetectionInfo.setLowsusranb(downSlowStartIntervalEndValue);
                lockedRotorDetectionInfo.setLowtorblothr(downTorqueStallThreshold);
                lockedRotorDetectionInfo.setLowtordetime(downTorqueDetectionTime);
            }
            if (!mSbPullUpEnable.isChecked()) {
                lockedRotorDetectionInfo.setUptbtss("0");
            } else {
                lockedRotorDetectionInfo.setUptbtss("1");
                lockedRotorDetectionInfo.setUpsusranb(pullUpSlowStartIntervalEndValue);
                lockedRotorDetectionInfo.setUpsusrana(pullUpSlowStopIntervalStartValue);
                lockedRotorDetectionInfo.setUptorblothr(pullUpTorqueStallThreshold);
                lockedRotorDetectionInfo.setUptordetime(pullUpTorqueDetectionTime);
            }
        }
        mBtnSave.setEnabled(true);
        //TODO  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
        saveConfigInfo();
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        if (isEditable) {
            mEtDownPulsesPerUnitTime.setHint("请输入");
            mEtDownPulseDetectionTime.setHint("请输入");
            mEtDownTorqueStallThreshold.setHint("请输入");
            mEtDownTorqueDetectionTime.setHint("请输入");
            mEtPullUpTorqueStallThreshold.setHint("请输入");
            mEtPullUpTorqueDetectionTime.setHint("请输入");
        } else {
            mEtDownPulsesPerUnitTime.setHint("");
            mEtDownPulseDetectionTime.setHint("");
            mEtDownTorqueStallThreshold.setHint("");
            mEtDownTorqueDetectionTime.setHint("");
            mEtPullUpTorqueStallThreshold.setHint("");
            mEtPullUpTorqueDetectionTime.setHint("");

            mEtDownPulsesPerUnitTime.clearFocus();
            mEtDownPulseDetectionTime.clearFocus();
            mEtDownTorqueStallThreshold.clearFocus();
            mEtDownTorqueDetectionTime.clearFocus();
            mEtPullUpTorqueStallThreshold.clearFocus();
            mEtPullUpTorqueDetectionTime.clearFocus();

            initParamConfigInfo();
        }
        maskLayerLayout.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}