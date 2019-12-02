package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.das.common.CollectorConfigInfo;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.util.StringUtil;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   GeneralSettingActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/15 15:23
 * 描述：    通用配置页面——采集器配置
 */
public class GeneralSettingActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_collector_address)
    ImageView mIvCollectorAddress;

    @BindView(R.id.et_collector_address)
    EditText mEtCollectorAddress;

    @BindView(R.id.iv_calculating_time)
    ImageView mIvCalculatingTime;

    @BindView(R.id.et_calculating_time)
    EditText mEtCalculatingTime;

    @BindView(R.id.iv_standby_time)
    ImageView mIvStandbyTime;

    @BindView(R.id.et_standby_time)
    EditText mEtStandbyTime;

    @BindView(R.id.iv_collect_time)
    ImageView mIvCollectTime;

    @BindView(R.id.et_collect_time)
    EditText mEtCollectTime;

    @BindView(R.id.btn_confirm_complete)
    Button mBtnConfirmComplete;

    private String collectorAddress;
    private String calculatTime;
    private String standbyTime;
    private String collectTime;

    private String collectorType;


    public static void startActivity(Context context, CollectorConfigInfo collectorConfigInfo, String collectorType) {
        Intent intent = new Intent(context, GeneralSettingActivity.class);
        intent.putExtra(Extras.PARAM_CONFIG_INFO, collectorConfigInfo);
        intent.putExtra(Extras.COLLECTOR_TYPE, collectorType);

        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_general_setting;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        parseIntent();
    }


    private void parseIntent() {
        mToolbarTitle.setText("通用设置");

        Intent intent = getIntent();
        if (intent.getExtras().containsKey(Extras.PARAM_CONFIG_INFO)) {
            CollectorConfigInfo collectorConfigInfo = (CollectorConfigInfo) intent.getSerializableExtra(Extras.PARAM_CONFIG_INFO);
            if (collectorConfigInfo != null) {
                mEtCollectorAddress.setText(collectorConfigInfo.getCollectorAddress());
                mEtCalculatingTime.setText(collectorConfigInfo.getWorkTime());
                mEtStandbyTime.setText(collectorConfigInfo.getStandbyTime());
                mEtCollectTime.setText(collectorConfigInfo.getCollectorInterval());
            }
        }

        if (intent.getExtras().containsKey(Extras.COLLECTOR_TYPE)) {
            collectorType = intent.getStringExtra(Extras.COLLECTOR_TYPE);
        }
    }


    @OnClick({R.id.iv_collector_address, R.id.iv_calculating_time, R.id.iv_standby_time, R.id.iv_collect_time, R.id.btn_confirm_complete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_collector_address:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.iv_calculating_time:
                showTipDialog(getResources().getString(R.string.calculating_time));
                break;

            case R.id.iv_standby_time:
                showTipDialog(getResources().getString(R.string.standby_time));
                break;

            case R.id.iv_collect_time:
                showTipDialog(getResources().getString(R.string.collect_time));
                break;

            case R.id.btn_confirm_complete:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                sendCollector();
                break;
        }
    }


    private void sendCollector() {
        collectorAddress = mEtCollectorAddress.getText().toString().trim();
        calculatTime = mEtCalculatingTime.getText().toString().trim();
        standbyTime = mEtStandbyTime.getText().toString().trim();
        collectTime = mEtCollectTime.getText().toString().trim();

        int address = Integer.parseInt(collectorAddress);
        if (address < 0 || address >= 255) {
            ToastUtils.show("采集器地址输入有误");
            return;
        }

        if (TextUtils.isEmpty(calculatTime)) {
            ToastUtils.show("解算时间不能为空");
            return;
        }

        if (TextUtils.isEmpty(standbyTime)) {
            ToastUtils.show("待机时间不能为空");
            return;
        }

        if (TextUtils.isEmpty(collectTime)) {
            ToastUtils.show("采集时间不能为空");
            return;
        }

        String cmdCollectorAddress = "##147" + collectorAddress + "\r\n";
        String cmdCollectTime = "##161" + collectorType + StringUtil.formatStringFive(collectTime) + "\r\n";
        String cmdStandbyTime = "##160" + collectorType + StringUtil.formatStringFour(standbyTime) + "\r\n";
        String cmdCalculatTime = "##163" + collectorType + StringUtil.formatStringFour(calculatTime) + "\r\n";

        sendCommonCommand(cmdCollectorAddress);
        Timber.d("发送设置采集器地址指令===" + cmdCollectorAddress);

        sendCommonCommand(cmdCollectTime);
        Timber.d("发送设置采集器采集频度指令===" + cmdCollectTime);

        sendCommonCommand(cmdStandbyTime);
        Timber.d("发送设置采集器待机时长指令===" + cmdStandbyTime);

        sendCommonCommand(cmdCalculatTime);
        Timber.d("发送设置采集器解算频度指令===" + cmdCalculatTime);
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
