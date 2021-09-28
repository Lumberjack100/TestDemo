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
import com.shmedo.configlibrary.ble.model.SensorStressGaugeInfo;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.mcloudapp.R;

import java.text.DecimalFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/3 <br/>
 * 描述：     应力计配置项视图
 */
public class SensorYLJView extends FrameLayout {
    @BindView(R.id.et_trigger_threshold)
    EditText mEtTriggerThreshold;//报警值
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//修正值
    @BindView(R.id.sensitivityK)
    EditText mEtSensitivityK;//灵敏度K
    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温修系数b
    @BindView(R.id.et_ReferenceValue)
    EditText mEtReferenceValue;//基准值F0
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度
    @BindView(R.id.et_elastic_mod)
    EditText mEtElasticMod;//弹性模量

    private String triggerThreshold, correctValue, coefficientK, coefficientB, referenceValue, initialTemperature, elasticMod;

    private DecimalFormat decimalFormat = new DecimalFormat();


    public SensorYLJView(@NonNull Context context) {
        this(context, null);
    }

    public SensorYLJView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorYLJView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.stressgauge_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtSensitivityK.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtReferenceValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtElasticMod.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        mEtCorrectValue.setText("0");
        mEtTemperatureCoefficient.setText("0");
        mEtReferenceValue.setText("0");
        mEtInitialTemperature.setText("0");
    }

    /**
     * 通过扫描二维码填充多项式参数
     *
     * @param sensorInfo
     */
    public void initDataByScan(SensorStressGaugeInfo sensorInfo) {
        if (sensorInfo != null) {
            mEtSensitivityK.setText(sensorInfo.getSensitivityK());
            mEtTemperatureCoefficient.setText(sensorInfo.getTemperatureCoefficientB());
            mEtReferenceValue.setText(TextUtils.isEmpty(sensorInfo.getReferenceValue()) ? "0" : sensorInfo.getReferenceValue());
        }
    }

    public void initData(SensorStressGaugeInfo sensorInfo) {
        if (sensorInfo != null) {
            try {
                mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%.0f", Double.parseDouble(sensorInfo.getTriggerThreshold())));

                decimalFormat.applyPattern("#.###");
                mEtCorrectValue.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getManualCorrection())));

                mEtSensitivityK.setText(sensorInfo.getSensitivityK());
                mEtTemperatureCoefficient.setText(sensorInfo.getTemperatureCoefficientB());
                mEtReferenceValue.setText(sensorInfo.getReferenceValue());

                decimalFormat.applyPattern("#.##");
                mEtInitialTemperature.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getCreateTemperature())));

                mEtElasticMod.setText(sensorInfo.getElasticMode());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean updateSensorData(SensorStressGaugeInfo sensorInfo) {
        if (!checkValue()) {
            return false;
        }
        try {
            sensorInfo.setTriggerThreshold(triggerThreshold);

            decimalFormat.applyPattern("#.###");
            sensorInfo.setManualCorrection(TextUtils.isEmpty(correctValue) ? "0" : decimalFormat.format(Double.parseDouble(correctValue)));
            sensorInfo.setSensitivityK(coefficientK);
            sensorInfo.setTemperatureCoefficientB(TextUtils.isEmpty(coefficientB) ? "0" : coefficientB);
            sensorInfo.setReferenceValue(TextUtils.isEmpty(referenceValue) ? "0" : referenceValue);

            decimalFormat.applyPattern("#.##");
            sensorInfo.setCreateTemperature(TextUtils.isEmpty(initialTemperature) ? "0" : decimalFormat.format(Double.parseDouble(initialTemperature)));
            sensorInfo.setElasticMode(TextUtils.isEmpty(elasticMod) ? "0" : elasticMod);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /****  物联网指令 start ******/
    public void initData(DasExternalSensorInfo sensorInfo) {
        if (sensorInfo != null) {
            try {
                mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%d", (int) Double.parseDouble(sensorInfo.getThreshold())));

                decimalFormat.applyPattern("#.###");
                mEtCorrectValue.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getCorrval())));
                mEtSensitivityK.setText(sensorInfo.getSens_k());
                mEtTemperatureCoefficient.setText(sensorInfo.getTemp_b());
                mEtReferenceValue.setText(sensorInfo.getReferval_f());

                decimalFormat.applyPattern("#.##");
                mEtInitialTemperature.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getTemp_t0())));
                mEtElasticMod.setText(sensorInfo.getElastic_mod());
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
            sensorInfo.setElastic_mod(TextUtils.isEmpty(elasticMod) ? "0" : elasticMod);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }
    /****  物联网指令 end ******/

    private boolean checkValue() {
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        coefficientK = mEtSensitivityK.getText().toString().trim();
        coefficientB = mEtTemperatureCoefficient.getText().toString().trim();
        referenceValue = mEtReferenceValue.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();
        elasticMod = mEtElasticMod.getText().toString().trim();

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
            mEtSensitivityK.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(coefficientK);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的灵敏度!");
                mEtSensitivityK.requestFocus();
                return false;
            }
        }

        if (!TextUtils.isEmpty(coefficientB)) {
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

        if (!TextUtils.isEmpty(elasticMod)) {
            try {
                double value = Double.parseDouble(elasticMod);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的弹性模量!");
                mEtElasticMod.requestFocus();
                return false;
            }
        }
        return true;
    }
}
