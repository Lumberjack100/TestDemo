package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.APPKeyEntity;
import com.shmedo.configlibrary.ble.cmd.entity.DataCenterCommunicateProtoclEntity;
import com.shmedo.configlibrary.ble.cmd.entity.MQTTKeepAliveEntity;
import com.shmedo.configlibrary.ble.cmd.entity.RegistrationPlatformEntity;
import com.shmedo.configlibrary.ble.cmd.entity.RegistrationPlatformSelectionEntity;
import com.shmedo.configlibrary.ble.cmd.entity.ServerAddressInfoEntity;
import com.shmedo.configlibrary.ble.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.configlibrary.ble.model.MqttConfigInfo;
import com.shmedo.configlibrary.ble.model.ServerAddressInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleDataCenterServerConfigFragment extends BaseBleConnectFragment {

    @BindView(R.id.ll_child_items)
    ViewGroup childItemsLayout;

    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbCenterEnable;

    @BindView(R.id.tv_communication_protocol)
    TextView mTvCommunicationProtocol;

    @BindView(R.id.tv_register_platform)
    TextView mTvRegisterPlatform;

    @BindView(R.id.et_data_server_address)
    ClearEditText mEtDataServerAddress;

    @BindView(R.id.appKeyET)
    ClearEditText mEtAppKey;

    @BindView(R.id.et_register_platform_address)
    ClearEditText mEtRegisterPlatformAddress;

    @BindView(R.id.et_keep_alive)
    ClearEditText mEtKeepAlive;

    @BindView(R.id.et_device_sn)
    ClearEditText mEtDeviceSN;

    @BindView(R.id.et_product_id)
    ClearEditText mEtProductId;

    @BindView(R.id.et_register_code)
    ClearEditText mEtRegisterCode;

    @BindView(R.id.et_mqtt_device_id)
    ClearEditText mEtMqttDeviceId;

    @BindView(R.id.et_mqtt_username)
    ClearEditText mEtMqttUsername;

    @BindView(R.id.et_mqtt_password)
    ClearEditText mEtMqttPwd;

    @BindView(R.id.ll_register_platform)
    ViewGroup registerPlatformLayout;

    @BindView(R.id.ll_app_key)
    ViewGroup appKeyLayout;

    @BindView(R.id.ll_register_platform_address)
    ViewGroup registerPlatformAddressLayout;

    @BindView(R.id.ll_keep_alive)
    ViewGroup keepAliveLayout;

    @BindView(R.id.ll_auto_register_special_info)
    ViewGroup autoRegisterSpecialInfoLayout;

    @BindView(R.id.ll_manual_register_special_info)
    ViewGroup manualRegisterSpecialInfoLayout;

    private static final String SERVER_NUMBER = "server_number";
    private ServerNumber serverNumber;
    private MqttConfigInfo mqttConfigInfo = new MqttConfigInfo();

    private int communicationProtocolPos;
    private int registerPlatformPos;

    private String communicationProtocol;//网络中心通讯协议
    private String dataServerAddress;//数据服务器地址、端口
    private String keepAliveValue;
    private String deviceSn;
    private String productId;
    private String registerCode;
    private String registerPlatform;//自动注册选择平台
    private String registerPlatformAddress;// 自动注册平台地址、端口
    private String appKey;//米度平台 AppKey
    private String mqttDeviceId;
    private String mqttUsername;
    private String mqttPassword;

    private String cmdCommunicationProtocol;//网络中心通讯协议
    private String cmdDataServerAddress;//设置数据服务器地址、端口
    private String cmdRegistrationPlatform;//自动注册选择平台
    private String cmdRegistrationPlatformAddress;// 自动注册平台地址、端口
    private String cmdKeepAliveValue;//
    private String cmdPlatformParam;//手动/自动注册平台参数
    private String cmdAppKey;//米度平台 AppKey


    public static BleDataCenterServerConfigFragment newInstance(ServerNumber serverNumber) {
        BleDataCenterServerConfigFragment fragment = new BleDataCenterServerConfigFragment();
        Bundle args = new Bundle();
        args.putSerializable(SERVER_NUMBER, serverNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverNumber = (ServerNumber) getArguments().getSerializable(SERVER_NUMBER);
        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_data_center_server_config;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSwitchViewListener();
        queryServerData();
    }

    private void setSwitchViewListener() {
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭数据中心？");
                } else {
                    childItemsLayout.setVisibility(View.VISIBLE);
                    ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                    sendCommonCommandImmediately(command);//查询数据中心1参数
                }
                Timber.i("mSbLinkOne===%s", isChecked);
            }
        });
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        childItemsLayout.setVisibility(View.GONE);
                        //确定关闭sb按钮。隐藏编辑字体
                        //发送对应关闭中心
                        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
                        String cmdAddress1 = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, serverNumberEntity);
                        sendCommonCommandImmediately(cmdAddress1);//关闭服务器
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void queryServerData() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("正在获取参数...", 25000);
        ServerNumberEntity serverNumberEntity;

        switch (serverNumber) {
            case NUMBER_ONE:
                //获取服务器地址,查询中心开启状态
                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
                String cmdAddress1 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
                sendCommonCommand(cmdAddress1);
                Timber.d("查询服务器地址1指令===%s", cmdAddress1);
                break;

            case NUMBER_TWO:
                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
                String cmdAddress2 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
                sendCommonCommand(cmdAddress2);
                Timber.d("查询服务器地址2指令===%s", cmdAddress2);
                break;

            case NUMBER_THREE:
                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt());
                String cmdAddress3 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
                sendCommonCommand(cmdAddress3);
                Timber.d("查询服务器地址3指令===%s", cmdAddress3);
                break;
        }
    }

    private void queryDataCenterData() {
        ServerNumberEntity serverNumberEntity;
        String command;
        switch (serverNumber) {
            case NUMBER_ONE:
                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
                command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                sendCommonCommandImmediately(command);//查询数据中心1参数
                Timber.d("查询数据中心1参数===%s", command);
                break;

            case NUMBER_TWO:
                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
                command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                sendCommonCommandImmediately(command);//查询数据中心2参数
                Timber.d("查询数据中心2参数===%s", command);
                break;

            case NUMBER_THREE:
                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt());
                command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                sendCommonCommandImmediately(command);//查询数据中心3参数
                Timber.d("查询数据中心3参数===%s", command);
                break;
        }
    }


    @OnClick({R.id.communicationProtocolLayout, R.id.ll_register_platform, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.communicationProtocolLayout:
                showCommunicationProtocolDialog();
                break;

            case R.id.ll_register_platform:
                showRegisterPlatformDialog();
                break;

            case R.id.btn_confirm:
                KeyBordUtils.hideSoftKeyboard(view);

                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

                if (!checkValue()) {
                    Timber.w("传感器参数存在错误!");
                    return;
                }

                processSave();
                break;
        }
    }

    private void showCommunicationProtocolDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"MDM协议", "MQTT自动注册", "MQTT手动注册"},
                        null, communicationProtocolPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                updateViewByCommunicationProtocol(position, text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private void showRegisterPlatformDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"地大平台", "成都理工平台", "米度平台"},
                        null, registerPlatformPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                updateViewByRegisterPlatform(position, text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private void updateViewByCommunicationProtocol(int position, String text) {
        communicationProtocolPos = position;
        mTvCommunicationProtocol.setText(text);

        switch (text) {
            case "MDM协议":
                communicationProtocol = "2";
                appKeyLayout.setVisibility(View.GONE);
                registerPlatformLayout.setVisibility(View.GONE);
                registerPlatformAddressLayout.setVisibility(View.GONE);
                keepAliveLayout.setVisibility(View.GONE);
                autoRegisterSpecialInfoLayout.setVisibility(View.GONE);
                manualRegisterSpecialInfoLayout.setVisibility(View.GONE);
                break;

            case "MQTT自动注册":
                communicationProtocol = "4";
                registerPlatformLayout.setVisibility(View.VISIBLE);
                registerPlatformAddressLayout.setVisibility(View.VISIBLE);
                keepAliveLayout.setVisibility(View.VISIBLE);
                autoRegisterSpecialInfoLayout.setVisibility(View.VISIBLE);
                manualRegisterSpecialInfoLayout.setVisibility(View.GONE);
                break;

            case "MQTT手动注册":
                communicationProtocol = "5";
                appKeyLayout.setVisibility(View.GONE);
                registerPlatformLayout.setVisibility(View.GONE);
                registerPlatformAddressLayout.setVisibility(View.GONE);
                keepAliveLayout.setVisibility(View.VISIBLE);
                autoRegisterSpecialInfoLayout.setVisibility(View.GONE);
                manualRegisterSpecialInfoLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateViewByRegisterPlatform(int position, String text) {
        registerPlatformPos = position;
        mTvRegisterPlatform.setText(text);

        switch (text) {
            case "地大平台":
                registerPlatform = "0";
                appKeyLayout.setVisibility(View.GONE);
                break;

            case "成都理工平台":
                registerPlatform = "1";
                appKeyLayout.setVisibility(View.GONE);
                break;

            case "米度平台":
                registerPlatform = "2";
                appKeyLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private boolean checkValue() {
        dataServerAddress = mEtDataServerAddress.getText().toString().trim();
        appKey = mEtAppKey.getText().toString().trim();
        registerPlatformAddress = mEtRegisterPlatformAddress.getText().toString().trim();
        keepAliveValue = mEtKeepAlive.getText().toString().trim();
        deviceSn = mEtDeviceSN.getText().toString().trim();
        productId = mEtProductId.getText().toString().trim();
        registerCode = mEtRegisterCode.getText().toString().trim();
        mqttDeviceId = mEtMqttDeviceId.getText().toString().trim();
        mqttUsername = mEtMqttUsername.getText().toString().trim();
        mqttPassword = mEtMqttPwd.getText().toString().trim();

        if (TextUtils.isEmpty(dataServerAddress)) {
            ToastUtils.show("数据服务器地址不能为空!");
            return false;
        }

        if (communicationProtocol.equals("4")) {//MQTT自动注册
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

            if (registerPlatform.equals("2")) {//米度平台
                if (TextUtils.isEmpty(appKey)) {
                    ToastUtils.show("AppKey不能为空!");
                    return false;
                }
            }
        } else if (communicationProtocol.equals("5")) {//MQTT手动注册
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

    private void processSave() {
        //网络中心通讯协议
        DataCenterCommunicateProtoclEntity communicateProtoclEntity = new DataCenterCommunicateProtoclEntity(serverNumber.toInt(), Integer.parseInt(communicationProtocol));
        cmdCommunicationProtocol = CommandManager.getInstance().getCommand(CommandType.NET_LINK_COMMUN_PROTOCOL, communicateProtoclEntity);

        //数据服务器地址、端口
        String[] strs = dataServerAddress.trim().split(" ");
        ServerAddressInfoEntity addressInfoEntity = new ServerAddressInfoEntity(serverNumber.toInt(), strs[0], Integer.parseInt(strs[1]));
        cmdDataServerAddress = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, addressInfoEntity);

        if (communicationProtocol.equals("4")) {//MQTT自动注册
            //选择注册平台
            RegistrationPlatformSelectionEntity platformSelectionEntity = new RegistrationPlatformSelectionEntity(serverNumber.toInt(), Integer.parseInt(registerPlatform));
            cmdRegistrationPlatform = CommandManager.getInstance().getCommand(CommandType.AUTO_REGISTRATION_PLATFORM, platformSelectionEntity);

            //自动注册平台地址端口
            strs = registerPlatformAddress.trim().split(" ");
            addressInfoEntity = new ServerAddressInfoEntity(serverNumber.toInt(), strs[0], Integer.parseInt(strs[1]));
            cmdRegistrationPlatformAddress = CommandManager.getInstance().getCommand(CommandType.SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT, addressInfoEntity);

            //MQTT KeepAlive值
            MQTTKeepAliveEntity keepAliveEntity = new MQTTKeepAliveEntity(serverNumber.toInt(), Integer.parseInt(keepAliveValue));
            cmdKeepAliveValue = CommandManager.getInstance().getCommand(CommandType.MQTT_KEEP_ALIVE, keepAliveEntity);

            //自动注册平台参数：设备SN号+产品ID+注册码
            RegistrationPlatformEntity registrationPlatformEntity = new RegistrationPlatformEntity(serverNumber.toInt(), deviceSn, productId, registerCode);
            cmdPlatformParam = CommandManager.getInstance().getCommand(CommandType.SET_AUTO_REGISTRATION_PLATFORM_PARAM, registrationPlatformEntity);
            if (registerPlatform.equals("2")) {
                //appKey(米度/北京平台特有)
                APPKeyEntity appKeyEntity = new APPKeyEntity(serverNumber.toInt(), appKey);
                cmdAppKey = CommandManager.getInstance().getCommand(CommandType.SET_MEDO_PLATFORM_APPKEY, appKeyEntity);
            }
        } else if (communicationProtocol.equals("5")) {//MQTT手动注册
            //MQTT KeepAlive值
            MQTTKeepAliveEntity keepAliveEntity = new MQTTKeepAliveEntity(serverNumber.toInt(), Integer.parseInt(keepAliveValue));
            cmdKeepAliveValue = CommandManager.getInstance().getCommand(CommandType.MQTT_KEEP_ALIVE, keepAliveEntity);

            //手动注册平台参数：产品ID+设备ID+设备KEY
            RegistrationPlatformEntity registrationPlatformEntity = new RegistrationPlatformEntity(serverNumber.toInt(), mqttUsername, mqttDeviceId, mqttPassword);
            cmdPlatformParam = CommandManager.getInstance().getCommand(CommandType.SET_MANUAL_REGISTRATION_PLATFORM_PARAM, registrationPlatformEntity);
        }

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
        sendCommonCommandImmediately(cmdCommunicationProtocol);
        Timber.d("设置网络中心通讯协议===%s", cmdCommunicationProtocol);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        super.onMessageEvent(messageEvent);

        if (messageEvent instanceof CmdResponseMessage) {
            if (!isActive) {
                return;
            }
            setResultData((CmdResponseMessage) messageEvent);
        }
    }

    private void setResultData(CmdResponseMessage responseMessage) {
        String cmdStr = responseMessage.getResult();
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SERVER_ADDRESS://获取服务器1、2、3 的地址
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    Timber.e("查询服务器地址指令出错!");
                    ToastUtils.show("查询服务器地址指令出错!");
                    return;
                }
                ServerAddressInfo serverAddressInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (!serverAddressInfo.getAddress().equals("0.0.0.0")) {
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
                    childItemsLayout.setVisibility(View.VISIBLE);
                } else {
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
                    childItemsLayout.setVisibility(View.GONE);
                }

                queryDataCenterData();
                break;

            case QUERY_DATA_CENTER_PARAM://查询数据中心 1、2、3 参数
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询数据中心指令出错!");
                    ToastUtils.show("查询数据中心指令出错!");
                    return;
                }
                initDataCenterData(cmdStr);
                break;

            case NET_LINK_COMMUN_PROTOCOL://设置通讯协议应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("网络中心通讯协议配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdDataServerAddress);
                Timber.d("数据服务器地址、端口配置===%s", cmdDataServerAddress);
                break;

            case SET_SERVER_ADDRESS_PORT://设置数据服务器地址、端口应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据服务器地址、端口配置错误!");
                    stopProgressRunnable();
                    return;
                }
                //处理关闭中心1、2、3的开关时，接收到的应答指令
                if (!mSbCenterEnable.isChecked()) {
                    return;
                }
                //处理关闭中心1、2、3的开关时，接收到的应答指令
//                if (TextUtils.isEmpty(communicationProtocol)) {
//                    return;
//                }

                if (communicationProtocol.equals("4")) {//MQTT自动注册
                    sendCommonCommandImmediately(cmdRegistrationPlatform);
                    Timber.d("选择平台配置===%s", cmdRegistrationPlatform);
                    return;
                } else if (communicationProtocol.equals("5")) {//MQTT手动注册
                    sendCommonCommandImmediately(cmdKeepAliveValue);
                    Timber.d("设置KeepAlive===%s", cmdKeepAliveValue);
                    return;
                }
                break;

            case AUTO_REGISTRATION_PLATFORM:// MQTT 自动注册设置通选择注册平台时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("选择平台配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdRegistrationPlatformAddress);
                Timber.d("自动注册平台地址配置===%s", cmdRegistrationPlatformAddress);
                break;

            case SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT:// MQTT 自动注册设置注册平台地址时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("自动注册平台地址配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdKeepAliveValue);
                Timber.d("设置KeepAlive===%s", cmdKeepAliveValue);
                break;

            case MQTT_KEEP_ALIVE://设置KeepAlive值应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("设置MQTT KeepAlive值错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdPlatformParam);
                Timber.d("自动/手动注册平台参数===%s", cmdPlatformParam);
                break;

            case SET_AUTO_REGISTRATION_PLATFORM_PARAM:// MQTT 自动注册设置参数时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("自动注册平台参数配置错误!");
                    stopProgressRunnable();
                    return;
                }
                //米度平台需要额外配置 APPKey
                if (!TextUtils.isEmpty(registerPlatform) && registerPlatform.equals("2")) {
                    sendCommonCommandImmediately(cmdAppKey);
                    Timber.d("设置 AppKey===%s", cmdAppKey);
                    return;
                } else {
                    stopProgressRunnable();
                    ToastUtils.show("设置完成");
                }
                break;

            case SET_MEDO_PLATFORM_APPKEY://设置appKey(米度/北京平台特有)
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("AppKey配置错误!");
                    stopProgressRunnable();
                    return;
                }
                stopProgressRunnable();
                ToastUtils.show("设置完成");
                break;

            case SET_MANUAL_REGISTRATION_PLATFORM_PARAM:// MQTT 手动注册设置参数时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("手动注册平台参数配置错误!");
                    stopProgressRunnable();
                    return;
                }
                stopProgressRunnable();
                ToastUtils.show("设置完成");
                break;
        }
    }

    private void initDataCenterData(String cmdStr) {
        mqttConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
        if (mqttConfigInfo == null) {
            Timber.w(" MqttConfigInfo 为空!");
            return;
        }

        switch (mqttConfigInfo.getCommunicationProtocol()) {
            case "2":
                updateViewByCommunicationProtocol(0, "MDM协议");
                break;

            case "4":
                updateViewByCommunicationProtocol(1, "MQTT自动注册");
                break;

            case "5":
                updateViewByCommunicationProtocol(1, "MQTT手动注册");
                break;
        }

        switch (mqttConfigInfo.getRegisterPlatform()) {
            case "0"://地大平台
                updateViewByRegisterPlatform(0, "地大平台");
                break;

            case "1"://成都理工平台
                updateViewByRegisterPlatform(0, "成都理工平台");
                break;

            case "2"://米度平台
                updateViewByRegisterPlatform(0, "米度平台");
                break;
        }

        mEtDataServerAddress.setText(TextUtils.isEmpty(mqttConfigInfo.getDataPlatformAddress()) ? "" : mqttConfigInfo.getDataPlatformAddress().trim());
        mEtAppKey.setText(TextUtils.isEmpty(mqttConfigInfo.getAppKey()) ? "" : mqttConfigInfo.getAppKey().trim());
        mEtRegisterPlatformAddress.setText(TextUtils.isEmpty(mqttConfigInfo.getRegisterPlatformAddress()) ? "" : mqttConfigInfo.getRegisterPlatformAddress().trim());
        mEtKeepAlive.setText(TextUtils.isEmpty(mqttConfigInfo.getKeepAliveValue()) ? "" : mqttConfigInfo.getKeepAliveValue().trim());
        mEtDeviceSN.setText(TextUtils.isEmpty(mqttConfigInfo.getDeviceSn()) ? "" : mqttConfigInfo.getDeviceSn().trim());
        mEtProductId.setText(TextUtils.isEmpty(mqttConfigInfo.getProductId()) ? "" : mqttConfigInfo.getProductId().trim());
        mEtRegisterCode.setText(TextUtils.isEmpty(mqttConfigInfo.getRegisterCode()) ? "" : mqttConfigInfo.getRegisterCode().trim());

        mEtMqttDeviceId.setText(TextUtils.isEmpty(mqttConfigInfo.getMqttDeviceId()) ? "" : mqttConfigInfo.getMqttDeviceId().trim());
        mEtMqttUsername.setText(TextUtils.isEmpty(mqttConfigInfo.getMqttUsername()) ? "" : mqttConfigInfo.getMqttUsername().trim());
        mEtMqttPwd.setText(TextUtils.isEmpty(mqttConfigInfo.getMqttPassword()) ? "" : mqttConfigInfo.getMqttPassword().trim());
    }

    @Override
    public boolean onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            if (checkValueIsChange()) {
                warnNotYetSave();
            } else {
                mActivity.finish();
            }
        } else {
            mActivity.finish();
        }

        return true;
    }

    private boolean checkValueIsChange() {
        if (!mqttConfigInfo.getCommunicationProtocol().equals(communicationProtocol)) {
            return true;
        }

        if (!mqttConfigInfo.getDataPlatformAddress().equals(mEtDataServerAddress.getText())) {
            return true;
        }

        if (communicationProtocol.equals("4")) {//MQTT自动注册
            if (!mqttConfigInfo.getRegisterPlatform().equals(registerPlatform)) {
                return true;
            }

            if (!mqttConfigInfo.getRegisterPlatformAddress().equals(mEtDataServerAddress.getText())) {
                return true;
            }

            if (!mqttConfigInfo.getKeepAliveValue().equals(mEtKeepAlive.getText())) {
                return true;
            }

            if (!mqttConfigInfo.getDeviceSn().equals(mEtDeviceSN.getText())) {
                return true;
            }

            if (!mqttConfigInfo.getProductId().equals(mEtProductId.getText())) {
                return true;
            }

            if (!mqttConfigInfo.getRegisterCode().equals(mEtRegisterCode.getText())) {
                return true;
            }

            if (registerPlatform.equals("2")) {
                if (!mqttConfigInfo.getAppKey().equals(mEtAppKey.getText())) {
                    return true;
                }
            }
        } else if (communicationProtocol.equals("5")) {//MQTT手动注册
            if (!mqttConfigInfo.getKeepAliveValue().equals(mEtKeepAlive.getText())) {
                return true;
            }

            if (!mqttConfigInfo.getMqttDeviceId().equals(mEtMqttDeviceId.getText())) {
                return true;
            }

            if (!mqttConfigInfo.getMqttUsername().equals(mEtMqttUsername.getText())) {
                return true;
            }

            if (!mqttConfigInfo.getMqttPassword().equals(mEtMqttPwd.getText())) {
                return true;
            }
        }

        return false;
    }

    private void warnNotYetSave() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                .title("温馨提示：")
                .content("您已经修改了参数，还未保存，是否确定离开页面？")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .negativeColor(Color.parseColor("#807B7B"))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mActivity.finish();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

}
