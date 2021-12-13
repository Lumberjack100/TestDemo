package com.shmedo.mcloudapp.deviceconfig.view.adme;

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
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeVoltageConfigInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：     ADME电压配置页面
 */
public class AdmeVoltageConfigView extends LinearLayout {
    @BindView(R.id.et_drive_overvoltage_threshold)
    ClearEditText mEtDriveOverVoltageThreshold;//驱动器过压阈值

    @BindView(R.id.et_drive_lowvoltage_threshold)
    ClearEditText mEtDriveLowVoltageThreshold;//驱动器低压阈值

    @BindView(R.id.et_drive_undervoltage_threshold)
    ClearEditText mEtDriveUnderVoltageThreshold;//驱动器欠压阈值

    @BindView(R.id.et_inclinometer_lowvoltage_threshold)
    ClearEditText mEtInclinometerLowVoltageThreshold;//测斜仪低压阈值

    @BindView(R.id.et_inclinometer_undervoltage_threshold)
    ClearEditText mEtInclinometerUnderVoltageThreshold;//测斜仪欠压阈值

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_drive_overvoltage_threshold)
    ViewGroup driveOverVoltageThresholdLayout;

    @BindView(R.id.ll_drive_lowvoltage_threshold)
    ViewGroup driveLowVoltageThresholdLayout;

    @BindView(R.id.ll_drive_undervoltage_threshold)
    ViewGroup driveUnderVoltageThresholdLayout;

    @BindView(R.id.ll_inclinometer_lowvoltage_threshold)
    ViewGroup inclinometerLowVoltageThresholdLayout;

    @BindView(R.id.ll_inclinometer_undervoltage_threshold)
    ViewGroup inclinometerUnderVoltageThresholdLayout;

    private String driveOverVoltageThreshold;
    private String driveLowVoltageThreshold;
    private String driveUnderVoltageThreshold;
    private String inclinometerLowVoltageThreshold;
    private String inclinometerUnderVoltageThreshold;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public AdmeVoltageConfigInfo voltageConfigInfo;


    public AdmeVoltageConfigView(Context context) {
        this(context, null);
    }

    public AdmeVoltageConfigView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeVoltageConfigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_voltage_config_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtDriveOverVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtDriveLowVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtDriveUnderVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInclinometerLowVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInclinometerUnderVoltageThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    public boolean checkValueIsValid() {
        if (!driveOverVoltageThreshold.equals("NullKey")) {
            driveOverVoltageThreshold = mEtDriveOverVoltageThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(driveOverVoltageThreshold)) {
                ToastUtils.show("请输入驱动器过压阈值!");
                mEtDriveOverVoltageThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(driveOverVoltageThreshold);
                if (value < 1) {
                    ToastUtils.show("请输入正确的驱动器过压阈值!");
                    mEtDriveOverVoltageThreshold.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的驱动器过压阈值!");
                mEtDriveOverVoltageThreshold.requestFocus();
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
        return true;
    }

    /**
     * 获取配置指令
     */
    public String getConfigCommand() {
        String command = "";
        try {
            AdmeVoltageConfigEntity entity = new AdmeVoltageConfigEntity();
            entity.setDriveovervolt(voltageConfigInfo.getDriveovervolt().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(driveOverVoltageThreshold)));
            entity.setDrivelowvolt(voltageConfigInfo.getDrivelowvolt().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(driveLowVoltageThreshold)));
            entity.setDriveundervolt(voltageConfigInfo.getDriveundervolt().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(driveUnderVoltageThreshold)));
            entity.setInclowvolt(voltageConfigInfo.getInclowvolt().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerLowVoltageThreshold)));
            entity.setIncundervolt(voltageConfigInfo.getIncundervolt().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerUnderVoltageThreshold)));

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_VOLTAGE, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    public void initParamConfigInfo() {
        if (voltageConfigInfo == null) {
            Timber.e("AdmeVoltageConfigInfo is Null!");
            voltageConfigInfo = new AdmeVoltageConfigInfo();
            return;
        }
        driveOverVoltageThreshold = voltageConfigInfo.getDriveovervolt().trim();
        driveLowVoltageThreshold = voltageConfigInfo.getDrivelowvolt().trim();
        driveUnderVoltageThreshold = voltageConfigInfo.getDriveundervolt().trim();
        inclinometerLowVoltageThreshold = voltageConfigInfo.getInclowvolt().trim();
        inclinometerUnderVoltageThreshold = voltageConfigInfo.getIncundervolt().trim();
        try {
            if (driveOverVoltageThreshold.equals("NullKey")) {
                driveOverVoltageThresholdLayout.setVisibility(View.GONE);
            } else {
                driveOverVoltageThreshold = decimalFormat.format(Double.parseDouble(driveOverVoltageThreshold));
                mEtDriveOverVoltageThreshold.setText(driveOverVoltageThreshold);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean checkValueIsChange(boolean configPageEditableChanged) {
        if (!configPageEditableChanged)
            return false;

        if (driveOverVoltageThreshold != null && !driveOverVoltageThreshold.equals("NullKey") && !driveOverVoltageThreshold.equals(mEtDriveOverVoltageThreshold.getText().toString().trim())) {
            return true;
        }
        if (driveLowVoltageThreshold != null && !driveLowVoltageThreshold.equals("NullKey") && !driveLowVoltageThreshold.equals(mEtDriveLowVoltageThreshold.getText().toString().trim())) {
            return true;
        }
        if (driveUnderVoltageThreshold != null && !driveUnderVoltageThreshold.equals("NullKey") && !driveUnderVoltageThreshold.equals(mEtDriveUnderVoltageThreshold.getText().toString().trim())) {
            return true;
        }
        if (inclinometerLowVoltageThreshold != null && !inclinometerLowVoltageThreshold.equals("NullKey") && !inclinometerLowVoltageThreshold.equals(mEtInclinometerLowVoltageThreshold.getText().toString().trim())) {
            return true;
        }
        if (inclinometerUnderVoltageThreshold != null && !inclinometerUnderVoltageThreshold.equals("NullKey") && !inclinometerUnderVoltageThreshold.equals(mEtInclinometerUnderVoltageThreshold.getText().toString().trim())) {
            return true;
        }
        return false;
    }

    public void onEditableChanged(boolean isEditable) {
        mEtDriveOverVoltageThreshold.setEnabled(isEditable);
        mEtDriveLowVoltageThreshold.setEnabled(isEditable);
        mEtDriveUnderVoltageThreshold.setEnabled(isEditable);
        mEtInclinometerLowVoltageThreshold.setEnabled(isEditable);
        mEtInclinometerUnderVoltageThreshold.setEnabled(isEditable);
        if (isEditable) {
            mEtDriveOverVoltageThreshold.setHint("请输入");
            mEtDriveLowVoltageThreshold.setHint("请输入");
            mEtDriveUnderVoltageThreshold.setHint("请输入");
            mEtInclinometerLowVoltageThreshold.setHint("请输入");
            mEtInclinometerUnderVoltageThreshold.setHint("请输入");

        }else {
            mEtDriveOverVoltageThreshold.setHint("");
            mEtDriveLowVoltageThreshold.setHint("");
            mEtDriveUnderVoltageThreshold.setHint("");
            mEtInclinometerLowVoltageThreshold.setHint("");
            mEtInclinometerUnderVoltageThreshold.setHint("");
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
