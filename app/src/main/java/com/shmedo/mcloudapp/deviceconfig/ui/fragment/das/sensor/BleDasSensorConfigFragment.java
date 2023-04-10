package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.BreakAlarmStatusEntity;
import com.shmedo.configlibrary.ble.cmd.entity.DigitalOsmometerFunctionEntity;
import com.shmedo.configlibrary.ble.cmd.entity.RainStationEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetOsmometerAddressEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetOsmometerCordLengthEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetOsmometerCorrectEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetOsmometerNozzelHeightEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetOsmometerTriggerEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetRainPrecisionEntity;
import com.shmedo.configlibrary.ble.enums.BreakAlarmStatus;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.OsmometerStatus;
import com.shmedo.configlibrary.ble.enums.RainStation;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.BreakAlarmStatusInfo;
import com.shmedo.configlibrary.ble.model.QueryOsmometerParameterInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor.DasExternalSensorListActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BaseBleCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wandersnail.widget.textview.RoundTextView;
import timber.log.Timber;

/**
 * DAS 传感器配置页面
 */
public class BleDasSensorConfigFragment extends BaseBleCommunicateFragment {
    static final int RAIN_GAUGE = 1;//是否设置雨量计
    static final int DIGITAL_OSMOMETER = 1 << 1;//是否打开水位计

