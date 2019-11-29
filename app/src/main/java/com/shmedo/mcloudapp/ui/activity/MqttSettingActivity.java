package com.shmedo.mcloudapp.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.MqttConfigInfoSub;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.inter.MQttOnClickListener;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.MqttDialogFactory;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

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
    private boolean editLinkOne,editLinkTwo,editLinkThree;
    //SwitchButton是否打开
    private boolean isCheckedLinkOne,isCheckedLinkTwo,isCheckedLinkThree;

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
        setToolBar(R.id.toolbar);
        initView();
        initData();
        setSwitchViewListener();
        showMqttConfigDialog();
    }

    private void initView() {
        toolbarTitle.setText("配置MQTT");

        ((TextView) communicationMethod.findViewById(R.id.tv_config_name)).setText("通讯方式:");
        mTvCommunicationMethod = communicationMethod.findViewById(R.id.tv_device_state);
        spCommunicationmethod = communicationMethod.findViewById(R.id.spinner);

        ((TextView) dataReport.findViewById(R.id.tv_config_name)).setText("数据上报:");
        mTvDataReport = dataReport.findViewById(R.id.tv_device_state);
        mCetDataReport = dataReport.findViewById(R.id.clear_edit_text);
        mCetDataReport.setHint("默认120s");

        llBd = findViewById(R.id.ll_bd);
        ((TextView) bdCardNumber.findViewById(R.id.tv_config_name)).setText("北斗配置:");
        mTvBdConfig = bdCardNumber.findViewById(R.id.tv_device_state);
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

        //断线报警器状态
        String[] cmData = getResources().getStringArray(R.array.communication_method);
        CommunicationMethodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cmData);
        CommunicationMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCommunicationmethod.setAdapter(CommunicationMethodAdapter);

        mLinkOneStatus.setOnClickListener(this);
        mLinkTwoStatus.setOnClickListener(this);
        mLinkThreeStatus.setOnClickListener(this);
    }

    private void initData() {
        //查询链路开启状态
        sendCommonCommand("##2001\r\n");
        sendCommonCommand("##2002\r\n");
        sendCommonCommand("##2003\r\n");
    }

    private void setSwitchViewListener() {
        mSbLinkOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLinkText(mLinkOneStatus,isChecked);
                isCheckedLinkOne = isChecked;
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭链路1？",1);
                } else {
                    editLinkOne = false;
                }
                Timber.i("mSbLinkOne===" + isChecked);
            }
        });
        mSbLinkTwo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLinkText(mLinkTwoStatus,isChecked);
                isCheckedLinkTwo = isChecked;
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭链路2？",2);
                } else {
                    editLinkTwo = false;
                }
                Timber.i("mSbLinkTwo===" + isChecked);
            }
        });
        mSbLinkThree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLinkText(mLinkThreeStatus,isChecked);
                isCheckedLinkThree = isChecked;
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭链路3？",3);
                } else {
                    editLinkThree = false;
                }
                Timber.i("mSbLinkThree===" + isChecked);
            }
        });
    }

    private void showMqttConfigDialog() {

    }

    private MQttOnClickListener onClickListener = new MQttOnClickListener() {
        @Override
        public boolean onSureClick(View v) {

            return true;
        }

        @Override
        public void onCancelClick(View view) {

        }
    };


    private void setLinkText(TextView textView,boolean isOpen){
        if (isOpen){
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
        Timber.i("链路==="+message);
        String commandType = StringUtil.extractCommandType(message);
        switch (commandType){
            case "201":
                if (!StringUtil.isOpenLink(message)){
                    if (StringUtil.linkNumber(message).equals("1")){
                        isCheckedLinkOne = true;
                    }else if (StringUtil.linkNumber(message).equals("2")){
                        isCheckedLinkTwo = true;
                    }else if (StringUtil.linkNumber(message).equals("3")){
                        isCheckedLinkThree = true;
                    }
                }else {
                    isCheckedLinkOne = false;
                    isCheckedLinkTwo = false;
                    isCheckedLinkThree = false;
                }
                break;
            case "889":
                if (isCheckedLinkOne){
                    mqttConfigInfoSub1 = StringUtil.parserMqttConfig(message);
                }else if (isCheckedLinkTwo){
                    mqttConfigInfoSub2 = StringUtil.parserMqttConfig(message);
                }else if (isCheckedLinkThree){
                    mqttConfigInfoSub3 = StringUtil.parserMqttConfig(message);
                }
                break;

        }

        //根据返回指令初始化按钮状态
        mSbLinkOne.setChecked(isCheckedLinkOne);
        mSbLinkTwo.setChecked(isCheckedLinkTwo);
        mSbLinkThree.setChecked(isCheckedLinkThree);
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_link_one:
                if (!editLinkOne){
                    editLinkOne = true;
                    //查询数据中心参数
                    sendCommonCommand("##8891\r\n");
                    Timber.i("链路1第1次编辑");
                    //弹框
                    factory.createDialog(MqttSettingActivity.this,"1",mqttConfigInfoSub1,onClickListener);
                }else {
                    //直接弹框
                    Timber.i("链路1第2次编辑");
                    factory.createDialog(MqttSettingActivity.this,"1",mqttConfigInfoSub1,onClickListener);
                }
                break;
            case R.id.tv_link_two:
                if (!editLinkTwo){
                    editLinkTwo = true;
                    //查询数据中心参数
                    sendCommonCommand("##8892\r\n");
                    Timber.i("链路2第1次编辑");
                    //弹框
                    factory.createDialog(MqttSettingActivity.this,"2",mqttConfigInfoSub2,onClickListener);
                }else {
                    //直接弹框
                    Timber.i("链路2第2次编辑");
                    factory.createDialog(MqttSettingActivity.this,"2",mqttConfigInfoSub2,onClickListener);
                }
                break;
            case R.id.tv_link_three:
                if (!editLinkThree){
                    editLinkThree = true;
                    //查询数据中心参数
                    sendCommonCommand("##8893\r\n");
                    Timber.i("链路3第1次编辑");
                    //弹框
                    factory.createDialog(MqttSettingActivity.this,"3",mqttConfigInfoSub3,onClickListener);
                }else {
                    //直接弹框
                    Timber.i("链路3第2次编辑");
                    factory.createDialog(MqttSettingActivity.this,"3",mqttConfigInfoSub3,onClickListener);
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
                                setLinkText(mLinkOneStatus,false);
                                sendCommonCommand("##2011\r\n");
                                break;
                            case 2:
                                mSbLinkTwo.setCheckedImmediatelyNoEvent(false);
                                setLinkText(mLinkTwoStatus,false);
                                sendCommonCommand("##2012\r\n");
                                break;
                            case 3:
                                mSbLinkThree.setCheckedImmediatelyNoEvent(false);
                                setLinkText(mLinkThreeStatus,false);
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
                                setLinkText(mLinkOneStatus,true);
                                break;
                            case 2:
                                mSbLinkTwo.setCheckedImmediatelyNoEvent(true);
                                setLinkText(mLinkTwoStatus,true);
                                break;
                            case 3:
                                mSbLinkThree.setCheckedImmediatelyNoEvent(true);
                                setLinkText(mLinkThreeStatus,true);
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
