package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
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
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Das数据中心参数配置页面
 */
public class BleDasDataCenterServerConfigFragment extends BaseBleCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

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

    @BindView(R.id.et_data_server_port)
    ClearEditText mEtDataServerPort;

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

    @BindView(R.id.ll_register_platform_address)
    ViewGroup registerPlatformAddressLayout;

    @BindView(R.id.ll_keep_alive)
    ViewGroup keepAliveLayout;

    @BindView(R.id.ll_auto_register_special_info)
    ViewGroup autoRegisterSpecialInfoLayout;

    @BindView(R.id.ll_manual_register_special_info)
    ViewGroup manualRegisterSpecialInfoLayout;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private static final String SERVER_NUMBER = "server_number";
    private ServerNumber serverNumber;
    private MqttConfigInfo mqttConfigInfo = new MqttConfigInfo();

    private int communicationProtocolPos;
    private int registerPlatformPos;

    private String communicationProtocolOld;//网络中心通讯协议
    private String registerPlatformOld;//网络中心通讯协议

    private String communicationProtocol;//网络中心通讯协议
    private String dataServerAddress;//数据服务器地址
    private String dataServerPort;//数据服务器端口
    private String keepAliveValue;
    private String deviceSn;
    private String productId;
    private String registerCode;
    private String registerPlatform;//自动注册选择平台
    private String registerPlatformAddress;// 自动注册平台地址、端口
    private String mqttDeviceId;
    private String mqttUsername;
    private String mqttPassword;

    private String cmdCommunicationProtocol;//网络中心通讯协议
    private String cmdDataServerAddress;//设置数据服务器地址、端口
    private String cmdRegistrationPlatform;//自动注册选择平台
    private String cmdRegistrationPlatformAddress;// 自动注册平台地址、端口
    private String cmdKeepAliveValue;//
    private String cmdPlatformParam;//手动/自动注册平台参数

    private String[] platforms = new String[]{"地灾一期", "成都理工平台", "MDNET", "地灾二期"};

    public static BleDasDataCenterServerConfigFragment newInstance(ServerNumber serverNumber) {
        BleDasDataCenterServerConfigFragment fragment = new BleDasDataCenterServerConfigFragment();
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
        return R.layout.fragment_ble_das_data_center_server_config;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setSwitchViewListener();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void setView() {
        childItemsLayout.setVisibility(View.GONE);
        mTvRegisterPlatform.setText("地大平台");
        registerPlatform = "0";

        mEtDataServerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDataServerPort.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtKeepAlive.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDeviceSN.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtProductId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtRegisterCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtMqttDeviceId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtMqttUsername.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        mEtMqttPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});

        mEtRegisterPlatformAddress.setHint("服务器地址 端口");
    }

    private void setSwitchViewListener() {
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭数据中心？");
                } else {
                    childItemsLayout.setVisibility(View.VISIBLE);
                    mBtnSave.setVisibility(View.VISIBLE);

                    queryDataCenterInfo();
                }
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
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        childItemsLayout.setVisibility(View.GONE);
                        mBtnSave.setVisibility(View.GONE);
                        closeDataServer();//关闭服务器
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

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                queryDataServerAddress();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_10000_MILLIS);
            }
        });
    }

    /**
     * 查询数据服务器地址、端口
     */
    private void queryDataServerAddress() {
        //获取服务器地址,查询中心开启状态
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommand(command);
        Timber.d("查询数据服务器%s的地址指令===%s", serverNumber.toInt(), command);
    }

    /**
     * 查询数据中心参数
     */
    private void queryDataCenterInfo() {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
        sendCommand(command);
        Timber.d("查询数据中心%s的参数===%s", serverNumber.toInt(), command);
    }

    /**
     * 关闭数据中心</br>
     * addr和port设置为空时，关闭该数据中心
     */
    private void closeDataServer() {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, serverNumberEntity);
        sendCommand(command);//关闭服务器
        Timber.d("关闭数据服务器%s指令===%s", serverNumber.toInt(), command);
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
                com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);

                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                if (!checkValueIsValid()) {
                    Timber.w("数据中心参数存在错误!");
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
                .asBottomList("", platforms,
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
                registerPlatformLayout.setVisibility(View.VISIBLE);
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
            case "地灾一期":
                registerPlatform = "0";
                break;

            case "成都理工平台":
                registerPlatform = "1";
                break;

            case "MDNET":
                registerPlatform = "2";
                break;

            case "地灾二期":
                registerPlatform = "3";
                break;
        }
    }

    private boolean checkValueIsValid() {
        dataServerAddress = mEtDataServerAddress.getText().toString().trim();
        dataServerPort = mEtDataServerPort.getText().toString().trim();
        registerPlatformAddress = mEtRegisterPlatformAddress.getText().toString().trim();
        keepAliveValue = mEtKeepAlive.getText().toString().trim();
        deviceSn = mEtDeviceSN.getText().toString().trim();
        productId = mEtProductId.getText().toString().trim();
        registerCode = mEtRegisterCode.getText().toString().trim();
        mqttDeviceId = mEtMqttDeviceId.getText().toString().trim();
        mqttUsername = mEtMqttUsername.getText().toString().trim();
        mqttPassword = mEtMqttPwd.getText().toString().trim();

        if (TextUtils.isEmpty(dataServerAddress)) {
            ToastUtils.show("数据中心地址不能为空!");
            mEtDataServerAddress.requestFocus();
            return false;
        }
        if (!TextUtils.isEmpty(dataServerPort)) {
            try {
                int port = Integer.parseInt(dataServerPort);
                if (port < 0 || port > 65535) {
                    ToastUtils.show("请输入正确的数据中心端口号!");
                    mEtDataServerPort.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据中心端口号!");
                mEtDataServerPort.requestFocus();
                return false;
            }
        }
        if (communicationProtocol.equals("4")) {//MQTT自动注册
            if (TextUtils.isEmpty(registerPlatformAddress)) {
                ToastUtils.show("注册平台地址不能为空!");
                mEtRegisterPlatformAddress.requestFocus();
                return false;
            }

            String[] strs = registerPlatformAddress.split(" ");
            if (strs.length < 2) {
                ToastUtils.show("注册平台地址格式错误!");
                mEtRegisterPlatformAddress.requestFocus();
                return false;
            }

            try {
                int port = Integer.parseInt(strs[1]);
                if (port < 0 || port > 65535) {
                    ToastUtils.show("注册平台地址端口号错误!");
                    mEtRegisterPlatformAddress.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("注册平台地址端口号错误!");
                mEtRegisterPlatformAddress.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(keepAliveValue)) {
                ToastUtils.show("KeepAlive值不能为空!");
                mEtKeepAlive.requestFocus();
                return false;
            }

            try {
                int value = Integer.parseInt(keepAliveValue);
                if (value < 0 || value > 65535) {
                    ToastUtils.show("KeepAlive值错误!");
                    mEtKeepAlive.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("KeepAlive值错误!");
                mEtKeepAlive.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(deviceSn)) {
                ToastUtils.show("设备SN号不能为空!");
                mEtDeviceSN.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(productId)) {
                ToastUtils.show("产品ID不能为空!");
                mEtProductId.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(registerCode)) {
                ToastUtils.show("注册码不能为空!");
                mEtRegisterCode.requestFocus();
                return false;
            }

        } else if (communicationProtocol.equals("5")) {//MQTT手动注册
            if (TextUtils.isEmpty(keepAliveValue)) {
                ToastUtils.show("KeepAlive值不能为空!");
                mEtKeepAlive.requestFocus();
                return false;
            }

            try {
                int value = Integer.parseInt(keepAliveValue);
                if (value < 0 || value > 65535) {
                    ToastUtils.show("KeepAlive值错误!");
                    mEtKeepAlive.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("KeepAlive值错误!");
                mEtKeepAlive.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(mqttDeviceId)) {
                ToastUtils.show("MQTT设备ID不能为空!");
                mEtMqttDeviceId.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(mqttUsername)) {
                ToastUtils.show("MQTT用户名不能为空!");
                mEtMqttUsername.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(mqttPassword)) {
                ToastUtils.show("MQTT密码不能为空!");
                mEtMqttPwd.requestFocus();
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
        ServerAddressInfoEntity addressInfoEntity = new ServerAddressInfoEntity(serverNumber.toInt(), dataServerAddress, Integer.parseInt(dataServerPort));
        cmdDataServerAddress = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, addressInfoEntity);

        if (communicationProtocol.equals("4")) {//MQTT自动注册
            //选择注册平台
            RegistrationPlatformSelectionEntity platformSelectionEntity = new RegistrationPlatformSelectionEntity(serverNumber.toInt(), Integer.parseInt(registerPlatform));
            cmdRegistrationPlatform = CommandManager.getInstance().getCommand(CommandType.AUTO_REGISTRATION_PLATFORM, platformSelectionEntity);

            //自动注册平台地址端口
            String[] strs = registerPlatformAddress.trim().split(" ");
            addressInfoEntity = new ServerAddressInfoEntity(serverNumber.toInt(), strs[0], Integer.parseInt(strs[1]));
            cmdRegistrationPlatformAddress = CommandManager.getInstance().getCommand(CommandType.SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT, addressInfoEntity);

            //MQTT KeepAlive值
            MQTTKeepAliveEntity keepAliveEntity = new MQTTKeepAliveEntity(serverNumber.toInt(), Integer.parseInt(keepAliveValue));
            cmdKeepAliveValue = CommandManager.getInstance().getCommand(CommandType.MQTT_KEEP_ALIVE, keepAliveEntity);

            //自动注册平台参数：设备SN号+产品ID+注册码
            RegistrationPlatformEntity registrationPlatformEntity = new RegistrationPlatformEntity(serverNumber.toInt(), deviceSn, productId, registerCode);
            cmdPlatformParam = CommandManager.getInstance().getCommand(CommandType.SET_AUTO_REGISTRATION_PLATFORM_PARAM, registrationPlatformEntity);
        } else if (communicationProtocol.equals("5")) {//MQTT手动注册
            //选择注册平台
            RegistrationPlatformSelectionEntity platformSelectionEntity = new RegistrationPlatformSelectionEntity(serverNumber.toInt(), Integer.parseInt(registerPlatform));
            cmdRegistrationPlatform = CommandManager.getInstance().getCommand(CommandType.AUTO_REGISTRATION_PLATFORM, platformSelectionEntity);

            //MQTT KeepAlive值
            MQTTKeepAliveEntity keepAliveEntity = new MQTTKeepAliveEntity(serverNumber.toInt(), Integer.parseInt(keepAliveValue));
            cmdKeepAliveValue = CommandManager.getInstance().getCommand(CommandType.MQTT_KEEP_ALIVE, keepAliveEntity);

            //手动注册平台参数：产品ID+设备ID+设备KEY
            RegistrationPlatformEntity registrationPlatformEntity = new RegistrationPlatformEntity(serverNumber.toInt(), mqttUsername, mqttDeviceId, mqttPassword);
            cmdPlatformParam = CommandManager.getInstance().getCommand(CommandType.SET_MANUAL_REGISTRATION_PLATFORM_PARAM, registrationPlatformEntity);
        }

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        sendCommand(cmdCommunicationProtocol);
        Timber.d("设置网络中心通讯协议===%s", cmdCommunicationProtocol);
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SERVER_ADDRESS://获取服务器1、2、3 的地址
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    Timber.e("查询服务器地址指令出错!");
                    ToastUtils.show("查询服务器地址指令出错!");
                    return;
                }
                ServerAddressInfo serverAddressInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (!serverAddressInfo.getAddress().equals("0.0.0.0")) {//地址为0.0.0.0，表示数据中心未启用
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
                    childItemsLayout.setVisibility(View.VISIBLE);
                    mBtnSave.setVisibility(View.VISIBLE);
                    queryDataCenterInfo();
                } else {
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
                    childItemsLayout.setVisibility(View.GONE);
                    mBtnSave.setVisibility(View.GONE);
                    mRefreshLayout.finishRefresh(true);
                }
                break;

            case QUERY_DATA_CENTER_PARAM://查询数据中心 1、2、3 参数
                mRefreshLayout.finishRefresh(true);
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
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
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommand(cmdDataServerAddress);
                Timber.d("数据服务器地址、端口配置===%s", cmdDataServerAddress);
                break;

            case SET_SERVER_ADDRESS_PORT://设置数据服务器地址、端口应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据服务器地址、端口配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                //处理关闭中心1、2、3的开关时，接收到的应答指令
                if (!mSbCenterEnable.isChecked()) {
                    doAfterSetting();
                    return;
                }
                if (communicationProtocol.equals("2")) {//MDM协议
                    doAfterSetting();
                } else if (communicationProtocol.equals("4") || communicationProtocol.equals("5")) {//MQTT自动注册/MQTT手动注册
                    sendCommand(cmdRegistrationPlatform);
                    Timber.d("选择平台配置===%s", cmdRegistrationPlatform);
                    return;
                }
                break;

            case AUTO_REGISTRATION_PLATFORM:// MQTT 自动注册设置通选择注册平台时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("选择平台配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                if (communicationProtocol.equals("4")) {//MQTT自动注册
                    sendCommand(cmdRegistrationPlatformAddress);
                    Timber.d("自动注册平台地址配置===%s", cmdRegistrationPlatformAddress);
                } else if (communicationProtocol.equals("5")) {//MQTT手动注册
                    sendCommand(cmdKeepAliveValue);
                    Timber.d("设置KeepAlive===%s", cmdKeepAliveValue);
                    return;
                }
                break;

            case SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT:// MQTT 自动注册设置注册平台地址时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("自动注册平台地址配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommand(cmdKeepAliveValue);
                Timber.d("设置KeepAlive===%s", cmdKeepAliveValue);
                break;

            case MQTT_KEEP_ALIVE://设置KeepAlive值应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("设置MQTT KeepAlive值错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommand(cmdPlatformParam);
                Timber.d("自动/手动注册平台参数===%s", cmdPlatformParam);
                break;

            case SET_AUTO_REGISTRATION_PLATFORM_PARAM:// MQTT 自动注册设置参数时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("自动注册平台参数配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                doAfterSetting();
                break;

            case SET_MANUAL_REGISTRATION_PLATFORM_PARAM:// MQTT 手动注册设置参数时应答
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("手动注册平台参数配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                doAfterSetting();
                break;

            case SAVE_CONFIG_INFO:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                Toast.makeText(getActivity(), "已保存", Toast.LENGTH_LONG).show();
                communicationProtocolOld = communicationProtocol;
                registerPlatformOld = registerPlatform;
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void initDataCenterData(String cmdStr) {
        mqttConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
        if (mqttConfigInfo == null) {
            Timber.w(" MqttConfigInfo 为空!");
            mqttConfigInfo = new MqttConfigInfo();
            return;
        }

        switch (mqttConfigInfo.getCommunicationProtocol()) {
            case "2":
                communicationProtocolOld = "2";
                updateViewByCommunicationProtocol(0, "MDM协议");
                break;

            case "4":
                communicationProtocolOld = "4";
                updateViewByCommunicationProtocol(1, "MQTT自动注册");
                break;

            case "5":
                communicationProtocolOld = "5";
                updateViewByCommunicationProtocol(2, "MQTT手动注册");
                break;
        }

        switch (mqttConfigInfo.getRegisterPlatform()) {
            case "0"://地大平台
                registerPlatformOld = "0";
                updateViewByRegisterPlatform(0, platforms[0]);
                break;

            case "1"://成都理工平台
                registerPlatformOld = "1";
                updateViewByRegisterPlatform(1, platforms[1]);
                break;

            case "2"://米度平台
                registerPlatformOld = "2";
                updateViewByRegisterPlatform(2, platforms[2]);
                break;

            case "3"://地灾二期
                registerPlatformOld = "3";
                updateViewByRegisterPlatform(3, platforms[3]);
                break;
        }

        dataServerAddress = mqttConfigInfo.getDataPlatformAddress().trim();
        String[] strs = dataServerAddress.split(" ");
        if (strs.length == 2) {
            dataServerAddress = strs[0];
            dataServerPort = strs[1];
        }
        registerPlatformAddress = mqttConfigInfo.getRegisterPlatformAddress().trim();
        keepAliveValue = mqttConfigInfo.getKeepAliveValue().trim();
        deviceSn = mqttConfigInfo.getDeviceSn().trim();
        productId = mqttConfigInfo.getProductId().trim();
        registerCode = mqttConfigInfo.getRegisterCode().trim();
        mqttDeviceId = mqttConfigInfo.getMqttDeviceId().trim();
        mqttUsername = mqttConfigInfo.getMqttUsername().trim();
        mqttPassword = mqttConfigInfo.getMqttPassword().trim();

        mEtDataServerAddress.setText(dataServerAddress);
        mEtDataServerPort.setText(TextUtils.isEmpty(dataServerPort) ? "0" : dataServerPort);
        mEtRegisterPlatformAddress.setText(registerPlatformAddress);
        mEtKeepAlive.setText(keepAliveValue);
        mEtDeviceSN.setText(deviceSn);
        mEtProductId.setText(productId);
        mEtRegisterCode.setText(registerCode);
        mEtMqttDeviceId.setText(mqttDeviceId);
        mEtMqttUsername.setText(mqttUsername);
        mEtMqttPwd.setText(mqttPassword);
    }

    private void doAfterSetting() {
        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
//        isExitMode = true;
        saveConfigInfoNoReboot();
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_SMART_REFRESH:
                if (mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.finishRefresh(false);
                    ToastUtils.show("刷新超时");
                }
                break;

            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isConnected()) {
            if (checkValueIsChange()) {
                warnNotYetSettingBeforeLeavePage();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private boolean checkValueIsChange() {
        if (!mSbCenterEnable.isChecked()) {
            return false;
        }

        if (communicationProtocolOld != null && communicationProtocol != null && !communicationProtocolOld.equals(communicationProtocol)) {
            return true;
        }
        if (dataServerAddress != null && !dataServerAddress.equals(mEtDataServerAddress.getText().toString().trim())) {
            return true;
        }
        if (dataServerPort != null && !dataServerPort.equals(mEtDataServerPort.getText().toString().trim())) {
            return true;
        }

        if (communicationProtocol != null && communicationProtocol.equals("4")) {//MQTT自动注册
            if (registerPlatformOld != null && registerPlatform != null && !registerPlatformOld.equals(registerPlatform)) {
                return true;
            }
            if (registerPlatformAddress != null && !registerPlatformAddress.equals(mEtRegisterPlatformAddress.getText().toString().trim())) {
                return true;
            }
            if (keepAliveValue != null && !keepAliveValue.equals(mEtKeepAlive.getText().toString().trim())) {
                return true;
            }
            if (deviceSn != null && !deviceSn.equals(mEtDeviceSN.getText().toString().trim())) {
                return true;
            }
            if (productId != null && !productId.equals(mEtProductId.getText().toString().trim())) {
                return true;
            }
            if (registerCode != null && !registerCode.equals(mEtRegisterCode.getText().toString().trim())) {
                return true;
            }
        } else if (communicationProtocol != null && communicationProtocol.equals("5")) {//MQTT手动注册
            if (keepAliveValue != null && !keepAliveValue.equals(mEtKeepAlive.getText().toString().trim())) {
                return true;
            }
            if (mqttDeviceId != null && !mqttDeviceId.equals(mEtMqttDeviceId.getText().toString().trim())) {
                return true;
            }
            if (mqttUsername != null && !mqttUsername.equals(mEtMqttUsername.getText().toString().trim())) {
                return true;
            }
            if (mqttPassword != null && !mqttPassword.equals(mEtMqttPwd.getText().toString().trim())) {
                return true;
            }
        }
        return false;
    }
}
