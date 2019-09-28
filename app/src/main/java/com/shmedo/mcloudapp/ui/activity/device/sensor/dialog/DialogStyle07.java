package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shmedo.das.common.SensorRadarLevelInfo;
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
 * 描述：      雷达物位计  07
 */
public class DialogStyle07 implements IDialogOpt<CollectorSensorParamsInfoSub> {
    private Context mContext;
    private View contentView;
    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private SensorRadarLevelInfo sensorRadarLevelInfo = new SensorRadarLevelInfo();
    private View.OnClickListener cancelClickListener;
    private MyOnClickListener sureClickListener;
    private SensorDataEvent event = new SensorDataEvent();
    private Dialog dialog;

    public DialogStyle07(Context context) {
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
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_04, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }


    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        collectorSensorParamsInfoSub = info;
        sensorRadarLevelInfo = (SensorRadarLevelInfo) info.getSensorData();

        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText triggerThreshold = contentView.findViewById(R.id.et_trigger_threshold);
        final EditText revised = contentView.findViewById(R.id.et_revised);
        final EditText measureLong = contentView.findViewById(R.id.et_measure_long);
        EditText note = contentView.findViewById(R.id.et_note);

        modbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        triggerThreshold.setText(String.valueOf(sensorRadarLevelInfo.getTriggerThreshold()));
        revised.setText(String.valueOf(sensorRadarLevelInfo.getRevised()));
        measureLong.setText(String.valueOf(sensorRadarLevelInfo.getProbeElevation()));

        if (sureClickListener != null) {
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sensorRadarLevelInfo.setTriggerThreshold(triggerThreshold.getText().toString());
                    sensorRadarLevelInfo.setRevised(Double.valueOf(revised.getText().toString()));
                    sensorRadarLevelInfo.setProbeElevation(measureLong.getText().toString());
                    collectorSensorParamsInfoSub.setSensorData(sensorRadarLevelInfo);
                    collectorSensorParamsInfoSub.setSensorAddress(modbusAddress.getText().toString());

                    if (sureClickListener.onClick(view)) {
                        String json = GsonFactory.getGson().toJson(collectorSensorParamsInfoSub);
                        event.setType("07");
                        event.setMessage(json);
                        EventBus.getDefault().post(event);

                        dialog.dismiss();
                    }
                }
            });
        }

        if (cancelClickListener != null) {
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
