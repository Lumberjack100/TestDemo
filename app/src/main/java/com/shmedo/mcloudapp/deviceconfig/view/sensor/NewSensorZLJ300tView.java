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
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.SensorJunXingZljInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/16 <br/>
 * 描述：     轴力计(ZLJ-300T)配置项视图
 */
public class NewSensorZLJ300tView extends FrameLayout {
    @BindView(R.id.et_trigger_threshold)
    EditText mEtTriggerThreshold;//触发阀值
    @BindView(R.id.polynomialRatioA)
    EditText mEtCoefficientA;//标定系数A
    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温修系数b
    @BindView(R.id.et_ReferenceValue)
    EditText mEtReferenceValue;//基准值F0
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//手动纠偏

    private String triggerThreshold, coefficientA, coefficientB, referenceValue, initialTemperature, correctValue;

    public NewSensorZLJ300tView(@NonNull Context context) {
        this(context, null);
    }

    public NewSensorZLJ300tView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewSensorZLJ300tView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.new_axialforcegauge_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCoefficientA.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtReferenceValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtTemperatureCoefficient.setText("0");
        mEtReferenceValue.setText("0");
        mEtInitialTemperature.setText("0");
        mEtCorrectValue.setText("0");
    }

    public void bindSensorData(SensorJunXingZljInfo sensorInfo) {
        if (sensorInfo != null) {
            mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%.0f", Double.parseDouble(sensorInfo.getTriggerThreshold())));
            mEtCoefficientA.setText(sensorInfo.getPolynomialRatioA());
            mEtTemperatureCoefficient.setText(sensorInfo.getTemperatureCoefficientB());
            mEtReferenceValue.setText(sensorInfo.getReferenceValue());
            mEtInitialTemperature.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getCreateTemperature()));
            mEtCorrectValue.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getManualCorrection()));
        }
    }

    /**
     * 通过扫描二维码填充多项式参数
     *
     * @param sensorInfo
     */
    public void initDataByScan(SensorJunXingZljInfo sensorInfo) {
        if (sensorInfo != null) {
            mEtCoefficientA.setText(sensorInfo.getPolynomialRatioA());
            mEtTemperatureCoefficient.setText(sensorInfo.getTemperatureCoefficientB());
            mEtReferenceValue.setText(TextUtils.isEmpty(sensorInfo.getReferenceValue()) ? "0" : sensorInfo.getReferenceValue());
        }
    }

    public boolean updateSensorData(SensorJunXingZljInfo sensorInfo) {
        if (!checkValue()) {
            return false;
        }

        sensorInfo.setTriggerThreshold(triggerThreshold);
        sensorInfo.setPolynomialRatioA(coefficientA);
        sensorInfo.setTemperatureCoefficientB(TextUtils.isEmpty(coefficientB) ? "0" : coefficientB);
        sensorInfo.setReferenceValue(TextUtils.isEmpty(referenceValue) ? "0" : referenceValue);
        sensorInfo.setCreateTemperature(TextUtils.isEmpty(initialTemperature) ? "0" : initialTemperature);
        sensorInfo.setManualCorrection(TextUtils.isEmpty(correctValue) ? "0" : correctValue);

        return true;
    }

    private boolean checkValue() {
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        coefficientA = mEtCoefficientA.getText().toString().trim();
        coefficientB = mEtTemperatureCoefficient.getText().toString().trim();
        referenceValue = mEtReferenceValue.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("触发阀值不能为空!");
            return false;
        }

        if (!ValidateUtil.isInteger(triggerThreshold)) {
            ToastUtils.show("请输入正确的触发阀值!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientA)) {
            ToastUtils.show("标定系数A不能为空!");
            return false;
        }

//        if (TextUtils.isEmpty(coefficientB)) {
//            ToastUtils.show("温修系数不能为空!");
//            return false;
//        }

//        if (TextUtils.isEmpty(referenceValue)) {
//            ToastUtils.show("基准值不能为空!");
//            return false;
//        }

        if (!TextUtils.isEmpty(initialTemperature) && !ValidateUtil.isDouble(initialTemperature)) {
            ToastUtils.show("请输入正确的初始温度!");
            return false;
        }

        if (!TextUtils.isEmpty(correctValue) && !ValidateUtil.isDouble(correctValue)) {
            ToastUtils.show("请输入正确的手动纠偏!");
            return false;
        }

        return true;
    }
}
