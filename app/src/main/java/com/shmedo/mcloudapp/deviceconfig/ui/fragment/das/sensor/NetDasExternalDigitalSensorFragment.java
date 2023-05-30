package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.DebouncingUtils;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/16 <br/>
 * 描述：     TODO
 */
public class NetDasExternalDigitalSensorFragment extends BaseFragment {
    /**
     * 阵列测斜仪模型切换
     */
    @BindView(R.id.tv_model_switch)
    TextView mTvModelSwitch;

    @BindView(R.id.modelSwitchLayout)
    ViewGroup modelSwitchLayout;
    /**
     * 传感器子类型
     */
    @BindView(R.id.tv_child_sensor_title)
    TextView mTvChildSensorTitle;

    @BindView(R.id.tv_child_sensor_type)
    TextView mTvChildSensorType;

    @BindView(R.id.childSensorTypeLayout)
    ViewGroup childSensorTypeLayout;

    /**
     * 地址
     */
    @BindView(R.id.et_modbus_address)
    EditText mEtModbusAddress;

    /**
     * 触发值
     */
    @BindView(R.id.tv_triggerThreshold)
    TextView mTvAlarmValue;

    @BindView(R.id.et_trigger_threshold)
    EditText mEtAlarmValue;

    /**
     * 修正值
     */
    @BindView(R.id.tv_correctionValue)
    TextView mTvCorrectValue;

    @BindView(R.id.et_revised)
    EditText mEtCorrectValue;

    @BindView(R.id.correction_layout)
    ViewGroup correctionLayout;

    /**
     * 扩展字段1
     */
    @BindView(R.id.tv_extension1)
    TextView mTvExtension1;

    @BindView(R.id.et_extension1)
    EditText mEtExtension1;

    @BindView(R.id.iv_extension1)
    ImageView mIvExtension1;

    @BindView(R.id.extension_layout1)
    ViewGroup extensionLayout1;

    /**
     * 扩展字段2
     */
    @BindView(R.id.tv_extension2)
    TextView mTvExtension2;

    @BindView(R.id.et_extension2)
    EditText mEtExtension2;

    @BindView(R.id.iv_extension2)
    ImageView mIvExtension2;

    @BindView(R.id.extension_layout2)
    ViewGroup extensionLayout2;

    /**
     * 扩展字段3
     */
    @BindView(R.id.tv_extension3)
    TextView mTvExtension3;

    @BindView(R.id.et_extension3)
    EditText mEtExtension3;

    @BindView(R.id.iv_extension3)
    ImageView mIvExtension3;

    @BindView(R.id.extension_layout3)
    ViewGroup extensionLayout3;

    private DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private IOTSensorType iotSensorType;//传感器类型
    private ArrayList<String> addressList = new ArrayList<>();
    private DasExternalSensorInfo externalSensorInfo;
    private String modelType;//阵列测斜仪物模型类型
    private String childRadarType;//子雷达类型
    private String sensorAddress, triggerThreshold, correctValue;
    private String exValue1, exValue2, exValue3;

    //阵列测斜仪物模型
    private List<String> modelTypeList = Arrays.asList("坐标模型", "ADME 模型");
    //子雷达类型
    private List<String> childRadarTypeList = Arrays.asList("雷达物位计", "精波雷达");

    public static NetDasExternalDigitalSensorFragment newInstance(ArrayList<String> addressList, IOTSensorType sensorType, DasExternalSensorInfo externalSensorInfo) {
        NetDasExternalDigitalSensorFragment fragment = new NetDasExternalDigitalSensorFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST, addressList);
        args.putSerializable(AppContants.Extras.SENSOR_TYPE, sensorType);
        args.putSerializable(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            addressList = getArguments().getStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST);
            externalSensorInfo = (DasExternalSensorInfo) getArguments().getSerializable(AppContants.Extras.SENSOR_PARAM);
            iotSensorType = (IOTSensorType) getArguments().getSerializable(AppContants.Extras.SENSOR_TYPE);
            if (externalSensorInfo != null && !TextUtils.isEmpty(externalSensorInfo.getAddr())) {
                addressList.remove(externalSensorInfo.getAddr());
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.das_external_digital_sensor_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        initValue();
    }

