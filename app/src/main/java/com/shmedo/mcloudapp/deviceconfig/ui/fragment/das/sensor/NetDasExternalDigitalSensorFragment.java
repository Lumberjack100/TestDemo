package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/16 <br/>
 * 描述：     TODO
 */
public class NetDasExternalDigitalSensorFragment extends BaseFragment {

    @BindView(R.id.tv_triggerThreshold)
    TextView mTvAlarmValue;//报警值

    @BindView(R.id.tv_correctionValue)
    TextView mTvCorrectValue;//修正值

    @BindView(R.id.tv_measure_long)
    TextView mTvMeasureLong;//测段长

    @BindView(R.id.tv_initial_reading)
    TextView mTvInitialReading;//初始读数

    @BindView(R.id.tv_head_on_weir)
    TextView mTvHeadOnWeir;//堰上水头


    @BindView(R.id.et_modbus_address)
    EditText mEtModbusAddress;

    @BindView(R.id.et_trigger_threshold)
    EditText mEtAlarmValue;

    @BindView(R.id.et_revised)
    EditText mEtCorrectValue;

    @BindView(R.id.correction_layout)
    ViewGroup correctionLayout;

    //测斜仪 测段长
    @BindView(R.id.et_measure_long)
    EditText mEtMeasureLong;

    @BindView(R.id.measure_long_layout)
    ViewGroup measureLongLayout;

    //量水堰计 测段长
    @BindView(R.id.et_initial_reading)
    EditText mEtInitialReading;

    @BindView(R.id.et_head_on_weir)
    EditText mEtHeadOnWeir;

    @BindView(R.id.weir_layout)
    ViewGroup weirLayout;

    //倾角仪 测段长
    @BindView(R.id.et_x_angle)
    EditText mEtXAngle;

    @BindView(R.id.et_y_angle)
    EditText mEtYAngle;

    @BindView(R.id.qingjiao_layout)
    ViewGroup qingJiaoLayout;

    private DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private IOTSensorType iotSensorType;//传感器类型
    private ArrayList<String> addressList = new ArrayList<>();
    private DasExternalSensorInfo externalSensorInfo;
    private String sensorAddress, triggerThreshold, correctValue, measureLong, initialReading, headOnWeir, xAngle, yAngle;


    public static NetDasExternalDigitalSensorFragment newInstance(ArrayList<String> addressList, IOTSensorType sensorType, DasExternalSensorInfo externalSensorInfo) {
        NetDasExternalDigitalSensorFragment fragment = new NetDasExternalDigitalSensorFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST, addressList);
        args.putSerializable(AppContants.Extras.SENSOR_TYPE, sensorType);
        args.putSerializable(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            addressList = getArguments().getStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST);
            externalSensorInfo = (DasExternalSensorInfo) getArguments().getSerializable(AppContants.Extras.SENSOR_PARAM);
            iotSensorType = (IOTSensorType) getArguments().getSerializable(AppContants.Extras.SENSOR_TYPE);
            if (externalSensorInfo != null && !TextUtils.isEmpty(externalSensorInfo.getAddr())) {
                addressList.remove(externalSensorInfo.getAddr());
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.das_external_digital_sensor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        initValue();
    }

    private void setView() {
        //测斜仪
        if (iotSensorType != null && iotSensorType == IOTSensorType.INCLINOMETER) {
            measureLongLayout.setVisibility(View.VISIBLE);
        }
        //量水堰计
        if (iotSensorType != null && iotSensorType == IOTSensorType.WEIR) {
            weirLayout.setVisibility(View.VISIBLE);
        }
        //倾角仪
        if (iotSensorType != null && iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) {
            qingJiaoLayout.setVisibility(View.VISIBLE);
            correctionLayout.setVisibility(View.GONE);
        }
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtMeasureLong.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtInitialReading.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtHeadOnWeir.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtXAngle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        mEtYAngle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
    }

