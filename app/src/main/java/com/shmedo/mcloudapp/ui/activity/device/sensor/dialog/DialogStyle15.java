package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shmedo.das.common.SensorUpliftPressureInfo;
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
 * 描述：    扬压力计 15
 */
public class DialogStyle15 implements IDialogOpt<CollectorSensorParamsInfoSub>{
    private Context mContext;
    private View contentView;
    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private SensorUpliftPressureInfo sensorUpliftPressureInfo = new SensorUpliftPressureInfo();
    private View.OnClickListener cancelClickListener;
    private MyOnClickListener sureClickListener;
    private SensorDataEvent event = new SensorDataEvent();
    private Dialog dialog;

    public DialogStyle15(Context context) {
        this.mContext = context;
    }


    @Override
    public Dialog getDialog() {
        dialog = new Dialog(mContext,R.style.dialog_bottom_full);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_15, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }


    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        collectorSensorParamsInfoSub = info;
         sensorUpliftPressureInfo = (SensorUpliftPressureInfo) info.getSensorData();
         
        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText triggerThreshold = contentView.findViewById(R.id.triggerThreshold);
        final EditText revised = contentView.findViewById(R.id.revised);
        final EditText cordlength = contentView.findViewById(R.id.cordlength);
        final EditText installationElevation = contentView.findViewById(R.id.installationElevation);
        final EditText note = contentView.findViewById(R.id.et_note);

        modbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        triggerThreshold.setText(sensorUpliftPressureInfo.getTriggerThreshold());
        revised.setText(String.valueOf(sensorUpliftPressureInfo.getRevised()));
        cordlength.setText(sensorUpliftPressureInfo.getCordlength());
        installationElevation.setText(sensorUpliftPressureInfo.getInstallationElevation());

        if (sureClickListener != null){
            save.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {

                    sensorUpliftPressureInfo.setTriggerThreshold(Integer.parseInt(triggerThreshold.getText().toString()));
                    sensorUpliftPressureInfo.setRevised(Double.parseDouble(revised.getText().toString()));
                    sensorUpliftPressureInfo.setCordlength(cordlength.getText().toString());
                    sensorUpliftPressureInfo.setInstallationElevation(installationElevation.getText().toString());
                    collectorSensorParamsInfoSub.setSensorData(sensorUpliftPressureInfo);
                    collectorSensorParamsInfoSub.setSensorAddress(modbusAddress.getText().toString());

                    if(sureClickListener.onClick(view)){
                        String json = GsonFactory.getGson().toJson(collectorSensorParamsInfoSub);
                        event.setType("15");
                        event.setMessage(json);
                        EventBus.getDefault().post(event);

                        dialog.dismiss();
                    }
                }
            });
        }
        if (cancelClickListener != null){
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    cancelClickListener.onClick(view);
                    dialog.dismiss();
                }
            });
        }
    }


    public void setSureClickListener(MyOnClickListener sureClickListener) {
        this.sureClickListener = sureClickListener;
    }


    public void setCancelClickListener(View.OnClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
    }

    @Override
    public CollectorSensorParamsInfoSub getData() {
        return collectorSensorParamsInfoSub;
    }
}
