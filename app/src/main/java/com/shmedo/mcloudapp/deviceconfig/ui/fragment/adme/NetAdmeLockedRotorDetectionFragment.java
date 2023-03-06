package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
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
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：      ADME 4g模式下电机运动堵转缓停参数配置页面
 */
public class NetAdmeLockedRotorDetectionFragment extends BaseNetIotCommunicateFragment {
    public static final int DOWN_OPERATOR = 0x0001;
    public static final int PULLUP_OPERATOR = 0x0002;

    @BindView(R.id.downEnableSBtn)
    SwitchButton mSbDownEnable;

    @BindView(R.id.et_down_pulses_per_unit_time)
    ClearEditText mEtDownPulsesPerUnitTime;//单位时间脉冲数

    @BindView(R.id.et_down_pulse_detection_time)
    ClearEditText mEtDownPulseDetectionTime;//检测判断时间

    @BindView(R.id.et_down_slow_start_interval)
    ClearEditText mEtDownSlowStartInterval;//下放缓起区间

    @BindView(R.id.et_down_slow_stop_interval)
    ClearEditText mEtDownSlowStopInterval;//下放缓停区间

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

    @BindView(R.id.et_pull_up_slow_start_interval)
    ClearEditText mEtPullUpSlowStartInterval;//上拉缓起区间

    @BindView(R.id.et_pull_up_slow_stop_interval)
    ClearEditText mEtPullUpSlowStopInterval;//上拉缓停区间

    @BindView(R.id.seekBar_pull_up_slow_stop_interval)
    RangeSeekBar seekBarPullUpSlowStartStopInterval;//上拉缓起缓停区间

    @BindView(R.id.et_pull_up_torque_stall_threshold)
    ClearEditText mEtPullUpTorqueStallThreshold;//上拉力矩堵转阈值

    @BindView(R.id.et_pull_up_torque_detection_time)
    ClearEditText mEtPullUpTorqueDetectionTime;//上拉力矩检测判断时间

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.downMeterChildMaskLayer)
    ViewGroup downMeterChildMaskLayer;

    @BindView(R.id.pullUpMeterChildMaskLayer)
    ViewGroup pullUpMeterChildMaskLayer;

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
    private int holedepth = 0;//下放距离
    private int measpacing = 0;//上拉测量间距

    private DecimalFormat decimalFormat = new DecimalFormat();

    public static NetAdmeLockedRotorDetectionFragment newInstance(DeviceInfo deviceInfo) {
        NetAdmeLockedRotorDetectionFragment fragment = new NetAdmeLockedRotorDetectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
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
        initTextChangedListener();
        initSeekBarListener();
        queryParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    private void setView() {
        InputFilter numberFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!"0123456789".contains(source.charAt(i) + "")) {
                        return "";
                    }
                }
                return null;
            }
        };
        mEtDownPulsesPerUnitTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5), numberFilter});
        mEtDownPulsesPerUnitTime.setHint("[1,10000]");

        mEtDownPulseDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDownPulseDetectionTime.setHint("[0.1,10.0]");

        mEtDownSlowStartInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6), numberFilter});
        mEtDownSlowStopInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6), numberFilter});

        mEtDownTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDownTorqueStallThreshold.setHint("[0.00,2.00]");

        mEtDownTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDownTorqueDetectionTime.setHint("[0.01,5.00]");

        mEtPullUpSlowStartInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3), numberFilter});
        mEtPullUpSlowStopInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3), numberFilter});

        mEtPullUpTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtPullUpTorqueStallThreshold.setHint("[1.00,6.00]");

        mEtPullUpTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtPullUpTorqueDetectionTime.setHint("[0.01,5.00]");
    }

    private void setSwitchViewListener() {
        mSbDownEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定使下放计米堵转检测不生效？", DOWN_OPERATOR);
                } else {
                    downMeterChildMaskLayer.setVisibility(View.GONE);
                }
            }
        });
        mSbPullUpEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定使上拉计米堵转检测不生效？", PULLUP_OPERATOR);
                } else {
                    pullUpMeterChildMaskLayer.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initTextChangedListener() {
        mEtDownSlowStartInterval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String ss = "";
                }
            }
        });
