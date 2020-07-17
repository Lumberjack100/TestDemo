package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.entity.APPKeyEntity;
import com.shmedo.core.cmd.entity.DataCenterCommunicateProtoclEntity;
import com.shmedo.core.cmd.entity.DataCommunicateModeEntity;
import com.shmedo.core.cmd.entity.DataReportIntervalEntity;
import com.shmedo.core.cmd.entity.MQTTKeepAliveEntity;
import com.shmedo.core.cmd.entity.RegistrationPlatformEntity;
import com.shmedo.core.cmd.entity.RegistrationPlatformSelectionEntity;
import com.shmedo.core.cmd.entity.ServerAddressInfoEntity;
import com.shmedo.core.cmd.entity.ServerNumberEntity;
import com.shmedo.core.cmd.entity.SixTargerBDNumberEntity;
import com.shmedo.core.cmd.parser.ParseManager;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.ServerNumber;
import com.shmedo.core.model.BaseConfigInfo;
import com.shmedo.core.model.MqttConfigInfo;
import com.shmedo.core.model.ServerAddressInfo;
import com.shmedo.core.utils.ResultParserUtil;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.MqttConfigDialogFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   MqttSettingActivity
 * 创建者:   dpc
 * 创建时间:  2019/11/26 15:23
 * 描述：    mqtt设置
 */
