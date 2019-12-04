package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.das.common.BaseConfigInfo;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.das.das.cmd.parser.ParseManager;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.MqttConfigInfoSub;
import com.shmedo.mcloudapp.inter.MQttOnClickListener;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.MqttDialogFactory;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.views.ClearEditText;

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
public class MqttSettingActivity extends BaseDeviceConnectActivity implements View.OnClickListener {
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

    @BindView(R.id.link_one)
    View linkOne;

    @BindView(R.id.link_two)
    View linkTwo;

    @BindView(R.id.link_three)
    View linkThree;

    @BindView(R.id.ll_bd)
    LinearLayout llBd;

    @BindView(R.id.btn_confirm)
    Button btnConfirm;


    private MqttDialogFactory factory = new MqttDialogFactory();

    private TextView mTvCommunicationMethod, mTvDataReport, mTvBdConfig, mTvLinkOne, mLinkOneStatus,
            mTvLinkTwo, mLinkTwoStatus, mTvLinkThree, mLinkThreeStatus;

    private Spinner spCommunicationmethod;

    private SwitchButton mSbLinkOne, mSbLinkTwo, mSbLinkThree;

    private ClearEditText mCetDataReport, mCetBDCardNumber;

    private ArrayAdapter<String> CommunicationMethodAdapter;

    private MqttConfigInfoSub mqttConfigInfoSub1 = new MqttConfigInfoSub();
    private MqttConfigInfoSub mqttConfigInfoSub2 = new MqttConfigInfoSub();
    private MqttConfigInfoSub mqttConfigInfoSub3 = new MqttConfigInfoSub();

    //第一次编辑发送指令，第二次直接弹框
    private boolean editLinkOne, editLinkTwo, editLinkThree;
    //SwitchButton是否打开
    private boolean isCheckedLinkOne, isCheckedLinkTwo, isCheckedLinkThree;

    private String dataCommunicationMode;

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
        tvTitle.setText("配置MQTT");
        tvSave.setVisibility(View.VISIBLE);
        ((TextView) communicationMethod.findViewById(R.id.tv_config_name)).setText("通讯方式:");
//        mTvCommunicationMethod = communicationMethod.findViewById(R.id.tv_device_state);
        spCommunicationmethod = communicationMethod.findViewById(R.id.spinner);

        ((TextView) dataReport.findViewById(R.id.tv_config_name)).setText("数据上报:");
//        mTvDataReport = dataReport.findViewById(R.id.tv_device_state);
        mCetDataReport = dataReport.findViewById(R.id.clear_edit_text);
        mCetDataReport.setHint("默认120分钟");

        llBd = findViewById(R.id.ll_bd);
        ((TextView) bdCardNumber.findViewById(R.id.tv_config_name)).setText("北斗配置:");
//        mTvBdConfig = bdCardNumber.findViewById(R.id.tv_device_state);
        mCetBDCardNumber = bdCardNumber.findViewById(R.id.clear_edit_text);
        mCetBDCardNumber.setHint("请输入BD卡号");

        ((TextView) linkOne.findViewById(R.id.tv_config_name)).setText("链路1:");
        mLinkOneStatus = linkOne.findViewById(R.id.tv_device_state);
        mSbLinkOne = linkOne.findViewById(R.id.switchButton);
        mLinkOneStatus.setId(R.id.tv_link_one);

        ((TextView) linkTwo.findViewById(R.id.tv_config_name)).setText("链路2:");
        mLinkTwoStatus = linkTwo.findViewById(R.id.tv_device_state);
        mSbLinkTwo = linkTwo.findViewById(R.id.switchButton);
        mLinkTwoStatus.setId(R.id.tv_link_two);

