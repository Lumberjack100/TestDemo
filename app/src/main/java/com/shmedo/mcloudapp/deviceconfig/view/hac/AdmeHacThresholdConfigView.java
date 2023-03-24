package com.shmedo.mcloudapp.deviceconfig.view.hac;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeVoltageConfigEntity;
import com.shmedo.configlibrary.iot.cmd.entity.hac.HacWarningValueEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeVoltageConfigInfo;
import com.shmedo.configlibrary.iot.model.hac.HacWarningValue;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：     HAC 阈值配置页面
 */
public class AdmeHacThresholdConfigView extends LinearLayout {
    @BindView(R.id.et_drive_standard_voltage_threshold)
    ClearEditText mEtDriveStandardVoltageThreshold;//驱动器标压阈值

    @BindView(R.id.et_drive_low_voltage_threshold)
    ClearEditText mEtDriveLowVoltageThreshold;//驱动器低压阈值

    @BindView(R.id.et_drive_under_voltage_threshold)
    ClearEditText mEtDriveUnderVoltageThreshold;//驱动器欠压阈值

    @BindView(R.id.et_inclinometer_standard_voltage_threshold)
    ClearEditText mEtInclinometerStandardVoltageThreshold;//测斜仪标压阈值

    @BindView(R.id.et_inclinometer_low_voltage_threshold)
    ClearEditText mEtInclinometerLowVoltageThreshold;//测斜仪低压阈值

    @BindView(R.id.et_inclinometer_under_voltage_threshold)
    ClearEditText mEtInclinometerUnderVoltageThreshold;//测斜仪欠压阈值


    @BindView(R.id.et_wire_rope_length)
    ClearEditText mEtWireRopeLength;//钢丝绳长度


    @BindView(R.id.et_first_level_x_axis_min)
    ClearEditText mEtFirstLevelXAxisMin;//X轴相对位移量(最小值)

    @BindView(R.id.et_first_level_x_axis_max)
    ClearEditText mEtFirstLevelXAxisMax;//X轴相对位移量(最大值)

    @BindView(R.id.et_first_level_y_axis_min)
    ClearEditText mEtFirstLevelYAxisMin;//Y轴相对位移量(最小值)

    @BindView(R.id.et_first_level_y_axis_max)
    ClearEditText mEtFirstLevelYAxisMax;//Y轴相对位移量(最大值)


    @BindView(R.id.et_second_level_x_axis_min)
    ClearEditText mEtSecondLevelXAxisMin;//X轴相对位移量(最小值)

    @BindView(R.id.et_second_level_x_axis_max)
    ClearEditText mEtSecondLevelXAxisMax;//X轴相对位移量(最大值)

    @BindView(R.id.et_second_level_y_axis_min)
    ClearEditText mEtSecondLevelYAxisMin;//Y轴相对位移量(最小值)

    @BindView(R.id.et_second_level_y_axis_max)
    ClearEditText mEtSecondLevelYAxisMax;//Y轴相对位移量(最大值)


    @BindView(R.id.et_third_level_x_axis_min)
    ClearEditText mEtThirdLevelXAxisMin;//X轴相对位移量(最小值)

    @BindView(R.id.et_third_level_x_axis_max)
    ClearEditText mEtThirdLevelXAxisMax;//X轴相对位移量(最大值)

    @BindView(R.id.et_third_level_y_axis_min)
    ClearEditText mEtThirdLevelYAxisMin;//Y轴相对位移量(最小值)

    @BindView(R.id.et_third_level_y_axis_max)
    ClearEditText mEtThirdLevelYAxisMax;//Y轴相对位移量(最大值)

    @BindView(R.id.btn_add_second_level)
    Button mBtnAddSecondLevel;

