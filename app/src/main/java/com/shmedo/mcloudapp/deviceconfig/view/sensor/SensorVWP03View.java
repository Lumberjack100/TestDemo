package com.shmedo.mcloudapp.deviceconfig.view.sensor;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.model.SensorGudanPercolateInfo;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.mcloudapp.R;

import java.text.DecimalFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/16 <br/>
 * 描述：    葛南渗压计(VWP-03)配置项视图
 */
public class SensorVWP03View extends FrameLayout {
    @BindView(R.id.et_trigger_threshold)
    EditText mEtTriggerThreshold;//报警值
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//修正值
    @BindView(R.id.sensitivityCoefficient)
    EditText mEtSensitivityCoefficient;//灵敏度k
    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温修系数b
    @BindView(R.id.et_ReferenceValue)
    EditText mEtReferenceValue;//基准值F0
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度
    @BindView(R.id.et_cord_length)
    EditText mEtCordLength;//绳长
    @BindView(R.id.et_install_elevation)
    EditText mEtInstallElevation;//安装高程

    private String triggerThreshold, correctValue, coefficientK, coefficientB, referenceValue, initialTemperature, cordLength, installElevation;

    private DecimalFormat decimalFormat = new DecimalFormat();


    public SensorVWP03View(@NonNull Context context) {
        this(context, null);
    }

    public SensorVWP03View(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorVWP03View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vwp_osmometer_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtSensitivityCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtReferenceValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCordLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInstallElevation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtCorrectValue.setText("0");
        mEtReferenceValue.setText("0");
        mEtInitialTemperature.setText("0");
    }

    /**
     * 通过扫描二维码填充多项式参数
     */
    public void initDataByScan(SensorGudanPercolateInfo sensorInfo) {
        if (sensorInfo != null) {
            mEtSensitivityCoefficient.setText(sensorInfo.getSensitivityK());
            mEtTemperatureCoefficient.setText(sensorInfo.getTemperatureCoefficientB());
            mEtReferenceValue.setText(TextUtils.isEmpty(sensorInfo.getReferenceValue()) ? "0" : sensorInfo.getReferenceValue());
        }
    }

    public void initData(SensorGudanPercolateInfo sensorInfo) {
        if (sensorInfo != null) {
            try {
                mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%d", (int) Double.parseDouble(sensorInfo.getTriggerThreshold())));

                decimalFormat.applyPattern("#.###");
                mEtCorrectValue.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getManualCorrection())));
                mEtSensitivityCoefficient.setText(sensorInfo.getSensitivityK());
                mEtTemperatureCoefficient.setText(sensorInfo.getTemperatureCoefficientB());
                mEtReferenceValue.setText(sensorInfo.getReferenceValue());

