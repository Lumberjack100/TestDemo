package com.shmedo.mcloudapp.deviceconfig.view.adme;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.hjq.toast.ToastUtils;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/13 <br/>
 * 描述：     TODO
 */
public class AdmeLockedRotorDetectionView extends LinearLayout {
    public static final int DOWN_OPERATOR = 0x0001;
    public static final int PULLUP_OPERATOR = 0x0002;

    @BindView(R.id.downEnableSBtn)
    public SwitchButton mSbDownEnable;

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
    public  SwitchButton mSbPullUpEnable;

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


    public AdmeLockedRotorDetectionView(Context context) {
        this(context, null);
    }

    public AdmeLockedRotorDetectionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeLockedRotorDetectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_locked_rotor_detection_fragment, this, true);
        ButterKnife.bind(this);
        initView();
        initTextChangedListener(context);
        initSeekBarListener();
    }

    @Nullable
    private static Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    private void initView() {
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

    private void initTextChangedListener(Context context) {
        KeyboardVisibilityEvent.setEventListener(
                findActivity(context),
                (LifecycleOwner) findActivity(context),
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        // some code depending on keyboard visiblity status
                        if(!isOpen){
                            mEtDownSlowStartInterval.clearFocus();
                            mEtDownSlowStopInterval.clearFocus();
                            mEtPullUpSlowStartInterval.clearFocus();
                            mEtPullUpSlowStopInterval.clearFocus();
                        }
                    }
                });
        mEtDownSlowStartInterval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //失去焦点时
                if (!hasFocus) {
                    if (TextUtils.isEmpty(mEtDownSlowStartInterval.getText())) {
                        ToastUtils.show("请输入下放加速距离");
//                        mEtDownSlowStartInterval.requestFocus();
                        return;
                    }
                    try {
                        int left = Integer.parseInt(mEtDownSlowStartInterval.getText().toString());
                        int right = TextUtils.isEmpty(mEtDownSlowStopInterval.getText()) ? 0 : Integer.parseInt(mEtDownSlowStopInterval.getText().toString());
                        if (left + right > holedepth) {
                            PopTip.show("下放加速距离与下放减速距离之和不能超过下放总距离 " + holedepth + "mm").autoDismiss(4500).iconError();
                            return;
                        }
                        float leftPercent = ((float) left / holedepth) * 100;
                        seekBarDownSlowStartStopInterval.setProgress(leftPercent, seekBarDownSlowStartStopInterval.getRightSeekBar().getProgress());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } else {
                    Timber.d("获得焦点");
                }
            }
        });
        mEtDownSlowStopInterval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //失去焦点时
                if (!hasFocus) {
                    if (TextUtils.isEmpty(mEtDownSlowStopInterval.getText())) {
                        ToastUtils.show("请输入下放减速距离");
                        return;
                    }
                    try {
                        int left = TextUtils.isEmpty(mEtDownSlowStartInterval.getText()) ? 0 : Integer.parseInt(mEtDownSlowStartInterval.getText().toString());
                        int right = Integer.parseInt(mEtDownSlowStopInterval.getText().toString());
                        if (left + right > holedepth) {
                            PopTip.show("下放加速距离与下放减速距离之和不能超过下放总距离 " + holedepth + "mm").autoDismiss(4500).iconError();
                            return;
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

                } else {
                    Timber.d("获得焦点");
                }
            }
        });
        mEtPullUpSlowStartInterval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //失去焦点时
                if (!hasFocus) {
                    if (TextUtils.isEmpty(mEtPullUpSlowStartInterval.getText())) {
                        ToastUtils.show("请输入上拉加速距离");
                        return;
                    }
                    try {
                        int left = Integer.parseInt(mEtPullUpSlowStartInterval.getText().toString());
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

                } else {
                    Timber.d("获得焦点");
                }
            }
        });
        mEtPullUpSlowStopInterval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //失去焦点时
                if (!hasFocus) {
                    if (TextUtils.isEmpty(mEtPullUpSlowStopInterval.getText())) {
                        ToastUtils.show("请输入下放减速距离");
                        return;
                    }
                    try {
                        int left = TextUtils.isEmpty(mEtPullUpSlowStartInterval.getText()) ? 0 : Integer.parseInt(mEtPullUpSlowStartInterval.getText().toString());
                        int right = Integer.parseInt(mEtPullUpSlowStopInterval.getText().toString());
                        if (left + right > measpacing) {
                            PopTip.show("上拉加速距离与上拉减速距离之和不能超过测量间距 " + measpacing + "mm").autoDismiss(4500).iconError();
                            return;
                        }
                        float rightPercent = ((float) right / measpacing) * 100;
                        seekBarPullUpSlowStartStopInterval.setProgress(seekBarPullUpSlowStartStopInterval.getLeftSeekBar().getProgress(), rightPercent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } else {
                    Timber.d("获得焦点");
                }
            }
        });
        //region  TextWatcher
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
//        mEtDownSlowStopInterval.addTextChangedListener(new TextWatcher() {
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
//                if (!TextUtils.isEmpty(s.toString()) && mEtDownSlowStopInterval.hasFocus()) {
//                    try {
//                        int right = Integer.parseInt(s.toString());
//                        int left = TextUtils.isEmpty(mEtDownSlowStartInterval.getText()) ? 0 : Integer.parseInt(mEtDownSlowStartInterval.getText().toString());
//                        if (left + right > holedepth) {
//                            PopTip.show("下放加速距离与下放减速距离之和不能超过下放总距离 " + holedepth + "mm").autoDismiss(4500).iconError();
//                        }
//                        float rightPercent = ((float) right / holedepth) * 100;
//                        seekBarDownSlowStartStopInterval.setProgress(seekBarDownSlowStartStopInterval.getLeftSeekBar().getProgress(), rightPercent);
//
//                        float downStallDetectionEndValue = seekBarDownStallDetectionInterval.getRightSeekBar().getProgress();
//                        if (downStallDetectionEndValue >= rightPercent) {
//                            seekBarDownStallDetectionInterval.setProgress(seekBarDownStallDetectionInterval.getLeftSeekBar().getProgress(), rightPercent < 50 ? 50 : rightPercent - 1);
//                        }
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//        mEtPullUpSlowStartInterval.addTextChangedListener(new TextWatcher() {
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
//                if (!TextUtils.isEmpty(s.toString()) && mEtPullUpSlowStartInterval.hasFocus()) {
//                    try {
//                        int left = Integer.parseInt(s.toString());
//                        int right = TextUtils.isEmpty(mEtPullUpSlowStopInterval.getText()) ? 0 : Integer.parseInt(mEtPullUpSlowStopInterval.getText().toString());
//                        if (left + right > measpacing) {
//                            PopTip.show("上拉加速距离与上拉减速距离之和不能超过测量间距 " + measpacing + "mm").autoDismiss(4500).iconError();
//                            return;
//                        }
//                        float leftPercent = ((float) left / measpacing) * 100;
//                        seekBarPullUpSlowStartStopInterval.setProgress(leftPercent, seekBarPullUpSlowStartStopInterval.getRightSeekBar().getProgress());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//        mEtPullUpSlowStopInterval.addTextChangedListener(new TextWatcher() {
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
//                if (!TextUtils.isEmpty(s.toString()) && mEtPullUpSlowStopInterval.hasFocus()) {
//                    try {
//                        int right = Integer.parseInt(s.toString());
//                        int left = TextUtils.isEmpty(mEtPullUpSlowStartInterval.getText()) ? 0 : Integer.parseInt(mEtPullUpSlowStartInterval.getText().toString());
//                        if (left + right > measpacing) {
//                            PopTip.show("上拉加速距离与上拉减速距离之和不能超过测量间距 " + measpacing + "mm").autoDismiss(4500).iconError();
//                            return;
//                        }
//                        float rightPercent = ((float) right / measpacing) * 100;
//                        seekBarPullUpSlowStartStopInterval.setProgress(seekBarPullUpSlowStartStopInterval.getLeftSeekBar().getProgress(), rightPercent);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
        //endregion
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
                if (rightValue >= downSlowStopStartValue) {
                    view.setProgress(leftValue, seekBarDownSlowStartStopInterval.getRightSeekBar().getProgress() - 1 < 50 ? 50 : seekBarDownSlowStartStopInterval.getRightSeekBar().getProgress() - 1);
                }
            }
        });
        //上拉缓起缓停区间
        seekBarPullUpSlowStartStopInterval.setIndicatorTextDecimalFormat("0");
        seekBarPullUpSlowStartStopInterval.setEnabled(false);
    }

}
