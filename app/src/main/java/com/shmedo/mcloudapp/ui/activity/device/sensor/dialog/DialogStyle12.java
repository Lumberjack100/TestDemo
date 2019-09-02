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
import com.shmedo.das.common.SensorTemperHumidityInfo;
import com.shmedo.mcloudapp.App;
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
 * 描述：    TODO
 */
public class DialogStyle12 implements IDialogOpt<CollectorSensorParamsInfoSub>{
    private View contentView;
    private CollectorSensorParamsInfoSub data;
    private Context mContext;
    private SensorTemperHumidityInfo infoSub = new SensorTemperHumidityInfo();
    private View.OnClickListener sureClickListener, cancelClickListener;
    private SensorDataEvent event = new SensorDataEvent();
    private Dialog dialog;

    public DialogStyle12(Context context) {
        this.mContext = context;
    }


    public void setSureClickListener(View.OnClickListener sureClickListener) {
        this.sureClickListener = sureClickListener;
    }


    public void setCancelClickListener(View.OnClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
    }


    @Override
    public Dialog getDialog() {
        dialog = new Dialog(mContext,R.style.dialog_bottom_full);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_12, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }


    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        data = info;
        SensorTemperHumidityInfo temperHumidityInfo = (SensorTemperHumidityInfo) info.getSensorData();
        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText temperatureTriggerThreshold = contentView.findViewById(R.id.temperatureTriggerThreshold);
        final EditText humidityTriggerThreshold = contentView.findViewById(R.id.humidityTriggerThreshold);

        EditText note = contentView.findViewById(R.id.et_note);
        modbusAddress.setText(data.getSensorAddress());
        temperatureTriggerThreshold.setText(temperHumidityInfo.getTemperatureTriggerThreshold());
        humidityTriggerThreshold.setText(temperHumidityInfo.getHumidityTriggerThreshold());

        if (sureClickListener != null){
            save.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    infoSub.setTemperatureTriggerThreshold(temperatureTriggerThreshold.getText().toString());
                    infoSub.setHumidityTriggerThreshold(humidityTriggerThreshold.getText().toString());

                    data.setSensorData(infoSub);
                    data.setSensorAddress(modbusAddress.getText().toString());
                    data.setSensorType(info.getSensorType());
                    data.setChannelNumber(info.getChannelNumber());
                    data.setCollectorModel(info.getCollectorModel());
                    String json = GsonFactory.getGson().toJson(data);

                    event.setType("12");
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


    @Override
    public CollectorSensorParamsInfoSub getData() {
        return data;
    }
}
