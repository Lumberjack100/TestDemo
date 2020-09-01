package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.model.MqttConfigInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * MQTT配置参数对话框
 */
public class MqttConfigDialogFragment extends BaseDialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.communication_protocol)
    Spinner spCommunicationProtocol;
    @BindView(R.id.sp_registration_platform)
    Spinner spRegistrationPlatform;

    @BindView(R.id.ll_register_platform)
    ViewGroup llRegisterPlatform;
    @BindView(R.id.ll_app_key)
    ViewGroup llAppKey;
    @BindView(R.id.ll_register_address)
    ViewGroup llRegisterAddress;
    @BindView(R.id.ll_keep_alive)
    ViewGroup llKeepAlive;
    @BindView(R.id.ll_device_config)
    ViewGroup llDeviceConfig;
    @BindView(R.id.ll_mqtt_config)
    ViewGroup llMqttConfig;

    @BindView(R.id.cet_service_address)
    ClearEditText mEtServiceAddress;
    @BindView(R.id.cet_app_Key)
    ClearEditText mEtAppKey;
    @BindView(R.id.cet_register_address)
    ClearEditText mEtRegisterPlatformAddress;
    @BindView(R.id.cet_KeepAlive_value)
    ClearEditText mEtKeepAliveValue;
    @BindView(R.id.cet_device_sn)
    ClearEditText mEtDeviceSn;
    @BindView(R.id.cet_product_id)
    ClearEditText mEtProductId;
    @BindView(R.id.cet_registration_code)
    ClearEditText mEtRegistrationCode;
    @BindView(R.id.cet_mqtt_device_id)
    ClearEditText mEtMqttDeviceId;
    @BindView(R.id.cet_mqtt_username)
    ClearEditText mEtMqttUsername;
    @BindView(R.id.cet_mqtt_password)
    ClearEditText mEtMqttPassword;

    private ArrayAdapter<String> protocolAdapter;
    private ArrayAdapter<String> registerAdapter;

    private String linkNumber;
    private String communicationProtocol;
    private String dataPlatformAddress;
    private String keepAliveValue;
    private String deviceSn;
    private String productId;
    private String registerCode;
    private String registerPlatform;
    private String registerPlatformAddress;
    private String appKey;
    private String mqttDeviceId;
    private String mqttUsername;
    private String mqttPassword;
    private MqttConfigInfo mqttConfigInfoSub;


    public static MqttConfigDialogFragment newInstance(MqttConfigInfo mqttConfigInfo, String linkNumber) {
        MqttConfigDialogFragment fragment = new MqttConfigDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, mqttConfigInfo);
        args.putString(ARG_PARAM2, linkNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mqttConfigInfoSub = (MqttConfigInfo) getArguments().getSerializable(ARG_PARAM1);
            linkNumber = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mqtt_config_dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        initProtocolAdapter();
        initRegisterPlatformAdapter();
        setValue();
        return rootView;
    }

    private void initProtocolAdapter() {
        //通讯协议
        String[] protocolData = getResources().getStringArray(R.array.communication_protocol);
        protocolAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, protocolData);
        protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCommunicationProtocol.setAdapter(protocolAdapter);
        spCommunicationProtocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item) {
                    case "MDM协议":
                        communicationProtocol = "2";
                        llAppKey.setVisibility(View.GONE);
                        llRegisterPlatform.setVisibility(View.GONE);
                        llRegisterAddress.setVisibility(View.GONE);
                        llKeepAlive.setVisibility(View.GONE);
                        llDeviceConfig.setVisibility(View.GONE);
                        llMqttConfig.setVisibility(View.GONE);
                        break;
                    case "MQTT自动注册":
                        communicationProtocol = "4";
                        llRegisterPlatform.setVisibility(View.VISIBLE);
                        llRegisterAddress.setVisibility(View.VISIBLE);
                        llKeepAlive.setVisibility(View.VISIBLE);
                        llDeviceConfig.setVisibility(View.VISIBLE);
                        llMqttConfig.setVisibility(View.GONE);
                        break;
                    case "MQTT手动注册":
                        communicationProtocol = "5";
                        llAppKey.setVisibility(View.GONE);
                        llRegisterPlatform.setVisibility(View.GONE);
                        llRegisterAddress.setVisibility(View.GONE);
                        llKeepAlive.setVisibility(View.VISIBLE);
                        llDeviceConfig.setVisibility(View.GONE);
                        llMqttConfig.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initRegisterPlatformAdapter() {
        //注册平台
        String[] registerData = getResources().getStringArray(R.array.register);
        registerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, registerData);
        registerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegistrationPlatform.setAdapter(registerAdapter);
        spRegistrationPlatform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item) {
                    case "地大平台":
                        registerPlatform = "0";
                        llAppKey.setVisibility(View.GONE);
                        break;
                    case "成都理工平台":
                        registerPlatform = "1";
                        llAppKey.setVisibility(View.GONE);
                        break;
                    case "米度平台":
                        registerPlatform = "2";
                        llAppKey.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void setValue() {
        if (mqttConfigInfoSub == null) {
            Timber.w("传递的 MqttConfigInfo 参数为空!");
            return;
        }

        //配置参数
        tvTitle.setText("中心" + linkNumber + "配置");
        switch (mqttConfigInfoSub.getCommunicationProtocol()) {
            case "2":
                spCommunicationProtocol.setSelection(0);
                break;
            case "4":
                spCommunicationProtocol.setSelection(1);
                break;
            case "5":
                spCommunicationProtocol.setSelection(2);
                break;
        }
        mEtServiceAddress.setText(mqttConfigInfoSub.getDataPlatformAddress().trim());

        switch (mqttConfigInfoSub.getRegisterPlatform()) {
            case "0"://地大平台
                spRegistrationPlatform.setSelection(0);
                break;
            case "1"://成都理工平台
                spRegistrationPlatform.setSelection(1);
                break;
            case "2"://米度平台
                spRegistrationPlatform.setSelection(2);
                break;
            default:
                spRegistrationPlatform.setSelection(0);
                break;
        }
        mEtAppKey.setText(mqttConfigInfoSub.getAppKey());
        mEtRegisterPlatformAddress.setText(mqttConfigInfoSub.getRegisterPlatformAddress().trim());
        mEtKeepAliveValue.setText(mqttConfigInfoSub.getKeepAliveValue());
        mEtDeviceSn.setText(mqttConfigInfoSub.getDeviceSn());
        mEtProductId.setText(mqttConfigInfoSub.getProductId());
        mEtRegistrationCode.setText(mqttConfigInfoSub.getRegisterCode());

        mEtMqttDeviceId.setText(mqttConfigInfoSub.getMqttDeviceId());
        mEtMqttUsername.setText(mqttConfigInfoSub.getMqttUsername());
        mEtMqttPassword.setText(mqttConfigInfoSub.getMqttPassword());
    }

    @OnClick({R.id.mqtt_cancel, R.id.mqtt_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mqtt_cancel:
                doNegativeClick(view);
                break;

            case R.id.mqtt_save:
                doPositiveClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
        if (!checkValue()) {
            Timber.w("传感器参数存在错误!");
            return;
        }

        updateSensorData();
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        if (listener.onPositiveClick(view, mqttConfigInfoSub)) {
            dismiss();
        }
    }

    private void doNegativeClick(View view) {
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }

    private boolean checkValue() {
        dataPlatformAddress = mEtServiceAddress.getText().toString().trim();
        appKey = mEtAppKey.getText().toString().trim();
        registerPlatformAddress = mEtRegisterPlatformAddress.getText().toString().trim();
        keepAliveValue = mEtKeepAliveValue.getText().toString().trim();
        deviceSn = mEtDeviceSn.getText().toString().trim();
        productId = mEtProductId.getText().toString().trim();
        registerCode = mEtRegistrationCode.getText().toString().trim();
        mqttDeviceId = mEtMqttDeviceId.getText().toString().trim();
        mqttUsername = mEtMqttUsername.getText().toString().trim();
        mqttPassword = mEtMqttPassword.getText().toString().trim();

        if (TextUtils.isEmpty(dataPlatformAddress)) {
            ToastUtils.show("数据服务器地址不能为空!");
            return false;
        }

        String item = spCommunicationProtocol.getSelectedItem().toString();
        if (item.equals("MQTT自动注册")) {
            if (TextUtils.isEmpty(registerPlatformAddress)) {
                ToastUtils.show("注册平台地址不能为空!");
                return false;
            }

            if (TextUtils.isEmpty(keepAliveValue)) {
                ToastUtils.show("KeepAlive值不能为空!");
                return false;
            }
            if (TextUtils.isEmpty(deviceSn)) {
                ToastUtils.show("设备SN号不能为空!");
                return false;
            }
            if (TextUtils.isEmpty(productId)) {
                ToastUtils.show("产品ID不能为空!");
                return false;
            }
            if (TextUtils.isEmpty(registerCode)) {
                ToastUtils.show("注册码不能为空!");
                return false;
            }

            if (registerPlatform.equals("2")) {
                if (TextUtils.isEmpty(appKey)) {
                    ToastUtils.show("AppKey不能为空!");
                    return false;
                }
            }
        } else if (item.equals("MQTT手动注册")) {
            if (TextUtils.isEmpty(keepAliveValue)) {
                ToastUtils.show("KeepAlive值不能为空!");
                return false;
            }
            if (TextUtils.isEmpty(mqttDeviceId)) {
                ToastUtils.show("MQTT设备ID不能为空!");
                return false;
            }
            if (TextUtils.isEmpty(mqttUsername)) {
                ToastUtils.show("MQTT用户名不能为空!");
                return false;
            }
            if (TextUtils.isEmpty(mqttPassword)) {
                ToastUtils.show("MQTT密码不能为空!");
                return false;
            }
        }

        return true;
    }

    private void updateSensorData() {
        mqttConfigInfoSub.setCommunicationProtocol(communicationProtocol);
        mqttConfigInfoSub.setDataPlatformAddress(dataPlatformAddress);
        mqttConfigInfoSub.setKeepAliveValue(keepAliveValue);
        mqttConfigInfoSub.setDeviceSn(deviceSn);
        mqttConfigInfoSub.setProductId(productId);
        mqttConfigInfoSub.setRegisterCode(registerCode);
        mqttConfigInfoSub.setRegisterPlatform(registerPlatform);
        mqttConfigInfoSub.setRegisterPlatformAddress(registerPlatformAddress);
        mqttConfigInfoSub.setAppKey(appKey);
        mqttConfigInfoSub.setMqttDeviceId(mqttDeviceId);
        mqttConfigInfoSub.setMqttUsername(mqttUsername);
        mqttConfigInfoSub.setMqttPassword(mqttPassword);
    }
}
