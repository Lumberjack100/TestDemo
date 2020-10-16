package com.shmedo.mcloudapp.deviceconfig.ui.fragment.sensor;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
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
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DASExternalSensorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseBleConnectFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * DAS 传感器配置页面
 */
public class BleDASSensorFragment extends BaseBleConnectFragment {
    @BindView(R.id.switchSensorEnableSBtn)
    SwitchButton mSbSwitchSensor;//开关量传感器

    @BindView(R.id.switchSensorChildsLayout)
    ViewGroup switchSensorChildsLayout;//开关量配置项

    @BindView(R.id.radio_rain_gauge)
    RadioButton rbRainGauge;

    @BindView(R.id.rainPrecisionEt)
    EditText mEtRainPrecision;//雨量计精度

    @BindView(R.id.radio_break_alarm)
    RadioButton rbBreakAlarm;

    @BindView(R.id.rg_break_alarm_items)
    RadioGroup rgBreakAlarmItems;

    @BindView(R.id.radio_break_alarm_open)
    RadioButton rbBreakAlarmOpen;

    @BindView(R.id.radio_break_alarm_close)
    RadioButton rbBreakAlarmClose;

    @BindView(R.id.digitalOsmometerEnableSBtn)
    SwitchButton mSbDigitalOsmometerEnable;

    @BindView(R.id.digitalOsmometerChildsLayout)
    ViewGroup digitalOsmometerChildsLayout;//渗压计配置项

    @BindView(R.id.osmometerAddressEt)
    EditText mEtOsmometerAddress;//渗压计地址

    @BindView(R.id.waterAlarmValueEt)
    EditText mEtWaterAlarmValue;//深度触发值-水位报警值

    @BindView(R.id.waterRevisedEt)
    EditText mEtWaterRevised;//深度修正值

    @BindView(R.id.osmometerCordEt)
    EditText mEtOsmometerCord;//渗压计绳长

    @BindView(R.id.nozzelHeightEt)
    EditText mEtNozzelHeight;//管口高程

    @BindView(R.id.btn_confirm)
    Button btnConfirm;//管口高程

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private String collectorModel = "";//采集器类型
    private BaseConfigInfo baseConfigInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;//数字式渗压计参数

    private String rainPrecision;
    private String osmometerAddress;
    private String depthTriggerValue;
    private String depthCorrection;
    private String osmometerLength;
    private String nozzelHeight;

    private String cmdRainPrecision;// 雨量计精度配置指令
    private String cmdOsmometerAddress;//渗压计地址配置指令
    private String cmdDepthTriggerValue;//水位报警值配置指令
    private String cmdDepthCorrection;//深度修正值配置指令
    private String cmdOsmometerLength;//渗压计绳长配置指令
    private String cmdNozzelHeight;//管口高程配置指令

