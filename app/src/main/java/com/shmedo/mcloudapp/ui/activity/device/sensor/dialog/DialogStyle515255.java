package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shmedo.core.model.SensorGudanSoilPressureInfo;
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
 *
 */
public class DialogStyle515255 implements IDialogOpt<CollectorSensorParamsInfoSub>{
    private Context mContext;
    private View contentView;
    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private SensorGudanSoilPressureInfo sensorGudanSoilPressureInfo = new SensorGudanSoilPressureInfo();
    private MyOnClickListener myOnClickListener;
    private SensorDataEvent event = new SensorDataEvent();
    private Dialog dialog;
    private String channelNumber;


    public DialogStyle515255(Context context, String channelNumber) {
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
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_51_52_55, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }


    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        collectorSensorParamsInfoSub = info;
         sensorGudanSoilPressureInfo = (SensorGudanSoilPressureInfo) info.getSensorData();
         
        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText triggerThreshold = contentView.findViewById(R.id.triggerThreshold);
        final EditText sensitivityK = contentView.findViewById(R.id.sensitivityK);
        final EditText temperatureCoefficientB = contentView.findViewById(R.id.temperatureCoefficientB);
        final EditText datumValueF0 = contentView.findViewById(R.id.datumValueF0);
        final EditText createTemperature = contentView.findViewById(R.id.createTemperature);
        final EditText manualCorrection = contentView.findViewById(R.id.manualCorrection);
        final EditText note = contentView.findViewById(R.id.et_note);

        modbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        triggerThreshold.setText(String.valueOf(sensorGudanSoilPressureInfo.getTriggerThreshold()));
        sensitivityK.setText(sensorGudanSoilPressureInfo.getSensitivityK());
        temperatureCoefficientB.setText(String.valueOf(sensorGudanSoilPressureInfo.getTemperatureCoefficientB()));
        datumValueF0.setText(String.valueOf(sensorGudanSoilPressureInfo.getDatumValueF0()));
        createTemperature.setText(String.valueOf(sensorGudanSoilPressureInfo.getCreateTemperature()));
        manualCorrection.setText(String.valueOf(sensorGudanSoilPressureInfo.getManualCorrection()));

        if (myOnClickListener != null){
            save.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {

                    sensorGudanSoilPressureInfo.setTriggerThreshold(Integer.parseInt(triggerThreshold.getText().toString()));
                    sensorGudanSoilPressureInfo.setSensitivityK(Integer.parseInt(sensitivityK.getText().toString()));
                    sensorGudanSoilPressureInfo.setTemperatureCoefficientB(Double.parseDouble(temperatureCoefficientB.getText().toString()));
                    sensorGudanSoilPressureInfo.setDatumValueF0(Double.parseDouble(datumValueF0.getText().toString()));
                    sensorGudanSoilPressureInfo.setManualCorrection(manualCorrection.getText().toString());
                    collectorSensorParamsInfoSub.setSensorData(sensorGudanSoilPressureInfo);
                    collectorSensorParamsInfoSub.setSensorAddress(modbusAddress.getText().toString());

                    if(myOnClickListener.onSureClick(view)){
                        String json = GsonFactory.getGson().toJson(collectorSensorParamsInfoSub);
                        event.setType("515255");
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
