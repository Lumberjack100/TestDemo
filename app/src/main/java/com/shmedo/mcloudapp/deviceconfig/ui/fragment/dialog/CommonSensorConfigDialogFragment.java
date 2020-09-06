package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.enums.SensorType;
import com.shmedo.core.model.SensorInclinometerInfo;
import com.shmedo.core.model.SensorInfrasoundInfo;
import com.shmedo.core.model.SensorRadarLevelInfo;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.core.model.SensorWireShiftInfo;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor.BaseSensorConfigActivity;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 数字式传感器配置参数对话框
 */
public class CommonSensorConfigDialogFragment extends BaseSensorConfigDialog {
    @BindView(R.id.tv_triggerThreshold)
    TextView mTvAlarmValue;
    @BindView(R.id.tv_correctionValue)
    TextView mTvCorrectValue;
    @BindView(R.id.tv_measure_long)
    TextView mTvMeasureLong;

    @BindView(R.id.et_modbus_address)
    EditText mEtModbusAddress;
    @BindView(R.id.et_trigger_threshold)
    EditText mEtAlarmValue;
    @BindView(R.id.et_revised)
    EditText mEtCorrectValue;
    @BindView(R.id.et_measure_long)
    EditText mEtMeasureLong;

    @BindView(R.id.measure_long_layout)
    ViewGroup measureLongLayout;