public class MqttSettingActivity extends BaseDeviceConnectActivity implements View.OnClickListener, BaseDialogFragment.DialogFragmentClickListener<MqttConfigInfo> {
    @BindView(R.id.back)
    ImageView back;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.tv_save)
    TextView tvSave;

    @BindView(R.id.communication_method)
    View communicationMethod;

    @BindView(R.id.data_report)
    View dataReport;

    @BindView(R.id.bd_card_number)
    View bdCardNumber;

    @BindView(R.id.ll_bd)
    LinearLayout llBd;

    @BindView(R.id.link_one)
    View linkOne;

    @BindView(R.id.link_two)
    View linkTwo;

    @BindView(R.id.link_three)
    View linkThree;

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private TextView mTvLinkOneStatus, mTvLinkTwoStatus, mTvLinkThreeStatus;

    private Spinner spCommunicationmethod;

    private SwitchButton mSbLinkOne, mSbLinkTwo, mSbLinkThree;

    private ClearEditText mCetDataReport, mCetBDCardNumber;

    private ArrayAdapter<String> CommunicationMethodAdapter;

    private MqttConfigInfo mqttConfigInfo1 = new MqttConfigInfo();
    private MqttConfigInfo mqttConfigInfo2 = new MqttConfigInfo();
    private MqttConfigInfo mqttConfigInfo3 = new MqttConfigInfo();

    //第一次编辑发送指令，第二次直接弹框
    private boolean editLinkOne, editLinkTwo, editLinkThree;

    private String dataCommunicationMode;
    private String communicationProtocol;//通讯协议，2：MDM协议，4：MQTT自动注册，5：MQTT手动注册
    private String registrationPlatform;//注册平台类型，0：地大平台，1：成都理工平台，2：米度平台
    private String cmdCommunicationProtocol;//网络中心通讯协议
    private String cmdDataPlatformAddress;//设置数据服务器地址、端口
    private String cmdRegistrationPlatform;//自动注册选择平台
    private String cmdRegistrationPlatformAddress;// 自动注册平台地址、端口
    private String cmdKeepAliveValue;//
    private String cmdPlatformParam;//手动/自动注册平台参数
    private String cmdAppKey;//米度平台 AppKey
    private String cmdDataReport;//数据上报间隔
    private String cmdBDCardNumber;//北斗卡号

    private String curLinkNumber;

    @Override
    protected int initContentView() {
        return R.layout.activity_mqtt_setting;
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MqttSettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        initView();
        initData();
        setSwitchViewListener();
    }

    private void initView() {
        tvTitle.setText("数据中心");
        tvSave.setText("保存");
        tvSave.setVisibility(View.GONE);

        ((TextView) communicationMethod.findViewById(R.id.tv_config_name)).setText("通讯方式:");
        spCommunicationmethod = communicationMethod.findViewById(R.id.spinner);

        ((TextView) dataReport.findViewById(R.id.tv_config_name)).setText("数据上报(分钟):");
        mCetDataReport = dataReport.findViewById(R.id.clear_edit_text);
        mCetDataReport.setHint("默认120分钟");

        ((TextView) bdCardNumber.findViewById(R.id.tv_config_name)).setText("北斗配置:");
        mCetBDCardNumber = bdCardNumber.findViewById(R.id.clear_edit_text);
        mCetBDCardNumber.setHint("请输入BD卡号");

        ((TextView) linkOne.findViewById(R.id.tv_config_name)).setText("中心1:");
        mTvLinkOneStatus = linkOne.findViewById(R.id.tv_device_state);
        mSbLinkOne = linkOne.findViewById(R.id.switchButton);
        mTvLinkOneStatus.setId(R.id.tv_link_one);

        ((TextView) linkTwo.findViewById(R.id.tv_config_name)).setText("中心2:");
        mTvLinkTwoStatus = linkTwo.findViewById(R.id.tv_device_state);
        mSbLinkTwo = linkTwo.findViewById(R.id.switchButton);
        mTvLinkTwoStatus.setId(R.id.tv_link_two);

        ((TextView) linkThree.findViewById(R.id.tv_config_name)).setText("中心3:");
        mTvLinkThreeStatus = linkThree.findViewById(R.id.tv_device_state);
        mSbLinkThree = linkThree.findViewById(R.id.switchButton);
        mTvLinkThreeStatus.setId(R.id.tv_link_three);

        //通讯方式
        String[] cmData = getResources().getStringArray(R.array.communication_method);
        CommunicationMethodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cmData);
        CommunicationMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCommunicationmethod.setAdapter(CommunicationMethodAdapter);
        spCommunicationmethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item) {
                    case "4G":
                        dataCommunicationMode = "1";
                        llBd.setVisibility(View.GONE);
                        break;

                    case "SMS":
                        dataCommunicationMode = "2";
                        llBd.setVisibility(View.GONE);
                        break;

                    case "BD":
                        dataCommunicationMode = "3";
                        llBd.setVisibility(View.VISIBLE);
                        break;

                    case "BD+4G":
                        dataCommunicationMode = "4";
                        llBd.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mTvLinkOneStatus.setOnClickListener(this);
        mTvLinkTwoStatus.setOnClickListener(this);
        mTvLinkThreeStatus.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        back.setOnClickListener(this);
        tvSave.setOnClickListener(this);
    }

    private void initData() {
        startProgressRunnable("正在获取指令参数...", CONFIG_PARAMS_DELAY_MILLIS);
        //获取服务器地址,查询中心开启状态
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
        String cmdAddress1 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommonCommand(cmdAddress1);
        Timber.d("查询服务器地址1指令===%s", cmdAddress1);

        serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
        String cmdAddress2 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommonCommand(cmdAddress2);
        Timber.d("查询服务器地址2指令===%s", cmdAddress2);

        serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt());
        String cmdAddress3 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommonCommand(cmdAddress3);
        Timber.d("查询服务器地址3指令===%s", cmdAddress3);

        String baseInfoCommand = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommonCommand(baseInfoCommand);
        Timber.d("基础配置信息指令===%s", baseInfoCommand);
    }

    private void setSwitchViewListener() {
        mSbLinkOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbLinkOne.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭中心1？", 1);
                } else {
                    ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                    sendCommonCommandImmediately(command);//查询数据中心1参数
                    setLinkTextVisibility(mTvLinkOneStatus, true);
                    setLinkTextEnabled(mTvLinkOneStatus, false);
                    editLinkOne = false;
                }
                Timber.i("mSbLinkOne===%s", isChecked);
            }
        });
        mSbLinkTwo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbLinkTwo.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭中心2？", 2);
                } else {
                    ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                    sendCommonCommandImmediately(command);//查询数据中心2参数
                    setLinkTextVisibility(mTvLinkTwoStatus, true);
                    setLinkTextEnabled(mTvLinkTwoStatus, false);
                    editLinkTwo = false;
                }
                Timber.i("mSbLinkTwo===%s", isChecked);
            }
        });
        mSbLinkThree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbLinkThree.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭中心3？", 3);
                } else {
                    ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                    sendCommonCommandImmediately(command);//查询数据中心2参数
                    setLinkTextVisibility(mTvLinkThreeStatus, true);
                    setLinkTextEnabled(mTvLinkThreeStatus, false);
                    editLinkThree = false;
                }
                Timber.i("mSbLinkThree===%s", isChecked);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_link_one:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                setLinkTextEnabled(mTvLinkOneStatus, false);
                if (!editLinkOne) {//第一次编辑时，需要查询数据中心参数
                    editLinkOne = true;
                    ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                    sendCommonCommandImmediately(command);//查询数据中心1参数
                    Timber.i("中心1第1次编辑");
                } else {
                    if (mqttConfigInfo1 == null) {
                        ToastUtils.show("等2s再点击");
                        return;
                    }
                    //直接弹框
                    Timber.i("中心1第2次编辑===%s", mqttConfigInfo1.toString());
                    curLinkNumber = "1";
                    showConfigDialog(mqttConfigInfo1, "1");
                }
                break;

            case R.id.tv_link_two:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }

                setLinkTextEnabled(mTvLinkTwoStatus, false);
                if (!editLinkTwo) {//第一次编辑时，需要查询数据中心参数
                    editLinkTwo = true;
                    ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                    sendCommonCommandImmediately(command);//查询数据中心2参数
                    Timber.i("中心2第1次编辑");
                } else {
                    if (mqttConfigInfo2 == null) {
                        ToastUtils.show("等2s再点击");
                        return;
                    }
                    //直接弹框
                    Timber.i("中心2第2次编辑===%s", mqttConfigInfo2.toString());
                    curLinkNumber = "2";
                    showConfigDialog(mqttConfigInfo2, "2");
                }
                break;

            case R.id.tv_link_three:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }

                setLinkTextEnabled(mTvLinkThreeStatus, false);
                if (!editLinkThree) {//第一次编辑时，需要查询数据中心参数
                    editLinkThree = true;
                    ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DATA_CENTER_PARAM, serverNumberEntity);
                    sendCommonCommandImmediately(command);//查询数据中心3参数
                    Timber.i("中心3第1次编辑");
                } else {
                    if (mqttConfigInfo3 == null) {
                        ToastUtils.show("等2s再点击");
                        return;
                    }
                    //直接弹框
                    Timber.i("中心3第2次编辑===%s", mqttConfigInfo3.toString());
                    curLinkNumber = "3";
                    showConfigDialog(mqttConfigInfo3, "3");
                }
                break;

            case R.id.btn_confirm:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }

                if (spCommunicationmethod.getSelectedItem().toString().contains("BD")) {
                    String bdNumber = mCetBDCardNumber.getText() == null ? "" : mCetBDCardNumber.getText().toString().trim();
                    if (bdNumber.equals("")) {
                        ToastUtils.show("北斗目标卡号不能为空");
                        return;
                    }
                    //设置六位目标北斗卡号
                    SixTargerBDNumberEntity bdNumberEntity = new SixTargerBDNumberEntity(bdNumber);
                    cmdBDCardNumber = CommandManager.getInstance().getCommand(CommandType.SIX_TARGER_BD_NUMBER, bdNumberEntity);
                }

                //设置数据上报间隔
                String report = mCetDataReport.getText() == null ? "" : mCetDataReport.getText().toString().trim();
                DataReportIntervalEntity intervalEntity = new DataReportIntervalEntity(report.equals("") ? 120 : Integer.parseInt(report));
                cmdDataReport = CommandManager.getInstance().getCommand(CommandType.DATA_REPORT_INTERVAL, intervalEntity);

                errMsg = "发送指令超时,请稍后尝试";
                startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
                DataCommunicateModeEntity communicateModeEntity = new DataCommunicateModeEntity(Integer.parseInt(dataCommunicationMode));
                String cmd = CommandManager.getInstance().getCommand(CommandType.DATA_MASSAGE_MODEL, communicateModeEntity);
                sendCommonCommandImmediately(cmd);
                Timber.d("设置数据通讯模式===%s", cmd);
                break;

            case R.id.back:
                onBackPressed();
                break;

            case R.id.tv_save:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                isExitMode = true;
                saveConfigInfo();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }

    private void setResultData(String cmdStr) {
        Timber.i("数据中心应答指令===%s", cmdStr);
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType cmdType = StringUtil.extractCommandType(cmdStr);
        switch (cmdType) {
            case SERVER_ADDRESS://获取服务器1、2、3 的地址
                ServerAddressInfo serverAddressInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (!serverAddressInfo.getAddress().equals("0.0.0.0")) {
                    switch (serverAddressInfo.getNumber()) {
                        case NUMBER_ONE:
                            //根据返回指令初始化按钮状态
                            mSbLinkOne.setCheckedImmediatelyNoEvent(true);
                            setLinkTextVisibility(mTvLinkOneStatus, true);
                            break;
                        case NUMBER_TWO:
                            mSbLinkTwo.setCheckedImmediatelyNoEvent(true);
                            setLinkTextVisibility(mTvLinkTwoStatus, true);
                            break;
                        case NUMBER_THREE:
                            mSbLinkThree.setCheckedImmediatelyNoEvent(true);
                            setLinkTextVisibility(mTvLinkThreeStatus, true);
                            break;
                    }
                }
                break;

            case BASE_CONFIG://获取基础配置信息
                CommandResult<BaseConfigInfo> bean = ParseManager.getInstance().parse(cmdStr);
                if (!bean.isSuccess()) {
                    stopProgressRunnable();
                    return;
                }
                BaseConfigInfo baseConfigInfo = bean.getResult();
                int communicateMode = baseConfigInfo.getDataCommunicateMode().toInt();
                switch (communicateMode) {
                    case 1:
                        spCommunicationmethod.setSelection(0);
                        break;
                    case 2:
                        spCommunicationmethod.setSelection(1);
                        break;
                    case 3:
                        spCommunicationmethod.setSelection(2);
                        break;
                    case 4:
                        spCommunicationmethod.setSelection(3);
                        break;
                }
                int reportInterval = baseConfigInfo.getDataReportInterval();
                mCetDataReport.setText(String.valueOf(reportInterval));
                mCetBDCardNumber.setText(baseConfigInfo.getTargetBGNum());
                stopProgressRunnable();
                break;

            case QUERY_DATA_CENTER_PARAM://查询数据中心 1、2、3 参数
                String[] strs = cmdStr.split(",", -1);
                ServerNumber dataCenterLinkNumber = ServerNumber.valueOf(Integer.parseInt(strs[0].substring(5)));
                switch (dataCenterLinkNumber) {
                    case NUMBER_ONE:
                        mqttConfigInfo1 = ResultParserUtil.getEntityObject(cmdStr);
                        setLinkTextEnabled(mTvLinkOneStatus, true);
                        if (mqttConfigInfo1 == null)
                            return;
                        //弹框
                        curLinkNumber = "1";
                        showConfigDialog(mqttConfigInfo1, "1");
                        break;

                    case NUMBER_TWO:
                        mqttConfigInfo2 = ResultParserUtil.getEntityObject(cmdStr);
                        setLinkTextEnabled(mTvLinkTwoStatus, true);
                        if (mqttConfigInfo2 == null)
                            return;
                        //弹框
                        curLinkNumber = "2";
                        showConfigDialog(mqttConfigInfo2, "2");
                        break;

                    case NUMBER_THREE:
                        mqttConfigInfo3 = ResultParserUtil.getEntityObject(cmdStr);
                        setLinkTextEnabled(mTvLinkThreeStatus, true);
                        if (mqttConfigInfo3 == null)
                            return;
                        //弹框
                        curLinkNumber = "3";
                        showConfigDialog(mqttConfigInfo3, "3");
                        break;
                }
                break;

            case NET_LINK_COMMUN_PROTOCOL:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("网络中心通讯协议配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdDataPlatformAddress);
                Timber.d("数据服务器地址、端口配置===%s", cmdDataPlatformAddress);
                break;

            case SET_SERVER_ADDRESS_PORT:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据服务器地址、端口配置错误!");
                    stopProgressRunnable();
                    return;
                }
                //处理关闭中心1、2、3的开关时，接收到的应答指令
                if (TextUtils.isEmpty(communicationProtocol)) {
                    return;
                }
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

            case AUTO_REGISTRATION_PLATFORM:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("选择平台配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdRegistrationPlatformAddress);
                Timber.d("自动注册平台地址配置===%s", cmdRegistrationPlatformAddress);
                break;

            case SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("自动注册平台地址配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdKeepAliveValue);
                Timber.d("设置KeepAlive===%s", cmdKeepAliveValue);
                break;

            case MQTT_KEEP_ALIVE:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("设置MQTT KeepAlive值错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdPlatformParam);
                Timber.d("自动/手动注册平台参数===%s", cmdPlatformParam);
                break;

            case SET_AUTO_REGISTRATION_PLATFORM_PARAM:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("自动注册平台参数配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (!TextUtils.isEmpty(registrationPlatform) && registrationPlatform.equals("2")) {
                    sendCommonCommandImmediately(cmdAppKey);
                    Timber.d("设置 AppKey===%s", cmdAppKey);
                    return;
                }
                stopProgressRunnable();
                ToastUtils.show("设置完成");
                break;

            case SET_MANUAL_REGISTRATION_PLATFORM_PARAM:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("手动注册平台参数配置错误!");
                    stopProgressRunnable();
                    return;
                }
                stopProgressRunnable();
                ToastUtils.show("设置完成");
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

            case DATA_MASSAGE_MODEL://设置数据通讯方式
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据通讯方式配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdDataReport);
                Timber.d("设置数据上报间隔===%s", cmdDataReport);
                break;

            case DATA_REPORT_INTERVAL://设置数据上报间隔
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据上报配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (spCommunicationmethod.getSelectedItem().toString().contains("BD")) {
                    sendCommonCommandImmediately(cmdBDCardNumber);
                    Timber.d("北斗配置参数===%s", cmdBDCardNumber);
                    return;
                }
                stopProgressRunnable();
                ToastUtils.show("设置完成");
                break;

            case SIX_TARGER_BD_NUMBER://北斗配置
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("北斗配置错误!");
                    stopProgressRunnable();
                    return;
                }
                stopProgressRunnable();
                ToastUtils.show("设置完成");
                break;
        }
    }

    private void showConfigDialog(MqttConfigInfo mqttConfigInfo, String linkNumber) {
        BaseDialogFragment newFragment = MqttConfigDialogFragment.newInstance(mqttConfigInfo, linkNumber);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onPositiveClick(View view, MqttConfigInfo mqttConfigInfo) {
        KeyBordUtils.hideSoftKeyboard(view);

        Timber.i("===MQttOnClickListener===%s", mqttConfigInfo.toString());
        communicationProtocol = mqttConfigInfo.getCommunicationProtocol();
        String dataPlatformAddress = mqttConfigInfo.getDataPlatformAddress();
        String keepAliveValue = mqttConfigInfo.getKeepAliveValue();
        String deviceSn = mqttConfigInfo.getDeviceSn();
        String productId = mqttConfigInfo.getProductId();
        String registrationCode = mqttConfigInfo.getRegisterCode();
        registrationPlatform = mqttConfigInfo.getRegisterPlatform();
        String registrationPlatformAddress = mqttConfigInfo.getRegisterPlatformAddress();
        String appkey = mqttConfigInfo.getAppKey();
        String mqttDeviceId = mqttConfigInfo.getMqttDeviceId();
        String mqttUsername = mqttConfigInfo.getMqttUsername();
        String mqttPassword = mqttConfigInfo.getMqttPassword();

        //网络中心通讯协议
        DataCenterCommunicateProtoclEntity communicateProtoclEntity = new DataCenterCommunicateProtoclEntity(Integer.parseInt(curLinkNumber), Integer.parseInt(communicationProtocol));
        cmdCommunicationProtocol = CommandManager.getInstance().getCommand(CommandType.NET_LINK_COMMUN_PROTOCOL, communicateProtoclEntity);

        //数据服务器地址、端口
        String[] strs = dataPlatformAddress.trim().split(" ");
        ServerAddressInfoEntity addressInfoEntity = new ServerAddressInfoEntity(Integer.parseInt(curLinkNumber), strs[0], Integer.parseInt(strs[1]));
        cmdDataPlatformAddress = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, addressInfoEntity);

        if (communicationProtocol.equals("4")) {//MQTT自动注册
            //选择注册平台
            RegistrationPlatformSelectionEntity platformSelectionEntity = new RegistrationPlatformSelectionEntity(Integer.parseInt(curLinkNumber), Integer.parseInt(registrationPlatform));
            cmdRegistrationPlatform = CommandManager.getInstance().getCommand(CommandType.AUTO_REGISTRATION_PLATFORM, platformSelectionEntity);

            //自动注册平台地址端口
            strs = registrationPlatformAddress.trim().split(" ");
            addressInfoEntity = new ServerAddressInfoEntity(Integer.parseInt(curLinkNumber), strs[0], Integer.parseInt(strs[1]));
            cmdRegistrationPlatformAddress = CommandManager.getInstance().getCommand(CommandType.SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT, addressInfoEntity);

            //MQTT KeepAlive值
            MQTTKeepAliveEntity keepAliveEntity = new MQTTKeepAliveEntity(Integer.parseInt(curLinkNumber), Integer.parseInt(keepAliveValue));
            cmdKeepAliveValue = CommandManager.getInstance().getCommand(CommandType.MQTT_KEEP_ALIVE, keepAliveEntity);

            //自动注册平台参数：设备SN号+产品ID+注册码
            RegistrationPlatformEntity registrationPlatformEntity = new RegistrationPlatformEntity(Integer.parseInt(curLinkNumber), deviceSn, productId, registrationCode);
            cmdPlatformParam = CommandManager.getInstance().getCommand(CommandType.SET_AUTO_REGISTRATION_PLATFORM_PARAM, registrationPlatformEntity);
            if (registrationPlatform.equals("2")) {
                //appKey(米度/北京平台特有)
                APPKeyEntity appKeyEntity = new APPKeyEntity(Integer.parseInt(curLinkNumber), appkey);
                cmdAppKey = CommandManager.getInstance().getCommand(CommandType.SET_MEDO_PLATFORM_APPKEY, appKeyEntity);
            }
        } else if (communicationProtocol.equals("5")) {//MQTT手动注册
            //MQTT KeepAlive值
            MQTTKeepAliveEntity keepAliveEntity = new MQTTKeepAliveEntity(Integer.parseInt(curLinkNumber), Integer.parseInt(keepAliveValue));
            cmdKeepAliveValue = CommandManager.getInstance().getCommand(CommandType.MQTT_KEEP_ALIVE, keepAliveEntity);

            //手动注册平台参数：产品ID+设备ID+设备KEY
            RegistrationPlatformEntity registrationPlatformEntity = new RegistrationPlatformEntity(Integer.parseInt(curLinkNumber), mqttUsername, mqttDeviceId, mqttPassword);
            cmdPlatformParam = CommandManager.getInstance().getCommand(CommandType.SET_MANUAL_REGISTRATION_PLATFORM_PARAM, registrationPlatformEntity);
        }

        setLinkTextEnabled(mTvLinkOneStatus, true);
        setLinkTextEnabled(mTvLinkTwoStatus, true);
        setLinkTextEnabled(mTvLinkThreeStatus, true);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
        sendCommonCommandImmediately(cmdCommunicationProtocol);
        Timber.d("设置网络中心通讯协议===%s", cmdCommunicationProtocol);
        return true;
    }

    @Override
    public void onNegativeClick(View view) {
        KeyBordUtils.hideSoftKeyboard(view);
        if (curLinkNumber.equals("1")) {
            setLinkTextEnabled(mTvLinkOneStatus, true);
        } else if (curLinkNumber.equals("2")) {
            setLinkTextEnabled(mTvLinkTwoStatus, true);
        } else if (curLinkNumber.equals("3")) {
            setLinkTextEnabled(mTvLinkThreeStatus, true);
        }
    }

    /**
     * 关闭SwitchButton
     */
    public void showCloseSwitchButtonDialog(String content, final int index) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
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
                        //确定关闭sb按钮。隐藏编辑字体
                        //发送对应关闭中心
                        switch (index) {
                            case 1:
                                mSbLinkOne.setCheckedImmediatelyNoEvent(false);
                                setLinkTextVisibility(mTvLinkOneStatus, false);
                                ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
                                String cmdAddress1 = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, serverNumberEntity);
                                sendCommonCommand(cmdAddress1);//关闭服务器1
                                break;
                            case 2:
                                mSbLinkTwo.setCheckedImmediatelyNoEvent(false);
                                setLinkTextVisibility(mTvLinkTwoStatus, false);
                                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
                                String cmdAddress2 = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, serverNumberEntity);
                                sendCommonCommand(cmdAddress2);//关闭服务器2
                                break;
                            case 3:
                                mSbLinkThree.setCheckedImmediatelyNoEvent(false);
                                setLinkTextVisibility(mTvLinkThreeStatus, false);
                                serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt());
                                String cmdAddress3 = CommandManager.getInstance().getCommand(CommandType.SET_SERVER_ADDRESS_PORT, serverNumberEntity);
                                sendCommonCommand(cmdAddress3);//关闭服务器3
                                break;
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (index) {
                            case 1:
                                mSbLinkOne.setCheckedImmediatelyNoEvent(true);
                                setLinkTextVisibility(mTvLinkOneStatus, true);
                                break;
                            case 2:
                                mSbLinkTwo.setCheckedImmediatelyNoEvent(true);
                                setLinkTextVisibility(mTvLinkTwoStatus, true);
                                break;
                            case 3:
                                mSbLinkThree.setCheckedImmediatelyNoEvent(true);
                                setLinkTextVisibility(mTvLinkThreeStatus, true);
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void setLinkTextVisibility(TextView textView, boolean isOpen) {
        if (isOpen) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("编辑");
            textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * 改变编辑字体状态
     *
     * @param textView
     * @param enabled
     */
    private void setLinkTextEnabled(TextView textView, boolean enabled) {
        textView.setEnabled(enabled);
        textView.setTextColor(enabled ? getResources().getColor(R.color.colorPrimaryDark) : getResources().getColor(R.color.font_main));
    }

    @Override
    public void onBackPressed() {
//        if (MCloudApp.isIsBluetoothDeviceConnected()) {
//            if (isConfigChange) {
//                isExitMode = true;
//                showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
//            } else {
//                finish();
//            }
//        } else {
//            finish();
//        }

        finish();
    }

    @Override
    public void showSaveDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .neutralText("取消")
                .positiveText("保存")
                .negativeText("不保存")
                .negativeColor(Color.parseColor("#807B7B"))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        saveConfigInfo();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();

                        if (isExitMode) {
                            finish();
                        }
                    }
                }).onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