//        mEtDownSlowStartInterval.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (!TextUtils.isEmpty(s.toString()) && mEtDownSlowStartInterval.hasFocus()) {
//                    try {
//                        int left = Integer.parseInt(s.toString());
//                        int right = TextUtils.isEmpty(mEtDownSlowStopInterval.getText()) ? 0 : Integer.parseInt(mEtDownSlowStopInterval.getText().toString());
//                        if (left + right > holedepth) {
//                            PopTip.show("下放加速距离与下放减速距离之和不能超过下放总距离 " + holedepth + "mm").autoDismiss(4500).iconError();
//                            return;
//                        }
//                        float leftPercent = ((float) left / holedepth) * 100;
//                        seekBarDownSlowStartStopInterval.setProgress(leftPercent, seekBarDownSlowStartStopInterval.getRightSeekBar().getProgress());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
        mEtDownSlowStopInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString()) && mEtDownSlowStopInterval.hasFocus()) {
                    try {
                        int right = Integer.parseInt(s.toString());
                        int left = TextUtils.isEmpty(mEtDownSlowStartInterval.getText()) ? 0 : Integer.parseInt(mEtDownSlowStartInterval.getText().toString());
                        if (left + right > holedepth) {
                            PopTip.show("下放加速距离与下放减速距离之和不能超过下放总距离 " + holedepth + "mm").autoDismiss(4500).iconError();
                        }
                        float rightPercent = ((float) right / holedepth) * 100;
                        seekBarDownSlowStartStopInterval.setProgress(seekBarDownSlowStartStopInterval.getLeftSeekBar().getProgress(), rightPercent);

                        float downStallDetectionEndValue = seekBarDownStallDetectionInterval.getRightSeekBar().getProgress();
                        if (downStallDetectionEndValue >= rightPercent) {
                            seekBarDownStallDetectionInterval.setProgress(seekBarDownStallDetectionInterval.getLeftSeekBar().getProgress(), rightPercent < 50 ? 50 : rightPercent - 1);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        mEtPullUpSlowStartInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString()) && mEtPullUpSlowStartInterval.hasFocus()) {
                    try {
                        int left = Integer.parseInt(s.toString());
                        int right = TextUtils.isEmpty(mEtPullUpSlowStopInterval.getText()) ? 0 : Integer.parseInt(mEtPullUpSlowStopInterval.getText().toString());
                        if (left + right > measpacing) {
                            PopTip.show("上拉加速距离与上拉减速距离之和不能超过测量间距 " + measpacing + "mm").autoDismiss(4500).iconError();
                            return;
                        }
                        float leftPercent = ((float) left / measpacing) * 100;
                        seekBarPullUpSlowStartStopInterval.setProgress(leftPercent, seekBarPullUpSlowStartStopInterval.getRightSeekBar().getProgress());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        mEtPullUpSlowStopInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString()) && mEtPullUpSlowStopInterval.hasFocus()) {
                    try {
                        int right = Integer.parseInt(s.toString());
                        int left = TextUtils.isEmpty(mEtPullUpSlowStartInterval.getText()) ? 0 : Integer.parseInt(mEtPullUpSlowStartInterval.getText().toString());
                        if (left + right > measpacing) {
                            PopTip.show("上拉加速距离与上拉减速距离之和不能超过测量间距 " + measpacing + "mm").autoDismiss(4500).iconError();
                            return;
                        }
                        float rightPercent = ((float) right / measpacing) * 100;
                        seekBarPullUpSlowStartStopInterval.setProgress(seekBarPullUpSlowStartStopInterval.getLeftSeekBar().getProgress(), rightPercent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void initSeekBarListener() {
        //下放缓起缓停区间
        seekBarDownSlowStartStopInterval.setIndicatorTextDecimalFormat("0");
        seekBarDownSlowStartStopInterval.setEnabled(false);

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
                float downSlowStopStartValue = seekBarDownSlowStartStopInterval.getRightSeekBar().getProgress();
                if (leftValue >= 50) {
                    ToastUtils.show("下放堵转检测区间起始值不能大于50%");
                    view.setProgress(49);
                }
                if (rightValue < 50) {
                    ToastUtils.show("下放堵转检测区间终值不能小于50%");
                    view.setProgress(leftValue, 50);
                }
//                if (rightValue >= downSlowStopStartValue) {
//                    seekBarDownSlowStartStopInterval.setProgress(seekBarDownSlowStartStopInterval.getLeftSeekBar().getProgress(), rightValue >= 100 ? 100 : rightValue + 1);
//                }
            }
        });
        //上拉缓起缓停区间
        seekBarPullUpSlowStartStopInterval.setIndicatorTextDecimalFormat("0");
        seekBarPullUpSlowStartStopInterval.setEnabled(false);
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
                            downMeterChildMaskLayer.setVisibility(View.VISIBLE);
                        } else if (type == PULLUP_OPERATOR) {
                            processSave();
                            pullUpMeterChildMaskLayer.setVisibility(View.VISIBLE);
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
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION);
        showWaitDialog("加载中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
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
        downSlowStartIntervalEndValue = mEtDownSlowStartInterval.getText().toString().trim();
        downSlowStopIntervalStartValue = mEtDownSlowStopInterval.getText().toString().trim();
        downTorqueStallThreshold = mEtDownTorqueStallThreshold.getText().toString().trim();
        downTorqueDetectionTime = mEtDownTorqueDetectionTime.getText().toString().trim();

        pullUpSlowStartIntervalEndValue = mEtPullUpSlowStartInterval.getText().toString().trim();
        pullUpSlowStopIntervalStartValue = mEtPullUpSlowStopInterval.getText().toString().trim();
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
                    MessageDialog.show("提示", "下放单位时间脉冲数不能小于1或大于10000!", "我已知晓");
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
                    MessageDialog.show("提示", "下放脉冲检测判断时间不能小于0.1或大于10!", "我已知晓");
                    mEtDownPulseDetectionTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放脉冲检测判断时间!");
                mEtDownPulseDetectionTime.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(downSlowStartIntervalEndValue)) {
            ToastUtils.show("请输入下放加速距离!");
            mEtDownSlowStartInterval.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(downSlowStopIntervalStartValue)) {
            ToastUtils.show("请输入下放减速距离!");
            mEtDownSlowStopInterval.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(downTorqueStallThreshold)) {
            ToastUtils.show("请输入下放力矩堵转阈值!");
            mEtDownTorqueStallThreshold.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(downTorqueStallThreshold);
            if (value < 0.00 || value > 2.00) {
                MessageDialog.show("提示", "下放力矩堵转阈值不能小于0或大于2!", "我已知晓");
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
                MessageDialog.show("提示", "下放力矩检测判断时间不能小于0.01或大于5!", "我已知晓");
                mEtDownTorqueDetectionTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放力矩检测判断时间!");
            mEtDownTorqueDetectionTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(pullUpSlowStartIntervalEndValue)) {
            ToastUtils.show("请输入上拉加速距离!");
            mEtPullUpSlowStartInterval.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(pullUpSlowStopIntervalStartValue)) {
            ToastUtils.show("请输入上拉减速距离!");
            mEtPullUpSlowStopInterval.requestFocus();
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
                MessageDialog.show("提示", "上拉力矩堵转阈值不能小于1或大于6!", "我已知晓");
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
                MessageDialog.show("提示", "上拉力矩检测判断时间不能小于0.01或大于5!", "我已知晓");
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
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
            showWaitDialog("处理中...");
            doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            case ADME_MD_GET_LOCKED_ROTOR_DETECTION: {
                dismissWaitDialog();
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
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置堵转参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(cmdResult.getReason().contains("equimodel_err") ? "设备模式错误，无法配置参数" : errMsg);
                    mBtnSave.setEnabled(true);
                    return;
                }
                mBtnSave.setEnabled(true);
                saveConfigInfo();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                dismissWaitDialog();
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
        downSlowStartIntervalEndValue = lockedRotorDetectionInfo.getLowsusranb().trim().replace("-", "");//下放缓起区间终值(加速阶段)
        downSlowStopIntervalStartValue = lockedRotorDetectionInfo.getLowsusrana().trim().replace("-", "");//下放缓停区间起始值(减速阶段)
        downStallDetectionIntervalStartValue = lockedRotorDetectionInfo.getDetintiona().trim();//堵转检测区间起始值
        downStallDetectionIntervalEndValue = lockedRotorDetectionInfo.getDetintionb().trim();//堵转检测区间终值
        downTorqueStallThreshold = lockedRotorDetectionInfo.getLowtorblothr().trim();//下放力矩堵转阈值
        downTorqueDetectionTime = lockedRotorDetectionInfo.getLowtordetime().trim();//下放力矩检测判断时间

        pullUpSlowStartIntervalEndValue = lockedRotorDetectionInfo.getUpsusranb().trim().replace("-", "");//上拉缓起区间终值(加速阶段)
        pullUpSlowStopIntervalStartValue = lockedRotorDetectionInfo.getUpsusrana().trim().replace("-", "");//上拉缓停区间起始值(减速阶段)
        pullUpTorqueStallThreshold = lockedRotorDetectionInfo.getUptorblothr().trim();//上拉力矩堵转阈值
        pullUpTorqueDetectionTime = lockedRotorDetectionInfo.getUptordetime().trim();//上拉力矩检测判断时间

        if (lockedRotorDetectionInfo.getLowtbtss().equals("0")) {
            mSbDownEnable.setCheckedImmediatelyNoEvent(false);
            downMeterChildMaskLayer.setVisibility(View.VISIBLE);
        } else {
            mSbDownEnable.setCheckedImmediatelyNoEvent(true);
            downMeterChildMaskLayer.setVisibility(View.GONE);
        }
        if (lockedRotorDetectionInfo.getUptbtss().equals("0")) {
            mSbPullUpEnable.setCheckedImmediatelyNoEvent(false);
            pullUpMeterChildMaskLayer.setVisibility(View.VISIBLE);
        } else {
            mSbPullUpEnable.setCheckedImmediatelyNoEvent(true);
            pullUpMeterChildMaskLayer.setVisibility(View.GONE);
        }

        try {
            mEtDownPulsesPerUnitTime.setText(downPulsesPerUnitTime);
            decimalFormat.applyPattern("#.#");
            downPulseDetectionTime = decimalFormat.format(Double.parseDouble(downPulseDetectionTime));
            mEtDownPulseDetectionTime.setText(downPulseDetectionTime);

            mEtDownSlowStartInterval.setText(downSlowStartIntervalEndValue);
            mEtDownSlowStopInterval.setText(downSlowStopIntervalStartValue);

            seekBarDownStallDetectionInterval.setProgress(Integer.parseInt(downStallDetectionIntervalStartValue), Integer.parseInt(downStallDetectionIntervalEndValue));

            decimalFormat.applyPattern("#.##");
            downTorqueStallThreshold = decimalFormat.format(Double.parseDouble(downTorqueStallThreshold));
            mEtDownTorqueStallThreshold.setText(downTorqueStallThreshold);

            decimalFormat.applyPattern("#.#");
            downTorqueDetectionTime = decimalFormat.format(Double.parseDouble(downTorqueDetectionTime));
            mEtDownTorqueDetectionTime.setText(downTorqueDetectionTime);

            mEtPullUpSlowStartInterval.setText(pullUpSlowStartIntervalEndValue);
            mEtPullUpSlowStopInterval.setText(pullUpSlowStopIntervalStartValue);

            decimalFormat.applyPattern("#.##");
            pullUpTorqueStallThreshold = decimalFormat.format(Double.parseDouble(pullUpTorqueStallThreshold));
            mEtPullUpTorqueStallThreshold.setText(pullUpTorqueStallThreshold);

            decimalFormat.applyPattern("#.#");
            pullUpTorqueDetectionTime = decimalFormat.format(Double.parseDouble(pullUpTorqueDetectionTime));
            mEtPullUpTorqueDetectionTime.setText(pullUpTorqueDetectionTime);

            holedepth = Integer.parseInt(Objects.requireNonNull(lockedRotorDetectionInfo.getHoledepth()));
            measpacing = Integer.parseInt(Objects.requireNonNull(lockedRotorDetectionInfo.getMeaspacing()));
            float leftValue = holedepth == 0 ? 0 : (Integer.parseInt(downSlowStartIntervalEndValue) / (float) holedepth) * 100;
            float rightValue = holedepth == 0 ? 0 : (Integer.parseInt(downSlowStopIntervalStartValue) / (float) holedepth) * 100;
            leftValue = leftValue > 100 ? 100 : leftValue;
            rightValue = rightValue > 100 ? 100 : rightValue;
            if (leftValue > rightValue)
                leftValue = rightValue;
            seekBarDownSlowStartStopInterval.setProgress(leftValue, rightValue);

            leftValue = measpacing == 0 ? 0 : (Integer.parseInt(pullUpSlowStartIntervalEndValue) / (float) measpacing) * 100;
            rightValue = measpacing == 0 ? 0 : (Integer.parseInt(pullUpSlowStopIntervalStartValue) / (float) measpacing) * 100;
            leftValue = leftValue > 100 ? 100 : leftValue;
            rightValue = rightValue > 100 ? 100 : rightValue;
            if (leftValue > rightValue)
                leftValue = rightValue;
            seekBarPullUpSlowStartStopInterval.setProgress(leftValue, rightValue);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
