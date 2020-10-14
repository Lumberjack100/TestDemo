package com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.SensorInclinometerInfo;
import com.shmedo.configlibrary.ble.model.SensorInfrasoundInfo;
import com.shmedo.configlibrary.ble.model.SensorRadarLevelInfo;
import com.shmedo.configlibrary.ble.model.SensorSoilMoistureInfo;
import com.shmedo.configlibrary.ble.model.SensorWireShiftInfo;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/14 <br/>
 * 描述：    数字传感器参数配置页面
 */
public class ExternalDigitalSensorActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

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

    private String sensorAddress;   //传感器通道号
    private SensorType sensorType;//传感器类型
    private Parcelable parcelableData;

    private String address, triggerThreshold, correctValue, measureLong;


    public static void startActivityForResultByFragment(Fragment context, int requestCode, String sensorAddress, SensorType sensorType, Parcelable parcelable) {
        Intent intent = new Intent(context.getActivity(), ExternalDigitalSensorActivity.class);
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, sensorAddress);
        intent.putExtra(AppContants.Extras.SENSOR_TYPE, sensorType);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, parcelable);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_external_digital_sensor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("扩展传感器配置");
        parseIntent();
        initView();
        initValue();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_ADDRESS)) {
            sensorAddress = intent.getStringExtra(AppContants.Extras.SENSOR_ADDRESS);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_TYPE)) {
            sensorType = (SensorType) intent.getSerializableExtra(AppContants.Extras.SENSOR_TYPE);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_PARAM)) {
            parcelableData = intent.getParcelableExtra(AppContants.Extras.SENSOR_PARAM);
        }

        String sensorName = BlueResultParserUtil.getSensorName(sensorType);
        mToolbarTitle.setText(sensorName);
    }

    private void initView() {
        //测斜仪
        if (sensorType == SensorType.INCLINOMETER) {
            measureLongLayout.setVisibility(View.VISIBLE);
        } else {
            measureLongLayout.setVisibility(View.GONE);
        }
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtMeasureLong.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void initValue() {
        mEtModbusAddress.setText(sensorAddress);
        switch (sensorType) {
            case WIRE_SHIFT://拉线位移计
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:m)");
                if (parcelableData != null) {
                    SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) parcelableData;
                    mEtAlarmValue.setText(String.valueOf(sensorWireShiftInfo.getTriggerThreshold()));
                    mEtCorrectValue.setText(String.valueOf(sensorWireShiftInfo.getCorrectionValue()));
                }
                break;

            case SOIL_MOISTURE://土壤含水率
                mTvAlarmValue.setText("触发阈值(单位:%rh)");
                mTvCorrectValue.setText("修正值(单位:%rh)");
                if (parcelableData != null) {
                    SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) parcelableData;
                    mEtAlarmValue.setText(sensorSoilMoistureInfo.getTriggerThreshold());
                    mEtCorrectValue.setText(String.valueOf(sensorSoilMoistureInfo.getCorrectionValue()));
                }
                break;

            case INCLINOMETER://测斜仪
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:m)");
                mTvMeasureLong.setText("测段长(单位:mm)");
                if (parcelableData != null) {
                    SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) parcelableData;
                    mEtAlarmValue.setText(String.valueOf(sensorInclinometerInfo.getTriggerThreshold()));
                    mEtCorrectValue.setText(String.valueOf(sensorInclinometerInfo.getCorrectionValue()));
                    mEtMeasureLong.setText(String.valueOf(sensorInclinometerInfo.getMeasureLength()));
                }
                break;

            case RADAR_LEVEL_GAUGE://雷达物位计
                mTvAlarmValue.setText("触发阈值(单位:mm)");
                mTvCorrectValue.setText("修正值(单位:mm)");
                if (parcelableData != null) {
                    SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) parcelableData;
                    mEtAlarmValue.setText(sensorRadarLevelInfo.getTriggerThreshold());
                    mEtCorrectValue.setText(String.valueOf(sensorRadarLevelInfo.getCorrectionValue()));
                }
                break;

            case INFRASOUND_SENSOR://次声
                mTvAlarmValue.setText("触发阈值(单位:Hz)");
                mTvCorrectValue.setText("修正值(单位:Hz)");
                if (parcelableData != null) {
                    SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) parcelableData;
                    mEtAlarmValue.setText(sensorInfrasoundInfo.getTriggerThreshold());
                    mEtCorrectValue.setText(String.valueOf(sensorInfrasoundInfo.getCorrectionValue()));
                }
                break;
        }
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                processSave();
                break;
        }
    }

    private void processSave() {
        if (!checkValue()) {
            Timber.w("传感器参数存在错误!");
            return;
        }

        Intent intent = getIntent();
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, address);
        switch (sensorType) {
            case WIRE_SHIFT://拉线位移计
                SensorWireShiftInfo sensorWireShiftInfo = new SensorWireShiftInfo();
                sensorWireShiftInfo.setTriggerThreshold(triggerThreshold);
                sensorWireShiftInfo.setCorrectionValue(correctValue);
                intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorWireShiftInfo);
                break;

            case SOIL_MOISTURE://土壤含水率
                SensorSoilMoistureInfo sensorSoilMoistureInfo = new SensorSoilMoistureInfo();
                sensorSoilMoistureInfo.setTriggerThreshold(triggerThreshold);
                sensorSoilMoistureInfo.setCorrectionValue(correctValue);
                intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorSoilMoistureInfo);
                break;

            case INCLINOMETER://测斜仪
                SensorInclinometerInfo sensorInclinometerInfo = new SensorInclinometerInfo();
                sensorInclinometerInfo.setTriggerThreshold(triggerThreshold);
                sensorInclinometerInfo.setCorrectionValue(correctValue);
                sensorInclinometerInfo.setMeasureLength(measureLong);
                intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorInclinometerInfo);
                break;

            case RADAR_LEVEL_GAUGE://雷达物位计
                SensorRadarLevelInfo sensorRadarLevelInfo = new SensorRadarLevelInfo();
                sensorRadarLevelInfo.setTriggerThreshold(triggerThreshold);
                sensorRadarLevelInfo.setCorrectionValue(correctValue);
                intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorRadarLevelInfo);
                break;

            case INFRASOUND_SENSOR://次声
                SensorInfrasoundInfo sensorInfrasoundInfo = new SensorInfrasoundInfo();
                sensorInfrasoundInfo.setTriggerThreshold(triggerThreshold);
                sensorInfrasoundInfo.setCorrectionValue(correctValue);
                intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorInfrasoundInfo);
                break;
        }

        setResult(RESULT_OK, intent);
        finish();
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


}
