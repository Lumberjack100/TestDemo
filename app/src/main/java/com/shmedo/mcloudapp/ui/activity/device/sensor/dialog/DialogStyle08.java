package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shmedo.core.model.SensorMoistureMeterInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.entity.event.SensorDataEvent;
import com.shmedo.mcloudapp.inter.MyOnClickListener;
import com.shmedo.mcloudapp.util.GsonFactory;

import org.greenrobot.eventbus.EventBus;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogStyle1
 * 创建者:   dpc
 * 创建时间:  2019/6/14 09:47
 * 描述：    墒情计 08
 */
public class DialogStyle08 implements IDialogOpt<CollectorSensorParamsInfoSub>{
    private Context mContext;
    private View contentView;
    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private SensorMoistureMeterInfo sensorMoistureMeterInfo = new SensorMoistureMeterInfo();
    private MyOnClickListener myOnClickListener;
    private SensorDataEvent event = new SensorDataEvent();
    private Dialog dialog;
    private String channelNumber;



    public DialogStyle08(Context context, String channelNumber) {
        this.mContext = context;
        this.channelNumber = channelNumber;
    }



    @Override
    public Dialog getDialog() {
        dialog = new Dialog(mContext,R.style.dialog_bottom_full);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_08, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }


    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        collectorSensorParamsInfoSub = info;
        sensorMoistureMeterInfo = (SensorMoistureMeterInfo) info.getSensorData();

        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText humidityTriggerThreshold = contentView.findViewById(R.id.humidityTriggerThreshold);
        final EditText humidityCorrectionValue = contentView.findViewById(R.id.humidityCorrectionValue);
        final EditText saltTriggerThreshold = contentView.findViewById(R.id.saltTriggerThreshold);
        final EditText saltCorrectionValue = contentView.findViewById(R.id.saltCorrectionValue);
        final EditText temperatureTriggerThreshold = contentView.findViewById(R.id.temperatureTriggerThreshold);
        final EditText temperatureCorrectionValue = contentView.findViewById(R.id.temperatureCorrectionValue);
        final EditText note = contentView.findViewById(R.id.et_note);

        modbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        humidityTriggerThreshold.setText(String.valueOf(sensorMoistureMeterInfo.getHumidityTriggerThreshold()));
        humidityCorrectionValue.setText(String.valueOf(sensorMoistureMeterInfo.getHumidityCorrectionValue()));
        saltTriggerThreshold.setText(String.valueOf(sensorMoistureMeterInfo.getSaltTriggerThreshold()));
        saltCorrectionValue.setText(String.valueOf(sensorMoistureMeterInfo.getSaltCorrectionValue()));
        temperatureTriggerThreshold.setText(String.valueOf(sensorMoistureMeterInfo.getTemperatureTriggerThreshold()));
        temperatureCorrectionValue.setText(String.valueOf(sensorMoistureMeterInfo.getTemperatureCorrectionValue()));


        if (myOnClickListener != null){
            save.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
//                    if (triggerThreshold.getText().length() == 0 || !StringUtil.isInteger(triggerThreshold.getText().toString())) {
//                        ToastUtils.show("请输入正确的触发值");
//                        return;
//                    }
//
//                    if (revised.getText().length() == 0 || (!StringUtil.isInteger(revised.getText().toString()) && !StringUtil.isDouble(revised.getText().toString()))) {
//                        ToastUtils.show("请输入正确的修正值");
//                        return;
//                    }

                    sensorMoistureMeterInfo.setHumidityTriggerThreshold(Integer.parseInt(humidityTriggerThreshold.getText().toString()));
                    sensorMoistureMeterInfo.setHumidityCorrectionValue(Double.valueOf(humidityCorrectionValue.getText().toString()));
                    sensorMoistureMeterInfo.setSaltTriggerThreshold(Integer.parseInt(saltTriggerThreshold.getText().toString()));
                    sensorMoistureMeterInfo.setSaltCorrectionValue(Double.valueOf(saltCorrectionValue.getText().toString()));
                    sensorMoistureMeterInfo.setTemperatureTriggerThreshold(Integer.parseInt(temperatureTriggerThreshold.getText().toString()));
                    sensorMoistureMeterInfo.setTemperatureCorrectionValue(Double.valueOf(temperatureCorrectionValue.getText().toString()));
                    collectorSensorParamsInfoSub.setSensorData(sensorMoistureMeterInfo);
                    collectorSensorParamsInfoSub.setSensorAddress(modbusAddress.getText().toString());

                    if(myOnClickListener.onSureClick(view)){
                        String json = GsonFactory.getGson().toJson(collectorSensorParamsInfoSub);
                        event.setType("08");
                        event.setMessage(json);
                        EventBus.getDefault().post(event);

                        dialog.dismiss();
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myOnClickListener.onCancelClick(view, channelNumber);
                    dialog.dismiss();
                }
            });
        }
    }


    public void setMyOnClickListener(MyOnClickListener myOnClickListener) {
        this.myOnClickListener = myOnClickListener;
    }


    @Override
    public CollectorSensorParamsInfoSub getData() {
        return collectorSensorParamsInfoSub;
    }
}