                decimalFormat.applyPattern("#.##");
                mEtInitialTemperature.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getCreateTemperature())));

                decimalFormat.applyPattern("#.###");
                mEtCordLength.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getCordLenght())));
                mEtInstallElevation.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getInstallElevation())));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean updateSensorData(SensorGudanPercolateInfo sensorInfo) {
        if (!checkValue()) {
            return false;
        }
        try {
            sensorInfo.setTriggerThreshold(triggerThreshold);

            decimalFormat.applyPattern("#.###");
            sensorInfo.setManualCorrection(TextUtils.isEmpty(correctValue) ? "0" : decimalFormat.format(Double.parseDouble(correctValue)));
            sensorInfo.setSensitivityK(coefficientK);
            sensorInfo.setTemperatureCoefficientB(coefficientB);
            sensorInfo.setReferenceValue(TextUtils.isEmpty(referenceValue) ? "0" : referenceValue);

            decimalFormat.applyPattern("#.##");
            sensorInfo.setCreateTemperature(TextUtils.isEmpty(initialTemperature) ? "0" : decimalFormat.format(Double.parseDouble(initialTemperature)));

            decimalFormat.applyPattern("#.###");
            sensorInfo.setCordLenght(decimalFormat.format(Double.parseDouble(cordLength)));
            sensorInfo.setInstallElevation(decimalFormat.format(Double.parseDouble(installElevation)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /**** 物联网指令******/
    public void initData(DasExternalSensorInfo sensorInfo) {
        if (sensorInfo != null) {
            try {
                mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%d", (int) Double.parseDouble(sensorInfo.getThreshold())));

                decimalFormat.applyPattern("#.###");
                mEtCorrectValue.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getCorrval())));
                mEtSensitivityCoefficient.setText(sensorInfo.getSens_k());
                mEtTemperatureCoefficient.setText(sensorInfo.getTemp_b());
                mEtReferenceValue.setText(sensorInfo.getReferval_f());

                decimalFormat.applyPattern("#.##");
                mEtInitialTemperature.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getTemp_t0())));

                decimalFormat.applyPattern("#.###");
                mEtCordLength.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getRopelen())));
                mEtInstallElevation.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getTubealti())));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean updateSensorData(DasExternalSensorInfo sensorInfo) {
        if (!checkValue()) {
            return false;
        }
        try {
            sensorInfo.setThreshold(triggerThreshold);

            decimalFormat.applyPattern("#.###");
            sensorInfo.setCorrval(TextUtils.isEmpty(correctValue) ? "0" : decimalFormat.format(Double.parseDouble(correctValue)));
            sensorInfo.setSens_k(coefficientK);
            sensorInfo.setTemp_b(coefficientB);
            sensorInfo.setReferval_f(TextUtils.isEmpty(referenceValue) ? "0" : referenceValue);

            decimalFormat.applyPattern("#.##");
            sensorInfo.setTemp_t0(TextUtils.isEmpty(initialTemperature) ? "0" : decimalFormat.format(Double.parseDouble(initialTemperature)));

            decimalFormat.applyPattern("#.###");
            sensorInfo.setRopelen(decimalFormat.format(Double.parseDouble(cordLength)));
            sensorInfo.setTubealti(decimalFormat.format(Double.parseDouble(installElevation)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /**** 物联网指令******/

    private boolean checkValue() {
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        coefficientK = mEtSensitivityCoefficient.getText().toString().trim();
        coefficientB = mEtTemperatureCoefficient.getText().toString().trim();
        referenceValue = mEtReferenceValue.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();
        cordLength = mEtCordLength.getText().toString().trim();
        installElevation = mEtInstallElevation.getText().toString().trim();

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("报警值不能为空!");
            mEtTriggerThreshold.requestFocus();
            return false;
        }
        if (!ValidateUtil.isInteger(triggerThreshold)) {
            ToastUtils.show("请输入正确的报警值!");
            mEtTriggerThreshold.requestFocus();
            return false;
        }

        if (!TextUtils.isEmpty(correctValue)) {
            try {
                double value = Double.parseDouble(correctValue);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的修正值!");
                mEtCorrectValue.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(coefficientK)) {
            ToastUtils.show("灵敏度不能为空!");
            mEtSensitivityCoefficient.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(coefficientK);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的灵敏度!");
                mEtSensitivityCoefficient.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(coefficientB)) {
            ToastUtils.show("温修系数不能为空!");
            mEtTemperatureCoefficient.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(coefficientB);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的温修系数!");
                mEtTemperatureCoefficient.requestFocus();
                return false;
            }
        }

        if (!TextUtils.isEmpty(referenceValue)) {
            try {
                double value = Double.parseDouble(referenceValue);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的基准值!");
                mEtReferenceValue.requestFocus();
                return false;
            }
        }

        if (!TextUtils.isEmpty(initialTemperature)) {
            try {
                double value = Double.parseDouble(initialTemperature);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的初始温度!");
                mEtInitialTemperature.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(cordLength)) {
            ToastUtils.show("绳长不能为空!");
            mEtCordLength.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(cordLength);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的绳长!");
                mEtCordLength.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(installElevation)) {
            ToastUtils.show("安装高程不能为空!");
            mEtInstallElevation.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(installElevation);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的安装高程!");
                mEtInstallElevation.requestFocus();
                return false;
            }
        }

        return true;
    }
}