        ((TextView) linkThree.findViewById(R.id.tv_config_name)).setText("链路3:");
        mLinkThreeStatus = linkThree.findViewById(R.id.tv_device_state);
        mSbLinkThree = linkThree.findViewById(R.id.switchButton);
        mLinkThreeStatus.setId(R.id.tv_link_three);

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
        mLinkOneStatus.setOnClickListener(this);
        mLinkTwoStatus.setOnClickListener(this);
        mLinkThreeStatus.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        back.setOnClickListener(this);
        tvSave.setOnClickListener(this);
    }

    private void initData() {
        startProgressRunnable("正在获取指令参数...", 10000);
        //查询链路开启状态
        sendCommonCommand("##2001\r\n");
        sendCommonCommand("##2002\r\n");
        sendCommonCommand("##2003\r\n");
        //获取基础配置信息
        sendCommonCommand("##000\r\n");
    }

    private void setSwitchViewListener() {
        mSbLinkOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLinkText(mLinkOneStatus, isChecked);
                isCheckedLinkOne = isChecked;
                Timber.i("lianlu1de boolean ==== " + isCheckedLinkOne);
                if (!isCheckedLinkOne) {
                    showCloseSwitchButtonDialog("确定要关闭链路1？", 1);
                } else {
                    Timber.i("11111111111=====手动打开");
                    sendCommonCommand("##8891\r\n");
                    editLinkOne = false;
                }
                Timber.i("mSbLinkOne===" + isChecked);
            }
        });
        mSbLinkTwo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLinkText(mLinkTwoStatus, isChecked);
                isCheckedLinkTwo = isChecked;
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭链路2？", 2);
                } else {
                    Timber.i("222222222=====手动打开");
                    sendCommonCommand("##8892\r\n");

                    editLinkTwo = false;
                }
                Timber.i("mSbLinkTwo===" + isChecked);
            }
        });
        mSbLinkThree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLinkText(mLinkThreeStatus, isChecked);
                isCheckedLinkThree = isChecked;
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭链路3？", 3);
                } else {
                    Timber.i("33333333=====手动打开");
                    sendCommonCommand("##8893\r\n");
                    editLinkThree = false;
                }
                Timber.i("mSbLinkThree===" + isChecked);
            }
        });
    }


    private MQttOnClickListener onClickListener = new MQttOnClickListener() {

        @Override
        public boolean onSureClick(View v, MqttConfigInfoSub mqttConfigInfoSub, String linkNumber) {
            Timber.i("===MQttOnClickListener===" + mqttConfigInfoSub.toString());
            String communicationProtocol = mqttConfigInfoSub.getCommunicationProtocol();
            String dataPlatformAddress = mqttConfigInfoSub.getDataPlatformAddress();
            String keepAliveValue = mqttConfigInfoSub.getKeepAliveValue();
            String deviceSn = mqttConfigInfoSub.getDeviceSn();
            String productId = mqttConfigInfoSub.getProductId();
            String registrationCode = mqttConfigInfoSub.getRegistrationCode();
            String registrationPlatform = mqttConfigInfoSub.getRegistrationPlatform();
            String registrationPlatformAddress = mqttConfigInfoSub.getRegistrationPlatformAddress();
            String appkey = mqttConfigInfoSub.getAppKey();
            String mqttDeviceId = mqttConfigInfoSub.getMqttDeviceId();
            String mqttUsername = mqttConfigInfoSub.getMqttUsername();
            String mqttPassword = mqttConfigInfoSub.getMqttPassword();

            //1、设置网络链路通讯协议
            sendCommonCommand("##202" + linkNumber + communicationProtocol + "\r\n");
            //2、设置自动注册平台参数
            sendCommonCommand("##803" + linkNumber + deviceSn + "," + productId + "," + registrationCode + "\r\n");
            //3、设置手动注册平台参数
            sendCommonCommand("##805" + linkNumber + mqttDeviceId + "," + mqttUsername + "," + mqttPassword + "\r\n");
            //4、设置自动注册平台地址端口
            sendCommonCommand("##807" + linkNumber + registrationPlatformAddress + "\r\n");
            //5、设置MQTT KeepAlive值
            sendCommonCommand("##809" + linkNumber + keepAliveValue + "\r\n");
            //6、设置数据平台地址端口
            sendCommonCommand("##201" + linkNumber + dataPlatformAddress + "\r\n");
            //7、选择平台
            sendCommonCommand("##810" + linkNumber + registrationPlatform + "\r\n");
            //8、appKey(米度/北京平台特有)：
            sendCommonCommand("##811" + linkNumber + appkey + "\r\n");
            return false;
        }

        @Override
        public void onCancelClick(View view) {
        }
    };


    private void setLinkText(TextView textView, boolean isOpen) {
        if (isOpen) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("编辑");
            textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }


    private void setResultData(String message) {
        Timber.i("链路===" + message);
        String commandType = StringUtil.extractCommandType(message);
        switch (commandType) {
            case "200":
                if (StringUtil.isOpenLink(message)) {
                    String linkNumber = StringUtil.linkNumber(message);
                    switch (linkNumber) {
                        case "1":
                            isCheckedLinkOne = true;
                            //根据返回指令初始化按钮状态
                            mSbLinkOne.setCheckedImmediatelyNoEvent(isCheckedLinkOne);
                            setLinkText(mLinkOneStatus, isCheckedLinkOne);
                            break;
                        case "2":
                            isCheckedLinkTwo = true;
                            mSbLinkTwo.setCheckedImmediatelyNoEvent(isCheckedLinkTwo);
                            setLinkText(mLinkTwoStatus, isCheckedLinkTwo);
                            break;
                        case "3":
                            isCheckedLinkThree = true;
                            mSbLinkThree.setCheckedImmediatelyNoEvent(isCheckedLinkThree);
                            setLinkText(mLinkThreeStatus, isCheckedLinkThree);
                            break;
                    }
                } else {
                    isCheckedLinkOne = false;
                    isCheckedLinkTwo = false;
                    isCheckedLinkThree = false;
                }
                break;
            case "889":
                if (isCheckedLinkOne) {
                    mqttConfigInfoSub1 = StringUtil.parserMqttConfig(message);
                    if (mqttConfigInfoSub1 == null)
                        return;
                    //弹框
                    factory.createDialog(MqttSettingActivity.this, "1", mqttConfigInfoSub1, onClickListener);
                } else if (isCheckedLinkTwo) {
                    mqttConfigInfoSub2 = StringUtil.parserMqttConfig(message);
                    if (mqttConfigInfoSub2 == null)
                        return;
                    //弹框
                    factory.createDialog(MqttSettingActivity.this, "2", mqttConfigInfoSub2, onClickListener);
                } else if (isCheckedLinkThree) {
                    mqttConfigInfoSub3 = StringUtil.parserMqttConfig(message);
                    if (mqttConfigInfoSub3 == null)
                        return;
                    //弹框
                    factory.createDialog(MqttSettingActivity.this, "3", mqttConfigInfoSub3, onClickListener);
                }
                break;
            case "000":
                CommandResult<BaseConfigInfo> bean = ParseManager.getInstance().parse(message);
                if (!bean.isSuccess())
                    return;

                Timber.i("===$$000===" + bean.getResult().toString());
                int communicateMode = bean.getResult().getDataCommunicateMode().toInt();
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
                int reportInterval = bean.getResult().getDataReportInterval();
                mCetDataReport.setText(String.valueOf(reportInterval));
                mCetBDCardNumber.setText(bean.getResult().getTargetBGNum());
                stopProgressRunnable();
                break;
        }
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_link_one:
                if (!editLinkOne) {
                    editLinkOne = true;
                    //查询数据中心参数
                    sendCommonCommand("##8891\r\n");
                    Timber.i("链路1第1次编辑");
                } else {
                    if (mqttConfigInfoSub1 == null) {
                        ToastUtils.show("等2s再点击");
                        return;
                    }
                    //直接弹框
                    Timber.i("链路1第2次编辑");
                    factory.createDialog(MqttSettingActivity.this, "1", mqttConfigInfoSub1, onClickListener);
                }
                break;
            case R.id.tv_link_two:
                if (!editLinkTwo) {
                    editLinkTwo = true;
                    //查询数据中心参数
                    sendCommonCommand("##8892\r\n");
                    Timber.i("链路2第1次编辑");
                } else {
                    if (mqttConfigInfoSub2 == null) {
                        ToastUtils.show("等2s再点击");
                        return;
                    }
                    //直接弹框
                    Timber.i("链路2第2次编辑");
                    factory.createDialog(MqttSettingActivity.this, "2", mqttConfigInfoSub2, onClickListener);
                }
                break;
            case R.id.tv_link_three:
                if (!editLinkThree) {
                    editLinkThree = true;
                    //查询数据中心参数
                    sendCommonCommand("##8893\r\n");
                    Timber.i("链路3第1次编辑");
                } else {
                    if (mqttConfigInfoSub3 == null) {
                        ToastUtils.show("等2s再点击");
                        return;
                    }
                    //直接弹框
                    Timber.i("链路3第2次编辑");
                    factory.createDialog(MqttSettingActivity.this, "3", mqttConfigInfoSub3, onClickListener);
                }
                break;
            case R.id.btn_confirm:
                if (MCloudApp.isIsBluetoothDeviceConnected()) {
                    //设置数据通讯模式
                    sendCommonCommand("##003" + dataCommunicationMode + "\r\n");
                    //设置数据上报间隔
                    String report = mCetDataReport.getText() == null ? "" : mCetDataReport.getText().toString().trim();
                    if (report.equals("")) {
                        sendCommonCommand("##143120\r\n");
                    } else {
                        sendCommonCommand("##143" + report + "\r\n");
                    }
                    //设置六位目标北斗卡号
                    String bdNumber = mCetBDCardNumber.getText() == null ? "" : mCetBDCardNumber.getText().toString().trim();
                    if (bdNumber.equals("")) {
                        ToastUtils.show("北斗目标卡号不能为空");
                        return;
                    } else {
                        sendCommonCommand("##001" + bdNumber + "\r\n");
                    }
                } else {
                    ToastUtils.show("蓝牙已断开！");
                }

                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.tv_save:
                if (MCloudApp.isIsBluetoothDeviceConnected()) {
                    showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
                }
                break;
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
                        //发送对应关闭链路
                        switch (index) {
                            case 1:
                                mSbLinkOne.setCheckedImmediatelyNoEvent(false);
                                setLinkText(mLinkOneStatus, false);
                                sendCommonCommand("##2011\r\n");
                                break;
                            case 2:
                                mSbLinkTwo.setCheckedImmediatelyNoEvent(false);
                                setLinkText(mLinkTwoStatus, false);
                                sendCommonCommand("##2012\r\n");
                                break;
                            case 3:
                                mSbLinkThree.setCheckedImmediatelyNoEvent(false);
                                setLinkText(mLinkThreeStatus, false);
                                sendCommonCommand("##2013\r\n");
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
                                setLinkText(mLinkOneStatus, true);
                                break;
                            case 2:
                                mSbLinkTwo.setCheckedImmediatelyNoEvent(true);
                                setLinkText(mLinkTwoStatus, true);
                                break;
                            case 3:
                                mSbLinkThree.setCheckedImmediatelyNoEvent(true);
                                setLinkText(mLinkThreeStatus, true);
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            if (isConfigChange) {
                isExitMode = true;
                showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}
