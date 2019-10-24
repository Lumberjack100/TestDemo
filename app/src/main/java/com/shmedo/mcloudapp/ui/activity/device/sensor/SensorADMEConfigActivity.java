package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.ToastUtil;

import butterknife.BindView;

public class SensorADMEConfigActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.collector_channel_layout)
    View collector_channel_layout;

    @BindView(R.id.sensor_calibration_value_layout)
    View sensorCalibrationValueLayout;

    @BindView(R.id.collector_standby_duration_layout)
    View collectorStandbyDurationLayout;

    @BindView(R.id.btn_confirm_complete)
    Button btnConfirm;

    private ImageView collectorChannelIV, sensorCalibrationValueIV, collectorStandbyDurationIV;

    private EditText sensorCalibrationValueET, collectorStandbyDurationET;

    private Spinner spinner;

    private ArrayAdapter<String> dataAdapter;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SensorADMEConfigActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_sensor_adme_config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initAdapter();
        initData();
    }


    private void initView() {
        mToolbarTitle.setText("传感器参数配置");

        ((TextView) collector_channel_layout.findViewById(R.id.itemNameTV)).setText("采集器信道号");
        collectorChannelIV = collector_channel_layout.findViewById(R.id.itemTipIV);
        spinner = collector_channel_layout.findViewById(R.id.spinner);

        ((TextView) sensorCalibrationValueLayout.findViewById(R.id.itemNameTV)).setText("传感器标定值");
        sensorCalibrationValueIV = sensorCalibrationValueLayout.findViewById(R.id.itemTipIV);
        sensorCalibrationValueET = sensorCalibrationValueLayout.findViewById(R.id.itemValueET);
        sensorCalibrationValueET.setInputType(InputType.TYPE_CLASS_NUMBER);
        sensorCalibrationValueET.setHint("请输入...");

        ((TextView) collectorStandbyDurationLayout.findViewById(R.id.itemNameTV)).setText("采集器待机时长");
        collectorStandbyDurationIV = collectorStandbyDurationLayout.findViewById(R.id.itemTipIV);
        collectorStandbyDurationET = collectorStandbyDurationLayout.findViewById(R.id.itemValueET);
        collectorStandbyDurationET.setInputType(InputType.TYPE_CLASS_NUMBER);
        collectorStandbyDurationET.setHint("请输入...");

        collectorChannelIV.setId(R.id.collector_channel);
        sensorCalibrationValueIV.setId(R.id.sensor_calibration_value);
        collectorStandbyDurationIV.setId(R.id.collector_standby_suration);

        collectorChannelIV.setOnClickListener(this);
        sensorCalibrationValueIV.setOnClickListener(this);
        collectorStandbyDurationIV.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }


    private void initAdapter(){
        String[] debugData = getResources().getStringArray(R.array.collector_channel);
//        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, debugData);
        dataAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, debugData);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String result = spinner.getSelectedItem().toString().replace("mm", "");

                ToastUtil.showShortToast(result);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void initData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collector_channel:

                ToastUtil.showShortToast("1111");
                break;

            case R.id.sensor_calibration_value:

                ToastUtil.showShortToast("2222");
                break;

            case R.id.collector_standby_suration:

                ToastUtil.showShortToast("3333");
                break;

        }
    }
}
