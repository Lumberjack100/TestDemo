package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorConfigEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorFrequencyEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorSolutionFrequencyEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorStandbyTimeEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetCollectorAddressEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 通过物联网平台蓝牙配置采集器
 */
public class BleCollectorSettingFragment extends BaseBleConnectFragment {
    private static final String COLLECTOR_MODEL = "collector_model";

    @BindView(R.id.collectorAddressET)
    EditText mEtCollectorAddress;

    @BindView(R.id.calculatingTimeET)
    EditText mEtCalculatingTime;

    @BindView(R.id.standbyTimeET)
    EditText mEtStandbyTime;

    @BindView(R.id.collectTimeET)
    EditText mEtCollectTime;

    @BindView(R.id.btn_confirm)
    Button mBtnConfirmComplete;

    private CollectorConfigInfo collectorConfigInfo = new CollectorConfigInfo();
    private String collectorModel;//采集器类型

    private String collectorAddress;//采集器地址
    private String calculatTime;//解算时间频度
    private String standbyTime;//待机时间
    private String collectTime;//采集时间频度

    private String cmdCollectorAddress;//采集器地址
    private String cmdCalculatTime;//解算时间频度
    private String cmdStandbyTime;//待机时间
    private String cmdCollectTime;//采集时间频度


    public static BleCollectorSettingFragment newInstance(String model) {
        BleCollectorSettingFragment fragment = new BleCollectorSettingFragment();
        Bundle args = new Bundle();
        args.putString(COLLECTOR_MODEL, model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectorModel = getArguments().getString(COLLECTOR_MODEL);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_collector_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        queryCollectorInfo();
    }

    private void setFilter() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCalculatingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtStandbyTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtCollectTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mEtCollectorAddress.setHint("0-255");
    }

    /**
     * 查询采集器配置信息
     */
    private void queryCollectorInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", 20000);
        CollectorConfigEntity collectorConfigEntity = new CollectorConfigEntity(collectorModel);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, collectorConfigEntity);
        sendCommonCommandImmediately(command);
        Timber.d("查询采集器配置信息===%s", command);
    }

    private void initValue() {
        if (collectorConfigInfo == null) {
            collectorConfigInfo = new CollectorConfigInfo();
            return;
        }

        collectorAddress = collectorConfigInfo.getCollectorAddress();
        calculatTime = collectorConfigInfo.getWorkTime();
        standbyTime = collectorConfigInfo.getStandbyTime();
        collectTime = collectorConfigInfo.getCollectorInterval();

        mEtCollectorAddress.setText(collectorAddress);
        mEtCalculatingTime.setText(calculatTime);
        mEtStandbyTime.setText(standbyTime);
        mEtCollectTime.setText(collectTime);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            sendCollector();
        }
    }

    private void sendCollector() {
        collectorAddress = mEtCollectorAddress.getText().toString().trim();
        calculatTime = mEtCalculatingTime.getText().toString().trim();
        standbyTime = mEtStandbyTime.getText().toString().trim();
        collectTime = mEtCollectTime.getText().toString().trim();

        if (TextUtils.isEmpty(collectorAddress)) {
            ToastUtils.show("采集器地址不能为空");
            mEtCollectorAddress.requestFocus();
            return;
        }
        if (Integer.parseInt(collectorAddress) < 0) {
            ToastUtils.show("采集器地址不能小于0");
            mEtCollectorAddress.requestFocus();
            return;
        }
        if (Integer.parseInt(collectorAddress) > 255) {
            ToastUtils.show("采集器地址不能大于255");
            mEtCollectorAddress.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(calculatTime)) {
            ToastUtils.show("解算频度不能为空");
            mEtCalculatingTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(standbyTime)) {
            ToastUtils.show("待机时长不能为空");
            mEtStandbyTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(collectTime)) {
            ToastUtils.show("采集频度不能为空");
            mEtCollectTime.requestFocus();
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
            case COLLECTOR_CONFIG://采集器配置信息 100
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    Timber.e("查询采集器配置信息指令出错!");
                    return;
                }
                stopProgressRunnable();
                collectorConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                initValue();
                break;

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
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器采集频度配置错误!");
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
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void doAfterSetting() {
        stopProgressRunnable();
        saveConfigInfoNoReboot();
    }


    @Override
    public boolean onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
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
        if (collectorAddress != null && !collectorAddress.equals(mEtCollectorAddress.getText().toString().trim())) {
            return true;
        }

        if (calculatTime != null && !calculatTime.equals(mEtCalculatingTime.getText().toString().trim())) {
            return true;
        }

        if (standbyTime != null && !standbyTime.equals(mEtStandbyTime.getText().toString().trim())) {
            return true;
        }

        if (collectTime != null && !collectTime.equals(mEtCollectTime.getText().toString().trim())) {
            return true;
        }

        return false;
    }
}