    private String address, triggerThreshold, correctValue, measureLong;
    private SensorType sensorType;//传感器类型


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_common_sensor_config_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectorSensorParamsInfo = ((BaseSensorConfigActivity) getActivity()).getCurrentCollectorSensorParamsInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        initView();
        setValue();
        return rootView;
    }

    private void initView() {
        if (collectorSensorParamsInfo != null) {
            sensorType = collectorSensorParamsInfo.getSensorType();
            if (sensorType.toString().equals("04")) {
                measureLongLayout.setVisibility(View.VISIBLE);
            } else {
                measureLongLayout.setVisibility(View.GONE);
            }
        }
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtMeasureLong.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }


    private void setValue() {
        mEtModbusAddress.setText(collectorSensorParamsInfo.getSensorAddress());
        switch (sensorType) {
            case WIRE_SHIFT://拉线位移计
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:m)");
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfo.getSensorData();
                mEtAlarmValue.setText(String.valueOf(sensorWireShiftInfo.getTriggerThreshold()));
                mEtCorrectValue.setText(String.valueOf(sensorWireShiftInfo.getCorrectionValue()));
                break;

            case SOIL_MOISTURE://土壤含水率
                mTvAlarmValue.setText("触发阈值(单位:%rh)");
                mTvCorrectValue.setText("修正值(单位:%rh)");
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfo.getSensorData();
                mEtAlarmValue.setText(sensorSoilMoistureInfo.getTriggerThreshold());
                mEtCorrectValue.setText(String.valueOf(sensorSoilMoistureInfo.getCorrectionValue()));
                break;

            case INCLINOMETER://测斜仪
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:m)");
                mTvMeasureLong.setText("测段长(单位:mm)");
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) collectorSensorParamsInfo.getSensorData();
                mEtAlarmValue.setText(String.valueOf(sensorInclinometerInfo.getTriggerThreshold()));
                mEtCorrectValue.setText(String.valueOf(sensorInclinometerInfo.getCorrectionValue()));
                mEtMeasureLong.setText(String.valueOf(sensorInclinometerInfo.getMeasureLength()));
                break;

            case RADAR_LEVEL_GAUGE://雷达物位计
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:mm)");
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) collectorSensorParamsInfo.getSensorData();
                mEtAlarmValue.setText(sensorRadarLevelInfo.getTriggerThreshold());
                mEtCorrectValue.setText(String.valueOf(sensorRadarLevelInfo.getCorrectionValue()));
                break;

            case INFRASOUND_SENSOR://次声
                mTvAlarmValue.setText("触发阈值(单位:Hz)");
                mTvCorrectValue.setText("修正值(单位:Hz)");
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) collectorSensorParamsInfo.getSensorData();
                mEtAlarmValue.setText(sensorInfrasoundInfo.getTriggerThreshold());
                mEtCorrectValue.setText(String.valueOf(sensorInfrasoundInfo.getCorrectionValue()));
                break;
        }
    }

    @OnClick({R.id.tv_cancel, R.id.tv_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                doNegativeClick(view);
                break;

            case R.id.tv_save:
                doPositiveClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
        if (!checkValue()) {
            Timber.w("传感器参数存在错误!");
            return;
        }

        updateSensorData();
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        if (listener.onPositiveClick(view, collectorSensorParamsInfo)) {
            dismiss();
        }
    }

    private void doNegativeClick(View view) {
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }

    private boolean checkValue() {
        address = mEtModbusAddress.getText().toString().trim();
        triggerThreshold = mEtAlarmValue.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        measureLong = mEtMeasureLong.getText().toString().trim();

        if (TextUtils.isEmpty(address)) {
            ToastUtils.show("地址不能为空!");
            return false;
        }

        if (!ValidateUtil.isInteger(address) || Integer.parseInt(address) < 0 || Integer.parseInt(address) > 99) {
            ToastUtils.show("请输入正确的地址!");
            return false;
        }

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("触发值不能为空!");
            return false;
        }

        if (!ValidateUtil.isInteger(triggerThreshold)) {
            ToastUtils.show("请输入正确的触发值!");
            return false;
        }

        if (TextUtils.isEmpty(correctValue)) {
            ToastUtils.show("修正值不能为空!");
            return false;
        }

        if (!ValidateUtil.isDouble(correctValue)) {
            ToastUtils.show("请输入正确的修正值!");
            return false;
        }

        if (sensorType.equals("04")) {
            if (TextUtils.isEmpty(measureLong)) {
                ToastUtils.show("测段长值不能为空!");
                return false;
            }
            if (!ValidateUtil.isDouble(measureLong)) {
                ToastUtils.show("请输入正确的测段长值!");
                return false;
            }
        }

        return true;
    }

    private void updateSensorData() {
        collectorSensorParamsInfo.setSensorAddress(address);
        switch (sensorType) {
            case WIRE_SHIFT://拉线位移计
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfo.getSensorData();
                sensorWireShiftInfo.setTriggerThreshold(triggerThreshold);
                sensorWireShiftInfo.setCorrectionValue(correctValue);
                collectorSensorParamsInfo.setSensorData(sensorWireShiftInfo);
                break;

            case SOIL_MOISTURE://土壤含水率
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfo.getSensorData();
                sensorSoilMoistureInfo.setTriggerThreshold(triggerThreshold);
                sensorSoilMoistureInfo.setCorrectionValue(correctValue);
                collectorSensorParamsInfo.setSensorData(sensorSoilMoistureInfo);
                break;

            case INCLINOMETER://测斜仪
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) collectorSensorParamsInfo.getSensorData();
                sensorInclinometerInfo.setTriggerThreshold(triggerThreshold);
                sensorInclinometerInfo.setCorrectionValue(correctValue);
                sensorInclinometerInfo.setMeasureLength(measureLong);
                break;

            case RADAR_LEVEL_GAUGE://雷达物位计
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) collectorSensorParamsInfo.getSensorData();
                sensorRadarLevelInfo.setTriggerThreshold(triggerThreshold);
                sensorRadarLevelInfo.setCorrectionValue(correctValue);
                collectorSensorParamsInfo.setSensorData(sensorRadarLevelInfo);
                break;

            case INFRASOUND_SENSOR://次声
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) collectorSensorParamsInfo.getSensorData();
                sensorInfrasoundInfo.setTriggerThreshold(triggerThreshold);
                sensorInfrasoundInfo.setCorrectionValue(correctValue);
                collectorSensorParamsInfo.setSensorData(sensorInfrasoundInfo);
                break;
        }
    }
}
