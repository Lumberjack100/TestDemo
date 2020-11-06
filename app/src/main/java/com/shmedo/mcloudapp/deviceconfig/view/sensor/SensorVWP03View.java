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
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;

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
    @BindView(R.id.sensitivityCoefficient)
    EditText mEtSensitivityCoefficient;//灵敏度k
    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温修系数b
    @BindView(R.id.et_ReferenceValue)
    EditText mEtReferenceValue;//基准值F0
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//修正值
    @BindView(R.id.et_cord_length)
    EditText mEtCordLength;//绳长
    @BindView(R.id.et_install_elevation)
    EditText mEtInstallElevation;//安装高程

    private String triggerThreshold, coefficientK, coefficientB, referenceValue, initialTemperature, correctValue, cordLength, installElevation;

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
        mEtSensitivityCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtReferenceValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCordLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInstallElevation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtReferenceValue.setText("0");
        mEtInitialTemperature.setText("0");
        mEtCorrectValue.setText("0");
    }

    public void bindSensorData(SensorGudanPercolateInfo sensorInfo) {
        if (sensorInfo != null) {
            mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%d", (int) Double.parseDouble(sensorInfo.getTriggerThreshold())));
            mEtSensitivityCoefficient.setText(sensorInfo.getSensitivityK());
            mEtTemperatureCoefficient.setText(sensorInfo.getTemperatureCoefficientB());
            mEtReferenceValue.setText(sensorInfo.getReferenceValue());
            mEtInitialTemperature.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getCreateTemperature()));
            mEtCorrectValue.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getManualCorrection()));
            mEtCordLength.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getCordLenght()));
            mEtInstallElevation.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getInstallElevation()));
        }
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

    public boolean updateSensorData(SensorGudanPercolateInfo sensorInfo) {
        if (!checkValue()) {
            return false;
        }

        sensorInfo.setTriggerThreshold(triggerThreshold);
        sensorInfo.setSensitivityK(coefficientK);
        sensorInfo.setTemperatureCoefficientB(coefficientB);
        sensorInfo.setReferenceValue(TextUtils.isEmpty(referenceValue) ? "0" : referenceValue);
        sensorInfo.setCreateTemperature(TextUtils.isEmpty(initialTemperature) ? "0" : initialTemperature);
        sensorInfo.setManualCorrection(TextUtils.isEmpty(correctValue) ? "0" : correctValue);
        sensorInfo.setCordLenght(cordLength);
        sensorInfo.setInstallElevation(installElevation);

        return true;
    }

    private boolean checkValue() {
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        coefficientK = mEtSensitivityCoefficient.getText().toString().trim();
        coefficientB = mEtTemperatureCoefficient.getText().toString().trim();
        referenceValue = mEtReferenceValue.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        cordLength = mEtCordLength.getText().toString().trim();
        installElevation = mEtInstallElevation.getText().toString().trim();

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("报警值不能为空!");
            return false;
        }

        if (!ValidateUtil.isInteger(triggerThreshold)) {
            ToastUtils.show("请输入正确的报警值!");
            return false;
        }

        if (!TextUtils.isEmpty(correctValue) && !ValidateUtil.isDouble(correctValue)) {
            ToastUtils.show("请输入正确的修正值!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientK)) {
            ToastUtils.show("灵敏度不能为空!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientB)) {
            ToastUtils.show("温修系数不能为空!");
            return false;
        }

        if (!TextUtils.isEmpty(initialTemperature) && !ValidateUtil.isDouble(initialTemperature)) {
            ToastUtils.show("请输入正确的初始温度!");
            return false;
        }

        if (TextUtils.isEmpty(cordLength)) {
            ToastUtils.show("绳长不能为空!");
            return false;
        }

        if (!ValidateUtil.isDouble(cordLength)) {
            ToastUtils.show("请输入正确的绳长!");
            return false;
        }

        if (TextUtils.isEmpty(installElevation)) {
            ToastUtils.show("安装高程不能为空!");
            return false;
        }

        if (!ValidateUtil.isDouble(installElevation)) {
            ToastUtils.show("请输入正确的安装高程!");
            return false;
        }
        return true;
    }
}
