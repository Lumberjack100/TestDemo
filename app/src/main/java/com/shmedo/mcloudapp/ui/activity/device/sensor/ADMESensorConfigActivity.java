package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.views.ClearEditText;

import java.util.UUID;

import butterknife.BindView;
import timber.log.Timber;

public class ADMESensorConfigActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.collector_address_layout)
    View collectorAddressLayout;

    @BindView(R.id.collector_collect_interval_layout)
    View collectorCollectIntervalLayout;

    @BindView(R.id.collector_solution_interval_layout)
    View collectorSolutionIntervalLayout;

    @BindView(R.id.communication_module_sleep_interval_layout)
    View communicationModuleSleepIntervalLayout;

    @BindView(R.id.sensor_type_layout)
    View sensorTypeLayout;

    @BindView(R.id.sensor_address_layout)
    View sensorAddressLayout;

    @BindView(R.id.sensor_correction_value_layout)
    View sensorCorrectionValueLayout;

    @BindView(R.id.btn_confirm_complete)
    Button btnConfirm;

    private ImageView mIvCollectorAddress, mIvCollectorCollectInterval, mIvCollectorSolutionInterval, mIvCommunicationModuleSleepInterval, mIvSensorType, mIvSensorAddress, mIvSensorCorrectionValue;

    private ClearEditText mEtCollectorAddress, mEtCollectorCollectInterval, mEtCollectorSolutionInterval, mEtCommunicationModuleSleepInterval, mEtSensorType, mEtSensorAddress, mEtSensorCorrectionValue;

    private String collectorAddress, collectorCollectInterval, collectorSolutionInterval, communicationModuleSleepInterval, sensorType, sensorAddress, sensorCorrectionValue;

    private MdBluetoothManager mdBluetoothManager;

    private String dagConfigInfo;//DAG 采集器配置指令


    public static void startActivity(Context context, String configInfo) {
        Intent intent = new Intent(context, ADMESensorConfigActivity.class);
        intent.putExtra(Extras.ADME_SENSOR_CONFIG_INFO, configInfo);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_adme_sensor_config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        parseIntent();
        initData();
    }


    private void initView() {
        mToolbarTitle.setText("采集器参数设置");

        ((TextView) collectorAddressLayout.findViewById(R.id.itemNameTV)).setText("采集器地址");
        mIvCollectorAddress = collectorAddressLayout.findViewById(R.id.itemTipIV);
        mEtCollectorAddress = collectorAddressLayout.findViewById(R.id.itemValueET);
        mEtCollectorAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCollectorAddress.setHint("请输入正数...");
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        ((TextView) collectorCollectIntervalLayout.findViewById(R.id.itemNameTV)).setText("采集器采集间隔（s）");
        mIvCollectorCollectInterval = collectorCollectIntervalLayout.findViewById(R.id.itemTipIV);
        mEtCollectorCollectInterval = collectorCollectIntervalLayout.findViewById(R.id.itemValueET);
        mEtCollectorCollectInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCollectorCollectInterval.setHint("请输入正数...");
        mEtCollectorCollectInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        ((TextView) collectorSolutionIntervalLayout.findViewById(R.id.itemNameTV)).setText("采集器解算间隔（s）");
        mIvCollectorSolutionInterval = collectorSolutionIntervalLayout.findViewById(R.id.itemTipIV);
        mEtCollectorSolutionInterval = collectorSolutionIntervalLayout.findViewById(R.id.itemValueET);
        mEtCollectorSolutionInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCollectorSolutionInterval.setHint("请输入正数...");
        mEtCollectorSolutionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        ((TextView) communicationModuleSleepIntervalLayout.findViewById(R.id.itemNameTV)).setText("通讯模块休眠间隔（s）");
        mIvCommunicationModuleSleepInterval = communicationModuleSleepIntervalLayout.findViewById(R.id.itemTipIV);
        mEtCommunicationModuleSleepInterval = communicationModuleSleepIntervalLayout.findViewById(R.id.itemValueET);
        mEtCommunicationModuleSleepInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCommunicationModuleSleepInterval.setHint("请输入正数...");
        mEtCommunicationModuleSleepInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCommunicationModuleSleepInterval.setText("5");

        ((TextView) sensorTypeLayout.findViewById(R.id.itemNameTV)).setText("传感器类型");
        mIvSensorType = sensorTypeLayout.findViewById(R.id.itemTipIV);
        mEtSensorType = sensorTypeLayout.findViewById(R.id.itemValueET);
        mEtSensorType.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtSensorType.setHint("请输入正数...");
        mEtSensorType.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        ((TextView) sensorAddressLayout.findViewById(R.id.itemNameTV)).setText("传感器地址");
        mIvSensorAddress = sensorAddressLayout.findViewById(R.id.itemTipIV);
        mEtSensorAddress = sensorAddressLayout.findViewById(R.id.itemValueET);
        mEtSensorAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtSensorAddress.setHint("请输入正数...");
        mEtSensorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        ((TextView) sensorCorrectionValueLayout.findViewById(R.id.itemNameTV)).setText("传感器修正值（mm）");
        mIvSensorCorrectionValue = sensorCorrectionValueLayout.findViewById(R.id.itemTipIV);
        mEtSensorCorrectionValue = sensorCorrectionValueLayout.findViewById(R.id.itemValueET);
        mEtSensorCorrectionValue.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtSensorCorrectionValue.setHint("请输入两位正小数...");
        mEtSensorCorrectionValue.setText("0.00");
        mEtSensorCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});


        mIvCollectorAddress.setId(R.id.collector_address);
        mIvCollectorCollectInterval.setId(R.id.collector_collect_interval);
        mIvCollectorSolutionInterval.setId(R.id.collector_solution_interval);
        mIvCommunicationModuleSleepInterval.setId(R.id.communication_module_sleep_interval);
        mIvSensorType.setId(R.id.sensor_type);
        mIvSensorAddress.setId(R.id.sensor_address);
        mIvSensorCorrectionValue.setId(R.id.sensor_correction_value);

        mIvCollectorAddress.setOnClickListener(this);
        mIvCollectorCollectInterval.setOnClickListener(this);
        mIvCollectorSolutionInterval.setOnClickListener(this);
        mIvCommunicationModuleSleepInterval.setOnClickListener(this);
        mIvSensorType.setOnClickListener(this);
        mIvSensorAddress.setOnClickListener(this);
        mIvSensorCorrectionValue.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }


    private void parseIntent() {
        mdBluetoothManager = MdBluetoothManager.getInstance();

        Intent intent = getIntent();
        if (intent.getExtras().containsKey(Extras.ADME_SENSOR_CONFIG_INFO)) {
            dagConfigInfo = intent.getStringExtra(Extras.ADME_SENSOR_CONFIG_INFO);
        }
    }


    private void initData() {
        if (TextUtils.isEmpty(dagConfigInfo)) {
            Timber.e("dagConfigInfo 为空或者null");
            return;
        }

        if (dagConfigInfo.length() < CommandResult.RESULT_MIN_LENGTH) {
            Timber.e("dagConfigInfo 长度过短:" + dagConfigInfo);
            return;
        }

        if (!dagConfigInfo.startsWith(CommandResult.COMMAND_RESULT_HEADER)) {
            Timber.e("dagConfigInfo 格式错误:" + dagConfigInfo);
            return;
        }

        String[] cmdArray = dagConfigInfo.replace("\r\n", "").split(",");
        if (cmdArray.length < 8) {
            Timber.e("dagConfigInfo 格式错误:" + dagConfigInfo);
            return;
        }

        mEtCollectorAddress.setText(cmdArray[1]);
        mEtCollectorCollectInterval.setText(cmdArray[2]);
        mEtCollectorSolutionInterval.setText(cmdArray[3]);
        mEtCommunicationModuleSleepInterval.setText(cmdArray[4]);
        mEtSensorType.setText(cmdArray[5]);
        mEtSensorAddress.setText(cmdArray[6]);

        try {
            mEtSensorCorrectionValue.setText( String.format("%.2f", cmdArray[7]));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collector_address:
                showTipDialog(getResources().getString(R.string.dag_collector_addres));
                break;

            case R.id.collector_collect_interval:
                showTipDialog(getResources().getString(R.string.dag_collector_collect_interval));
                break;

            case R.id.collector_solution_interval:
                showTipDialog(getResources().getString(R.string.dag_collector_solution_interval));
                break;

            case R.id.communication_module_sleep_interval:
                showTipDialog(getResources().getString(R.string.dag_communication_module_sleep_interval));
                break;

            case R.id.sensor_type:
                showTipDialog(getResources().getString(R.string.dag_sensor_type));
                break;

            case R.id.sensor_address:
                showTipDialog(getResources().getString(R.string.dag_sensor_address));
                break;

            case R.id.sensor_correction_value:
                showTipDialog(getResources().getString(R.string.dag_sensor_correction_value));
                break;

            case R.id.btn_confirm_complete:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show("设备已断开连接，暂无法进行设置");
                    return;
                }
                doConfirm();
                break;
        }
    }


    private void doConfirm() {
        collectorAddress = mEtCollectorAddress.getText().toString().trim();
        collectorCollectInterval = mEtCollectorCollectInterval.getText().toString().trim();
        collectorSolutionInterval = mEtCollectorSolutionInterval.getText().toString().trim();
        communicationModuleSleepInterval = mEtCommunicationModuleSleepInterval.getText().toString().trim();
        sensorType = mEtSensorType.getText().toString().trim();
        sensorAddress = mEtSensorAddress.getText().toString().trim();
        sensorCorrectionValue = mEtSensorCorrectionValue.getText().toString().trim();

        if (TextUtils.isEmpty(collectorAddress)) {
            ToastUtils.show("采集器地址不能为空");
            return;
        }

        if (TextUtils.isEmpty(collectorCollectInterval)) {
            ToastUtils.show("采集器采集间隔不能为空");
            return;
        }

        if (TextUtils.isEmpty(collectorSolutionInterval)) {
            ToastUtils.show("采集器解算间隔不能为空");
            return;
        }

        if (TextUtils.isEmpty(communicationModuleSleepInterval)) {
            ToastUtils.show("通讯模块休眠间隔不能为空");
            return;
        }

        if (TextUtils.isEmpty(sensorType)) {
            ToastUtils.show("传感器类型不能为空");
            return;
        }

        if (TextUtils.isEmpty(sensorAddress)) {
            ToastUtils.show("传感器地址不能为空");
            return;
        }

        if (TextUtils.isEmpty(sensorCorrectionValue)) {
            ToastUtils.show("传感器修正值不能为空");
            return;
        }

        int address = Integer.parseInt(collectorAddress);
        if (address < 0 || address >= 255) {
            ToastUtils.show("采集器地址输入有误");
            return;
        }

        if (Integer.parseInt(collectorCollectInterval) < 0) {
            ToastUtils.show("采集器采集间隔必须输入正数");
            return;
        }

        if (Integer.parseInt(collectorSolutionInterval) < 0) {
            ToastUtils.show("采集器解算间隔必须输入正数");
            return;
        }

        if (Integer.parseInt(collectorSolutionInterval) < Integer.parseInt(collectorCollectInterval)) {
            ToastUtils.show("采集器解算间隔必须大于等于采集间隔");
            return;
        }

        if (Integer.parseInt(communicationModuleSleepInterval) < 0) {
            ToastUtils.show("通讯模块休眠间隔必须输入正数");
            return;
        }

        if (Integer.parseInt(sensorType) < 0) {
            ToastUtils.show("传感器类型编号必须输入正数");
            return;
        }

        address = Integer.parseInt(sensorAddress);
        if (address < 0 || address >= 255) {
            ToastUtils.show("传感器地址输入有误");
            return;
        }

        if (Double.parseDouble(sensorCorrectionValue) < 0) {
            ToastUtils.show("传感器修正值必须输入正数");
            return;
        }


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("##7001,");
        stringBuilder.append(collectorAddress + ",");
        stringBuilder.append(collectorCollectInterval + ",");
        stringBuilder.append(collectorSolutionInterval + ",");
        stringBuilder.append(communicationModuleSleepInterval + ",");
        stringBuilder.append(sensorType + ",");
        stringBuilder.append(sensorAddress + ",");
        stringBuilder.append(sensorCorrectionValue + "\r\n");

        String cmdStr = String.valueOf(stringBuilder);
        Message msg = new Message(UUID.randomUUID().toString(), cmdStr, true);
        mdBluetoothManager.writeMessage(msg);
        Timber.d("发送DAG 配置指令===" + cmdStr);
//        finish();
    }
}