    private void initValue() {
        try {
            if (externalSensorInfo == null) {
                externalSensorInfo = new DasExternalSensorInfo();
            }
            sensorAddress = externalSensorInfo.getAddr();
            triggerThreshold = externalSensorInfo.getThreshold();
            correctValue = externalSensorInfo.getCorrval();

            measureLong = externalSensorInfo.getSpacing();

            initialReading = externalSensorInfo.getLsycsds();
            headOnWeir = externalSensorInfo.getLsyysst();

            xAngle = externalSensorInfo.getInitvalx();
            yAngle = externalSensorInfo.getInitvaly();

            mEtModbusAddress.setText(sensorAddress);
            if (!TextUtils.isEmpty(triggerThreshold)) {
                triggerThreshold = decimalFormat.format(Double.parseDouble(triggerThreshold));
                mEtAlarmValue.setText(triggerThreshold);
            }
            if (!TextUtils.isEmpty(correctValue)) {
                correctValue = decimalFormat.format(Double.parseDouble(correctValue));
                mEtCorrectValue.setText(correctValue);
            }
            switch (iotSensorType) {
                case RAIN_GAUGE://压电式雨量计
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    break;

                case WIRE_SHIFT://拉线位移计
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    break;

                case SOIL_MOISTURE://土壤含水率
                    mTvAlarmValue.setText("报警值(单位:%rh)");
                    mTvCorrectValue.setText("修正值(单位:%rh)");
                    break;

                case INCLINOMETER://测斜仪
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    mTvMeasureLong.setText("测段长(单位:mm)");
                    if (!TextUtils.isEmpty(measureLong)) {
                        measureLong = decimalFormat.format(Double.parseDouble(measureLong));
                        mEtMeasureLong.setText(measureLong);
                    }
                    break;

                case ULTRASONIC_LEVEL_GAUGE://超声波物位计
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("安装高程(单位:m)");
                    break;

                case RADAR_LEVEL_GAUGE://雷达物位计
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("安装高程(单位:m)");
                    break;

                case INFRASOUND://次声
                    mTvAlarmValue.setText("报警值(单位:Hz)");
                    mTvCorrectValue.setText("修正值(单位:Hz)");
                    break;

                case WEATHER_STATION://气象计
                    mTvAlarmValue.setText("报警值(单位:m/s)");
                    mTvCorrectValue.setText("修正值(单位:m/s)");
                    break;

                case WEIR://量水堰计
                    mTvAlarmValue.setText("报警值(单位:m³/s)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    mTvInitialReading.setText("初始读数(单位:mm)");
                    mTvHeadOnWeir.setText("堰上水头(单位:mm)");
                    if (!TextUtils.isEmpty(initialReading)) {
                        initialReading = decimalFormat.format(Double.parseDouble(initialReading));
                        mEtInitialReading.setText(initialReading);
                    }
                    if (!TextUtils.isEmpty(headOnWeir)) {
                        headOnWeir = decimalFormat.format(Double.parseDouble(headOnWeir));
                        mEtHeadOnWeir.setText(headOnWeir);
                    }
                    break;

                case TURBIDITY_METER://浊度仪
                    mTvAlarmValue.setText("报警值(单位:m/s)");
                    mTvCorrectValue.setText("修正值(单位:m/s)");
                    break;

                case LUYAN_INCLINOMETER://倾角仪
                    mTvAlarmValue.setText("报警值(单位:°)");
                    if (!TextUtils.isEmpty(xAngle)) {
                        xAngle = decimalFormat.format(Double.parseDouble(xAngle));
                        mEtXAngle.setText(xAngle);
                    }
                    if (!TextUtils.isEmpty(yAngle)) {
                        yAngle = decimalFormat.format(Double.parseDouble(yAngle));
                        mEtYAngle.setText(yAngle);
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        sensorAddress = mEtModbusAddress.getText().toString().trim();
        triggerThreshold = mEtAlarmValue.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();

        measureLong = mEtMeasureLong.getText().toString().trim();

        initialReading = mEtInitialReading.getText().toString().trim();
        headOnWeir = mEtHeadOnWeir.getText().toString().trim();

        xAngle = mEtXAngle.getText().toString().trim();
        yAngle = mEtYAngle.getText().toString().trim();

        if (TextUtils.isEmpty(sensorAddress)) {
            ToastUtils.show("传感器地址不能为空!");
            mEtModbusAddress.requestFocus();
            return false;
        }
        if (!ValidateUtil.isInteger(sensorAddress) || Integer.parseInt(sensorAddress) <= 0) {
            ToastUtils.show("请输入正确的传感器地址!");
            mEtModbusAddress.requestFocus();
            return false;
        }

        int num = 0;
        for (String ss : addressList) {
            if (ss.equals(sensorAddress)) {
                num++;
            }
        }
        if (num >= 1) {
            ToastUtils.show("传感器地址不能重复!");
            mEtModbusAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("报警值不能为空!");
            mEtAlarmValue.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(triggerThreshold);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的报警值!");
            mEtAlarmValue.requestFocus();
            return false;
        }

        if (iotSensorType != IOTSensorType.LUYAN_INCLINOMETER) {
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
        }

        if (iotSensorType == IOTSensorType.INCLINOMETER) {//测斜仪
            if (TextUtils.isEmpty(measureLong)) {
                ToastUtils.show("测段长值不能为空!");
                mEtMeasureLong.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(measureLong);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测段长值!");
                mEtMeasureLong.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.WEIR) {//量水堰计
            if (TextUtils.isEmpty(initialReading)) {
                ToastUtils.show("初始读数不能为空!");
                mEtInitialReading.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(initialReading);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的初始读数!");
                mEtInitialReading.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(headOnWeir)) {
                ToastUtils.show("堰上水头不能为空!");
                mEtHeadOnWeir.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(headOnWeir);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的堰上水头!");
                mEtHeadOnWeir.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) {//倾角仪
            if (TextUtils.isEmpty(xAngle)) {
                ToastUtils.show("X轴初始值不能为空!");
                mEtXAngle.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(xAngle);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的X轴初始值!");
                mEtXAngle.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(yAngle)) {
                ToastUtils.show("Y轴初始值不能为空!");
                mEtYAngle.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(yAngle);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的Y轴初始值!");
                mEtYAngle.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void processSave() {
        if (externalSensorInfo == null)
            externalSensorInfo = new DasExternalSensorInfo();

        externalSensorInfo.setAddr(sensorAddress);
        externalSensorInfo.setType(iotSensorType.getCode());
        externalSensorInfo.setThreshold(triggerThreshold);
        externalSensorInfo.setCorrval(correctValue);
        externalSensorInfo.setSpacing(measureLong);
        externalSensorInfo.setLsycsds(initialReading);
        externalSensorInfo.setLsyysst(headOnWeir);
        externalSensorInfo.setInitvalx(xAngle);
        externalSensorInfo.setInitvaly(yAngle);

        Intent intent = new Intent();
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    private void setResult() {
        Intent intent = new Intent();
        mActivity.setResult(Activity.RESULT_CANCELED, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
        return false;
    }
}
