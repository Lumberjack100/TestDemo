package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.model.SensorInfrasoundInfo;
import com.shmedo.core.model.SensorRadarLevelInfo;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.core.model.SensorWireShiftInfo;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.interfaces.MyOnClickListener;
import com.shmedo.mcloudapp.util.UserConfig;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogStyle1
 * 创建者:   dpc
 * 创建时间:  2019/6/14 09:47
 * 描述：    0203dialog
 */
public class DialogStyle02 implements IDialogOpt<CollectorSensorParamsInfoSub> {

    private View contentView;

    private Context mContext;

    private Dialog dialog;

    private MyOnClickListener myOnClickListener;

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;

    private UserConfig userConfig;

    private String channelNumber;

    private String oldAddress, newAddress, oldTriggerThreshold, newTriggerThreshold, oldRevised, newRevised;


    public DialogStyle02(Context context, String channelNumber) {
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
        TextView mTvCancel = contentView.findViewById(R.id.tv_cancel);
        final TextView mTvSave = contentView.findViewById(R.id.tv_save);
        //触发阈值(单位：mm)
        final TextView mTvTriggerThreshold = contentView.findViewById(R.id.tv_triggerThreshold);
        //修正值(单位：m)
        final TextView mTvCorrectionValue = contentView.findViewById(R.id.tv_correctionValue);
        final EditText mEtModbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText mEtTriggerThreshold = contentView.findViewById(R.id.et_trigger_threshold);
        final EditText mEtRevised = contentView.findViewById(R.id.et_revised);
        final EditText mEtNote = contentView.findViewById(R.id.et_note);

        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});


        collectorSensorParamsInfoSub = info;
        CollectorModel collectorModel = CollectorModel.value(collectorSensorParamsInfoSub.getCollectorModel());
        switch (collectorModel) {
            case DS08://裂缝计采集器
            {
                mTvTriggerThreshold.setText("触发阈值(单位：mm)");
                mTvCorrectionValue.setText("修正值(单位：m)");
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtTriggerThreshold.setText(sensorWireShiftInfo.getTriggerThreshold() + "");
                mEtRevised.setText(String.valueOf(sensorWireShiftInfo.getCorrectionValue()));
            }
            break;

            case CS08://次声采集器
            {
                mTvTriggerThreshold.setText("触发阈值(单位：Hz)");
                mTvCorrectionValue.setText("修正值(单位：Hz)");
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtTriggerThreshold.setText(sensorInfrasoundInfo.getTriggerThreshold());
                mEtRevised.setText(String.valueOf(sensorInfrasoundInfo.getRevised()));
            }
            break;

            case HD08://土壤湿度采集器
            {
                mTvTriggerThreshold.setText("触发阈值(单位：%rh)");
                mTvCorrectionValue.setText("修正值(单位：%rh)");
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtTriggerThreshold.setText(sensorSoilMoistureInfo.getTriggerThreshold());
                mEtRevised.setText(String.valueOf(sensorSoilMoistureInfo.getRevised()));
            }
            break;

            case RD08://雷达采集器
            {
                mTvTriggerThreshold.setText("触发阈值(单位：mm)");
                mTvCorrectionValue.setText("修正值(单位：mm)");
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) collectorSensorParamsInfoSub.getSensorData();
                mEtTriggerThreshold.setText(sensorRadarLevelInfo.getTriggerThreshold());
                mEtRevised.setText(String.valueOf(sensorRadarLevelInfo.getRevised()));
            }
            break;
        }
        mEtModbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        userConfig = UserConfig.getConfig(mContext, String.valueOf(channelNumber));
        mEtNote.setText(userConfig.readString(String.valueOf(channelNumber)));

        oldAddress = mEtModbusAddress.getText().toString().trim();
        oldTriggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        oldRevised = mEtRevised.getText().toString().trim();

        if (myOnClickListener != null) {
            mTvSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newAddress = mEtModbusAddress.getText().toString().trim();
                    newTriggerThreshold = mEtTriggerThreshold.getText().toString().trim();
                    newRevised = mEtRevised.getText().toString().trim();

                    if (TextUtils.isEmpty(newAddress) || !ValidateUtil.isInteger(newAddress) || Integer.parseInt(newAddress) < 0 || Integer.parseInt(newAddress) > 99) {
                        ToastUtils.show("请输入正确的地址");
                        return;
                    }

                    if (TextUtils.isEmpty(newTriggerThreshold) || !ValidateUtil.isInteger(newTriggerThreshold)) {
                        ToastUtils.show("请输入正确的触发值");
                        return;
                    }

                    if (TextUtils.isEmpty(newRevised) || (!ValidateUtil.isInteger(newRevised) && !ValidateUtil.isDouble(newRevised))) {
                        ToastUtils.show("请输入正确的修正值");
                        return;
                    }

                    updateParamsInfo(collectorModel, true);
                    userConfig.writeString(String.valueOf(channelNumber), mEtNote.getText().toString().trim());
                    if (myOnClickListener.onSureClick(view)) {
                        dialog.dismiss();
                    }
                }
            });

            mTvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateParamsInfo(collectorModel, false);
                    myOnClickListener.onCancelClick(view, channelNumber);
                    dialog.dismiss();
                }
            });
        }
    }


    public DialogStyle02 setMyOnClickListener(MyOnClickListener listener) {
        this.myOnClickListener = listener;
        return this;
    }


    @Override
    public CollectorSensorParamsInfoSub getData() {
        return collectorSensorParamsInfoSub;
    }

    private void updateParamsInfo(CollectorModel collectorModel, boolean isSure) {
        switch (collectorModel) {
            case DS08://裂缝计采集器
            {
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorWireShiftInfo.setTriggerThreshold(isSure ? Integer.parseInt(newTriggerThreshold) : Integer.parseInt(oldTriggerThreshold));
                sensorWireShiftInfo.setCorrectionValue(isSure ? Double.valueOf(newRevised) : Double.valueOf(oldRevised));
                collectorSensorParamsInfoSub.setSensorData(sensorWireShiftInfo);
            }
            break;

            case CS08://次声采集器
            {
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorInfrasoundInfo.setTriggerThreshold(isSure ? newTriggerThreshold : oldTriggerThreshold);
                sensorInfrasoundInfo.setRevised(isSure ? Double.valueOf(newRevised) : Double.valueOf(oldRevised));
                collectorSensorParamsInfoSub.setSensorData(sensorInfrasoundInfo);
            }
            break;

            case HD08://土壤湿度采集器
            {
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorSoilMoistureInfo.setTriggerThreshold(isSure ? newTriggerThreshold : oldTriggerThreshold);
                sensorSoilMoistureInfo.setRevised(isSure ? Double.valueOf(newRevised) : Double.valueOf(oldRevised));
                collectorSensorParamsInfoSub.setSensorData(sensorSoilMoistureInfo);
            }
            break;

            case RD08://雷达采集器
            {
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) collectorSensorParamsInfoSub.getSensorData();
                sensorRadarLevelInfo.setTriggerThreshold(isSure ? newTriggerThreshold : oldTriggerThreshold);
                sensorRadarLevelInfo.setRevised(isSure ? Double.valueOf(newRevised) : Double.valueOf(oldRevised));
                collectorSensorParamsInfoSub.setSensorData(sensorRadarLevelInfo);
            }
            break;
        }

        collectorSensorParamsInfoSub.setSensorAddress(isSure ? newAddress : oldAddress);
    }

}
