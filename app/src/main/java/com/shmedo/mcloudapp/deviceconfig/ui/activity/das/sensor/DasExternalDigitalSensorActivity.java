package com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

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

    @BindView(R.id.iv_correction)
    ImageView mIvCorrection;

    @BindView(R.id.correction_layout)
    ViewGroup correctionLayout;

    //扩展字段
    @BindView(R.id.tv_extension1)
    TextView mTvExtension1;

    @BindView(R.id.et_extension1)
    EditText mEtExtension1;

    @BindView(R.id.iv_extension1)
    ImageView mIvExtension1;

    @BindView(R.id.extension_layout1)
    ViewGroup extensionLayout1;

    //扩展字段
    @BindView(R.id.tv_extension2)
    TextView mTvExtension2;

    @BindView(R.id.et_extension2)
    EditText mEtExtension2;

    @BindView(R.id.iv_extension2)
    ImageView mIvExtension2;

    @BindView(R.id.extension_layout2)
    ViewGroup extensionLayout2;

    //扩展字段
    @BindView(R.id.tv_extension3)
    TextView mTvExtension3;

    @BindView(R.id.et_extension3)
    EditText mEtExtension3;

    @BindView(R.id.iv_extension3)
    ImageView mIvExtension3;

    @BindView(R.id.extension_layout3)
    ViewGroup extensionLayout3;

    private DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private SensorType sensorType;//传感器类型
    private Parcelable parcelableData;
    private ArrayList<String> addressList = new ArrayList<>();
    private String sensorAddress, triggerThreshold, correctValue;
    private String exValue1, exValue2, exValue3;


    public static void startActivityForResultByFragment(Context context, ActivityResultLauncher<Intent> launcher, ArrayList<String> addressList, String sensorAddress, SensorType sensorType, Parcelable parcelable) {
        Intent intent = new Intent(context, DasExternalDigitalSensorActivity.class);
        intent.putStringArrayListExtra(SENSOR_ITEM_LIST, addressList);
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, sensorAddress);
        intent.putExtra(AppContants.Extras.SENSOR_TYPE, sensorType);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, parcelable);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
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
                case WIRE_SHIFT://裂缝计
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    break;

                case SOIL_MOISTURE://管式含水率计
                    mTvAlarmValue.setText("触发值(单位:%rh)");
                    mTvCorrectValue.setText("修正值(单位:%rh)");
                    break;

                case INCLINOMETER://测斜仪
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("测段长(单位:mm)");
                    if (!TextUtils.isEmpty(exValue1)) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    break;

                case ULTRASONIC_LEVEL_GAUGE://超声波物位计
                case RADAR_LEVEL_GAUGE://雷达物位计
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("安装高程(单位:m)");
                    break;

                case INFRASOUND://次声仪
                    mTvAlarmValue.setText("触发值(单位:Hz)");
                    mTvCorrectValue.setText("修正值(单位:Hz)");
                    break;

                case WEIR://量水堰计
                    mTvAlarmValue.setText("触发值(单位:m³/s)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    extensionLayout2.setVisibility(View.VISIBLE);
                    mIvCorrection.setVisibility(View.VISIBLE);
                    mIvExtension1.setVisibility(View.VISIBLE);
                    mIvExtension2.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("初始读数(单位:mm)");
                    mTvExtension2.setText("初始堰上水头(单位:mm)");
                    if (!TextUtils.isEmpty(exValue1) && !exValue1.equals("NullKey")) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    if (!TextUtils.isEmpty(exValue2) && !exValue2.equals("NullKey")) {
                        exValue2 = decimalFormat.format(Double.parseDouble(exValue2));
                        mEtExtension2.setText(exValue2);
                    }
                    break;

                case STATIC_LEVEL://静力水准
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("高程(单位:m)");
                    if (!TextUtils.isEmpty(exValue1)) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    break;

                case WEATHER_STATION://气象计
                    mTvAlarmValue.setText("触发值(单位:m/s)");
                    mTvCorrectValue.setText("修正值(单位:m/s)");
                    break;

                case DIGITAL_WATER_LEVEL_GAUGE://数字式水位计
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    extensionLayout2.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("高程(单位:m)");
                    mTvExtension2.setText("绳长(单位:m)");
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
            if (!TextUtils.isEmpty(sensorAddress)) {
                mEtModbusAddress.setText(sensorAddress);
            }
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

    @OnClick({R.id.iv_correction, R.id.iv_extension1, R.id.iv_extension2, R.id.iv_extension3, R.id.btn_confirm})
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.btn_confirm) {
            processSave();
            
        } else if (id == R.id.iv_correction) {
            if (sensorType == SensorType.WEIR) //量水堰计修正值
                showTipDialog("修正浮子高度  ");

        } else if (id == R.id.iv_extension1) {
            if (sensorType == SensorType.LUYAN_INCLINOMETER) //倾角仪
                showTipDialog("初始值大于 360，设备将自动计算");
            else if (sensorType == SensorType.WEIR) //量水堰计初始读数
                showTipDialog("当初始读数设置值小于 0  时，设备将自动计算初始值");

        } else if (id == R.id.iv_extension2) {
            if (sensorType == SensorType.LUYAN_INCLINOMETER) //倾角仪
                showTipDialog("初始值大于 360，设备将自动计算");
            else if (sensorType == SensorType.WEIR) //量水堰计初始堰上水头
                showTipDialog("当水经堰顶点流出时，设置值为堰顶点到水面的距离；否则，设置值为堰顶点到浮子距离的负值；");

        } else if (id == R.id.iv_extension3) {
            if (sensorType == SensorType.LUYAN_INCLINOMETER) //倾角仪
                showTipDialog("初始值大于 360，设备将自动计算");
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

        if (sensorType == SensorType.INCLINOMETER) {//测斜仪
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

        if (sensorType == SensorType.WEIR) {//量水堰计
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
                ToastUtils.show("初始堰上水头不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的初始堰上水头!");
                mEtExtension2.requestFocus();
                return false;
            }
        }

        if (sensorType == SensorType.STATIC_LEVEL) {//静力水准
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

        if (sensorType == SensorType.DIGITAL_WATER_LEVEL_GAUGE) {//数字式水位计
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

            if (TextUtils.isEmpty(exValue2)) {
                ToastUtils.show("绳长不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的绳长!");
                mEtExtension2.requestFocus();
                return false;
            }
        }

        return true;
    }
}
