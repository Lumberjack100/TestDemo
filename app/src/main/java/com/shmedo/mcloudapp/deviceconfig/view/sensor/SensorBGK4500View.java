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
import com.shmedo.configlibrary.ble.model.SensorKangPercolateInfo;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.mcloudapp.R;

import java.text.DecimalFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/15 <br/>
 * 描述：      基康渗压计(BGK-4500)配置项视图
 */
public class SensorBGK4500View extends FrameLayout {
    @BindView(R.id.et_trigger_threshold)
    EditText mEtTriggerThreshold;//触发值
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//修正值
    @BindView(R.id.polynomialRatioA)
    EditText mEtCoefficientA;//多项式系数A
    @BindView(R.id.polynomialRatioB)
    EditText mEtCoefficientB;//多项式系数B
    @BindView(R.id.polynomialRatioC)
    EditText mEtCoefficientC;//多项式系数C
    @BindView(R.id.temperatureCoefficientK)
    EditText mEtCoefficientK;//温度系数K
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度
    @BindView(R.id.et_cord_length)
    EditText mEtCordLength;//绳长
    @BindView(R.id.et_install_elevation)
    EditText mEtInstallElevation;//安装高程

    private String triggerThreshold, correctValue, coefficientA, coefficientB, coefficientC, coefficientK, initialTemperature, cordLength, installElevation;

    private DecimalFormat decimalFormat = new DecimalFormat();


    public SensorBGK4500View(@NonNull Context context) {
        this(context, null);
    }

    public SensorBGK4500View(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorBGK4500View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bgk_osmometer_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCoefficientA.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCoefficientB.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCoefficientC.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCoefficientK.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCordLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInstallElevation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtCorrectValue.setText("0");
        mEtInitialTemperature.setText("0");
    }


    /**
     * 通过扫描二维码填充多项式参数
     */
    public void initDataByScan(SensorKangPercolateInfo sensorInfo) {
        if (sensorInfo != null) {
            mEtCoefficientA.setText(sensorInfo.getPolynomialRatioA());
            mEtCoefficientB.setText(sensorInfo.getPolynomialRatioB());
            mEtCoefficientC.setText(sensorInfo.getPolynomialRatioC());
            mEtCoefficientK.setText(sensorInfo.getTemperatureCoefficientK());
        }
    }

    public void initData(SensorKangPercolateInfo sensorInfo) {
        if (sensorInfo != null) {
            try {
                mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%d", (int) Double.parseDouble(sensorInfo.getTriggerThreshold())));

                decimalFormat.applyPattern("#.###");
                mEtCorrectValue.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getManualCorrection())));
                mEtCoefficientA.setText(sensorInfo.getPolynomialRatioA());
                mEtCoefficientB.setText(sensorInfo.getPolynomialRatioB());
                mEtCoefficientC.setText(sensorInfo.getPolynomialRatioC());
                mEtCoefficientK.setText(sensorInfo.getTemperatureCoefficientK());

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

    public boolean updateSensorData(SensorKangPercolateInfo sensorInfo) {
        if (!checkValue()) {
            return false;
        }
        try {
            sensorInfo.setTriggerThreshold(triggerThreshold);

            decimalFormat.applyPattern("#.###");
            sensorInfo.setManualCorrection(TextUtils.isEmpty(correctValue) ? "0" : decimalFormat.format(Double.parseDouble(correctValue)));
            sensorInfo.setPolynomialRatioA(coefficientA);
            sensorInfo.setPolynomialRatioB(coefficientB);
            sensorInfo.setPolynomialRatioC(coefficientC);
            sensorInfo.setTemperatureCoefficientK(coefficientK);

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

    /****  物联网指令 start ******/
    public void initData(DasExternalSensorInfo sensorInfo) {
        if (sensorInfo != null) {
            try {
                mEtTriggerThreshold.setText(String.format(Locale.getDefault(), "%d", (int) Double.parseDouble(sensorInfo.getThreshold())));

                decimalFormat.applyPattern("#.###");
                mEtCorrectValue.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getCorrval())));

                mEtCoefficientA.setText(sensorInfo.getPoly_a());
                mEtCoefficientB.setText(sensorInfo.getPoly_b());
                mEtCoefficientC.setText(sensorInfo.getPoly_c());
                mEtCoefficientK.setText(sensorInfo.getTemp_k());

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
            sensorInfo.setPoly_a(coefficientA);
            sensorInfo.setPoly_b(coefficientB);
            sensorInfo.setPoly_c(coefficientC);
            sensorInfo.setTemp_k(coefficientK);

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

    /****  物联网指令  end ******/

    private boolean checkValue() {
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        coefficientA = mEtCoefficientA.getText().toString().trim();
        coefficientB = mEtCoefficientB.getText().toString().trim();
        coefficientC = mEtCoefficientC.getText().toString().trim();
        coefficientK = mEtCoefficientK.getText().toString().trim();
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

        if (TextUtils.isEmpty(coefficientA)) {
            ToastUtils.show("多项式系数A不能为空!");
            mEtCoefficientA.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(coefficientA);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的多项式系数A!");
                mEtCoefficientA.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(coefficientB)) {
            ToastUtils.show("多项式系数B不能为空!");
            mEtCoefficientB.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(coefficientB);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的多项式系数B!");
                mEtCoefficientB.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(coefficientC)) {
            ToastUtils.show("多项式系数C不能为空!");
            mEtCoefficientC.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(coefficientC);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的多项式系数C!");
                mEtCoefficientC.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(coefficientK)) {
            ToastUtils.show("温度系数K不能为空!");
            mEtCoefficientK.requestFocus();
            return false;
        } else {
            try {
                double value = Double.parseDouble(coefficientK);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的温度系数K!");
                mEtCoefficientK.requestFocus();
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