    //参数配置指令集合
    private List<String> cmdList = new LinkedList<>();


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_d_a_s_sensor;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        setSwitchViewListener();
        setRadioButtonListener();
        querySwitchSensorInfo();
    }

    private void setFilter() {
        mEtRainPrecision.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtOsmometerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtWaterAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtWaterRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
    }

    /**
     * 开关控件事件
     */
    private void setSwitchViewListener() {
        mSbSwitchSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbSwitchSensor.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    setSwitchSensorCmd(RainStation.CLOSE);
                    switchSensorChildsLayout.setVisibility(View.GONE);
                    if (!mSbDigitalOsmometerEnable.isChecked()) {
                        btnConfirm.setEnabled(false);
                    }
                    //恢复刚进入页面时的开关传感器原始数据
                    initSwitchSensor();
                } else {
                    setSwitchSensorCmd(RainStation.ALARM_OPEN);
                    switchSensorChildsLayout.setVisibility(View.VISIBLE);
                    rbBreakAlarm.setChecked(true);
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    mEtRainPrecision.setEnabled(false);
                    btnConfirm.setEnabled(true);
                }
            }
        });

        mSbDigitalOsmometerEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_CLOSE);
                    digitalOsmometerChildsLayout.setVisibility(View.GONE);
                    if (!mSbSwitchSensor.isChecked()) {
                        btnConfirm.setEnabled(false);
                    }

                    //恢复刚进入页面时的渗压计原始数据
                    initDigitalOsmometerInfo();
                } else {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_OPEN);
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
                    btnConfirm.setEnabled(true);
                }
            }
        });
    }

    /**
     * 单选框事件
     */
    private void setRadioButtonListener() {
        rbRainGauge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    mEtRainPrecision.setEnabled(true);
                    rbBreakAlarm.setChecked(false);
                } else {
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    mEtRainPrecision.setEnabled(false);
                }
            }
        });

        rbBreakAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rgBreakAlarmItems.setVisibility(View.VISIBLE);
                    rbRainGauge.setChecked(false);
                } else {
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    rgBreakAlarmItems.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 查询开关量传感器信息
     */
    private void querySwitchSensorInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", 20000);
        String command = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommonCommandImmediately(command);
        Timber.d("获取基础配置信息指令===%s", command);
    }

    /**
     * 设置开关量传感器启用情况
     */
    private void setSwitchSensorCmd(RainStation rainStation) {
        RainStationEntity entity = new RainStationEntity(rainStation.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.RAIN_STATION, entity);
        sendCommonCommandImmediately(command);
        Timber.d("设置开关量指令==%s", command);
    }

    /**
     * 查询或设置断线报警器
     */
    private void queryOrSetBreakAlarmCmd(BreakAlarmStatus breakAlarmStatus) {
        BreakAlarmStatusEntity entity = new BreakAlarmStatusEntity(breakAlarmStatus.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.BREAK_ALARM_STATUS, entity);
        sendCommonCommandImmediately(command);
        Timber.d("设置断线报警器指令==%s", command);
    }

    /**
     * 查询 数字式渗压计配置信息  ##400
     */
    private void queryDigitalOsmometerCmd() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER, null);
        sendCommonCommandImmediately(command);
        Timber.d("查询数字式渗压计配置信息===%s", command);
    }

    /**
     * 设置数字式渗压计启用状态 ##401
     */
    private void enableOrDisableDigitalOsmometerCmd(OsmometerStatus status) {
        DigitalOsmometerFunctionEntity entity = new DigitalOsmometerFunctionEntity(status.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.DIGITAL_OSMOMETER_FUNCTION, entity);
        sendCommonCommandImmediately(command);
        Timber.d("设置数字式渗压计指令==%s", command);
    }

    @OnClick({R.id.extendSensorLayout, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.extendSensorLayout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DASExternalSensorActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, collectorModel);
                break;

            case R.id.btn_confirm:
                KeyBordUtils.hideSoftKeyboard(view);

                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

                cmdRainPrecision = "";
                cmdOsmometerAddress = "";
                cmdDepthTriggerValue = "";
                cmdDepthCorrection = "";
                cmdOsmometerLength = "";
                cmdNozzelHeight = "";

                if (!processRainPrecisionParam() || !processDigitalOsmometerParam()) {
                    return;
                }
                processSave();
                break;
        }
    }


    /**
     * 处理雨量计精度参数
     *
     * @return
     */
    private boolean processRainPrecisionParam() {
        rainPrecision = mEtRainPrecision.getText().toString().trim();
        if (!mSbSwitchSensor.isChecked() || !rbRainGauge.isChecked()) {
            return true;
        }

        if (decimalFormat.format((double) baseConfigInfo.getRainAccuracy() / 100).equals(rainPrecision)) {
            return true;
        }

        if (TextUtils.isEmpty(rainPrecision)) {
            ToastUtils.show("雨量计精度不能为空");
            mEtRainPrecision.requestFocus();
            return false;
        }

        try {
            double value = Double.parseDouble(rainPrecision);
            if (value >= 1) {
                ToastUtils.show("请输入有效的雨量计精度!");
                mEtRainPrecision.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入有效的雨量计精度!");
            mEtRainPrecision.requestFocus();
            return false;
        }

        int precision = (int) (Double.parseDouble(rainPrecision) * 100);
        SetRainPrecisionEntity entity = new SetRainPrecisionEntity(precision);
        cmdRainPrecision = CommandManager.getInstance().getCommand(CommandType.SETTING_RAIN_PRECISION, entity);
        return true;
    }

    private boolean processDigitalOsmometerParam() {
        if (!mSbDigitalOsmometerEnable.isChecked()) {
            return true;
        }

        osmometerAddress = mEtOsmometerAddress.getText().toString().trim();
        depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();
        depthCorrection = mEtWaterRevised.getText().toString().trim();
        osmometerLength = mEtOsmometerCord.getText().toString().trim();
        nozzelHeight = mEtNozzelHeight.getText().toString().trim();

        if (!queryOsmometerParameterInfo.getOsmometerAddress().equals(osmometerAddress)) {
            if (TextUtils.isEmpty(osmometerAddress)) {
                ToastUtils.show("渗压计地址不能为空");
                mEtOsmometerAddress.requestFocus();
                return false;
            }
            if (Integer.parseInt(osmometerAddress) < 0) {
                ToastUtils.show("渗压计地址不能小于0");
                mEtOsmometerAddress.requestFocus();
                return false;
            }
            if (Integer.parseInt(osmometerAddress) > 255) {
                ToastUtils.show("渗压计地址不能大于255");
                mEtOsmometerAddress.requestFocus();
                return false;
            }

            SetOsmometerAddressEntity addressEntity = new SetOsmometerAddressEntity(Integer.parseInt(osmometerAddress));
            cmdOsmometerAddress = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETER_ADDRESS, addressEntity);
        }

        if (!queryOsmometerParameterInfo.getDepthTrigger().equals(depthTriggerValue)) {
            if (TextUtils.isEmpty(depthTriggerValue)) {
                ToastUtils.show("水位报警值不能为空");
                return false;
            }

            try {
                int value = Integer.parseInt(depthTriggerValue);
                if (value < 1 || value > 65535) {
                    ToastUtils.show("请输入有效的水位报警值!");
                    mEtWaterAlarmValue.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的水位报警值!");
                mEtWaterAlarmValue.requestFocus();
                return false;
            }

            SetOsmometerTriggerEntity triggerEntity = new SetOsmometerTriggerEntity(Integer.parseInt(depthTriggerValue), 10);
            cmdDepthTriggerValue = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETER_TRIGGER, triggerEntity);
        }

        if (!queryOsmometerParameterInfo.getDepthCorrect().equals(depthCorrection)) {
            if (TextUtils.isEmpty(depthCorrection)) {
                ToastUtils.show("水深修正值不能为空");
                return false;
            }

            try {
                double value = Double.parseDouble(depthCorrection);

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的水深修正值!");
                mEtWaterRevised.requestFocus();
                return false;
            }

            SetOsmometerCorrectEntity correctEntity = new SetOsmometerCorrectEntity(Double.parseDouble(depthCorrection), 10);
            cmdDepthCorrection = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETR_CORRECT, correctEntity);
        }

        if (!queryOsmometerParameterInfo.getCordLenght().equals(osmometerLength)) {
            if (TextUtils.isEmpty(osmometerLength)) {
                ToastUtils.show("渗压计绳长不能为空");
                return false;
            }

            try {
                double value = Double.parseDouble(osmometerLength);

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的渗压计绳长!");
                mEtOsmometerCord.requestFocus();
                return false;
            }

            SetOsmometerCordLengthEntity cordLengthEntity = new SetOsmometerCordLengthEntity(Double.parseDouble(osmometerLength));
            cmdOsmometerLength = CommandManager.getInstance().getCommand(CommandType.SET_CORD_LENGTH, cordLengthEntity);
        }

        if (!queryOsmometerParameterInfo.getInstallHeight().equals(nozzelHeight)) {
            if (TextUtils.isEmpty(nozzelHeight)) {
                ToastUtils.show("管口高程值不能为空");
                return false;
            }

            try {
                double value = Double.parseDouble(nozzelHeight);

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的管口高程值!");
                mEtNozzelHeight.requestFocus();
                return false;
            }

            SetOsmometerNozzelHeightEntity nozzelHeightEntity = new SetOsmometerNozzelHeightEntity(Double.parseDouble(nozzelHeight));
            cmdNozzelHeight = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETR_NOZZEL_HEIGHT, nozzelHeightEntity);
        }

        return true;
    }


    private void processSave() {
        cmdList.clear();
        if (!TextUtils.isEmpty(cmdRainPrecision)) {
            cmdList.add(cmdRainPrecision);
        }
        if (!TextUtils.isEmpty(cmdOsmometerAddress)) {
            cmdList.add(cmdOsmometerAddress);
        }
        if (!TextUtils.isEmpty(cmdDepthTriggerValue)) {
            cmdList.add(cmdDepthTriggerValue);
        }
        if (!TextUtils.isEmpty(cmdDepthCorrection)) {
            cmdList.add(cmdDepthCorrection);
        }
        if (!TextUtils.isEmpty(cmdOsmometerLength)) {
            cmdList.add(cmdOsmometerLength);
        }
        if (!TextUtils.isEmpty(cmdNozzelHeight)) {
            cmdList.add(cmdNozzelHeight);
        }

        if (!cmdList.isEmpty()) {
            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
        }
        sendParamConfigCmd();
    }

    /**
     * 从修正参数指令集合中逐个取出发送
     */
    private void sendParamConfigCmd() {
        if (cmdList.isEmpty()) {
            Timber.d("雨量计或渗压计配置参数指令已发送完毕");
            return;
        }

        String command = cmdList.get(0);
        sendCommonCommandImmediately(command);
        Timber.d("设置雨量计或渗压计配置参数指令===%s", command);
        //移除已发送的指令
        cmdList.remove(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        if (messageEvent instanceof CmdResponseMessage) {
            if (!isActive) {
                return;
            }
            setResultData((CmdResponseMessage) messageEvent);
        } else {
            super.onMessageEvent(messageEvent);
        }
    }

    private void setResultData(CmdResponseMessage responseMessage) {
        String cmdStr = responseMessage.getResult();
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case BASE_CONFIG://基础配置信息 000
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    Timber.e("查询基础配置信息指令出错!");
                    return;
                }
                baseConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                initBaseConfigInfo();
                //查询数字式渗压计
                queryDigitalOsmometerCmd();
                break;

            case QUERY_OSMOMETER_PARAMETER://查询数字式渗压计参数 400
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询数字式渗压计参数指令出错!");
                    return;
                }
                queryOsmometerParameterInfo = ResultParserUtil.getEntityObject(cmdStr);
                initDigitalOsmometerInfo();
                break;

            case RAIN_STATION://开关量传感器 0051：雨量计开启  0052：关闭  0053：断线报警器开启
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
                    queryOrSetBreakAlarmCmd(BreakAlarmStatus.QUERY);
                }
                break;

            case BREAK_ALARM_STATUS: //查询或设置断线报警器状态 227
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    if (tempStr.contains("2270")) {
                        Timber.e("查询断线报警器状态指令出错!");
                    } else {
                        ToastUtils.show("断线报警器配置错误!");
                    }
                    return;
                }
                breakAlarmStatusInfo = ResultParserUtil.getEntityObject(cmdStr);
                initBreakAlarmStatus();
                break;

            case DIGITAL_OSMOMETER_FUNCTION://开启/关闭数字式渗压计功能 401
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (tempStr.contains("4011")) {
                        ToastUtils.show("开启数字式渗压计错误!");
                    } else if (tempStr.contains("4012")) {
                        ToastUtils.show("关闭数字式渗压计错误!");
                    }
                    return;
                }
                break;

            case SETTING_RAIN_PRECISION://设置雨量计精度 121
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("雨量计精度配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (!cmdList.isEmpty()) {
                    sendParamConfigCmd();
                    return;
                } else {
                    stopProgressRunnable();
                    isExitMode = true;
                    warnNotYetRebootToSaveParam();
                }
                break;

            case SET_OSMOMETER_ADDRESS://设置数字渗压计地址 402
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计地址配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (!cmdList.isEmpty()) {
                    sendParamConfigCmd();
                    return;
                } else {
                    stopProgressRunnable();
                    isExitMode = true;
                    warnNotYetRebootToSaveParam();
                }
                break;

            case SET_OSMOMETER_TRIGGER://设置数字渗压计水位报警值 403
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计水位报警值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (!cmdList.isEmpty()) {
                    sendParamConfigCmd();
                    return;
                } else {
                    stopProgressRunnable();
                    isExitMode = true;
                    warnNotYetRebootToSaveParam();
                }
                break;

            case SET_OSMOMETR_CORRECT://设置数字渗压计水深修正值 404
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计水深修正值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (!cmdList.isEmpty()) {
                    sendParamConfigCmd();
                    return;
                } else {
                    stopProgressRunnable();
                    isExitMode = true;
                    warnNotYetRebootToSaveParam();
                }
                break;

            case SET_CORD_LENGTH://设置数字渗压计绳长 405
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计绳长配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (!cmdList.isEmpty()) {
                    sendParamConfigCmd();
                    return;
                } else {
                    stopProgressRunnable();
                    isExitMode = true;
                    warnNotYetRebootToSaveParam();
                }
                break;

            case SET_OSMOMETR_NOZZEL_HEIGHT://设置数字渗压计安装高程 406
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    ToastUtils.show("数字渗压计安装高程配置错误!");
                    return;
                }
                if (!cmdList.isEmpty()) {
                    sendParamConfigCmd();
                    return;
                } else {
                    stopProgressRunnable();
                    isExitMode = true;
                    warnNotYetRebootToSaveParam();
                }
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
        collectorModel = baseConfigInfo.getCollectorModel().toString();
        initSwitchSensor();
    }

    private void initSwitchSensor() {
        try {
            mEtRainPrecision.setText(decimalFormat.format((double) baseConfigInfo.getRainAccuracy() / 100));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //设备雨量站开关量
        switch (baseConfigInfo.getRainStation()) {
            case CLOSE:
                mSbSwitchSensor.setCheckedImmediatelyNoEvent(false);
                switchSensorChildsLayout.setVisibility(View.GONE);
                break;

            case OPEN://雨量站开启
                mSbSwitchSensor.setCheckedImmediatelyNoEvent(true);
                switchSensorChildsLayout.setVisibility(View.VISIBLE);
                rbRainGauge.setChecked(true);
                rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                break;

            case ALARM_OPEN://断线报警器开启
                mSbSwitchSensor.setCheckedImmediatelyNoEvent(true);
                switchSensorChildsLayout.setVisibility(View.VISIBLE);
                rbBreakAlarm.setChecked(true);
                rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                mEtRainPrecision.setEnabled(false);
                //查询断线报警器状态
                queryOrSetBreakAlarmCmd(BreakAlarmStatus.QUERY);
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

        switch (breakAlarmStatusInfo.getStatus()) {
            case OPEN:
                rbBreakAlarmOpen.setChecked(true);
                break;
            case CLOSE:
                rbBreakAlarmClose.setChecked(true);
                break;
        }
    }

    /**
     * 初始化数字渗压计参数
     */
    private void initDigitalOsmometerInfo() {
        if (queryOsmometerParameterInfo == null) {
            Timber.e("数字渗压计信息为空!");
            queryOsmometerParameterInfo = new QueryOsmometerParameterInfo();
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            return;
        }

        if (queryOsmometerParameterInfo.getOsmometerStatus() == OsmometerStatus.OSMOMETER_OPEN) {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(true);
            digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
        } else {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
        }

        mEtOsmometerAddress.setText(queryOsmometerParameterInfo.getOsmometerAddress());
        try {
            DecimalFormat df = new DecimalFormat("#.##");
            mEtWaterAlarmValue.setText(df.format(Double.parseDouble(queryOsmometerParameterInfo.getDepthTrigger())));
            mEtWaterRevised.setText(df.format(Double.parseDouble(queryOsmometerParameterInfo.getDepthCorrect())));
            mEtOsmometerCord.setText(df.format(Double.parseDouble(queryOsmometerParameterInfo.getCordLenght())));
            mEtNozzelHeight.setText(df.format(Double.parseDouble(queryOsmometerParameterInfo.getInstallHeight())));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        if (!mEtRainPrecision.getText().toString().trim().equals(decimalFormat.format((double) baseConfigInfo.getRainAccuracy() / 100))) {
            return true;
        }

        if (!queryOsmometerParameterInfo.getOsmometerAddress().equals(mEtOsmometerAddress.getText().toString().trim())) {
            return true;
        }

        if (!queryOsmometerParameterInfo.getDepthTrigger().equals(mEtWaterAlarmValue.getText().toString().trim())) {
            return true;
        }

        if (!queryOsmometerParameterInfo.getDepthCorrect().equals(mEtWaterRevised.getText().toString().trim())) {
            return true;
        }

        if (!queryOsmometerParameterInfo.getCordLenght().equals(mEtOsmometerCord.getText().toString().trim())) {
            return true;
        }

        if (!queryOsmometerParameterInfo.getInstallHeight().equals(mEtNozzelHeight.getText().toString().trim())) {
            return true;
        }

        return false;
    }

}
