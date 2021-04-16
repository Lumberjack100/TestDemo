package com.shmedo.mcloudapp.deviceconfig.view.sensor.vms;

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
import com.shmedo.configlibrary.iot.cmd.entity.vms.SetVmsTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：    Vms 终端接入的模数结算方式传感器参数
 */
public class ModulusView extends FrameLayout {

    @BindView(R.id.conversionFactor)
    EditText mEtConversionFactor;//转换系数K

    private String conversionFactor;


    public ModulusView(@NonNull Context context) {
        this(context, null);
    }

    public ModulusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ModulusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vms_modulus_sensor_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtConversionFactor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
    }

    public void initData(VmsTerminalSensorInfo sensorInfo) {
        if (sensorInfo == null) {
            return;
        }

        conversionFactor = sensorInfo.getParamk();
        mEtConversionFactor.setText(conversionFactor);
    }

    public boolean updateSensorData(SetVmsTerminalSensorParamsEntity sensorParamsEntity) {
        if (!checkValueValid()) {
            return false;
        }

        sensorParamsEntity.setParamk(conversionFactor);

        return true;
    }

    private boolean checkValueValid() {
        conversionFactor = mEtConversionFactor.getText().toString().trim();
        if (!TextUtils.isEmpty(conversionFactor)) {
            try {
                double value = Double.parseDouble(conversionFactor);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的转换系数!");
                mEtConversionFactor.requestFocus();
                return false;
            }
        }
        return true;
    }

    public boolean checkValueIsChange() {
        if (conversionFactor != null && !conversionFactor.equals(mEtConversionFactor.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
