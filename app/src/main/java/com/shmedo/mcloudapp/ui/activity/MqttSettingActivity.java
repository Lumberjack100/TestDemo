package com.shmedo.mcloudapp.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.MqttConfigInfoSub;
import com.shmedo.mcloudapp.inter.MQttOnClickListener;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.MqttDialogFactory;
import com.shmedo.mcloudapp.views.ClearEditText;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   MqttSettingActivity
 * 创建者:   dpc
 * 创建时间:  2019/11/26 15:23
 * 描述：    mqtt设置
 */
public class MqttSettingActivity extends BaseDeviceConnectActivity {

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

    private Dialog dialog;
    private View contentView;
    private MqttConfigInfoSub mqttConfigInfoSub = new MqttConfigInfoSub();

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

        ((TextView) linkTwo.findViewById(R.id.tv_config_name)).setText("链路2:");
        mLinkTwoStatus = linkTwo.findViewById(R.id.tv_device_state);
        mSbLinkTwo = linkTwo.findViewById(R.id.switchButton);

        ((TextView) linkThree.findViewById(R.id.tv_config_name)).setText("链路3:");
        mLinkThreeStatus = linkThree.findViewById(R.id.tv_device_state);
        mSbLinkThree = linkThree.findViewById(R.id.switchButton);

        //断线报警器状态
        String[] cmData = getResources().getStringArray(R.array.communication_method);
        CommunicationMethodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cmData);
        CommunicationMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCommunicationmethod.setAdapter(CommunicationMethodAdapter);


    }

    private void setSwitchViewListener() {
        mSbLinkOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Timber.i("mSbLinkOne" + isChecked);

                    factory.createDialog(MqttSettingActivity.this,"1",mqttConfigInfoSub,onClickListener);
                } else {
                    Timber.i("mSbLinkOne" + isChecked);
                }
            }
        });
        mSbLinkTwo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Timber.i("mSbLinkTwo" + isChecked);
                } else {
                    Timber.i("mSbLinkTwo" + isChecked);
                }
            }
        });
        mSbLinkThree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Timber.i("mSbLinkThree" + isChecked);
                } else {
                    Timber.i("mSbLinkThree" + isChecked);
                }
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


    private void setLinkText(){

    }
}
