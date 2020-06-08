package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.model.SensorInclinometerInfo;
import com.shmedo.core.model.SensorInfrasoundInfo;
import com.shmedo.core.model.SensorRadarLevelInfo;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.core.model.SensorWireShiftInfo;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;


public class CommonSensorConfigDialogFragment extends BaseDialogFragment {
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

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private String address, triggerThreshold, correctValue, measureLong;

    private String sensorType;//传感器类型

    public static final String ARG_PARAM1 = "param1";

    public CommonSensorConfigDialogFragment() {
        // Required empty public constructor
    }


    public static CommonSensorConfigDialogFragment newInstance(CollectorSensorParamsInfoSub infoSub) {
        CommonSensorConfigDialogFragment fragment = new CommonSensorConfigDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, infoSub);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_common_sensor_config_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectorSensorParamsInfoSub = (CollectorSensorParamsInfoSub) getArguments().getSerializable(ARG_PARAM1);
        }
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
        if (collectorSensorParamsInfoSub != null) {
            sensorType = collectorSensorParamsInfoSub.getSensorType();
            if (sensorType.equals("04")) {
                measureLongLayout.setVisibility(View.VISIBLE);
            } else {
                measureLongLayout.setVisibility(View.GONE);
            }
        }
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasureLong.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
    }

    private void setValue() {
        switch (sensorType) {
            case "02"://拉线位移计
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:m)");
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtAlarmValue.setText(sensorWireShiftInfo.getTriggerThreshold() + "");
                mEtCorrectValue.setText(String.valueOf(sensorWireShiftInfo.getCorrectionValue()));
                break;

            case "03"://土壤含水率
                mTvAlarmValue.setText("触发阈值(单位:%rh)");
                mTvCorrectValue.setText("修正值(单位:%rh)");
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtAlarmValue.setText(sensorSoilMoistureInfo.getTriggerThreshold());
                mEtCorrectValue.setText(String.valueOf(sensorSoilMoistureInfo.getRevised()));
                break;

            case "04"://测斜仪
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:m)");
                mTvMeasureLong.setText("测段长(单位:mm)");
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtAlarmValue.setText(sensorInclinometerInfo.getTriggerThreshold());
                mEtCorrectValue.setText(String.valueOf(sensorInclinometerInfo.getCorrectionValue()));
                mEtMeasureLong.setText(sensorInclinometerInfo.getMeasureLength());
                break;

            case "07"://雷达物位计
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:mm)");
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtAlarmValue.setText(sensorRadarLevelInfo.getTriggerThreshold());
                mEtCorrectValue.setText(String.valueOf(sensorRadarLevelInfo.getRevised()));
                break;

            case "21"://次声
                mTvAlarmValue.setText("触发阈值(单位:Hz)");
                mTvCorrectValue.setText("修正值(单位:Hz)");
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtAlarmValue.setText(sensorInfrasoundInfo.getTriggerThreshold());
                mEtCorrectValue.setText(String.valueOf(sensorInfrasoundInfo.getRevised()));
                break;
        }

        mEtModbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
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
        if (listener.onPositiveClick(view)) {
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
        measureLong = mEtCorrectValue.getText().toString().trim();

        if (TextUtils.isEmpty(address) || !ValidateUtil.isInteger(address) || Integer.parseInt(address) < 0 || Integer.parseInt(address) > 99) {
            ToastUtils.show("请输入正确的通道号");
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

        if (sensorType.equals("04")) {
            if (TextUtils.isEmpty(measureLong) || !ValidateUtil.isInteger(measureLong)) {
                ToastUtils.show("请输入正确的测段长值");
                return false;
            }
        }

        return true;
    }

    private void updateSensorData() {
        switch (sensorType) {
            case "02"://拉线位移计
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorWireShiftInfo.setTriggerThreshold(Integer.parseInt(triggerThreshold));
                sensorWireShiftInfo.setCorrectionValue(Double.parseDouble(correctValue));
                collectorSensorParamsInfoSub.setSensorData(sensorWireShiftInfo);
                break;

            case "03"://土壤含水率
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorSoilMoistureInfo.setTriggerThreshold(triggerThreshold);
                sensorSoilMoistureInfo.setRevised(Double.parseDouble(correctValue));
                collectorSensorParamsInfoSub.setSensorData(sensorSoilMoistureInfo);
                break;

            case "04"://测斜仪
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorInclinometerInfo.setTriggerThreshold(Integer.parseInt(triggerThreshold));
                sensorInclinometerInfo.setCorrectionValue(Double.parseDouble(correctValue));
                sensorInclinometerInfo.setMeasureLength(Integer.parseInt(measureLong));
                break;

            case "07"://雷达物位计
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorRadarLevelInfo.setTriggerThreshold(triggerThreshold);
                sensorRadarLevelInfo.setRevised(Double.parseDouble(correctValue));
                collectorSensorParamsInfoSub.setSensorData(sensorRadarLevelInfo);
                break;

            case "21"://次声
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorInfrasoundInfo.setTriggerThreshold(triggerThreshold);
                sensorInfrasoundInfo.setRevised(Double.parseDouble(correctValue));
                collectorSensorParamsInfoSub.setSensorData(sensorInfrasoundInfo);
                break;
        }

        collectorSensorParamsInfoSub.setSensorAddress(address);
    }
}
