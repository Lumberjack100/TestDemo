package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.shmedo.das.common.SensorKangPercolateInfo;
import com.shmedo.das.common.SensorUpliftPressureInfo;
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
 * 描述：    基康渗压计  50
 */
public class DialogStyle50 implements IDialogOpt<CollectorSensorParamsInfoSub>{
    private View contentView;
    private CollectorSensorParamsInfoSub data;
    private Context mContext;
    private SensorKangPercolateInfo infoSub = new SensorKangPercolateInfo();
    private View.OnClickListener sureClickListener, cancelClickListener;
    private SensorDataEvent event = new SensorDataEvent();
    private Dialog dialog;

    public DialogStyle50(Context context) {
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
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_50, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }


    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        data = info;
        SensorKangPercolateInfo kangPercolateInfo = (SensorKangPercolateInfo) info.getSensorData();
        TextView cancel = contentView.findViewById(R.id.tv_cancel);
        final TextView save = contentView.findViewById(R.id.tv_save);
        final EditText modbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText triggerThreshold = contentView.findViewById(R.id.triggerThreshold);
        final EditText polynomialRatioA = contentView.findViewById(R.id.polynomialRatioA);
        final EditText polynomialRatioB = contentView.findViewById(R.id.polynomialRatioB);
        final EditText polynomialRatioC = contentView.findViewById(R.id.polynomialRatioC);
        final EditText temperatureCoefficientK = contentView.findViewById(R.id.temperatureCoefficientK);
        final EditText createTemperature = contentView.findViewById(R.id.createTemperature);
        final EditText manualCorrection = contentView.findViewById(R.id.manualCorrection);
        EditText note = contentView.findViewById(R.id.et_note);

        modbusAddress.setText(data.getSensorAddress());
        triggerThreshold.setText(String.valueOf(kangPercolateInfo.getTriggerThreshold()));
        polynomialRatioA.setText(kangPercolateInfo.getPolynomialRatioA());
        polynomialRatioB.setText(kangPercolateInfo.getPolynomialRatioB());
        polynomialRatioC.setText(kangPercolateInfo.getPolynomialRatioC());
        temperatureCoefficientK.setText(String.valueOf(kangPercolateInfo.getTemperatureCoefficientK()));
        createTemperature.setText(String.valueOf(kangPercolateInfo.getCreateTemperature()));
        manualCorrection.setText(String.valueOf(kangPercolateInfo.getManualCorrection()));

        if (sureClickListener != null){
            save.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {

                    infoSub.setTriggerThreshold(Integer.parseInt(triggerThreshold.getText().toString()));
                    infoSub.setPolynomialRatioA(polynomialRatioA.getText().toString());
                    infoSub.setPolynomialRatioB(polynomialRatioB.getText().toString());
                    infoSub.setPolynomialRatioC(polynomialRatioC.getText().toString());
                    infoSub.setTemperatureCoefficientK(Double.parseDouble(temperatureCoefficientK.getText().toString()));
                    infoSub.setCreateTemperature(Double.parseDouble(createTemperature.getText().toString()));
                    infoSub.setManualCorrection(manualCorrection.getText().toString());

                    data.setSensorData(infoSub);
                    data.setSensorAddress(modbusAddress.getText().toString());
                    data.setSensorType(info.getSensorType());
                    data.setChannelNumber(info.getChannelNumber());
                    data.setCollectorModel(info.getCollectorModel());
                    String json = GsonFactory.getGson().toJson(data);

                    event.setType("50");
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