    private void setView() {
        mEtModbusAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtExtension1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtExtension2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtExtension3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void initValue() {
        try {
            if (externalSensorInfo == null) {
                externalSensorInfo = new DasExternalSensorInfo();
            }
            sensorAddress = externalSensorInfo.getAddr();
            triggerThreshold = externalSensorInfo.getThreshold();
            correctValue = externalSensorInfo.getCorrval();
            switch (iotSensorType) {
                case RAIN_GAUGE://压电式雨量计
                case WIRE_SHIFT://拉线位移计
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    break;

                case SOIL_MOISTURE://土壤含水率
                    mTvAlarmValue.setText("触发值(单位:%rh)");
                    mTvCorrectValue.setText("修正值(单位:%rh)");
                    break;

                case INCLINOMETER://测斜仪
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:m)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("测段长(单位:mm)");
                    exValue1 = externalSensorInfo.getSpacing();
                    if (!TextUtils.isEmpty(exValue1) && !exValue1.equals("NullKey")) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    modelType = externalSensorInfo.getModel_type();
                    if (!TextUtils.isEmpty(modelType) && !modelType.equals("NullKey")) {
                        modelSwitchLayout.setVisibility(View.VISIBLE);
                        mTvModelSwitch.setText(modelType.equals("0") ? modelTypeList.get(0) : modelTypeList.get(1));

                        extensionLayout2.setVisibility(View.VISIBLE);
                        mEtExtension2.setEnabled(false);
                        mEtExtension2.setHint("");
                        mTvExtension2.setText("解算方式");
                        mEtExtension2.setText(externalSensorInfo.getDatatype().equals("0") ? "顶部" : "底部");

                        extensionLayout3.setVisibility(View.VISIBLE);
                        mEtExtension3.setEnabled(false);
                        mEtExtension3.setHint("");
                        mTvExtension3.setText("测量间隔(ms)");
                        mEtExtension3.setText(externalSensorInfo.getMeasinval());
                    }
                    break;

                case ULTRASONIC_LEVEL_GAUGE://超声波物位计
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("安装高程(单位:m)");
                    break;

                case RADAR_LEVEL_GAUGE://雷达物位计
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("安装高程(单位:m)");
                    childRadarType = externalSensorInfo.getChild_type();
                    if (!TextUtils.isEmpty(childRadarType) && !childRadarType.equals("NullKey")) {
                        childSensorTypeLayout.setVisibility(View.VISIBLE);
                        mTvChildSensorTitle.setText("雷达类型");
                        if (childRadarType.equals("1"))
                            mTvChildSensorType.setText(childRadarTypeList.get(0));
                        else
                            mTvChildSensorType.setText(childRadarTypeList.get(1));
                    }
                    break;

                case LUYAN_INCLINOMETER://倾角仪
                    mTvAlarmValue.setText("触发值(单位:°)");
                    correctionLayout.setVisibility(View.GONE);
                    extensionLayout1.setVisibility(View.VISIBLE);
                    extensionLayout2.setVisibility(View.VISIBLE);
                    extensionLayout3.setVisibility(View.VISIBLE);
                    mIvExtension1.setVisibility(View.VISIBLE);
                    mIvExtension2.setVisibility(View.VISIBLE);
                    mIvExtension3.setVisibility(View.VISIBLE);

                    mTvExtension1.setText("X轴初始角度(°)");
                    mTvExtension2.setText("Y轴初始角度(°)");
                    mTvExtension3.setText("Z轴初始角度(°)");
                    exValue1 = externalSensorInfo.getInitvalx();
                    exValue2 = externalSensorInfo.getInitvaly();
                    exValue3 = externalSensorInfo.getInitvalz();
                    if (!TextUtils.isEmpty(exValue1) && !exValue1.equals("NullKey")) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    if (!TextUtils.isEmpty(exValue2) && !exValue2.equals("NullKey")) {
                        exValue2 = decimalFormat.format(Double.parseDouble(exValue2));
                        mEtExtension2.setText(exValue2);
                    }
                    if (!TextUtils.isEmpty(exValue3) && !exValue3.equals("NullKey")) {
                        exValue3 = decimalFormat.format(Double.parseDouble(exValue3));
                        mEtExtension3.setText(exValue3);
                    }
                    break;

                case INFRASOUND://次声
                    mTvAlarmValue.setText("触发值(单位:Hz)");
                    mTvCorrectValue.setText("修正值(单位:Hz)");
                    break;

                case WEIR://量水堰计
                    mTvAlarmValue.setText("触发值(单位:m³/s)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    extensionLayout2.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("初始读数(单位:mm)");
                    mTvExtension2.setText("堰上水头(单位:mm)");
                    exValue1 = externalSensorInfo.getLsycsds();
                    exValue2 = externalSensorInfo.getLsyysst();
                    if (!TextUtils.isEmpty(exValue1) && !exValue1.equals("NullKey")) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    if (!TextUtils.isEmpty(exValue2) && !exValue2.equals("NullKey")) {
                        exValue2 = decimalFormat.format(Double.parseDouble(exValue2));
                        mEtExtension2.setText(exValue2);
                    }
                    break;

                case STATIC_LEVEL://静力水准
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("高程(单位:m)");
                    exValue1 = externalSensorInfo.getTubealti();
                    if (!TextUtils.isEmpty(exValue1) && !exValue1.equals("NullKey")) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    break;

                case WEATHER_STATION://气象计
                case TURBIDITY_METER://浊度仪
                    mTvAlarmValue.setText("触发值(单位:m/s)");
                    mTvCorrectValue.setText("修正值(单位:m/s)");
                    break;

                case DIGITAL_WATER_LEVEL_GAUGE://数字式水位计
                    mTvAlarmValue.setText("触发值(单位:mm)");
                    mTvCorrectValue.setText("修正值(单位:mm)");
                    extensionLayout1.setVisibility(View.VISIBLE);
                    extensionLayout2.setVisibility(View.VISIBLE);
                    mTvExtension1.setText("安装高程(单位:m)");
                    mTvExtension2.setText("绳长(单位:m)");
                    exValue1 = externalSensorInfo.getTubealti();
                    exValue2 = externalSensorInfo.getRopelen();
                    if (!TextUtils.isEmpty(exValue1) && !exValue1.equals("NullKey")) {
                        exValue1 = decimalFormat.format(Double.parseDouble(exValue1));
                        mEtExtension1.setText(exValue1);
                    }
                    if (!TextUtils.isEmpty(exValue2) && !exValue2.equals("NullKey")) {
                        exValue2 = decimalFormat.format(Double.parseDouble(exValue2));
                        mEtExtension2.setText(exValue2);
                    }
                    break;
            }

            mEtModbusAddress.setText(sensorAddress);
            if (!TextUtils.isEmpty(triggerThreshold)) {
                triggerThreshold = decimalFormat.format(Double.parseDouble(triggerThreshold));
                mEtAlarmValue.setText(triggerThreshold);
            }
            if (!TextUtils.isEmpty(correctValue)) {
                correctValue = decimalFormat.format(Double.parseDouble(correctValue));
                mEtCorrectValue.setText(correctValue);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnClick({R.id.modelSwitchLayout, R.id.childSensorTypeLayout, R.id.iv_extension1, R.id.iv_extension2, R.id.iv_extension3, R.id.btn_confirm})
    public void onClick(View view) {
        if (!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.modelSwitchLayout) {
            showModelSwitchDialog();

        } else if (id == R.id.childSensorTypeLayout) {
            showChildRadarTypeListDialog();
        } else if (id == R.id.iv_extension1) {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showTipDialog("初始值大于 360，设备将自动计算");

        } else if (id == R.id.iv_extension2) {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showTipDialog("初始值大于 360，设备将自动计算");

        } else if (id == R.id.iv_extension3) {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showTipDialog("初始值大于 360，设备将自动计算");

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    /**
     * 阵列测斜仪选择物模型
     */
    private void showModelSwitchDialog() {
        int pos = modelTypeList.indexOf(String.valueOf(mTvModelSwitch.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", modelTypeList.toArray(new String[0]),
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvModelSwitch.setText(text);
                                if (text.equals(modelTypeList.get(0))) {
                                    modelType = "0";
                                } else if (text.equals(modelTypeList.get(1))) {
                                    modelType = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择子雷达传感器类型
     */
    private void showChildRadarTypeListDialog() {
        int pos = childRadarTypeList.indexOf(String.valueOf(mTvChildSensorType.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", childRadarTypeList.toArray(new String[0]),
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvChildSensorType.setText(text);
                                if (text.equals(childRadarTypeList.get(0))) {
                                    childRadarType = "1";
                                } else if (text.equals(childRadarTypeList.get(1))) {
                                    childRadarType = "3";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }


    private boolean checkValueIsValid() {
        sensorAddress = mEtModbusAddress.getText().toString().trim();
        triggerThreshold = mEtAlarmValue.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        exValue1 = mEtExtension1.getText().toString().trim();
        exValue2 = mEtExtension2.getText().toString().trim();
        if (TextUtils.isEmpty(sensorAddress)) {
            ToastUtils.show("传感器地址不能为空!");
            mEtModbusAddress.requestFocus();
            return false;
        }
        if (!ValidateUtil.isInteger(sensorAddress) || Integer.parseInt(sensorAddress) <= 0) {
            ToastUtils.show("请输入正确的传感器地址!");
            mEtModbusAddress.requestFocus();
            return false;
        }

        int num = 0;
        for (String ss : addressList) {
            if (ss.equals(sensorAddress)) {
                num++;
            }
        }
        if (num >= 1) {
            ToastUtils.show("传感器地址不能重复!");
            mEtModbusAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("触发值不能为空!");
            mEtAlarmValue.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(triggerThreshold);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的触发值!");
            mEtAlarmValue.requestFocus();
            return false;
        }

        if (iotSensorType != IOTSensorType.LUYAN_INCLINOMETER) {
            if (TextUtils.isEmpty(correctValue)) {
                ToastUtils.show("修正值不能为空!");
                mEtCorrectValue.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(correctValue);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的修正值!");
                mEtCorrectValue.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.INCLINOMETER) {//测斜仪
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("测段长值不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测段长值!");
                mEtExtension1.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) {//倾角仪
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("X轴初始值不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的X轴初始值!");
                mEtExtension1.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(exValue2)) {
                ToastUtils.show("Y轴初始值不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的Y轴初始值!");
                mEtExtension2.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(exValue3)) {
                ToastUtils.show("Z轴初始值不能为空!");
                mEtExtension3.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue3);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的Z轴初始值!");
                mEtExtension3.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.WEIR) {//量水堰计
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("初始读数不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的初始读数!");
                mEtExtension1.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(exValue2)) {
                ToastUtils.show("堰上水头不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的堰上水头!");
                mEtExtension2.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.STATIC_LEVEL) {//静力水准
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("高程值不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的高程值!");
                mEtExtension1.requestFocus();
                return false;
            }
        }

        if (iotSensorType == IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE) {//数字式水位计
            if (TextUtils.isEmpty(exValue1)) {
                ToastUtils.show("高程值不能为空!");
                mEtExtension1.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue1);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的高程值!");
                mEtExtension1.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(exValue2)) {
                ToastUtils.show("绳长不能为空!");
                mEtExtension2.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(exValue2);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的绳长!");
                mEtExtension2.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void processSave() {
        if (externalSensorInfo == null)
            externalSensorInfo = new DasExternalSensorInfo();

        externalSensorInfo.setType(iotSensorType.getCode());
        externalSensorInfo.setAddr(sensorAddress);
        externalSensorInfo.setThreshold(triggerThreshold);
        externalSensorInfo.setCorrval(correctValue);
        switch (iotSensorType) {
            case INCLINOMETER:
                externalSensorInfo.setSpacing(exValue1);//测斜仪
                if (!TextUtils.isEmpty(modelType) && !modelType.equals("NullKey"))
                    externalSensorInfo.setModel_type(modelType);
                else
                    externalSensorInfo.setModel_type("NullKey");
                break;

            case LUYAN_INCLINOMETER://倾角仪
                externalSensorInfo.setInitvalx(exValue1);
                externalSensorInfo.setInitvaly(exValue2);
                externalSensorInfo.setInitvalz(exValue3);
                break;

            case WEIR://量水堰计
                externalSensorInfo.setLsycsds(exValue1);
                externalSensorInfo.setLsyysst(exValue2);
                break;

            case STATIC_LEVEL://静力水准
                externalSensorInfo.setTubealti(exValue1);
                break;

            case DIGITAL_WATER_LEVEL_GAUGE://数字式水位计
                externalSensorInfo.setTubealti(exValue1);
                externalSensorInfo.setRopelen(exValue2);
                break;

            case RADAR_LEVEL_GAUGE://雷达液(物)位计 设置子雷达传感器型号
                if (!TextUtils.isEmpty(childRadarType) && !childRadarType.equals("NullKey"))
                    externalSensorInfo.setChild_type(childRadarType);
                else
                    externalSensorInfo.setChild_type("NullKey");
                break;

            default:
                break;
        }
        Intent intent = new Intent();
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    private void setResult() {
        Intent intent = new Intent();
        mActivity.setResult(Activity.RESULT_CANCELED, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
        return false;
    }
}
