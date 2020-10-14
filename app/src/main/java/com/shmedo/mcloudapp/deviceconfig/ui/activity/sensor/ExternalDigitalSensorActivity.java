package com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;

import butterknife.BindView;

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

    private String address, triggerThreshold, correctValue, measureLong;
    private SensorType sensorType;//传感器类型

    @Override
    protected int getLayoutId() {
        return R.layout.activity_external_digital_sensor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("采集器配置");
//        parseIntent();
//        initFragment();
    }


}
