package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.MqttConfigInfoSub;
import com.shmedo.mcloudapp.inter.MQttOnClickListener;
import com.shmedo.mcloudapp.views.ClearEditText;
import timber.log.Timber;

/**
 * mqtt  dialog
 */
public class MqttConfigDialog implements IDialogOpt<MqttConfigInfoSub>{

    private Dialog dialog;
    private View contentView;
    private MqttConfigInfoSub mqttConfigInfoSub;

    private MQttOnClickListener mQttOnClickListener;
    private String link;

    private Context context;

    private ArrayAdapter<String> protocolAdapter;
    private ArrayAdapter<String> registerAdapter;

    private String protocolItem,registerPlatformItem;

    public MqttConfigDialog(Context context, String link) {
        this.context = context;
        this.link = link;
    }


    @Override
    public Dialog getDialog() {
        dialog = new Dialog(context, R.style.dialog_bottom_full);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER_VERTICAL);
        window.setWindowAnimations(R.style.share_animation);
        contentView = View.inflate(context, R.layout.dialog_mqtt_config, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    @Override
    public void initData(MqttConfigInfoSub info) {
        mqttConfigInfoSub = info;
        TextView tvTitle = contentView.findViewById(R.id.tv_title);
        final Spinner communicationProtocol = contentView.findViewById(R.id.communication_protocol);
        final ClearEditText dataPlatformAddress = contentView.findViewById(R.id.cet_service_address);
        Spinner spRegistrationPlatform = contentView.findViewById(R.id.sp_registration_platform);
        final LinearLayout llRegisterPlatform = contentView.findViewById(R.id.ll_register_platform);
        ClearEditText cetAppKey = contentView.findViewById(R.id.cet_app_Key);
        final LinearLayout llAppKey = contentView.findViewById(R.id.ll_app_key);
        final ClearEditText cetRegisterAddress = contentView.findViewById(R.id.cet_register_address);
        final LinearLayout llRegisterAddress = contentView.findViewById(R.id.ll_register_address);
        final ClearEditText cetKeepAliveValue = contentView.findViewById(R.id.cet_KeepAlive_value);
        final LinearLayout llKeepAlive = contentView.findViewById(R.id.ll_keep_alive);
        final ClearEditText cetDeviceSn = contentView.findViewById(R.id.cet_device_sn);
        final ClearEditText cetProductId = contentView.findViewById(R.id.cet_product_id);
        final ClearEditText cetRegistrationCode = contentView.findViewById(R.id.cet_registration_code);
        final LinearLayout llDeviceConfig = contentView.findViewById(R.id.ll_device_config);
        final ClearEditText cetMqttDeviceId = contentView.findViewById(R.id.cet_mqtt_device_id);
        final ClearEditText cetMqttUsername = contentView.findViewById(R.id.cet_mqtt_username) ;
        final ClearEditText cetMqttPassword = contentView.findViewById(R.id.cet_mqtt_password);
        final LinearLayout llMqttConfig = contentView.findViewById(R.id.ll_mqtt_config);
        TextView mqttCancel = contentView.findViewById(R.id.mqtt_cancel);
        TextView mqttSave = contentView.findViewById(R.id.mqtt_save);
        //通讯协议
        String[] protocolData = context.getResources().getStringArray(R.array.communication_protocol);
        protocolAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, protocolData);
        protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        communicationProtocol.setAdapter(protocolAdapter);
        communicationProtocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item){
                    case "MDM协议":
                        protocolItem = "2";
                        llAppKey.setVisibility(View.GONE);
                        llRegisterPlatform.setVisibility(View.GONE);
                        llRegisterAddress.setVisibility(View.GONE);
                        llDeviceConfig.setVisibility(View.GONE);
                        llKeepAlive.setVisibility(View.GONE);
                        llMqttConfig.setVisibility(View.GONE);
                        llKeepAlive.setVisibility(View.GONE);
                        break;
                    case "MQTT自动注册":
                        protocolItem = "4";
                        llRegisterPlatform.setVisibility(View.VISIBLE);
                        llRegisterAddress.setVisibility(View.VISIBLE);
                        llDeviceConfig.setVisibility(View.VISIBLE);
                        llMqttConfig.setVisibility(View.GONE);
                        llKeepAlive.setVisibility(View.VISIBLE);
                        break;
                    case "MQTT手动注册":
                        protocolItem = "5";
                        llAppKey.setVisibility(View.GONE);
                        llRegisterPlatform.setVisibility(View.GONE);
                        llRegisterAddress.setVisibility(View.GONE);
                        llDeviceConfig.setVisibility(View.GONE);
                        llMqttConfig.setVisibility(View.VISIBLE);
                        llKeepAlive.setVisibility(View.VISIBLE);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //注册平台
        String[] registerData = context.getResources().getStringArray(R.array.register);
        registerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, registerData);
        registerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegistrationPlatform.setAdapter(registerAdapter);
        spRegistrationPlatform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item){
                    case "地大平台":
                        registerPlatformItem = "0";
                        llAppKey.setVisibility(View.GONE);
                        break;
                    case "成都理工平台":
                        registerPlatformItem = "1";
                        llAppKey.setVisibility(View.GONE);
                        break;
                    case "米度平台":
                        registerPlatformItem = "2";
                        llAppKey.setVisibility(View.VISIBLE);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //配置参数
        tvTitle.setText("链路"+link+"配置");
        switch (mqttConfigInfoSub.getCommunicationProtocol()){
            case "2":
                communicationProtocol.setSelection(0);
                break;
            case "4":
                communicationProtocol.setSelection(1);
                break;
            case "5":
                communicationProtocol.setSelection(2);
                break;
        }
        dataPlatformAddress.setText(mqttConfigInfoSub.getDataPlatformAddress());
        cetKeepAliveValue.setText(mqttConfigInfoSub.getKeepAliveValue());
        cetDeviceSn.setText(mqttConfigInfoSub.getDeviceSn());
        cetProductId.setText(mqttConfigInfoSub.getProductId());
        cetRegistrationCode.setText(mqttConfigInfoSub.getRegistrationCode());
        switch (mqttConfigInfoSub.getRegistrationPlatform()){
            case "0"://地大平台
                spRegistrationPlatform.setSelection(0);
                break;
            case "1"://成都理工平台
                spRegistrationPlatform.setSelection(1);
                break;
            case "2"://米度平台
                spRegistrationPlatform.setSelection(2);
                break;
            default:
                spRegistrationPlatform.setSelection(0);
                break;
        }
        cetRegisterAddress.setText(mqttConfigInfoSub.getRegistrationPlatformAddress());
        cetAppKey.setText(mqttConfigInfoSub.getAppKey());
        cetMqttDeviceId.setText(mqttConfigInfoSub.getMqttDeviceId());
        cetMqttUsername.setText(mqttConfigInfoSub.getMqttUsername());
        cetMqttPassword.setText(mqttConfigInfoSub.getMqttPassword());



        if (mQttOnClickListener != null) {
            mqttSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mqttConfigInfoSub.setCommunicationProtocol(protocolItem);
                    mqttConfigInfoSub.setDataPlatformAddress(dataPlatformAddress.getText()!=null?dataPlatformAddress.getText().toString():"");
                    mqttConfigInfoSub.setKeepAliveValue(cetKeepAliveValue.getText()!=null?cetKeepAliveValue.getText().toString():"");
                    mqttConfigInfoSub.setDeviceSn(cetDeviceSn.getText()!=null?cetDeviceSn.getText().toString():"");
                    mqttConfigInfoSub.setProductId(cetProductId.getText()!=null?cetProductId.getText().toString():"");
                    mqttConfigInfoSub.setRegistrationCode(cetRegistrationCode.getText()!=null?cetRegistrationCode.getText().toString():"");
                    mqttConfigInfoSub.setRegistrationPlatform(registerPlatformItem);
                    mqttConfigInfoSub.setRegistrationPlatformAddress(cetRegisterAddress.getText()!=null?cetRegisterAddress.getText().toString():"");
                    mqttConfigInfoSub.setAppKey(cetRegistrationCode.getText()!=null?cetRegistrationCode.getText().toString():"");
                    mqttConfigInfoSub.setMqttDeviceId(cetMqttDeviceId.getText()!=null?cetMqttDeviceId.getText().toString():"");
                    mqttConfigInfoSub.setMqttUsername(cetMqttUsername.getText()!=null?cetMqttUsername.getText().toString():"");
                    mqttConfigInfoSub.setMqttPassword(cetMqttPassword.getText()!=null?cetMqttDeviceId.getText().toString():"");
                    mQttOnClickListener.onSureClick(view,mqttConfigInfoSub,link);
                    dialog.dismiss();
                }
            });

            mqttCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mQttOnClickListener.onCancelClick(view);
                    Timber.i("mqtt----取消了");
                    dialog.dismiss();
                }
            });
        }
    }

    public MqttConfigDialog setMyOnClickListener(MQttOnClickListener listener) {
        this.mQttOnClickListener = listener;
        return this;
    }
    @Override
    public MqttConfigInfoSub getData() {
        return mqttConfigInfoSub;
    }


}
