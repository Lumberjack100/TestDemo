package com.shmedo.mcloudapp.deviceconfig.view.sensor.vms;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.entity.vms.SetVmsTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTSensorUtil;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/6/17 <br/>
 * 描述：    Vms 终端接入的倍率传感器参数
 */
public class MagnificationView extends FrameLayout {
    @BindView(R.id.viewstub_rainfall)
    ViewStub viewStubRainFall;//雨量计

    @BindView(R.id.viewstub_inclinometer)
    ViewStub viewStubInclinometer;//钻孔测斜仪

    private View viewRainFall;//雨量计
    private EditText mEtThreshold;//触发阈值
    private String threshold;

    private View viewInclinometer;//钻孔测斜仪
    private EditText mEtRopeLength;//测段长
    private EditText mEtCorrectValue;//修正值
    private String ropeLength;
    private String correctValue;

    private String sensorSerialNumber;

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
    }

    public void setVisibilityBySensorType(String sensorName) {
        String code = IOTSensorUtil.getInstance().getSensorTypeCodeByName(sensorName);
        if (code.equals("201")) {
            initRainView();
            if (viewInclinometer != null) {
                viewInclinometer.setVisibility(View.GONE);
            }
        } else if (code.equals("222")) {
            initInclinometerView();
            if (viewRainFall != null) {
                viewRainFall.setVisibility(View.GONE);
            }
        }
    }

    public void initData(VmsTerminalSensorInfo sensorInfo) {
        if (sensorInfo == null) {
            return;
        }
        //解析出传感器序号
        String[] strs = sensorInfo.getName().split("_");
        sensorSerialNumber = strs.length > 1 ? strs[0] : "";
        if (sensorSerialNumber.equals("201")) {
            initRainView();
            initRainValue(sensorInfo);
        } else if (sensorSerialNumber.equals("222")) {
            initInclinometerView();
            initInclinometerValue(sensorInfo);
        }
    }

    private void initRainView() {
        if (viewRainFall != null) {
            viewRainFall.setVisibility(View.VISIBLE);
            return;
        }
        viewRainFall = viewStubRainFall.inflate();
        mEtThreshold = viewRainFall.findViewById(R.id.et_trigger_threshold);
        mEtThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
    }

    private void initInclinometerView() {
        if (viewInclinometer != null) {
            viewInclinometer.setVisibility(View.VISIBLE);
            return;
        }
        viewInclinometer = viewStubInclinometer.inflate();
        mEtRopeLength = viewInclinometer.findViewById(R.id.ropeLength);
        mEtCorrectValue = viewInclinometer.findViewById(R.id.correctValue);
        mEtRopeLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
    }

    private void initRainValue(VmsTerminalSensorInfo sensorInfo) {
        threshold = sensorInfo.getGateval();
        mEtThreshold.setText(threshold);
    }

    private void initInclinometerValue(VmsTerminalSensorInfo sensorInfo) {
        ropeLength = sensorInfo.getRopelen();
        correctValue = sensorInfo.getParamm();
        mEtRopeLength.setText(ropeLength);
        mEtCorrectValue.setText(correctValue);
    }

    public boolean updateSensorData(SetVmsTerminalSensorParamsEntity sensorParamsEntity) {
        if (!checkValueValid()) {
            return false;
        }
        if (sensorSerialNumber.equals("201")) {
            sensorParamsEntity.setGateval(threshold);

        } else if (sensorSerialNumber.equals("222")) {
            sensorParamsEntity.setRopelen(ropeLength);
            sensorParamsEntity.setParamm(correctValue);
        }
        return true;
    }

    private boolean checkValueValid() {
        if (sensorSerialNumber.equals("201")) {
            threshold = mEtThreshold.getText().toString().trim();
            if (!TextUtils.isEmpty(threshold)) {
                try {
                    double value = Double.parseDouble(threshold);

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的触发阈值!");
                    mEtThreshold.requestFocus();
                    return false;
                }
            }

        } else if (sensorSerialNumber.equals("222")) {
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
        }
        return true;
    }

    public boolean checkValueIsChange() {
        if (sensorSerialNumber.equals("201")) {
            if (threshold != null && !threshold.equals(mEtThreshold.getText().toString().trim())) {
                return true;
            }
        } else if (sensorSerialNumber.equals("222")) {
            if (ropeLength != null && !ropeLength.equals(mEtRopeLength.getText().toString().trim())) {
                return true;
            }
            if (correctValue != null && !correctValue.equals(mEtCorrectValue.getText().toString().trim())) {
                return true;
            }
        }
        return false;
    }
}
