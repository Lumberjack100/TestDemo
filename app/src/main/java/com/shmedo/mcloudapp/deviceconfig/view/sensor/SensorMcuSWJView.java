package com.shmedo.mcloudapp.deviceconfig.view.sensor;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.mcloudapp.R;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/5/11 <br/>
 * 描述：     MCU 水位计配置项视图
 */
public class SensorMcuSWJView extends LinearLayout {
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//修正值
    @BindView(R.id.et_cord_length)
    EditText mEtCordLength;//绳长
    @BindView(R.id.et_install_elevation)
    EditText mEtInstallElevation;//安装高程

    private String correctValue, cordLength, installElevation;

    private DecimalFormat decimalFormat = new DecimalFormat("#.#");

    public SensorMcuSWJView(@NonNull Context context) {
        this(context, null);
    }

    public SensorMcuSWJView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorMcuSWJView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.sensor_mcu_swj_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCordLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInstallElevation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtCorrectValue.setText("0");
    }

    /****  物联网指令 start ******/
    public void initData(DasExternalSensorInfo sensorInfo) {
        if (sensorInfo != null) {
            try {
                mEtCorrectValue.setText(decimalFormat.format(Double.parseDouble(sensorInfo.getCorrval())));
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
            sensorInfo.setCorrval(TextUtils.isEmpty(correctValue) ? "0" : decimalFormat.format(Double.parseDouble(correctValue)));
            sensorInfo.setRopelen(decimalFormat.format(Double.parseDouble(cordLength)));
            sensorInfo.setTubealti(decimalFormat.format(Double.parseDouble(installElevation)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /****  物联网指令  end ******/

    private boolean checkValue() {
        correctValue = mEtCorrectValue.getText().toString().trim();
        cordLength = mEtCordLength.getText().toString().trim();
        installElevation = mEtInstallElevation.getText().toString().trim();

        if (!TextUtils.isEmpty(correctValue)) {
            try {
                double value = Double.parseDouble(correctValue);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的修正值!");
                mEtCorrectValue.requestFocus();
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