    @BindView(R.id.btn_add_third_level)
    Button mBtnAddThirdLevel;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_drive_standard_voltage_threshold)
    ViewGroup driveStandardVoltageThresholdLayout;

    @BindView(R.id.ll_drive_low_voltage_threshold)
    ViewGroup driveLowVoltageThresholdLayout;

    @BindView(R.id.ll_drive_under_voltage_threshold)
    ViewGroup driveUnderVoltageThresholdLayout;

    @BindView(R.id.ll_inclinometer_standard_voltage_threshold)
    ViewGroup inclinometerStandardVoltageThresholdLayout;

    @BindView(R.id.ll_inclinometer_low_voltage_threshold)
    ViewGroup inclinometerLowVoltageThresholdLayout;

    @BindView(R.id.ll_inclinometer_under_voltage_threshold)
    ViewGroup inclinometerUnderVoltageThresholdLayout;

    @BindView(R.id.ll_wire_rope_length)
    ViewGroup wireRopeLengthLayout;

    @BindView(R.id.ll_first_level_warning)
    public ViewGroup firstLevelWarningLayout;

    @BindView(R.id.ll_second_level_warning)
    public ViewGroup secondLevelWarningLayout;

    @BindView(R.id.ll_third_level_warning)
    public ViewGroup thirdLevelWarningLayout;

    private String driveStandardVoltageThreshold;
    private String driveLowVoltageThreshold;
    private String driveUnderVoltageThreshold;
    private String inclinometerStandardVoltageThreshold;
    private String inclinometerLowVoltageThreshold;
    private String inclinometerUnderVoltageThreshold;
    private String wireRopeLength;

    private String firstLevelXAxisMin;
    private String firstLevelXAxisMax;
    private String firstLevelYAxisMin;
    private String firstLevelYAxisMax;

    private String secondLevelXAxisMin;
    private String secondLevelXAxisMax;
    private String secondLevelYAxisMin;
    private String secondLevelYAxisMax;

    private String thirdLevelXAxisMin;
    private String thirdLevelXAxisMax;
    private String thirdLevelYAxisMin;
    private String thirdLevelYAxisMax;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public AdmeVoltageConfigInfo voltageConfigInfo = new AdmeVoltageConfigInfo();
    public HacWarningValue warningValue;


    public AdmeHacThresholdConfigView(Context context) {
        this(context, null);
    }

    public AdmeHacThresholdConfigView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeHacThresholdConfigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_hac_threshold_config_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtDriveStandardVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtDriveLowVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtDriveUnderVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInclinometerStandardVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInclinometerLowVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInclinometerUnderVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtFirstLevelXAxisMin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtFirstLevelXAxisMax.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtFirstLevelYAxisMin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtFirstLevelYAxisMax.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtSecondLevelXAxisMin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtSecondLevelXAxisMax.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtSecondLevelYAxisMin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtSecondLevelYAxisMax.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtThirdLevelXAxisMin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtThirdLevelXAxisMax.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtThirdLevelYAxisMin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtThirdLevelYAxisMax.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    public boolean checkValueIsValid() {
        if (!driveStandardVoltageThreshold.equals("NullKey")) {
            driveStandardVoltageThreshold = mEtDriveStandardVoltageThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(driveStandardVoltageThreshold)) {
                ToastUtils.show("请输入驱动器标压阈值!");
                mEtDriveStandardVoltageThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(driveStandardVoltageThreshold);
                if (value < 1) {
                    ToastUtils.show("请输入正确的驱动器标压阈值!");
                    mEtDriveStandardVoltageThreshold.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的驱动器标压阈值!");
                mEtDriveStandardVoltageThreshold.requestFocus();
                return false;
            }
        }

        if (!driveLowVoltageThreshold.equals("NullKey")) {
            driveLowVoltageThreshold = mEtDriveLowVoltageThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(driveLowVoltageThreshold)) {
                ToastUtils.show("请输入驱动器低压阈值!");
                mEtDriveLowVoltageThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(driveLowVoltageThreshold);
                if (value < 1) {
                    ToastUtils.show("请输入正确的驱动器低压阈值!");
                    mEtDriveLowVoltageThreshold.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的驱动器低压阈值!");
                mEtDriveLowVoltageThreshold.requestFocus();
                return false;
            }
        }

        if (!driveUnderVoltageThreshold.equals("NullKey")) {
            driveUnderVoltageThreshold = mEtDriveUnderVoltageThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(driveUnderVoltageThreshold)) {
                ToastUtils.show("请输入驱动器欠压阈值!");
                mEtDriveUnderVoltageThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(driveUnderVoltageThreshold);
                if (value < 1) {
                    ToastUtils.show("请输入正确的驱动器欠压阈值!");
                    mEtDriveUnderVoltageThreshold.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的驱动器欠压阈值!");
                mEtDriveUnderVoltageThreshold.requestFocus();
                return false;
            }
        }

        if (!inclinometerStandardVoltageThreshold.equals("NullKey")) {
            inclinometerStandardVoltageThreshold = mEtInclinometerStandardVoltageThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(inclinometerStandardVoltageThreshold)) {
                ToastUtils.show("请输入测斜仪标压阈值!");
                mEtInclinometerStandardVoltageThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(inclinometerStandardVoltageThreshold);
                if (value < 1) {
                    ToastUtils.show("请输入正确的测斜仪标压阈值!");
                    mEtInclinometerStandardVoltageThreshold.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测斜仪标压阈值!");
                mEtInclinometerStandardVoltageThreshold.requestFocus();
                return false;
            }
        }
        if (!inclinometerLowVoltageThreshold.equals("NullKey")) {
            inclinometerLowVoltageThreshold = mEtInclinometerLowVoltageThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(inclinometerLowVoltageThreshold)) {
                ToastUtils.show("请输入测斜仪低压阈值!");
                mEtInclinometerLowVoltageThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(inclinometerLowVoltageThreshold);
                if (value < 1) {
                    ToastUtils.show("请输入正确的测斜仪低压阈值!");
                    mEtInclinometerLowVoltageThreshold.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测斜仪低压阈值!");
                mEtInclinometerLowVoltageThreshold.requestFocus();
                return false;
            }
        }
        if (!inclinometerUnderVoltageThreshold.equals("NullKey")) {
            inclinometerUnderVoltageThreshold = mEtInclinometerUnderVoltageThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(inclinometerUnderVoltageThreshold)) {
                ToastUtils.show("请输入测斜仪欠压阈值!");
                mEtInclinometerUnderVoltageThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(inclinometerUnderVoltageThreshold);
                if (value < 1) {
                    ToastUtils.show("请输入正确的测斜仪欠压阈值!");
                    mEtInclinometerUnderVoltageThreshold.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测斜仪欠压阈值!");
                mEtInclinometerUnderVoltageThreshold.requestFocus();
                return false;
            }
        }

        if (!wireRopeLength.equals("NullKey")) {
            wireRopeLength = mEtWireRopeLength.getText().toString().trim();
            if (TextUtils.isEmpty(wireRopeLength)) {
                ToastUtils.show("请输入钢丝绳长度!");
                mEtWireRopeLength.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(wireRopeLength);
                if (value < 1) {
                    ToastUtils.show("请输入正确的钢丝绳长度!");
                    mEtWireRopeLength.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的钢丝绳长度!");
                mEtWireRopeLength.requestFocus();
                return false;
            }
        }


        /** 预警级别阈值处理 */
//        if ((TextUtils.isEmpty(firstLevelXAxisMin) || firstLevelXAxisMin.equals("0")) && (TextUtils.isEmpty(firstLevelXAxisMax) || firstLevelXAxisMax.equals("0")))
//            return true;
//
//        if (TextUtils.isEmpty(firstLevelXAxisMin)) {
//            ToastUtils.show("请输入测斜仪欠压阈值!");
//            mEtInclinometerUnderVoltageThreshold.requestFocus();
//            return false;
//        }
//        try {
//            double value = Double.parseDouble(firstLevelXAxisMin);
//
//        } catch (Exception ex) {
//            ToastUtils.show("请输入正确的测斜仪欠压阈值!");
//            mEtInclinometerUnderVoltageThreshold.requestFocus();
//            return false;
//        }

        return true;
    }

    /**
     * 获取电压阈值参数配置指令
     */
    public String getVoltageThresholdConfigCommand() {
        String command = "";
        try {
            AdmeVoltageConfigEntity entity = new AdmeVoltageConfigEntity();
            entity.setVolt_power_standard(voltageConfigInfo.getVolt_power_standard().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(driveStandardVoltageThreshold)));
            entity.setVolt_power_low(voltageConfigInfo.getVolt_power_low().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(driveLowVoltageThreshold)));
            entity.setVolt_power_under(voltageConfigInfo.getVolt_power_under().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(driveUnderVoltageThreshold)));
            entity.setVolt_sensor_standard(voltageConfigInfo.getVolt_sensor_standard().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerStandardVoltageThreshold)));
            entity.setVolt_sensor_low(voltageConfigInfo.getVolt_sensor_low().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerLowVoltageThreshold)));
            entity.setVolt_sensor_under(voltageConfigInfo.getVolt_sensor_under().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerUnderVoltageThreshold)));
            entity.setRope_length(voltageConfigInfo.getRope_length().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(wireRopeLength)));
            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_VOLTAGE, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    /**
     * 获取报警级别阈值参数配置指令
     */
    public String getWarningThresholdConfigCommand() {
        String command = "";
        if ((TextUtils.isEmpty(firstLevelXAxisMin) || firstLevelXAxisMin.equals("0")) && (TextUtils.isEmpty(firstLevelXAxisMax) || firstLevelXAxisMax.equals("0")))
            return "";

        try {
            HacWarningValueEntity entity = new HacWarningValueEntity();
            entity.setX1min(TextUtils.isEmpty(firstLevelXAxisMin) ? "0" : firstLevelXAxisMin);
            entity.setX1max(TextUtils.isEmpty(firstLevelXAxisMax) ? "0" : firstLevelXAxisMax);
            entity.setY1min(TextUtils.isEmpty(firstLevelYAxisMin) ? "0" : firstLevelYAxisMin);
            entity.setY1max(TextUtils.isEmpty(firstLevelYAxisMax) ? "0" : firstLevelYAxisMax);
            entity.setX2min(TextUtils.isEmpty(secondLevelXAxisMin) ? "0" : secondLevelXAxisMin);
            entity.setX2max(TextUtils.isEmpty(secondLevelXAxisMax) ? "0" : secondLevelXAxisMax);
            entity.setY2min(TextUtils.isEmpty(secondLevelYAxisMin) ? "0" : secondLevelYAxisMin);
            entity.setY2max(TextUtils.isEmpty(secondLevelYAxisMax) ? "0" : secondLevelYAxisMax);
            entity.setX3min(TextUtils.isEmpty(thirdLevelXAxisMin) ? "0" : thirdLevelXAxisMin);
            entity.setX3max(TextUtils.isEmpty(thirdLevelXAxisMax) ? "0" : thirdLevelXAxisMax);
            entity.setY3min(TextUtils.isEmpty(thirdLevelYAxisMin) ? "0" : thirdLevelYAxisMin);
            entity.setY3max(TextUtils.isEmpty(thirdLevelYAxisMax) ? "0" : thirdLevelYAxisMax);

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_SET_WARN, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    /**
     * 初始化电压阈值参数
     */
    public void initVoltageThresholdInfo() {
        if (voltageConfigInfo == null) {
            Timber.e("AdmeVoltageConfigInfo is Null!");
            voltageConfigInfo = new AdmeVoltageConfigInfo();
            return;
        }
        driveStandardVoltageThreshold = voltageConfigInfo.getVolt_power_standard().trim();
        driveLowVoltageThreshold = voltageConfigInfo.getVolt_power_low().trim();
        driveUnderVoltageThreshold = voltageConfigInfo.getVolt_power_under().trim();
        inclinometerStandardVoltageThreshold = voltageConfigInfo.getVolt_sensor_standard().trim();
        inclinometerLowVoltageThreshold = voltageConfigInfo.getVolt_sensor_low().trim();
        inclinometerUnderVoltageThreshold = voltageConfigInfo.getVolt_sensor_under().trim();
        wireRopeLength = voltageConfigInfo.getRope_length().trim();
        try {
            if (driveStandardVoltageThreshold.equals("NullKey")) {
                driveStandardVoltageThresholdLayout.setVisibility(View.GONE);
            } else {
                driveStandardVoltageThreshold = decimalFormat.format(Double.parseDouble(driveStandardVoltageThreshold));
                mEtDriveStandardVoltageThreshold.setText(driveStandardVoltageThreshold);
            }

            if (driveLowVoltageThreshold.equals("NullKey")) {
                driveLowVoltageThresholdLayout.setVisibility(View.GONE);
            } else {
                driveLowVoltageThreshold = decimalFormat.format(Double.parseDouble(driveLowVoltageThreshold));
                mEtDriveLowVoltageThreshold.setText(driveLowVoltageThreshold);
            }

            if (driveUnderVoltageThreshold.equals("NullKey")) {
                driveUnderVoltageThresholdLayout.setVisibility(View.GONE);
            } else {
                driveUnderVoltageThreshold = decimalFormat.format(Double.parseDouble(driveUnderVoltageThreshold));
                mEtDriveUnderVoltageThreshold.setText(driveUnderVoltageThreshold);
            }

            if (inclinometerStandardVoltageThreshold.equals("NullKey")) {
                inclinometerStandardVoltageThresholdLayout.setVisibility(View.GONE);
            } else {
                inclinometerStandardVoltageThreshold = decimalFormat.format(Double.parseDouble(inclinometerStandardVoltageThreshold));
                mEtInclinometerStandardVoltageThreshold.setText(inclinometerStandardVoltageThreshold);
            }

            if (inclinometerLowVoltageThreshold.equals("NullKey")) {
                inclinometerLowVoltageThresholdLayout.setVisibility(View.GONE);
            } else {
                inclinometerLowVoltageThreshold = decimalFormat.format(Double.parseDouble(inclinometerLowVoltageThreshold));
                mEtInclinometerLowVoltageThreshold.setText(inclinometerLowVoltageThreshold);
            }

            if (inclinometerUnderVoltageThreshold.equals("NullKey")) {
                inclinometerUnderVoltageThresholdLayout.setVisibility(View.GONE);
            } else {
                inclinometerUnderVoltageThreshold = decimalFormat.format(Double.parseDouble(inclinometerUnderVoltageThreshold));
                mEtInclinometerUnderVoltageThreshold.setText(inclinometerUnderVoltageThreshold);
            }

            if (wireRopeLength.equals("NullKey")) {
                wireRopeLengthLayout.setVisibility(View.GONE);
            } else {
                wireRopeLength = decimalFormat.format(Double.parseDouble(wireRopeLength));
                mEtWireRopeLength.setText(wireRopeLength);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化报警级别阈值
     */
    public void initWarningThresholdInfo() {
        if (warningValue == null) {
            Timber.e("HacWarningValue is Null!");
            warningValue = new HacWarningValue();
            firstLevelWarningLayout.setVisibility(View.VISIBLE);
            secondLevelWarningLayout.setVisibility(View.GONE);
            thirdLevelWarningLayout.setVisibility(View.GONE);
            return;
        }
        firstLevelXAxisMin = warningValue.getX1min();
        firstLevelXAxisMax = warningValue.getX1max();
        firstLevelYAxisMin = warningValue.getY1min();
        firstLevelYAxisMax = warningValue.getY1max();
        secondLevelXAxisMin = warningValue.getX2min();
        secondLevelXAxisMax = warningValue.getX2max();
        secondLevelYAxisMin = warningValue.getY2min();
        secondLevelYAxisMax = warningValue.getY2max();
        thirdLevelXAxisMin = warningValue.getX3min();
        thirdLevelXAxisMax = warningValue.getX3max();
        thirdLevelYAxisMin = warningValue.getY3min();
        thirdLevelYAxisMax = warningValue.getY3max();

        firstLevelWarningLayout.setVisibility(View.VISIBLE);
        mEtFirstLevelXAxisMin.setText(firstLevelXAxisMin);
        mEtFirstLevelXAxisMax.setText(firstLevelXAxisMax);
        mEtFirstLevelYAxisMin.setText(firstLevelYAxisMin);
        mEtFirstLevelYAxisMax.setText(firstLevelYAxisMax);

        if (secondLevelXAxisMin.equals("0") && secondLevelXAxisMax.equals("0")) {
            secondLevelWarningLayout.setVisibility(View.GONE);
            thirdLevelWarningLayout.setVisibility(View.GONE);
            return;
        }
        secondLevelWarningLayout.setVisibility(View.VISIBLE);
        mEtSecondLevelXAxisMin.setText(secondLevelXAxisMin);
        mEtSecondLevelXAxisMax.setText(secondLevelXAxisMax);
        mEtSecondLevelYAxisMin.setText(secondLevelYAxisMin);
        mEtSecondLevelYAxisMax.setText(secondLevelYAxisMax);

        if (thirdLevelXAxisMin.equals("0") && thirdLevelXAxisMax.equals("0")) {
            thirdLevelWarningLayout.setVisibility(View.GONE);
            return;
        }
        thirdLevelWarningLayout.setVisibility(View.VISIBLE);
        mEtThirdLevelXAxisMin.setText(thirdLevelXAxisMin);
        mEtThirdLevelXAxisMax.setText(thirdLevelXAxisMax);
        mEtThirdLevelYAxisMin.setText(thirdLevelYAxisMin);
        mEtThirdLevelYAxisMax.setText(thirdLevelYAxisMax);
    }

    public void onEditableChanged(boolean isEditable) {
        mEtDriveStandardVoltageThreshold.setEnabled(isEditable);
        mEtDriveLowVoltageThreshold.setEnabled(isEditable);
        mEtDriveUnderVoltageThreshold.setEnabled(isEditable);
        mEtInclinometerStandardVoltageThreshold.setEnabled(isEditable);
        mEtInclinometerLowVoltageThreshold.setEnabled(isEditable);
        mEtInclinometerUnderVoltageThreshold.setEnabled(isEditable);

        mEtFirstLevelXAxisMin.setEnabled(isEditable);
        mEtFirstLevelXAxisMax.setEnabled(isEditable);
        mEtFirstLevelYAxisMin.setEnabled(isEditable);
        mEtFirstLevelYAxisMax.setEnabled(isEditable);

        mEtSecondLevelXAxisMin.setEnabled(isEditable);
        mEtSecondLevelXAxisMax.setEnabled(isEditable);
        mEtSecondLevelYAxisMin.setEnabled(isEditable);
        mEtSecondLevelYAxisMax.setEnabled(isEditable);

        mEtThirdLevelXAxisMin.setEnabled(isEditable);
        mEtThirdLevelXAxisMax.setEnabled(isEditable);
        mEtThirdLevelYAxisMin.setEnabled(isEditable);
        mEtThirdLevelYAxisMax.setEnabled(isEditable);

        if (isEditable) {
            mEtDriveStandardVoltageThreshold.setHint("请输入");
            mEtDriveLowVoltageThreshold.setHint("请输入");
            mEtDriveUnderVoltageThreshold.setHint("请输入");
            mEtInclinometerStandardVoltageThreshold.setHint("请输入");
            mEtInclinometerLowVoltageThreshold.setHint("请输入");
            mEtInclinometerUnderVoltageThreshold.setHint("请输入");

            mEtFirstLevelXAxisMin.setHint("请输入");
            mEtFirstLevelXAxisMax.setHint("请输入");
            mEtFirstLevelYAxisMin.setHint("请输入");
            mEtFirstLevelYAxisMax.setHint("请输入");

            mEtSecondLevelXAxisMin.setHint("请输入");
            mEtSecondLevelXAxisMax.setHint("请输入");
            mEtSecondLevelYAxisMin.setHint("请输入");
            mEtSecondLevelYAxisMax.setHint("请输入");

            mEtThirdLevelXAxisMin.setHint("请输入");
            mEtThirdLevelXAxisMax.setHint("请输入");
            mEtThirdLevelYAxisMin.setHint("请输入");
            mEtThirdLevelYAxisMax.setHint("请输入");

        } else {
            mEtDriveStandardVoltageThreshold.setHint("");
            mEtDriveLowVoltageThreshold.setHint("");
            mEtDriveUnderVoltageThreshold.setHint("");
            mEtInclinometerStandardVoltageThreshold.setHint("");
            mEtInclinometerLowVoltageThreshold.setHint("");
            mEtInclinometerUnderVoltageThreshold.setHint("");

            mEtFirstLevelXAxisMin.setHint("");
            mEtFirstLevelXAxisMax.setHint("");
            mEtFirstLevelYAxisMin.setHint("");
            mEtFirstLevelYAxisMax.setHint("");

            mEtSecondLevelXAxisMin.setHint("");
            mEtSecondLevelXAxisMax.setHint("");
            mEtSecondLevelYAxisMin.setHint("");
            mEtSecondLevelYAxisMax.setHint("");

            mEtThirdLevelXAxisMin.setHint("");
            mEtThirdLevelXAxisMax.setHint("");
            mEtThirdLevelYAxisMin.setHint("");
            mEtThirdLevelYAxisMax.setHint("");
        }
        mBtnAddSecondLevel.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        mBtnAddThirdLevel.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
