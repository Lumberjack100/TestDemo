package com.shmedo.mcloudapp.deviceconfig.view.sensor.vms;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.cmd.entity.SetTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.model.TerminalSensorInfo;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/27/20 <br/>
 * 描述：    Vms 终端接入的直线式传感器参数
 */
public class LinearParamView extends FrameLayout {
    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温度修正系数B

    @BindView(R.id.sensitivityCoefficient)
    EditText mEtSensitivityCoefficient;//灵敏度K

    @BindView(R.id.initialTemperature)
    EditText mEtInitialTemperature;//初始温度T

    @BindView(R.id.initialModulus)
    EditText mEtInitModulus;//初始模数F

    @BindView(R.id.correctValue)
    EditText mEtCorrectValue;//修正值

    private String temperatureCoefficient;
    private String sensitivityCoefficient;
    private String initialTemperature;
    private String initialModulus;
    private String correctValue;

    public LinearParamView(@NonNull Context context) {
        this(context, null);
    }

    public LinearParamView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearParamView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vms_linear_sensor_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtSensitivityCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtInitModulus.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
    }

    public void initData(TerminalSensorInfo sensorInfo) {
        if (sensorInfo == null) {
            return;
        }
        temperatureCoefficient = sensorInfo.getParamb();
        sensitivityCoefficient = sensorInfo.getParamk();
        initialTemperature = sensorInfo.getParamt();
        initialModulus = sensorInfo.getParamf();
        correctValue = sensorInfo.getParamm();

        mEtTemperatureCoefficient.setText(temperatureCoefficient);
        mEtSensitivityCoefficient.setText(sensitivityCoefficient);
        mEtInitialTemperature.setText(initialTemperature);
        mEtInitModulus.setText(initialModulus);
        mEtCorrectValue.setText(correctValue);
    }

    public boolean updateSensorData(SetTerminalSensorParamsEntity sensorParamsEntity) {
        if (!checkValueValid()) {
            return false;
        }

        sensorParamsEntity.setParamb(temperatureCoefficient);
        sensorParamsEntity.setParamk(sensitivityCoefficient);
        sensorParamsEntity.setParamt(initialTemperature);
        sensorParamsEntity.setParamf(initialModulus);
        sensorParamsEntity.setParamm(correctValue);
        return true;
    }

    private boolean checkValueValid() {
        temperatureCoefficient = mEtTemperatureCoefficient.getText().toString().trim();
        sensitivityCoefficient = mEtSensitivityCoefficient.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();
        initialModulus = mEtInitModulus.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();

        return true;
    }

    public boolean checkValueIsChange() {
        if (temperatureCoefficient != null && !temperatureCoefficient.equals(mEtTemperatureCoefficient.getText().toString().trim())) {
            return true;
        }

        if (sensitivityCoefficient != null && !sensitivityCoefficient.equals(mEtSensitivityCoefficient.getText().toString().trim())) {
            return true;
        }

        if (initialTemperature != null && !initialTemperature.equals(mEtInitialTemperature.getText().toString().trim())) {
            return true;
        }

        if (initialModulus != null && !initialModulus.equals(mEtInitModulus.getText().toString().trim())) {
            return true;
        }

        if (correctValue != null && !correctValue.equals(mEtCorrectValue.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
