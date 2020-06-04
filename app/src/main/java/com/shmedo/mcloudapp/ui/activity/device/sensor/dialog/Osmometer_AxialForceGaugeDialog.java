package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.interfaces.MyOnClickListener;
import com.shmedo.mcloudapp.util.UserConfig;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/4 <br/>
 * 描述：    基康渗压计、葛南渗压计、轴力计等传感器配置页面
 */
public class Osmometer_AxialForceGaugeDialog implements IDialogOpt<CollectorSensorParamsInfoSub> {
    private Context mContext;
    private View contentView;
    private Spinner spinnerType;
    private ViewGroup scanLayout;
    //基康渗压计、葛南渗压计、轴力计布局
    private ViewGroup bgkOsmometerConfigLayout, ngnOsmometerConfigLayout, axialforcegaugeConfigLayout;
    //基康渗压计特有参数
    private EditText mEtCoefficientA, mEtCoefficientB, mEtCoefficientC, mEtCoefficientK;
    //葛南渗压计特有参数
    private EditText mEtSensitivityCoefficient, mEtTemperatureCoefficient;
    //通用参数
    private EditText mEtModbusAddress, mEtNozzelHeight, mEtOsmometerCord, mEtAlarmValue, mEtCorrectValue, mEtNote;
    private Dialog dialog;
    private MyOnClickListener myOnClickListener;
    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private UserConfig userConfig;
    private String channelNumber;

    private String oldCoefficientA, newCoefficientA, oldCoefficientB, newCoefficientB, oldCoefficientC, newCoefficientC, oldCoefficientK, newCoefficientK;
    private String oldSensitivityCoefficient, newSensitivityCoefficient, oldTemperatureCoefficient, newTemperatureCoefficient;
    private String oldAddress, newAddress, oldNozzelHeight, newNozzelHeight, oldOsmometerCord, newOsmometerCord, oldAlarmValue, newAlarmValue, oldCorrectValue, newCorrectValue;

    public Osmometer_AxialForceGaugeDialog(Context context, String channelNumber) {
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
        contentView = View.inflate(mContext, R.layout.dialog_sensor_config_osmometer_axialforcegauge, null);
        window.setContentView(contentView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        return dialog;
    }

    @Override
    public void initData(final CollectorSensorParamsInfoSub info) {
        TextView mTvCancel = contentView.findViewById(R.id.tv_cancel);
        TextView mTvSave = contentView.findViewById(R.id.tv_save);
        scanLayout = contentView.findViewById(R.id.rl_scan_config);
        bgkOsmometerConfigLayout = contentView.findViewById(R.id.bgk_osmometer_config_layout);
        ngnOsmometerConfigLayout = contentView.findViewById(R.id.ngn_osmometer_config_layout);
        axialforcegaugeConfigLayout = contentView.findViewById(R.id.axialforcegauge_config_layout);

        spinnerType = contentView.findViewById(R.id.spinner);
        mEtCoefficientA = contentView.findViewById(R.id.polynomialRatioA);
        mEtCoefficientB = contentView.findViewById(R.id.polynomialRatioB);
        mEtCoefficientC = contentView.findViewById(R.id.polynomialRatioC);
        mEtCoefficientK = contentView.findViewById(R.id.temperatureCoefficientK);

        mEtSensitivityCoefficient = contentView.findViewById(R.id.sensitivityCoefficient);
        mEtTemperatureCoefficient = contentView.findViewById(R.id.temperatureCoefficient);

        mEtModbusAddress = contentView.findViewById(R.id.et_modbus_address);
        mEtNozzelHeight = contentView.findViewById(R.id.et_nozzel_height);
        mEtOsmometerCord = contentView.findViewById(R.id.et_osmometer_cord);
        mEtAlarmValue = contentView.findViewById(R.id.et_alarm_value);
        mEtCorrectValue = contentView.findViewById(R.id.et_correct_value);
        mEtNote = contentView.findViewById(R.id.et_note);

        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        //传感器类型
        String[] stringArray = mContext.getResources().getStringArray(R.array.sensor_osmometer);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item) {
                    case "渗压计-BGK":
                        bgkOsmometerConfigLayout.setVisibility(View.VISIBLE);
                        ngnOsmometerConfigLayout.setVisibility(View.GONE);
                        axialforcegaugeConfigLayout.setVisibility(View.GONE);
                        break;

                    case "渗压计-NGN":
                        bgkOsmometerConfigLayout.setVisibility(View.GONE);
                        ngnOsmometerConfigLayout.setVisibility(View.VISIBLE);
                        axialforcegaugeConfigLayout.setVisibility(View.GONE);
                        break;

                    case "轴力计":
                        bgkOsmometerConfigLayout.setVisibility(View.GONE);
                        ngnOsmometerConfigLayout.setVisibility(View.GONE);
                        axialforcegaugeConfigLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        collectorSensorParamsInfoSub = info;
        setValue();

        scanLayout.setOnClickListener(clickListener);
        if (myOnClickListener != null) {
            mTvSave.setOnClickListener(clickListener);
            mTvCancel.setOnClickListener(clickListener);
        }
    }

    private void setValue(){
        String sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case "50":
                mEtCoefficientA.setText("");
                mEtCoefficientB.setText("");
                mEtCoefficientC.setText("");
                mEtCoefficientK.setText("");

                break;

            case "51":
                mEtSensitivityCoefficient.setText("");
                mEtTemperatureCoefficient.setText("");
                break;

            case "55":
                break;
        }

        mEtModbusAddress.setText(collectorSensorParamsInfoSub.getSensorAddress());
        userConfig = UserConfig.getConfig(mContext, String.valueOf(channelNumber));
        mEtNote.setText(userConfig.readString(String.valueOf(channelNumber)));
        oldAddress = mEtModbusAddress.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();

    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.rl_scan_config) {

            } else if (view.getId() == R.id.tv_save) {
                newAddress = mEtModbusAddress.getText().toString().trim();
                newAlarmValue = mEtAlarmValue.getText().toString().trim();
                newCorrectValue = mEtCorrectValue.getText().toString().trim();

                if (TextUtils.isEmpty(newAddress) || !ValidateUtil.isInteger(newAddress) || Integer.parseInt(newAddress) < 0 || Integer.parseInt(newAddress) > 99) {
                    ToastUtils.show("请输入正确的地址");
                    return;
                }

                if (TextUtils.isEmpty(newAlarmValue) || !ValidateUtil.isInteger(newAlarmValue)) {
                    ToastUtils.show("请输入正确的触发值");
                    return;
                }

                if (TextUtils.isEmpty(newCorrectValue) || (!ValidateUtil.isInteger(newCorrectValue) && !ValidateUtil.isDouble(newCorrectValue))) {
                    ToastUtils.show("请输入正确的修正值");
                    return;
                }

                updateParamsInfo(null, true);
                userConfig.writeString(String.valueOf(channelNumber), mEtNote.getText().toString().trim());
                if (myOnClickListener.onSureClick(view)) {
                    dialog.dismiss();
                }
            } else if (view.getId() == R.id.tv_cancel) {
                myOnClickListener.onCancelClick(view, channelNumber);
                dialog.dismiss();
            }
        }
    };

    private void updateParamsInfo(CollectorModel collectorModel, boolean isSure) {

    }


    public Osmometer_AxialForceGaugeDialog setMyOnClickListener(MyOnClickListener listener) {
        this.myOnClickListener = listener;
        return this;
    }

    @Override
    public CollectorSensorParamsInfoSub getData() {
        return collectorSensorParamsInfoSub;
    }
}
