package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
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
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 通过物联网平台蓝牙配置采集器
 */
public class BleDasCollectorSettingFragment extends BaseBleCommunicateFragment {
    private static final String COLLECTOR_MODEL = "collector_model";

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.collectorAddressET)
    EditText mEtCollectorAddress;

    @BindView(R.id.calculatingTimeET)
    EditText mEtCalculatingTime;

    @BindView(R.id.standbyTimeET)
    EditText mEtStandbyTime;

    @BindView(R.id.collectTimeET)
    EditText mEtCollectTime;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private CollectorConfigInfo collectorConfigInfo = new CollectorConfigInfo();
    private String collectorModel;//采集器类型

    private String collectorAddress;//采集器地址
    private String calculatTime;//解算时间频度
    private String standbyTime;//待机时间
    private String collectTime;//采集时间频度


    public static BleDasCollectorSettingFragment newInstance(String model) {
        BleDasCollectorSettingFragment fragment = new BleDasCollectorSettingFragment();
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
        return R.layout.fragment_das_collector_setting;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFilter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void setFilter() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCalculatingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtStandbyTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtCollectTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCollectorAddress.setHint("0-255");
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
                queryCollectorInfo();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_5000_MILLIS);
            }
        });
    }

    /**
     * 查询采集器配置信息
     */
    private void queryCollectorInfo() {
        CollectorConfigEntity collectorConfigEntity = new CollectorConfigEntity(collectorModel);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, collectorConfigEntity);
        sendCommand(command);
        Timber.d("查询采集器配置信息===%s", command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        collectorAddress = mEtCollectorAddress.getText().toString().trim();
        calculatTime = mEtCalculatingTime.getText().toString().trim();
        standbyTime = mEtStandbyTime.getText().toString().trim();
        collectTime = mEtCollectTime.getText().toString().trim();

        if (TextUtils.isEmpty(collectorAddress)) {
            ToastUtils.show("请输入采集器地址!");
            mEtCollectorAddress.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(collectorAddress);
            if (port < 0 || port > 255) {
                ToastUtils.show("请输入正确的采集器地址!");
                mEtCollectorAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的采集器地址!");
            mEtCollectorAddress.requestFocus();
            return false;
        }

        if (!collectorConfigInfo.getWorkTime().equals(calculatTime)) {
            if (TextUtils.isEmpty(calculatTime)) {
                ToastUtils.show("请输入解算时间!");
                mEtCalculatingTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(calculatTime);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的解算时间!");
                mEtCalculatingTime.requestFocus();
                return false;
            }
        } else {
            calculatTime = "";
        }

        if (!collectorConfigInfo.getStandbyTime().equals(standbyTime)) {
            if (TextUtils.isEmpty(standbyTime)) {
                ToastUtils.show("请输入待机时间!");
                mEtStandbyTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(standbyTime);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的待机时间!");
                mEtStandbyTime.requestFocus();
                return false;
            }
        } else {
            standbyTime = "";
        }

        if (!collectorConfigInfo.getCollectorInterval().equals(collectTime)) {
            if (TextUtils.isEmpty(collectTime)) {
                ToastUtils.show("请输入采集时间!");
                mEtCollectTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(collectTime);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的采集时间!");
                mEtStandbyTime.requestFocus();
                return false;
            }
        } else {
            collectTime = "";
        }
        return true;
    }

    private void processSave() {
        commandItems.clear();

        SetCollectorAddressEntity collectorAddressEntity = new SetCollectorAddressEntity(Integer.parseInt(collectorAddress));
        String command = CommandManager.getInstance().getCommand(CommandType.SET_COLLECTOR_ADDRESS, collectorAddressEntity);
        commandItems.add(command);

        if (!TextUtils.isEmpty(calculatTime)) {
            CollectorSolutionFrequencyEntity frequencyEntity = new CollectorSolutionFrequencyEntity(collectorModel, StringUtil.formatStringFour(calculatTime));
            command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_SOLUTION_FREQUENCY, frequencyEntity);
            commandItems.add(command);
        }
        if (!TextUtils.isEmpty(standbyTime)) {
            CollectorStandbyTimeEntity collectorStandbyTimeEntity = new CollectorStandbyTimeEntity(collectorModel, StringUtil.formatStringFour(standbyTime));
            command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_STANDBY_TIME, collectorStandbyTimeEntity);
            commandItems.add(command);
        }
        if (!TextUtils.isEmpty(collectTime)) {
            CollectorFrequencyEntity collectorFrequencyEntity = new CollectorFrequencyEntity(collectorModel, StringUtil.formatStringFive(collectTime));
            command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_FREQUENCY, collectorFrequencyEntity);
            commandItems.add(command);
        }

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        sendCommand(commandItems.getFirst());
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
                mRefreshLayout.finishRefresh(true);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询采集器配置信息指令出错!");
                    return;
                }
                collectorConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                initValue();
                break;

            case SET_COLLECTOR_ADDRESS:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器地址配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;

            case COLLECTOR_SOLUTION_FREQUENCY:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器解算频度配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;

            case COLLECTOR_STANDBY_TIME:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器待机时长配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;

            case COLLECTOR_FREQUENCY:
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器采集频度配置错误!");
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    sendCommand(commandItems.getFirst());
                } else {
                    doAfterSetting();
                }
                break;

            case SAVE_CONFIG_INFO:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                collectorAddress = mEtCollectorAddress.getText().toString().trim();
                calculatTime = mEtCalculatingTime.getText().toString().trim();
                standbyTime = mEtStandbyTime.getText().toString().trim();
                collectTime = mEtCollectTime.getText().toString().trim();
                ToastUtils.show("保存成功");
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
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

    private void doAfterSetting() {
        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
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

                case AppContants.MsgWhat.MSG_HEART:
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
