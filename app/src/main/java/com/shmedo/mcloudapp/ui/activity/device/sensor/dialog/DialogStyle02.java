package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.das.common.SensorWireShiftInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.inter.MyOnClickListener;
import com.shmedo.mcloudapp.util.StringUtil;
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

    private SensorWireShiftInfo sensorWireShiftInfo = new SensorWireShiftInfo();

    private UserConfig userConfig;

    private String channelNumber;


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
        final EditText mEtModbusAddress = contentView.findViewById(R.id.et_modbus_address);
        final EditText mEtTriggerThreshold = contentView.findViewById(R.id.et_trigger_threshold);
        final EditText mEtRevised = contentView.findViewById(R.id.et_revised);
        final EditText mEtNote = contentView.findViewById(R.id.et_note);

        collectorSensorParamsInfoSub = info;
        sensorWireShiftInfo = (SensorWireShiftInfo) collectorSensorParamsInfoSub.getSensorData();

        mEtModbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        mEtTriggerThreshold.setText(sensorWireShiftInfo.getTriggerThreshold() + "");
        mEtRevised.setText(String.valueOf(sensorWireShiftInfo.getCorrectionValue()));
        userConfig = UserConfig.getConfig(mContext, String.valueOf(channelNumber));
        mEtNote.setText(userConfig.readString(String.valueOf(channelNumber)));

        if (myOnClickListener != null) {
            mTvSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String address = mEtModbusAddress.getText().toString().trim();
                    String triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
                    String revised = mEtRevised.getText().toString().trim();

                    if (TextUtils.isEmpty(address) || !StringUtil.isInteger(address) || Integer.parseInt(address) < 0 || Integer.parseInt(address) > 99) {
                        ToastUtils.show("请输入正确的地址");
                        return;
                    }

                    if (TextUtils.isEmpty(triggerThreshold) || !StringUtil.isInteger(triggerThreshold)) {
                        ToastUtils.show("请输入正确的触发值");
                        return;
                    }

                    if (TextUtils.isEmpty(revised) || (!StringUtil.isInteger(revised) && !StringUtil.isDouble(revised))) {
                        ToastUtils.show("请输入正确的修正值");
                        return;
                    }

                    collectorSensorParamsInfoSub.setSensorAddress(address);
                    sensorWireShiftInfo.setTriggerThreshold(Integer.parseInt(triggerThreshold));
                    sensorWireShiftInfo.setCorrectionValue(Double.valueOf(revised));
                    collectorSensorParamsInfoSub.setSensorData(sensorWireShiftInfo);

                    userConfig.writeString(String.valueOf(channelNumber), mEtNote.getText().toString().trim());

                    if (myOnClickListener.onSureClick(view)) {
                        dialog.dismiss();
                    }
                }
            });

            mTvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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

}
