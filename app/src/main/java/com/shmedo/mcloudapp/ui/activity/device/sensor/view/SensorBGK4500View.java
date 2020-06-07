package com.shmedo.mcloudapp.ui.activity.device.sensor.view;

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
import com.shmedo.core.model.SensorKangPercolateInfo;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/6 <br/>
 * 描述：   基康渗压计(BGK-4500)配置项视图
 */
public class SensorBGK4500View extends FrameLayout {
    @BindView(R.id.et_modbus_address)
    EditText mEtModbusAddress;//通道号
    @BindView(R.id.polynomialRatioA)
    EditText mEtCoefficientA;//多项式系数A
    @BindView(R.id.polynomialRatioB)
    EditText mEtCoefficientB;//多项式系数B
    @BindView(R.id.polynomialRatioC)
    EditText mEtCoefficientC;//多项式系数C
    @BindView(R.id.temperatureCoefficientK)
    EditText mEtCoefficientK;//温度系数K
    @BindView(R.id.et_install_elevation)
    EditText mEtInstallElevation;//安装高程
    @BindView(R.id.et_osmometer_cord)
    EditText mEtCordLenght;//绳长
    @BindView(R.id.et_trigger_threshold)
    EditText mEtTriggerThreshold;//触发阀值
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//修正值
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private SensorKangPercolateInfo sensorKangPercolateInfo;

    private String coefficientA, coefficientB, coefficientC, coefficientK, address, installElevation, cordLenght, triggerThreshold, correctValue, initialTemperature;


    public SensorBGK4500View(@NonNull Context context) {
        this(context, null);
    }

    public SensorBGK4500View(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorBGK4500View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.bgk_osmometer_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtInstallElevation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCordLenght.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

    }

    public void bindData(CollectorSensorParamsInfoSub infoSub) {
        SensorKangPercolateInfo sensorKangPercolateInfo = (SensorKangPercolateInfo) infoSub.getSensorData();
        mEtCoefficientA.setText(sensorKangPercolateInfo.getPolynomialRatioA());
        mEtCoefficientB.setText(sensorKangPercolateInfo.getPolynomialRatioB());
        mEtCoefficientC.setText(sensorKangPercolateInfo.getPolynomialRatioC());
        mEtCoefficientK.setText(sensorKangPercolateInfo.getTemperatureCoefficientK() + "");
        mEtModbusAddress.setText(infoSub.getSensorAddress());
        mEtInstallElevation.setText(sensorKangPercolateInfo.getInstallElevation() + "");
        mEtCordLenght.setText(sensorKangPercolateInfo.getCordLenght() + "");
        mEtTriggerThreshold.setText(sensorKangPercolateInfo.getTriggerThreshold());
        mEtCorrectValue.setText(sensorKangPercolateInfo.getManualCorrection());
        mEtInitialTemperature.setText(sensorKangPercolateInfo.getCreateTemperature() + "");

    }

    public void updateData(CollectorSensorParamsInfoSub infoSub) {
        SensorKangPercolateInfo sensorKangPercolateInfo = (SensorKangPercolateInfo) infoSub.getSensorData();
        if (!checkValue()) {
            return;
        }

        infoSub.setSensorAddress(address);
        sensorKangPercolateInfo.setPolynomialRatioA(coefficientA);
        sensorKangPercolateInfo.setPolynomialRatioB(coefficientB);
        sensorKangPercolateInfo.setPolynomialRatioC(coefficientC);
        sensorKangPercolateInfo.setTemperatureCoefficientK(Double.parseDouble(coefficientK));
        sensorKangPercolateInfo.setInstallElevation(Double.parseDouble(coefficientK));
        sensorKangPercolateInfo.setCordLenght(Integer.parseInt(cordLenght));
        sensorKangPercolateInfo.setTriggerThreshold(Integer.parseInt(triggerThreshold));
        sensorKangPercolateInfo.setManualCorrection(correctValue);
        sensorKangPercolateInfo.setCreateTemperature(Double.parseDouble(initialTemperature));
    }

    private boolean checkValue() {
        address = mEtModbusAddress.getText().toString().trim();
        coefficientA = mEtCoefficientA.getText().toString().trim();
        coefficientB = mEtCoefficientB.getText().toString().trim();
        coefficientC = mEtCoefficientC.getText().toString().trim();
        coefficientK = mEtCoefficientK.getText().toString().trim();
        installElevation = mEtInstallElevation.getText().toString().trim();
        cordLenght = mEtCordLenght.getText().toString().trim();
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();

        if (TextUtils.isEmpty(address) || !ValidateUtil.isInteger(address) || Integer.parseInt(address) < 0 || Integer.parseInt(address) > 99) {
            ToastUtils.show("请输入正确的通道号");
            return false;
        }

        if (TextUtils.isEmpty(coefficientA)) {
            ToastUtils.show("请输入正确的多项式系数A");
            return false;
        }

        if (TextUtils.isEmpty(coefficientB)) {
            ToastUtils.show("请输入正确的多项式系数B");
            return false;
        }

        if (TextUtils.isEmpty(coefficientC)) {
            ToastUtils.show("请输入正确的多项式系数C");
            return false;
        }

        if (TextUtils.isEmpty(coefficientK)) {
            ToastUtils.show("请输入正确的温度系数K");
            return false;
        }

        if (TextUtils.isEmpty(installElevation)) {
            ToastUtils.show("请输入安装高程");
            return false;
        }

        if (TextUtils.isEmpty(cordLenght)) {
            ToastUtils.show("请输入绳长");
            return false;
        }

        if (TextUtils.isEmpty(triggerThreshold) || !ValidateUtil.isInteger(triggerThreshold)) {
            ToastUtils.show("请输入正确的触发值");
            return false;
        }

        if (TextUtils.isEmpty(correctValue) || (!ValidateUtil.isInteger(correctValue) && !ValidateUtil.isDouble(correctValue))) {
            ToastUtils.show("请输入正确的修正值");
            return false;
        }

        if (TextUtils.isEmpty(cordLenght)) {
            ToastUtils.show("请输入初始温度");
            return false;
        }


        return true;
    }
}