    private int mExistingUpdateTypes = 0;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.radio_close_switch_sensor)
    RadioButton rbCloseSwitchSensor;//关闭开关量传感器

    @BindView(R.id.radio_rain_gauge)
    RadioButton rbRainGauge;

    @BindView(R.id.rainPrecisionLayout)
    View rainPrecisionLayout;

    @BindView(R.id.tvPrecision)
    RoundTextView mTvPrecision;

    @BindView(R.id.radio_break_alarm)
    RadioButton rbBreakAlarm;

    @BindView(R.id.rg_break_alarm_items)
    RadioGroup rgBreakAlarmItems;//断线报警器状态单选按钮组

    @BindView(R.id.radio_break_alarm_open)
    RadioButton rbBreakAlarmOpen;//断线报警器常开

    @BindView(R.id.radio_break_alarm_close)
    RadioButton rbBreakAlarmClose;//断线报警器常闭

    @BindView(R.id.digitalOsmometerEnableSBtn)
    SwitchButton mSbDigitalOsmometerEnable;

    @BindView(R.id.digitalOsmometerChildsLayout)
    ViewGroup digitalOsmometerChildsLayout;//水位计配置项

    @BindView(R.id.osmometerAddressEt)
    EditText mEtOsmometerAddress;//水位计地址

    @BindView(R.id.waterAlarmValueEt)
    EditText mEtWaterAlarmValue;//深度触发值-水位报警值

    @BindView(R.id.waterRevisedEt)
    EditText mEtWaterRevised;//深度修正值

    @BindView(R.id.osmometerCordEt)
    EditText mEtOsmometerCord;//水位计绳长

    @BindView(R.id.nozzelHeightEt)
    EditText mEtNozzelHeight;//安装高程

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private DecimalFormat decimalFormat = new DecimalFormat();

    private String collectorModel = "";//采集器类型
    private BaseConfigInfo baseConfigInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo = new QueryOsmometerParameterInfo();//数字水位计参数

    private String rainPrecision;
    private String osmometerAddress;
    private String depthTriggerValue;
    private String depthCorrection;
    private String osmometerLength;
    private String nozzelHeight;

    private BreakAlarmStatus breakAlarmStatus = BreakAlarmStatus.QUERY;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_das_sensor;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFilter();
        setRadioButtonListener();
        setSwitchViewListener();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void setFilter() {
        mEtOsmometerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtWaterAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtWaterRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        rainPrecision = "0.1";
        mTvPrecision.setText(rainPrecision);
    }

    /**
     * 单选框事件
     */
    private void setRadioButtonListener() {
        //关闭单选按钮
        rbCloseSwitchSensor.setOnCheckedChangeListener(onCheckedChangeListener);
        //雨量计单选按钮
        rbRainGauge.setOnCheckedChangeListener(onCheckedChangeListener);
        //断线报警器单选按钮
        rbBreakAlarm.setOnCheckedChangeListener(onCheckedChangeListener);
        rgBreakAlarmItems.setOnCheckedChangeListener(breakAlarmOnCheckedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.radio_close_switch_sensor) {//关闭开关传感器单选按钮
                if (isChecked) {
                    rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_343434));
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    mExistingUpdateTypes &= ~RAIN_GAUGE;

                    //发送关闭传感器指令
                    setSwitchSensorCmd(RainStation.CLOSE);
                } else {
                    rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                }
            } else if (id == R.id.radio_rain_gauge) {//雨量计单选按钮
                if (isChecked) {
                    rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_343434));
                    rainPrecisionLayout.setVisibility(View.VISIBLE);
                    rbCloseSwitchSensor.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    mExistingUpdateTypes |= RAIN_GAUGE;

                    setSwitchSensorCmd(RainStation.OPEN);
                } else {
                    rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                    rainPrecisionLayout.setVisibility(View.GONE);
                }
            } else if (id == R.id.radio_break_alarm) {  //断线报警器单选按钮
                if (isChecked) {
                    rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_343434));
                    rgBreakAlarmItems.setVisibility(View.VISIBLE);
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setChecked(false);
                    mExistingUpdateTypes &= ~RAIN_GAUGE;

                    setSwitchSensorCmd(RainStation.ALARM_OPEN);
                } else {
                    rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                    rgBreakAlarmItems.setVisibility(View.GONE);
                }
            }
            btnConfirm.setVisibility((mExistingUpdateTypes & (RAIN_GAUGE | DIGITAL_OSMOMETER)) != 0 ? View.VISIBLE : View.GONE);
        }
    };

    private RadioGroup.OnCheckedChangeListener breakAlarmOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            //断线报警器常开
            if (checkedId == R.id.radio_break_alarm_open) {
                breakAlarmStatus = BreakAlarmStatus.OPEN;
                queryOrSetBreakAlarmCmd();

            } else if (checkedId == R.id.radio_break_alarm_close) {
                breakAlarmStatus = BreakAlarmStatus.CLOSE;
                queryOrSetBreakAlarmCmd();
            }
        }
    };

    /**
     * 数字水位计启用开关事件
     */
    private void setSwitchViewListener() {
        //数字水位计启用开关事件
        mSbDigitalOsmometerEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_OPEN);
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
                    mExistingUpdateTypes |= DIGITAL_OSMOMETER;
                } else {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_CLOSE);
                    digitalOsmometerChildsLayout.setVisibility(View.GONE);
                    mExistingUpdateTypes &= ~DIGITAL_OSMOMETER;

                    //恢复刚进入页面时的水位计原始数据
                    mEtOsmometerAddress.setText(osmometerAddress);
                    mEtWaterAlarmValue.setText(depthTriggerValue);
                    mEtWaterRevised.setText(depthCorrection);
                    mEtOsmometerCord.setText(osmometerLength);
                    mEtNozzelHeight.setText(nozzelHeight);
                }
                btnConfirm.setVisibility((mExistingUpdateTypes & (RAIN_GAUGE | DIGITAL_OSMOMETER)) != 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                queryData();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_10000_MILLIS);
            }
        });
    }

    private void queryData() {
        commandItems.clear();

        String command = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        Timber.d("获取基础配置信息指令===%s", command);
        commandItems.add(command);

        command = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER);
        Timber.d("查询数字水位计配置信息===%s", command);
        commandItems.add(command);

        sendCommandFromCmdList(null);
    }

    /**
     * 设置开关量传感器启用情况
     */
    private void setSwitchSensorCmd(RainStation rainStation) {
        RainStationEntity entity = new RainStationEntity(rainStation.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.RAIN_STATION, entity);
        sendCommand(command);
        Timber.d("设置开关量指令==%s", command);
    }

    /**
     * 查询或设置断线报警器
     */
    private void queryOrSetBreakAlarmCmd() {
        BreakAlarmStatusEntity entity = new BreakAlarmStatusEntity(breakAlarmStatus.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.BREAK_ALARM_STATUS, entity);
        sendCommand(command);
        if (breakAlarmStatus == BreakAlarmStatus.QUERY) {
            Timber.d("查询断线报警器指令==%s", command);
        } else {
            Timber.d("设置断线报警器指令==%s", command);
        }
    }

    /**
     * 设置数字水位计启用状态 ##401
     */
    private void enableOrDisableDigitalOsmometerCmd(OsmometerStatus status) {
        DigitalOsmometerFunctionEntity entity = new DigitalOsmometerFunctionEntity(status.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.DIGITAL_OSMOMETER_FUNCTION, entity);
        sendCommand(command);
        Timber.d("设置数字水位计指令==%s", command);
    }

    @OnClick({R.id.tvPrecision, R.id.extendSensorLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
            return;
        }
        int id = view.getId();
        if (id == R.id.tvPrecision) {
            String[] values = StringUtils.getStringArray(R.array.rain_value);
            int pos = Arrays.asList(values).indexOf(rainPrecision);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("雨量精度");
            builder.setSingleChoiceItems(values, pos, (dialog, item1) -> {
                rainPrecision = values[item1];
                mTvPrecision.setText(values[item1]);
                dialog.dismiss();
            });
            builder.create().show();
        } else if (id == R.id.extendSensorLayout) {
            DasExternalSensorListActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, collectorModel);

        } else if (id == R.id.btn_confirm) {
            try {
                com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
                commandItems.clear();
                if (!checkRainPrecisionParam() || !checkDigitalOsmometerParam()) {
                    return;
                }
                startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 处理雨量计精度参数
     *
     * @return 校验通过返回 true,否则返回 false
     */
    private boolean checkRainPrecisionParam() {
        if (!rbRainGauge.isChecked()) {
            return true;
        }
        int precision = (int) (Double.parseDouble(rainPrecision) * 100);
        SetRainPrecisionEntity entity = new SetRainPrecisionEntity(precision);
        String command = CommandManager.getInstance().getCommand(CommandType.SETTING_RAIN_PRECISION, entity);
        commandItems.add(command);
        return true;
    }

    /**
     * 处理数字水位计参数
     *
     * @return 校验通过返回 true,否则返回 false
     */
    private boolean checkDigitalOsmometerParam() {
        if (!mSbDigitalOsmometerEnable.isChecked()) {
            return true;
        }
        osmometerAddress = mEtOsmometerAddress.getText().toString().trim();
        depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();
        depthCorrection = mEtWaterRevised.getText().toString().trim();
        osmometerLength = mEtOsmometerCord.getText().toString().trim();
        nozzelHeight = mEtNozzelHeight.getText().toString().trim();

        if (TextUtils.isEmpty(osmometerAddress)) {
            ToastUtils.show("请输入水位计地址");
            mEtOsmometerAddress.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(osmometerAddress);
            if (value < 0 || value > 255) {
                ToastUtils.show("请输入正确的水位计地址!");
                mEtOsmometerAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的水位计地址!");
            mEtOsmometerAddress.requestFocus();
            return false;
        }
        SetOsmometerAddressEntity addressEntity = new SetOsmometerAddressEntity(Integer.parseInt(osmometerAddress));
        String command = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETER_ADDRESS, addressEntity);
        commandItems.add(command);

        if (!decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getDepthTrigger())).equals(depthTriggerValue)) {
            if (TextUtils.isEmpty(depthTriggerValue)) {
                ToastUtils.show("请输入水位报警值");
                return false;
            }
            try {
                int value = Integer.parseInt(depthTriggerValue);
                if (value < 1 || value > 65535) {
                    ToastUtils.show("请输入正确的水位报警值!");
                    mEtWaterAlarmValue.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水位报警值!");
                mEtWaterAlarmValue.requestFocus();
                return false;
            }
            SetOsmometerTriggerEntity triggerEntity = new SetOsmometerTriggerEntity(depthTriggerValue, "10");
            command = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETER_TRIGGER, triggerEntity);
            commandItems.add(command);
        }

        if (!decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getDepthCorrect())).equals(depthCorrection)) {
            if (TextUtils.isEmpty(depthCorrection)) {
                ToastUtils.show("请输入水深修正值");
                return false;
            }
            try {
                double value = Double.parseDouble(depthCorrection);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水深修正值!");
                mEtWaterRevised.requestFocus();
                return false;
            }
            SetOsmometerCorrectEntity correctEntity = new SetOsmometerCorrectEntity(depthCorrection, "10");
            command = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETR_CORRECT, correctEntity);
            commandItems.add(command);
        }

        if (!decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getCordLenght())).equals(osmometerLength)) {
            if (TextUtils.isEmpty(osmometerLength)) {
                ToastUtils.show("请输入水位计绳长");
                return false;
            }
            try {
                double value = Double.parseDouble(osmometerLength);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水位计绳长!");
                mEtOsmometerCord.requestFocus();
                return false;
            }
            SetOsmometerCordLengthEntity cordLengthEntity = new SetOsmometerCordLengthEntity(osmometerLength);
            command = CommandManager.getInstance().getCommand(CommandType.SET_CORD_LENGTH, cordLengthEntity);
            commandItems.add(command);
        }

        if (!decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getInstallHeight())).equals(nozzelHeight)) {
            if (TextUtils.isEmpty(nozzelHeight)) {
                ToastUtils.show("请输入安装高程值");
                return false;
            }
            try {
                double value = Double.parseDouble(nozzelHeight);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的安装高程值!");
                mEtNozzelHeight.requestFocus();
                return false;
            }
            SetOsmometerNozzelHeightEntity nozzelHeightEntity = new SetOsmometerNozzelHeightEntity(nozzelHeight);
            command = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETR_NOZZEL_HEIGHT, nozzelHeightEntity);
            commandItems.add(command);
        }
        return true;
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        // TODO #gh# 屏蔽从其他页面返回到当前页面时，接收到其他页面的最后接收到的指令数据(LiveData事件)
        if (!isResumed()) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case BASE_CONFIG://基础配置信息 000
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    Timber.e("查询基础配置信息指令出错!");
                    return;
                }
                baseConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                initBaseConfigInfo();
                sendCommandFromCmdList(() -> {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                });
                break;

            case BREAK_ALARM_STATUS: //查询或设置断线报警器状态 227
                if (mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.finishRefresh(true);
                }
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    if (breakAlarmStatus == BreakAlarmStatus.QUERY) {
                        Timber.e("查询断线报警器状态指令出错!");
                    } else {
                        ToastUtils.show("断线报警器配置错误!");
                    }
                    return;
                }
                //查询断线报警器状态
                if (breakAlarmStatus == BreakAlarmStatus.QUERY) {
                    breakAlarmStatusInfo = ResultParserUtil.getEntityObject(cmdStr);
                    initBreakAlarmStatus();
                }
                break;

            case QUERY_OSMOMETER_PARAMETER://查询数字水位计参数 400
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询数字水位计参数指令出错!");
                    return;
                }
                sendCommandFromCmdList(() -> {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                });
                queryOsmometerParameterInfo = ResultParserUtil.getEntityObject(cmdStr);
                initDigitalOsmometerInfo();
                break;

            case RAIN_STATION://开关量传感器   0051：雨量计开启  0052：关闭  0053：断线报警器开启
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (tempStr.contains("0051")) {
                        ToastUtils.show("启用雨量计错误!");
                    } else if (tempStr.contains("0052")) {
                        ToastUtils.show("关闭开关量传感器错误!");
                    } else if (tempStr.contains("0053")) {
                        ToastUtils.show("启用断线报警器错误!");
                    }
                    return;
                }
                if (tempStr.contains("0053")) {//开启断线报警器的话，查询断线报警器的状态
                    breakAlarmStatus = BreakAlarmStatus.QUERY;
                    queryOrSetBreakAlarmCmd();
                }
                break;

            case SETTING_RAIN_PRECISION://设置雨量计精度 121
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("雨量计精度配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case DIGITAL_OSMOMETER_FUNCTION://开启/关闭数字水位计功能 401
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (tempStr.contains("4011")) {
                        ToastUtils.show("开启数字水位计错误!");
                    } else if (tempStr.contains("4012")) {
                        ToastUtils.show("关闭数字水位计错误!");
                    }
                    return;
                }
                break;

            case SET_OSMOMETER_ADDRESS://设置数字水位计地址 402
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字水位计地址配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SET_OSMOMETER_TRIGGER://设置数字水位计水位报警值 403
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字水位计水位报警值配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SET_OSMOMETR_CORRECT://设置数字水位计水深修正值 404
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字水位计水深修正值配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SET_CORD_LENGTH://设置数字水位计绳长 405
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字水位计绳长配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SET_OSMOMETR_NOZZEL_HEIGHT://设置数字水位计安装高程 406
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    ToastUtils.show("数字水位计安装高程配置错误!");
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SAVE_CONFIG_INFO://保存配置信息
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                ToastUtils.show("保存成功!");
                break;

            default:
                break;
        }
    }

    /**
     * 处理基础配置信息
     */
    private void initBaseConfigInfo() {
        if (baseConfigInfo == null) {
            Timber.e("基础配置信息为空!");
            return;
        }
        collectorModel = baseConfigInfo.getCollectorModel().getCode();
        switch (baseConfigInfo.getRainStation()) {
            case CLOSE:
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbCloseSwitchSensor.setOnCheckedChangeListener(null);
                rbCloseSwitchSensor.setChecked(true);
                rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rainPrecisionLayout.setVisibility(View.GONE);
                rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbCloseSwitchSensor.setOnCheckedChangeListener(onCheckedChangeListener);
                break;

            case OPEN://雨量站开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbRainGauge.setOnCheckedChangeListener(null);
                rbRainGauge.setChecked(true);
                rainPrecisionLayout.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbRainGauge.setOnCheckedChangeListener(onCheckedChangeListener);
                mExistingUpdateTypes |= RAIN_GAUGE;
                btnConfirm.setVisibility(View.VISIBLE);
                try {
                    decimalFormat.applyPattern("#.#");
                    rainPrecision = decimalFormat.format((double) baseConfigInfo.getRainAccuracy() / 10000);
                    mTvPrecision.setText(rainPrecision);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;

            case ALARM_OPEN://断线报警器开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbBreakAlarm.setOnCheckedChangeListener(null);
                rbBreakAlarm.setChecked(true);
                rgBreakAlarmItems.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rainPrecisionLayout.setVisibility(View.GONE);
                rbBreakAlarm.setOnCheckedChangeListener(onCheckedChangeListener);
                //查询断线报警器状态
                breakAlarmStatus = BreakAlarmStatus.QUERY;
                BreakAlarmStatusEntity entity = new BreakAlarmStatusEntity(breakAlarmStatus.toInt());
                String command = CommandManager.getInstance().getCommand(CommandType.BREAK_ALARM_STATUS, entity);
                commandItems.add(command);
                break;
        }
    }

    /**
     * 初始化断线报警器状态
     */
    private void initBreakAlarmStatus() {
        if (breakAlarmStatusInfo == null) {
            Timber.e("断线报警器状态为空!");
            return;
        }
        rgBreakAlarmItems.setOnCheckedChangeListener(null);
        switch (breakAlarmStatusInfo.getStatus()) {
            case OPEN:
                rbBreakAlarmOpen.setChecked(true);
                break;
            case CLOSE:
                rbBreakAlarmClose.setChecked(true);
                break;
        }
        rgBreakAlarmItems.setOnCheckedChangeListener(breakAlarmOnCheckedChangeListener);
    }

    /**
     * 初始化数字水位计参数
     */
    private void initDigitalOsmometerInfo() {
        if (queryOsmometerParameterInfo == null) {
            Timber.e("数字水位计信息为空!");
            queryOsmometerParameterInfo = new QueryOsmometerParameterInfo();
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            return;
        }
        if (queryOsmometerParameterInfo.getOsmometerStatus() == OsmometerStatus.OSMOMETER_CLOSE) {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);

        } else {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(true);
            digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
            mExistingUpdateTypes |= DIGITAL_OSMOMETER;
            btnConfirm.setVisibility(View.VISIBLE);
        }
        try {
            osmometerAddress = queryOsmometerParameterInfo.getOsmometerAddress();
            decimalFormat.applyPattern("#");
            depthTriggerValue = decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getDepthTrigger()));
            decimalFormat.applyPattern("#.###");
            depthCorrection = decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getDepthCorrect()));
            osmometerLength = decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getCordLenght()));
            nozzelHeight = decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getInstallHeight()));

            mEtOsmometerAddress.setText(osmometerAddress);
            mEtWaterAlarmValue.setText(depthTriggerValue);
            mEtWaterRevised.setText(depthCorrection);
            mEtOsmometerCord.setText(osmometerLength);
            mEtNozzelHeight.setText(nozzelHeight);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        if (!mSbDigitalOsmometerEnable.isChecked()) {
            return false;
        }
        if (osmometerAddress != null && !osmometerAddress.equals(mEtOsmometerAddress.getText().toString().trim())) {
            return true;
        }
        if (depthTriggerValue != null && !depthTriggerValue.equals(mEtWaterAlarmValue.getText().toString().trim())) {
            return true;
        }
        if (depthCorrection != null && !depthCorrection.equals(mEtWaterRevised.getText().toString().trim())) {
            return true;
        }
        if (osmometerLength != null && !osmometerLength.equals(mEtOsmometerCord.getText().toString().trim())) {
            return true;
        }
        if (nozzelHeight != null && !nozzelHeight.equals(mEtNozzelHeight.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
