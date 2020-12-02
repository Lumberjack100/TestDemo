package com.shmedo.mcloudapp.deviceconfig.view.sensor.vms;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.cmd.entity.SetVmsTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.model.VmsTerminalSensorInfo;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/27/20 <br/>
 * 描述：     Vms 终端接入的多项式传感器参数
 */
public class PolynomialParamView extends FrameLayout {
    @BindView(R.id.polynomialRatioA)
    EditText mEtPolynomialRatioA;//多项式系数A

    @BindView(R.id.polynomialRatioB)
    EditText mEtPolynomialRatioB;//多项式系数B

    @BindView(R.id.polynomialRatioC)
    EditText mEtPolynomialRatioC;//多项式系数C

    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温度修正系数K

    @BindView(R.id.initialTemperature)
    EditText mEtInitialTemperature;//初始温度T

    @BindView(R.id.correctValue)
    EditText mEtCorrectValue;//修正值M

    private String polynomialRatioA;
    private String polynomialRatioB;
    private String polynomialRatioC;
    private String temperatureCoefficient;
    private String initialTemperature;
    private String correctValue;

    public PolynomialParamView(@NonNull Context context) {
        this(context, null);
    }

    public PolynomialParamView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolynomialParamView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vms_polynomia_sensor_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtPolynomialRatioA.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtPolynomialRatioB.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtPolynomialRatioC.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
    }

    public void initData(VmsTerminalSensorInfo sensorInfo) {
        if (sensorInfo == null) {
            return;
        }

        polynomialRatioA = sensorInfo.getParama();
        polynomialRatioB = sensorInfo.getParamb();
        polynomialRatioC = sensorInfo.getParamc();
        temperatureCoefficient = sensorInfo.getParamk();
        initialTemperature = sensorInfo.getParamt();
        correctValue = sensorInfo.getParamm();

        mEtPolynomialRatioA.setText(polynomialRatioA);
        mEtPolynomialRatioB.setText(polynomialRatioB);
        mEtPolynomialRatioC.setText(polynomialRatioC);
        mEtTemperatureCoefficient.setText(temperatureCoefficient);
        mEtInitialTemperature.setText(initialTemperature);
        mEtCorrectValue.setText(correctValue);
    }

    public boolean updateSensorData(SetVmsTerminalSensorParamsEntity sensorParamsEntity) {
        if (!checkValueValid()) {
            return false;
        }

        sensorParamsEntity.setParama(polynomialRatioA);
        sensorParamsEntity.setParamb(polynomialRatioB);
        sensorParamsEntity.setParamc(polynomialRatioC);
        sensorParamsEntity.setParamk(temperatureCoefficient);
        sensorParamsEntity.setParamt(initialTemperature);
        sensorParamsEntity.setParamm(correctValue);

        return true;
    }

    private boolean checkValueValid() {
        polynomialRatioA = mEtPolynomialRatioA.getText().toString().trim();
        polynomialRatioB = mEtPolynomialRatioB.getText().toString().trim();
        polynomialRatioC = mEtPolynomialRatioC.getText().toString().trim();
        temperatureCoefficient = mEtTemperatureCoefficient.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();

        return true;
    }

    public boolean checkValueIsChange() {
        if (polynomialRatioA != null && !polynomialRatioA.equals(mEtPolynomialRatioA.getText().toString().trim())) {
            return true;
        }

        if (polynomialRatioB != null && !polynomialRatioB.equals(mEtPolynomialRatioB.getText().toString().trim())) {
            return true;
        }

        if (polynomialRatioC != null && !polynomialRatioC.equals(mEtPolynomialRatioC.getText().toString().trim())) {
            return true;
        }

        if (temperatureCoefficient != null && !temperatureCoefficient.equals(mEtTemperatureCoefficient.getText().toString().trim())) {
            return true;
        }

        if (initialTemperature != null && !initialTemperature.equals(mEtInitialTemperature.getText().toString().trim())) {
            return true;
        }

        if (correctValue != null && !correctValue.equals(mEtCorrectValue.getText().toString().trim())) {
            return true;
        }
        return false;
    }

}
