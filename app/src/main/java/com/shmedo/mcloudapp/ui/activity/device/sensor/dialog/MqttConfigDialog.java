package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
//        mqttConfigInfoSub = info;
        TextView tvTitle = contentView.findViewById(R.id.tv_title);
        Spinner communicationProtocol = contentView.findViewById(R.id.communication_protocol);
        ClearEditText cetServiceAddress = contentView.findViewById(R.id.cet_service_address);
        Spinner spRegistrationPlatform = contentView.findViewById(R.id.sp_registration_platform);
        LinearLayout llRegisterPlatform = contentView.findViewById(R.id.ll_register_platform);
        ClearEditText cetAppKey = contentView.findViewById(R.id.cet_app_Key);
        LinearLayout llAppKey = contentView.findViewById(R.id.ll_app_key);
        ClearEditText cetRegisterAddress = contentView.findViewById(R.id.cet_register_address);
        LinearLayout llRegisterAddress = contentView.findViewById(R.id.ll_register_address);
        ClearEditText cetKeepAliveValue = contentView.findViewById(R.id.cet_KeepAlive_value);
        LinearLayout llKeepAlive = contentView.findViewById(R.id.ll_keep_alive);
        ClearEditText cetDeviceSn = contentView.findViewById(R.id.cet_device_sn);
        ClearEditText cetProductId = contentView.findViewById(R.id.cet_product_id);
        ClearEditText cetRegistrationCode = contentView.findViewById(R.id.cet_registration_code);
        LinearLayout llDeviceConfig = contentView.findViewById(R.id.ll_device_config);
        ClearEditText cetMqttDeviceId = contentView.findViewById(R.id.cet_mqtt_device_id);
        ClearEditText cetMqttUsername = contentView.findViewById(R.id.cet_mqtt_username) ;
        ClearEditText cetMqttPassword = contentView.findViewById(R.id.cet_mqtt_password);
        LinearLayout llMqttConfig = contentView.findViewById(R.id.ll_mqtt_config);
        TextView mqttCancel = contentView.findViewById(R.id.mqtt_cancel);
        TextView mqttSave = contentView.findViewById(R.id.mqtt_save);


        if (mQttOnClickListener != null) {
            mqttSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Timber.i("mqtt----确认了");

                    if (mQttOnClickListener.onSureClick(view)) {
                        dialog.dismiss();
                    }
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
