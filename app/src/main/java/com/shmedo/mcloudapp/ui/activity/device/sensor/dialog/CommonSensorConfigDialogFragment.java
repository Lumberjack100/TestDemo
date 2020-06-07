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
import com.shmedo.mcloudapp.util.UserConfig;

import butterknife.BindView;
import butterknife.OnClick;


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
    @BindView(R.id.et_note)
    EditText mEtNote;

    @BindView(R.id.measure_long_layout)
    ViewGroup measureLongLayout;

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private UserConfig userConfig;
    private String oldAddress, newAddress, oldAlarmValue, newAlarmValue, oldCorrectValue, newCorrectValue, oldMeasureLong, newMeasureLong;

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
        userConfig = UserConfig.getConfig(getActivity(), collectorSensorParamsInfoSub.getChannelNumber());
        mEtNote.setText(userConfig.readString(collectorSensorParamsInfoSub.getChannelNumber()));
        oldAddress = mEtModbusAddress.getText().toString().trim();
        oldAlarmValue = mEtAlarmValue.getText().toString().trim();
        oldCorrectValue = mEtCorrectValue.getText().toString().trim();
        oldMeasureLong = mEtMeasureLong.getText().toString().trim();
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
        newAddress = mEtModbusAddress.getText().toString().trim();
        newAlarmValue = mEtAlarmValue.getText().toString().trim();
        newCorrectValue = mEtCorrectValue.getText().toString().trim();
        newMeasureLong = mEtCorrectValue.getText().toString().trim();

        if (TextUtils.isEmpty(newAddress) || !ValidateUtil.isInteger(newAddress) || Integer.parseInt(newAddress) < 0 || Integer.parseInt(newAddress) > 99) {
            ToastUtils.show("请输入正确的通道号");
            return;
        }

        if (TextUtils.isEmpty(newAlarmValue) || !ValidateUtil.isInteger(newAlarmValue)) {
            ToastUtils.show("请输入正确的触发值");
            return;
        }

        if (TextUtils.isEmpty(newCorrectValue) || (!ValidateUtil.isInteger(newCorrectValue) && !ValidateUtil.isDouble(newCorrectValue))) {
            ToastUtils.show("请输入正确的修正值");
            return;
        }

        if (sensorType.equals("04")) {
            if (TextUtils.isEmpty(newMeasureLong) || !ValidateUtil.isInteger(newMeasureLong)) {
                ToastUtils.show("请输入正确的测段长值");
                return;
            }
        }

        userConfig.writeString(String.valueOf(collectorSensorParamsInfoSub.getChannelNumber()), mEtNote.getText().toString().trim());
        updateParamsInfo(true);
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        if (listener.onPositiveClick(view)) {
            dismiss();
        }
    }

    private void doNegativeClick(View view) {
        // Do stuff here.
        updateParamsInfo(false);
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }

    private void updateParamsInfo(boolean isUseNewValue) {
        switch (sensorType) {
            case "02"://拉线位移计
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorWireShiftInfo.setTriggerThreshold(isUseNewValue ? Integer.parseInt(newAlarmValue) : Integer.parseInt(oldAlarmValue));
                sensorWireShiftInfo.setCorrectionValue(isUseNewValue ? Double.valueOf(newCorrectValue) : Double.valueOf(oldCorrectValue));
                collectorSensorParamsInfoSub.setSensorData(sensorWireShiftInfo);
                break;

            case "03"://土壤含水率
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorSoilMoistureInfo.setTriggerThreshold(isUseNewValue ? newAlarmValue : oldAlarmValue);
                sensorSoilMoistureInfo.setRevised(isUseNewValue ? Double.valueOf(newCorrectValue) : Double.valueOf(oldCorrectValue));
                collectorSensorParamsInfoSub.setSensorData(sensorSoilMoistureInfo);
                break;

            case "04"://测斜仪
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorInclinometerInfo.setTriggerThreshold(isUseNewValue ? Integer.parseInt(newAlarmValue) : Integer.parseInt(oldAlarmValue));
                sensorInclinometerInfo.setCorrectionValue(isUseNewValue ? Double.valueOf(newCorrectValue) : Double.valueOf(oldCorrectValue));
                sensorInclinometerInfo.setMeasureLength(isUseNewValue ? Integer.parseInt(newMeasureLong) : Integer.parseInt(oldMeasureLong));
                break;

            case "07"://雷达物位计
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorRadarLevelInfo.setTriggerThreshold(isUseNewValue ? newAlarmValue : oldAlarmValue);
                sensorRadarLevelInfo.setRevised(isUseNewValue ? Double.valueOf(newCorrectValue) : Double.valueOf(oldCorrectValue));
                collectorSensorParamsInfoSub.setSensorData(sensorRadarLevelInfo);
                break;

            case "21"://次声
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorInfrasoundInfo.setTriggerThreshold(isUseNewValue ? newAlarmValue : oldAlarmValue);
                sensorInfrasoundInfo.setRevised(isUseNewValue ? Double.valueOf(newCorrectValue) : Double.valueOf(oldCorrectValue));
                collectorSensorParamsInfoSub.setSensorData(sensorInfrasoundInfo);
                break;
        }

        collectorSensorParamsInfoSub.setSensorAddress(isUseNewValue ? newAddress : oldAddress);
    }
}
