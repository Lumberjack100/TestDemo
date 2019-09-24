package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shmedo.das.common.SensorSoilMoistureInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.entity.event.SensorDataEvent;
import com.shmedo.mcloudapp.util.GsonFactory;

import org.greenrobot.eventbus.EventBus;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogStyle1
 * 创建者:   dpc
 * 创建时间:  2019/6/14 09:47
 * 描述：    03dialog 土壤含水率
 */
public class DialogStyle03 implements IDialogOpt<CollectorSensorParamsInfoSub>{

    private View contentView;
    private CollectorSensorParamsInfoSub data;
    private SensorSoilMoistureInfo infoSub = new SensorSoilMoistureInfo();
    private Dialog dialog;
    private View.OnClickListener sureClickListener, cancelClickListener;
    private Context mContext;
    private SensorDataEvent event = new SensorDataEvent();
    public DialogStyle03(Context context) {
        this.mContext = context;
    }


    @Override
    public Dialog getDialog() {
        dialog = new Dialog(mContext, R.style.dialog_bottom_full);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_02_03, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }


    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        data = info;
        SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo)data.getSensorData();
        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText triggerThreshold = contentView.findViewById(R.id.et_trigger_threshold);
        final EditText revised = contentView.findViewById(R.id.et_revised);
        EditText note = contentView.findViewById(R.id.et_note);
        modbusAddress.setText(data.getSensorAddress());
        triggerThreshold.setText(sensorSoilMoistureInfo.getTriggerThreshold()+"");
        revised.setText(String.valueOf(sensorSoilMoistureInfo.getRevised()));

        if (sureClickListener != null){
            save.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    infoSub.setTriggerThreshold(triggerThreshold.getText().toString());
                    infoSub.setRevised(Double.valueOf(revised.getText().toString()));
                    data.setSensorData(infoSub);
                    data.setSensorAddress(modbusAddress.getText().toString());
                    data.setSensorType(info.getSensorType());
                    data.setChannelNumber(info.getChannelNumber());
                    data.setCollectorModel(info.getCollectorModel());
                    String json = GsonFactory.getGson().toJson(data);
                    event.setType("02");
                    event.setMessage(json);
                    EventBus.getDefault().post(event);

                    sureClickListener.onClick(view);
                    dialog.dismiss();
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
    public DialogStyle03 setSureonClickListener(View.OnClickListener listener){
        this.sureClickListener = listener;
        return this;
    }
    public DialogStyle03 setCancelOnClickListener(View.OnClickListener listener){
        this.cancelClickListener = listener;
        return this;
    }


    @Override
    public CollectorSensorParamsInfoSub getData() {
        return data;
    }


}
