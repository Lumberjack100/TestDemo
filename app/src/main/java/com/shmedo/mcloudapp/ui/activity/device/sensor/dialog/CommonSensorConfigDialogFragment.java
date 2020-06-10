package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.model.SensorInclinometerInfo;
import com.shmedo.core.model.SensorInfrasoundInfo;
import com.shmedo.core.model.SensorRadarLevelInfo;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.core.model.SensorWireShiftInfo;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.ui.activity.device.sensor.BaseSensorConfigActivity;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;


public class CommonSensorConfigDialogFragment extends BaseDialogFragment {
    @BindView(R.id.spinnerWay)
    Spinner spinnerWay;

    @BindView(R.id.tv_triggerThreshold)
    TextView mTvAlarmValue;
    @BindView(R.id.tv_correctionValue)
    TextView mTvCorrectValue;
    @BindView(R.id.tv_measure_long)
    TextView mTvMeasureLong;
    @BindView(R.id.et_trigger_threshold)
    EditText mEtAlarmValue;
    @BindView(R.id.et_revised)
    EditText mEtCorrectValue;
    @BindView(R.id.et_measure_long)
    EditText mEtMeasureLong;

    @BindView(R.id.measure_long_layout)
    ViewGroup measureLongLayout;

    private String triggerThreshold, correctValue, measureLong;
    private String sensorType;//传感器类型

    private ArrayAdapter<String> adapterWay;
    private List<String> wayList = Arrays.asList("00", "01", "02", "03", "04", "05", "06", "07");

    @Override
    protected int initContentView() {
        return R.layout.fragment_common_sensor_config_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectorSensorParamsInfoSub = ((BaseSensorConfigActivity) getActivity()).getCurrentCollectorSensorParamsInfoSub();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        initView();
        initSensorWayAdapter();
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
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasureLong.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
    }

    private void initSensorWayAdapter() {
        adapterWay = new ArrayAdapter<>(getActivity(), R.layout.sensor_spinner_item, wayList);
        adapterWay.setDropDownViewResource(R.layout.spinner_item);
        spinnerWay.setAdapter(adapterWay);
        spinnerWay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChannelNumber = wayList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String channelNumber = StringUtil.formatStringTwo(collectorSensorParamsInfoSub.getChannelNumber());
        switch (channelNumber) {
            case "00":
                spinnerWay.setSelection(0);
                break;

            case "01":
                spinnerWay.setSelection(1);
                break;

            case "02":
                spinnerWay.setSelection(2);
                break;

            case "03":
                spinnerWay.setSelection(3);
                break;

            case "04":
                spinnerWay.setSelection(4);
                break;

            case "05":
                spinnerWay.setSelection(5);
                break;

            case "06":
                spinnerWay.setSelection(6);
                break;

            case "07":
                spinnerWay.setSelection(7);
                break;
        }
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
        triggerThreshold = mEtAlarmValue.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        measureLong = mEtCorrectValue.getText().toString().trim();

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("触发值不能为空!");
            return false;
        }

        if (!ValidateUtil.isDouble(triggerThreshold)) {
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
        collectorSensorParamsInfoSub.setChannelNumber(selectedChannelNumber);
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
    }
}
