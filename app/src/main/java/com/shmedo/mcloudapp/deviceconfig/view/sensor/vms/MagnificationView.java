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
 * 创建时间:  2021/6/17 <br/>
 * 描述：    Vms 终端接入的倍率传感器参数
 */
public class MagnificationView extends FrameLayout {
    @BindView(R.id.ropeLength)
    EditText mEtRopeLength;//测段长

    @BindView(R.id.correctValue)
    EditText mEtCorrectValue;//修正值

    private String ropeLength;
    private String correctValue;

    public MagnificationView(@NonNull Context context) {
        this(context, null);
    }

    public MagnificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagnificationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vms_magnification_sensor_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtRopeLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
    }

    public void initData(VmsTerminalSensorInfo sensorInfo) {
        if (sensorInfo == null) {
            return;
        }
        ropeLength = sensorInfo.getRopelen();
        correctValue = sensorInfo.getParamm();

        mEtRopeLength.setText(ropeLength);
        mEtCorrectValue.setText(correctValue);
    }

    public boolean updateSensorData(SetVmsTerminalSensorParamsEntity sensorParamsEntity) {
        if (!checkValueValid()) {
            return false;
        }

        sensorParamsEntity.setRopelen(ropeLength);
        sensorParamsEntity.setParamm(correctValue);
        return true;
    }

    private boolean checkValueValid() {
        ropeLength = mEtRopeLength.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();

        if (!TextUtils.isEmpty(ropeLength)) {
            try {
                double value = Double.parseDouble(ropeLength);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测段长!");
                mEtRopeLength.requestFocus();
                return false;
            }
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
        return true;
    }

    public boolean checkValueIsChange() {
        if (ropeLength != null && !ropeLength.equals(mEtRopeLength.getText().toString().trim())) {
            return true;
        }

        if (correctValue != null && !correctValue.equals(mEtCorrectValue.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
