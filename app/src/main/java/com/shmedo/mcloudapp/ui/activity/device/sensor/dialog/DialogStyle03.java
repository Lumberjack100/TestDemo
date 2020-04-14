package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.entity.event.SensorDataEvent;
import com.shmedo.mcloudapp.inter.MyOnClickListener;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.StringUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogStyle1
 * 创建者:   dpc
 * 创建时间:  2019/6/14 09:47
 * 描述：    03dialog 土壤含水率
 */
public class DialogStyle03 implements IDialogOpt<CollectorSensorParamsInfoSub> {

    private View contentView;
    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private SensorSoilMoistureInfo sensorSoilMoistureInfo = new SensorSoilMoistureInfo();
    private Dialog dialog;
    private MyOnClickListener myOnClickListener;
    private Context mContext;
    private SensorDataEvent event = new SensorDataEvent();
    private String channelNumber;


    public DialogStyle03(Context context, String channelNumber) {
        this.mContext = context;
        this.channelNumber = channelNumber;
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
        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText triggerThreshold = contentView.findViewById(R.id.et_trigger_threshold);
        final EditText revised = contentView.findViewById(R.id.et_revised);
        EditText note = contentView.findViewById(R.id.et_note);

        collectorSensorParamsInfoSub = info;
        sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfoSub.getSensorData();

        modbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        triggerThreshold.setText(sensorSoilMoistureInfo.getTriggerThreshold() + "");
        revised.setText(String.valueOf(sensorSoilMoistureInfo.getRevised()));

        if (myOnClickListener != null) {
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (triggerThreshold.getText().length() == 0 || !StringUtil.isInteger(triggerThreshold.getText().toString())) {
                        ToastUtils.show("请输入正确的触发值");
                        return;
                    }

                    if (revised.getText().length() == 0 || (!StringUtil.isInteger(revised.getText().toString()) && !StringUtil.isDouble(revised.getText().toString()))) {
                        ToastUtils.show("请输入正确的修正值");
                        return;
                    }

                    sensorSoilMoistureInfo.setTriggerThreshold(triggerThreshold.getText().toString());
                    sensorSoilMoistureInfo.setRevised(Double.valueOf(revised.getText().toString()));
                    collectorSensorParamsInfoSub.setSensorData(sensorSoilMoistureInfo);
                    collectorSensorParamsInfoSub.setSensorAddress(modbusAddress.getText().toString());

                    if (myOnClickListener.onSureClick(view)) {
                        String json = GsonFactory.getGson().toJson(collectorSensorParamsInfoSub);
                        event.setType("02");
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

    public DialogStyle03 setMyOnClickListener(MyOnClickListener listener) {
        this.myOnClickListener = listener;
        return this;
    }


    @Override
    public CollectorSensorParamsInfoSub getData() {
        return collectorSensorParamsInfoSub;
    }


}
