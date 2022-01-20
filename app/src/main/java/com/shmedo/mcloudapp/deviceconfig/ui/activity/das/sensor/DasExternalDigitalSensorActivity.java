package com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor;

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
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.CommonDigitalSensorInfo;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/14 <br/>
 * 描述：     Das数字传感器参数配置页面
 *
 * @deprecated 后面将用物联网指令模式取代
 */
public class DasExternalDigitalSensorActivity extends BaseActivity {
    private static final String SENSOR_ITEM_LIST = "sensor_item_list";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.et_modbus_address)
    EditText mEtModbusAddress;

    @BindView(R.id.tv_triggerThreshold)
    TextView mTvAlarmValue;

    @BindView(R.id.et_trigger_threshold)
    EditText mEtAlarmValue;


    @BindView(R.id.tv_correctionValue)
    TextView mTvCorrectValue;

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

    private SensorType sensorType;//传感器类型
    private Parcelable parcelableData;
    private ArrayList<String> addressList = new ArrayList<>();
    private String sensorAddress, triggerThreshold, correctValue;
    private String exValue1, exValue2, exValue3;


    public static void startActivityForResultByFragment(Fragment context, int requestCode, ArrayList<String> addressList, String sensorAddress, SensorType sensorType, Parcelable parcelable) {
        Intent intent = new Intent(context.getActivity(), DasExternalDigitalSensorActivity.class);
        intent.putStringArrayListExtra(SENSOR_ITEM_LIST, addressList);
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, sensorAddress);
        intent.putExtra(AppContants.Extras.SENSOR_TYPE, sensorType);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, parcelable);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_das_external_digital_sensor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("数字式传感器配置");
        parseIntent();
        initView();
        initValue();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;
        if (intent.getExtras().containsKey(SENSOR_ITEM_LIST)) {
            addressList = intent.getStringArrayListExtra(SENSOR_ITEM_LIST);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_ADDRESS)) {
            sensorAddress = intent.getStringExtra(AppContants.Extras.SENSOR_ADDRESS);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_TYPE)) {
            sensorType = (SensorType) intent.getSerializableExtra(AppContants.Extras.SENSOR_TYPE);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_PARAM)) {
            parcelableData = intent.getParcelableExtra(AppContants.Extras.SENSOR_PARAM);
        }
        if (!TextUtils.isEmpty(sensorAddress)) {
            addressList.remove(sensorAddress);
        }
        String sensorName = sensorType.getDescription();
        mToolbarTitle.setText(sensorName);
    }

    private void initView() {
        //测斜仪
        if (sensorType != null && sensorType == SensorType.INCLINOMETER) {
            extensionLayout1.setVisibility(View.VISIBLE);
        }
        //倾角仪
        if (sensorType != null && sensorType == SensorType.LUYAN_INCLINOMETER) {
            correctionLayout.setVisibility(View.GONE);
            extensionLayout1.setVisibility(View.VISIBLE);
            extensionLayout2.setVisibility(View.VISIBLE);
        }
        //量水堰计
        if (sensorType != null && sensorType == SensorType.WEIR) {
            extensionLayout1.setVisibility(View.VISIBLE);
            extensionLayout2.setVisibility(View.VISIBLE);
        }
        //静力水准
        if (sensorType != null && sensorType == SensorType.STATIC_LEVEL) {
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
            if (parcelableData != null) {
                CommonDigitalSensorInfo commonDigitalSensorInfo = (CommonDigitalSensorInfo) parcelableData;
                triggerThreshold = commonDigitalSensorInfo.getTriggerThreshold();
                correctValue = commonDigitalSensorInfo.getCorrectionValue();
                exValue1 = commonDigitalSensorInfo.getExValue1();
                exValue2 = commonDigitalSensorInfo.getExValue2();
                exValue3 = commonDigitalSensorInfo.getExValue3();
            }
            switch (sensorType) {
                case RAIN_GAUGE://雨量计
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    break;

                case WIRE_SHIFT://裂缝计
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    break;

                case SOIL_MOISTURE://管式含水率计
                    mTvAlarmValue.setText("报警值(单位:%rh)");
                    mTvCorrectValue.setText("修正值(单位:%rh)");
                    break;

                case INCLINOMETER://测斜仪
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    mTvExtension1.setText("测段长(单位:mm)");
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

                case INFRASOUND://次声仪
                    mTvAlarmValue.setText("报警值(单位:Hz)");
                    mTvCorrectValue.setText("修正值(单位:Hz)");
                    break;

                case STATIC_LEVEL://静力水准
                    mTvAlarmValue.setText("报警值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    mTvExtension1.setText("高程(单位:m)");
                    if (!TextUtils.isEmpty(exValue1)) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    break;

                case WEATHER_STATION://气象计
                    mTvAlarmValue.setText("报警值(单位:m/s)");
                    mTvCorrectValue.setText("修正值(单位:m/s)");
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
        if (view.getId() == R.id.btn_confirm) {
            processSave();
        }
    }

    private void processSave() {
        if (!checkValue()) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        Intent intent = getIntent();
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, sensorAddress);
        intent.putExtra(AppContants.Extras.SENSOR_TYPE, sensorType);
        switch (sensorType) {
            case RAIN_GAUGE://雨量计
            case WIRE_SHIFT://裂缝计
            case SOIL_MOISTURE://管式含水率计
            case INCLINOMETER://测斜仪
            case ULTRASONIC_LEVEL_GAUGE://超声波物位计
            case RADAR_LEVEL_GAUGE://雷达物位计
            case INFRASOUND://次声仪
            case STATIC_LEVEL://静力水准
            case WEATHER_STATION://气象计
            {
                CommonDigitalSensorInfo commonDigitalSensorInfo = new CommonDigitalSensorInfo();
                commonDigitalSensorInfo.setTriggerThreshold(triggerThreshold);
                commonDigitalSensorInfo.setCorrectionValue(correctValue);
                if (!TextUtils.isEmpty(exValue1))
                    commonDigitalSensorInfo.setExValue1(exValue1);
                if (!TextUtils.isEmpty(exValue2))
                    commonDigitalSensorInfo.setExValue2(exValue2);
                if (!TextUtils.isEmpty(exValue3))
                    commonDigitalSensorInfo.setExValue3(exValue3);
                intent.putExtra(AppContants.Extras.SENSOR_PARAM, commonDigitalSensorInfo);
            }
            break;
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean checkValue() {
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

        if (sensorType == SensorType.INCLINOMETER) {
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

        if (sensorType == SensorType.STATIC_LEVEL) {
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
}
