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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;

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

    @BindView(R.id.et_trigger_threshold)
    EditText mEtAlarmValue;

    @BindView(R.id.et_modbus_address)
    EditText mEtModbusAddress;

    @BindView(R.id.tv_correctionValue)
    TextView mTvCorrectValue;//修正值

    @BindView(R.id.et_revised)
    EditText mEtCorrectValue;

    @BindView(R.id.correction_layout)
    ViewGroup correctionLayout;

    //扩展字段
    @BindView(R.id.tv_extension1)
    TextView mTvExtension1;

    @BindView(R.id.et_extension1)
    EditText mEtExtension1;

    @BindView(R.id.extension_layout1)
    ViewGroup extensionLayout1;

    //扩展字段
    @BindView(R.id.tv_extension2)
    TextView mTvExtension2;

    @BindView(R.id.et_extension2)
    EditText mEtExtension2;

    @BindView(R.id.extension_layout2)
    ViewGroup extensionLayout2;

    //扩展字段
    @BindView(R.id.tv_extension3)
    TextView mTvExtension3;

    @BindView(R.id.et_extension3)
    EditText mEtExtension3;

    @BindView(R.id.extension_layout3)
    ViewGroup extensionLayout3;

    private DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private IOTSensorType iotSensorType;//传感器类型
    private ArrayList<String> addressList = new ArrayList<>();
    private DasExternalSensorInfo externalSensorInfo;
    private String sensorAddress, triggerThreshold, correctValue;
    private String exValue1, exValue2, exValue3;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        initValue();
    }

    private void setView() {
        //测斜仪
        if (iotSensorType != null && iotSensorType == IOTSensorType.INCLINOMETER) {
            extensionLayout1.setVisibility(View.VISIBLE);
        }
        //倾角仪
        if (iotSensorType != null && iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) {
            correctionLayout.setVisibility(View.GONE);
            extensionLayout1.setVisibility(View.VISIBLE);
            extensionLayout2.setVisibility(View.VISIBLE);
        }
        //量水堰计
        if (iotSensorType != null && iotSensorType == IOTSensorType.WEIR) {
            extensionLayout1.setVisibility(View.VISIBLE);
            extensionLayout2.setVisibility(View.VISIBLE);
        }
        //静力水准
        if (iotSensorType != null && iotSensorType == IOTSensorType.STATIC_LEVEL) {
            extensionLayout1.setVisibility(View.VISIBLE);
        }
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtExtension1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtExtension2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtExtension3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void initValue() {
        try {
            if (externalSensorInfo == null) {
                externalSensorInfo = new DasExternalSensorInfo();
            }
            sensorAddress = externalSensorInfo.getAddr();
            triggerThreshold = externalSensorInfo.getThreshold();
            correctValue = externalSensorInfo.getCorrval();
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
                    mTvExtension1.setText("测段长(单位:mm)");
                    exValue1 = externalSensorInfo.getSpacing();
                    if (!TextUtils.isEmpty(exValue1)) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
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

                case STATIC_LEVEL://静力水准
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    mTvExtension1.setText("高程(单位:m)");
                    exValue1 = externalSensorInfo.getTubealti();
                    if (!TextUtils.isEmpty(exValue1)) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    break;

                case WEATHER_STATION://气象计
                    mTvAlarmValue.setText("报警值(单位:m/s)");
                    mTvCorrectValue.setText("修正值(单位:m/s)");
                    break;

                case WEIR://量水堰计
                    mTvAlarmValue.setText("报警值(单位:m³/s)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    mTvExtension1.setText("初始读数(单位:mm)");
                    mTvExtension2.setText("堰上水头(单位:mm)");
                    exValue1 = externalSensorInfo.getLsycsds();
                    exValue2 = externalSensorInfo.getLsyysst();
                    if (!TextUtils.isEmpty(exValue1)) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    if (!TextUtils.isEmpty(exValue2)) {
                        exValue2 = decimalFormat.format(Double.parseDouble(exValue2));
                        mEtExtension2.setText(exValue2);
                    }
                    break;

                case TURBIDITY_METER://浊度仪
                    mTvAlarmValue.setText("报警值(单位:m/s)");
                    mTvCorrectValue.setText("修正值(单位:m/s)");
                    break;

                case LUYAN_INCLINOMETER://倾角仪
                    mTvAlarmValue.setText("报警值(单位:°)");
                    mTvExtension1.setText("X轴角度(°)");
                    mTvExtension2.setText("Y轴角度(°)");
                    exValue1 = externalSensorInfo.getInitvalx();
                    exValue2 = externalSensorInfo.getInitvaly();
                    if (!TextUtils.isEmpty(exValue1)) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    if (!TextUtils.isEmpty(exValue2)) {
                        exValue2 = decimalFormat.format(Double.parseDouble(exValue2));
                        mEtExtension2.setText(exValue2);
                    }
                    break;
            }
            mEtModbusAddress.setText(sensorAddress);
            if (!TextUtils.isEmpty(triggerThreshold)) {
                triggerThreshold = decimalFormat.format(Double.parseDouble(triggerThreshold));
                mEtAlarmValue.setText(triggerThreshold);
            }
            if (!TextUtils.isEmpty(correctValue)) {
                correctValue = decimalFormat.format(Double.parseDouble(correctValue));
                mEtCorrectValue.setText(correctValue);
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
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
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
        exValue1 = mEtExtension1.getText().toString().trim();
        exValue2 = mEtExtension2.getText().toString().trim();
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
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("测段长值不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测段长值!");
                mEtExtension1.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) {//倾角仪
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("X轴初始值不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的X轴初始值!");
                mEtExtension1.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(exValue2)) {
                ToastUtils.show("Y轴初始值不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的Y轴初始值!");
                mEtExtension2.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.WEIR) {//量水堰计
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("初始读数不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的初始读数!");
                mEtExtension1.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(exValue2)) {
                ToastUtils.show("堰上水头不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的堰上水头!");
                mEtExtension2.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.STATIC_LEVEL) {//静力水准
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("高程值不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的高程值!");
                mEtExtension1.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void processSave() {
        if (externalSensorInfo == null)
            externalSensorInfo = new DasExternalSensorInfo();

        externalSensorInfo.setType(iotSensorType.getCode());
        externalSensorInfo.setAddr(sensorAddress);
        externalSensorInfo.setThreshold(triggerThreshold);
        externalSensorInfo.setCorrval(correctValue);
        if (iotSensorType == IOTSensorType.INCLINOMETER) {//测斜仪
            externalSensorInfo.setSpacing(exValue1);
        }
        if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) {//倾角仪
            externalSensorInfo.setInitvalx(exValue1);
            externalSensorInfo.setInitvaly(exValue2);
        }
        if (iotSensorType == IOTSensorType.WEIR) {//量水堰计
            externalSensorInfo.setLsycsds(exValue1);
            externalSensorInfo.setLsyysst(exValue2);
        }
        if (iotSensorType == IOTSensorType.STATIC_LEVEL) {//静力水准
            externalSensorInfo.setTubealti(exValue1);
        }
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
