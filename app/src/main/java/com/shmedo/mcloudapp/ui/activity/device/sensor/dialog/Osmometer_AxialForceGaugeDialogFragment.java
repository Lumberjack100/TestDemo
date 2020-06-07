package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.ui.activity.device.sensor.view.SensorBGK4500View;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 基康渗压计(BGK-4500)、葛南渗压计(VWP-03)、军星轴力计(ZLJ-300T)
 */
public class Osmometer_AxialForceGaugeDialogFragment extends BaseDialogFragment {
    @BindView(R.id.spinner)
    Spinner spinnerType;

    @BindView(R.id.sensorBGK4500View)
    SensorBGK4500View sensorBGK4500View;

    //基康渗压计、葛南渗压计、轴力计布局
//    @BindView(R.id.bgk_osmometer_config_layout)
//    ViewGroup bgkOsmometerConfigLayout;
    @BindView(R.id.ngn_osmometer_config_layout)
    ViewGroup ngnOsmometerConfigLayout;
    @BindView(R.id.axialforcegauge_config_layout)
    ViewGroup axialforcegaugeConfigLayout;

    //基康渗压计(BGK-4500)参数
    @BindView(R.id.polynomialRatioA)
    EditText mEtCoefficientA;//多项式系数A
    @BindView(R.id.polynomialRatioB)
    EditText mEtCoefficientB;//多项式系数B
    @BindView(R.id.polynomialRatioC)
    EditText mEtCoefficientC;//多项式系数C
    @BindView(R.id.temperatureCoefficientK)
    EditText mEtCoefficientK;//温度系数K

    //葛南渗压计特有参数
    @BindView(R.id.sensitivityCoefficient)
    EditText mEtSensitivityCoefficient;//灵敏度k
    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温修系数b


    @BindView(R.id.et_modbus_address)
    EditText mEtModbusAddress;//通道号
    @BindView(R.id.et_install_elevation)
    EditText mEtNozzelHeight;//安装高程
    @BindView(R.id.et_osmometer_cord)
    EditText mEtOsmometerCord;//绳长
    @BindView(R.id.et_trigger_threshold)
    EditText mEtAlarmValue;//触发阀值
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//修正值
    @BindView(R.id.et_initialtemperature)
    EditText mEtTemperature;//初始温度
    @BindView(R.id.et_ReferenceValue)
    EditText mEtReferenceValue;//基准值

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;
    private String channelNumber;
    private String oldCoefficientA, newCoefficientA, oldCoefficientB, newCoefficientB, oldCoefficientC, newCoefficientC, oldCoefficientK, newCoefficientK;
    private String oldSensitivityCoefficient, newSensitivityCoefficient, oldTemperatureCoefficient, newTemperatureCoefficient;
    private String oldAddress, newAddress, oldNozzelHeight, newNozzelHeight, oldOsmometerCord, newOsmometerCord, oldAlarmValue, newAlarmValue, oldCorrectValue, newCorrectValue;


    private static final String ARG_PARAM1 = "param1";


    public Osmometer_AxialForceGaugeDialogFragment() {
        // Required empty public constructor
    }


    public static Osmometer_AxialForceGaugeDialogFragment newInstance(CollectorSensorParamsInfoSub infoSub) {
        Osmometer_AxialForceGaugeDialogFragment fragment = new Osmometer_AxialForceGaugeDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, infoSub);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_osmometer__axial_force_gauge_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectorSensorParamsInfoSub = (CollectorSensorParamsInfoSub) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        setValue();
        return rootView;
    }

    private void initView() {
        String sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case "50"://基康渗压计(BGK-4500)
                sensorBGK4500View.setVisibility(View.VISIBLE);
                ngnOsmometerConfigLayout.setVisibility(View.GONE);
                axialforcegaugeConfigLayout.setVisibility(View.GONE);
                break;

            case "51"://葛南渗压计(VWP-03)
                sensorBGK4500View.setVisibility(View.GONE);
                ngnOsmometerConfigLayout.setVisibility(View.VISIBLE);
                axialforcegaugeConfigLayout.setVisibility(View.GONE);
                break;

            case "58"://军星轴力计(ZLJ-300T)
                sensorBGK4500View.setVisibility(View.GONE);
                ngnOsmometerConfigLayout.setVisibility(View.GONE);
                axialforcegaugeConfigLayout.setVisibility(View.VISIBLE);
                break;
        }
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        //传感器类型
        String[] stringArray = getActivity().getResources().getStringArray(R.array.sensor_osmometer);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item) {
                    case "渗压计-BGK":
                        sensorBGK4500View.setVisibility(View.VISIBLE);
                        ngnOsmometerConfigLayout.setVisibility(View.GONE);
                        axialforcegaugeConfigLayout.setVisibility(View.GONE);
                        break;

                    case "渗压计-NGN":
                        sensorBGK4500View.setVisibility(View.GONE);
                        ngnOsmometerConfigLayout.setVisibility(View.VISIBLE);
                        axialforcegaugeConfigLayout.setVisibility(View.GONE);
                        break;

                    case "轴力计":
                        sensorBGK4500View.setVisibility(View.GONE);
                        ngnOsmometerConfigLayout.setVisibility(View.GONE);
                        axialforcegaugeConfigLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setValue() {
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
        oldAddress = mEtModbusAddress.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
        oldNozzelHeight = mEtNozzelHeight.getText().toString().trim();
    }


    @OnClick({R.id.rl_scan_config, R.id.tv_cancel, R.id.tv_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_scan_config:
                doScanButtonClick();
                break;

            case R.id.tv_cancel:
                doNegativeClick(view);
                break;
            case R.id.tv_save:
                doPositiveClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
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
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        if (listener.onPositiveClick(view)) {
            dismiss();
        }
    }

    private void doNegativeClick(View view) {
        // Do stuff here.
        updateParamsInfo(null, false);
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }

    private void updateParamsInfo(CollectorModel collectorModel, boolean isSure) {

    }
}
