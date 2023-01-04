package com.shmedo.mcloudapp.deviceconfig.view.sensor.vms;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.entity.vms.SetVmsTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.enums.MonitoringType;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/6/17 <br/>
 * 描述：    Vms 终端接入的倍率传感器参数
 */
public class MagnificationView extends FrameLayout {
    /**
     * 触发值
     */
    @BindView(R.id.tv_trigger_threshold)
    TextView mTvThreshold;

    @BindView(R.id.et_trigger_threshold)
    EditText mEtThreshold;

    /**
     * 修正值
     */
    @BindView(R.id.tv_correctValue)
    TextView mTvCorrectValue;

    @BindView(R.id.et_correctValue)
    EditText mEtCorrectValue;

    /**
     * 扩展字段1
     */
    @BindView(R.id.tv_extension1)
    TextView mTvExtension1;

    @BindView(R.id.et_extension1)
    EditText mEtExtension1;

    @BindView(R.id.extension_layout1)
    ViewGroup extensionLayout1;

    /**
     * 扩展字段2
     */
    @BindView(R.id.tv_extension2)
    TextView mTvExtension2;

    @BindView(R.id.et_extension2)
    EditText mEtExtension2;

    @BindView(R.id.extension_layout2)
    ViewGroup extensionLayout2;

    private String threshold, correctValue;
    private String exValue1, exValue2, exValue3;

    public MagnificationView(@NonNull Context context) {
        this(context, null);
    }

    public MagnificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagnificationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vms_magnification_sensor_config_extra_fields, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtExtension1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtExtension2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
    }

    public void initData(VmsTerminalSensorInfo sensorInfo, MonitoringType monitoringType) {
        if (sensorInfo == null) {
            return;
        }
        threshold = sensorInfo.getGateval();
        correctValue = sensorInfo.getParamm();
        mEtThreshold.setText(threshold);
        mEtCorrectValue.setText(correctValue);
        switch (monitoringType) {
            case BOREHOLE_INCLINOMETER://钻孔测斜仪
                mTvThreshold.setText("触发值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:mm)");
                extensionLayout1.setVisibility(View.VISIBLE);
                extensionLayout2.setVisibility(View.GONE);
                mTvExtension1.setText("测段长(单位:m)");
                exValue1 = sensorInfo.getRopelen();
                //exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                mEtExtension1.setText(exValue1);
                break;

            case WATER_LEVEL_METER://地下水水位计
                mTvThreshold.setText("触发值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:mm)");
                extensionLayout1.setVisibility(View.VISIBLE);
                extensionLayout2.setVisibility(View.VISIBLE);
                mTvExtension1.setText("安装高程(单位:m)");
                mTvExtension2.setText("绳长(单位:m)");
                exValue1 = sensorInfo.getFixsite();
                exValue2 = sensorInfo.getRopelen();
                //exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                mEtExtension1.setText(exValue1);
                //exValue2 = decimalFormat.format(Double.parseDouble(exValue2));
                mEtExtension2.setText(exValue2);
                break;

            default:
                mTvThreshold.setText("触发值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:mm)");
                extensionLayout1.setVisibility(View.GONE);
                extensionLayout2.setVisibility(View.GONE);
                break;
        }
    }

    public boolean updateSensorData(SetVmsTerminalSensorParamsEntity sensorParamsEntity) {
        //解析出传感器序号
        String[] strs = sensorParamsEntity.getName().split("_");
        String sensorSerialNumber = strs.length > 1 ? strs[0] : "";
        MonitoringType monitoringType = MonitoringType.valueByCode(sensorSerialNumber);
        if (!checkValueValid(monitoringType)) {
            return false;
        }
        sensorParamsEntity.setGateval(threshold);
        sensorParamsEntity.setParamm(correctValue);
        switch (monitoringType) {
            case BOREHOLE_INCLINOMETER://钻孔测斜仪
                sensorParamsEntity.setRopelen(exValue1);//测段长
                break;

            case WATER_LEVEL_METER://地下水水位计
                sensorParamsEntity.setFixsite(exValue1);//安装高程
                sensorParamsEntity.setRopelen(exValue2);//绳长
                break;
        }
        return true;
    }

    private boolean checkValueValid(MonitoringType monitoringType) {
        threshold = mEtThreshold.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString();
        exValue1 = mEtExtension1.getText().toString().trim();
        exValue2 = mEtExtension2.getText().toString().trim();

        if (TextUtils.isEmpty(threshold)) {
            ToastUtils.show("请输入触发值!");
            mEtThreshold.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(threshold);
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的触阈值!");
            mEtThreshold.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(correctValue)) {
            ToastUtils.show("修正值不能为空!");
            mEtCorrectValue.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(correctValue);
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的修正值!");
            mEtCorrectValue.requestFocus();
            return false;
        }

        if (monitoringType == MonitoringType.BOREHOLE_INCLINOMETER) {//钻孔测斜仪
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("测段长不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测段长!");
                mEtExtension1.requestFocus();
                return false;
            }
        }

        if (monitoringType == MonitoringType.WATER_LEVEL_METER) {//地下水水位计
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("安装高程不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的安装高程!");
                mEtExtension1.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(exValue2)) {
                ToastUtils.show("绳长不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的绳长!");
                mEtExtension2.requestFocus();
                return false;
            }
        }

        return true;
    }
}
