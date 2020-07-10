package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.entity.CollectorFrequencyEntity;
import com.shmedo.core.cmd.entity.CollectorSolutionFrequencyEntity;
import com.shmedo.core.cmd.entity.CollectorStandbyTimeEntity;
import com.shmedo.core.cmd.entity.SetCollectorAddressEntity;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.model.CollectorConfigInfo;
import com.shmedo.core.utils.StringUtil;
import com.dragon.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.interfaces.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    @BindView(R.id.tv_title)
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

    private String collectorModel;//采集器类型

    private String collectorAddress;//采集器地址
    private String calculatTime;//解算时间频度
    private String standbyTime;//待机时间
    private String collectTime;//采集时间频度

    private String cmdCollectorAddress;//采集器地址

    private String cmdCalculatTime;//解算时间频度

    private String cmdStandbyTime;//待机时间

    private String cmdCollectTime;//采集时间频度

    private CollectorConfigInfo collectorConfigInfo;


    public static void startActivity(Context context, CollectorConfigInfo collectorConfigInfo, String collectorModel) {
        Intent intent = new Intent(context, GeneralSettingActivity.class);
        intent.putExtra(Extras.PARAM_CONFIG_INFO, collectorConfigInfo);
        intent.putExtra(Extras.COLLECTOR_MODE, collectorModel);
        context.startActivity(intent);
    }

    public static void startActivityForResultByFragment(Fragment context, int requestCode, CollectorConfigInfo collectorConfigInfo, String collectorModel) {
        Intent intent = new Intent(context.getActivity(), GeneralSettingActivity.class);
        intent.putExtra(Extras.PARAM_CONFIG_INFO, collectorConfigInfo);
        intent.putExtra(Extras.COLLECTOR_MODE, collectorModel);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_general_setting;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        parseIntent();
    }

    private void initView() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCalculatingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtStandbyTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtCollectTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
    }


    private void parseIntent() {
        mToolbarTitle.setText("采集器设置");

        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(Extras.PARAM_CONFIG_INFO)) {
            collectorConfigInfo = (CollectorConfigInfo) intent.getSerializableExtra(Extras.PARAM_CONFIG_INFO);
            if (collectorConfigInfo != null) {
                mEtCollectorAddress.setText(collectorConfigInfo.getCollectorAddress());
                mEtCalculatingTime.setText(collectorConfigInfo.getWorkTime());
                mEtStandbyTime.setText(collectorConfigInfo.getStandbyTime());
                mEtCollectTime.setText(collectorConfigInfo.getCollectorInterval());
            }
        }

        if (intent.getExtras().containsKey(Extras.COLLECTOR_MODE)) {
            collectorModel = intent.getStringExtra(Extras.COLLECTOR_MODE);
        }
    }


    @OnClick({R.id.back, R.id.iv_collector_address, R.id.iv_calculating_time, R.id.iv_standby_time, R.id.iv_collect_time, R.id.btn_confirm_complete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;

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

        if (TextUtils.isEmpty(collectorAddress) || Integer.parseInt(collectorAddress) < 0 || Integer.parseInt(collectorAddress) > 255) {
            ToastUtils.show("请输入正确的采集器地址");
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

        SetCollectorAddressEntity collectorAddressEntity = new SetCollectorAddressEntity(Integer.parseInt(collectorAddress));
        cmdCollectorAddress = CommandManager.getInstance().getCommand(CommandType.SET_COLLECTOR_ADDRESS, collectorAddressEntity);

        CollectorSolutionFrequencyEntity frequencyEntity = new CollectorSolutionFrequencyEntity(collectorModel, StringUtil.formatStringFour(calculatTime));
        cmdCalculatTime = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_SOLUTION_FREQUENCY, frequencyEntity);

        CollectorStandbyTimeEntity collectorStandbyTimeEntity = new CollectorStandbyTimeEntity(collectorModel, StringUtil.formatStringFour(standbyTime));
        cmdStandbyTime = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_STANDBY_TIME, collectorStandbyTimeEntity);

        CollectorFrequencyEntity collectorFrequencyEntity = new CollectorFrequencyEntity(collectorModel, StringUtil.formatStringFive(collectTime));
        cmdCollectTime = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_FREQUENCY, collectorFrequencyEntity);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
        sendCommonCommandImmediately(cmdCollectorAddress);
        Timber.d("设置采集器地址指令===%s", cmdCollectorAddress);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SET_COLLECTOR_ADDRESS:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器地址配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdCalculatTime);
                Timber.d("设置采集器解算频度指令===%s", cmdCalculatTime);
                break;

            case COLLECTOR_SOLUTION_FREQUENCY:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器解算频度配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdStandbyTime);
                Timber.d("设置采集器待机时长指令===%s", cmdStandbyTime);
                break;

            case COLLECTOR_STANDBY_TIME:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器待机时长配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sendCommonCommandImmediately(cmdCollectTime);
                Timber.d("设置采集器采集频度指令===%s", cmdCollectTime);
                break;

            case COLLECTOR_FREQUENCY:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器采集频度配置错误!");
                    stopProgressRunnable();
                    return;
                }
                collectorConfigInfo.setCollectorAddress(collectorAddress);
                collectorConfigInfo.setWorkTime(calculatTime);
                collectorConfigInfo.setStandbyTime(standbyTime);
                collectorConfigInfo.setCollectorInterval(collectTime);

                stopProgressRunnable();
                ToastUtils.show("设置完成");
                hander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = getIntent();
                        intent.putExtra(Extras.PARAM_CONFIG_INFO, collectorConfigInfo);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, 2000);
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (TextUtils.isEmpty(messageEvent) || !messageEvent.startsWith("$$")) {
            return;
        }
        setResultData(messageEvent);
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

}
