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
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor.DasExternalSensorListActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BaseBleCommunicateFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wandersnail.widget.textview.RoundTextView;
import timber.log.Timber;

/**
 * DAS 传感器配置页面
 */
public class BleDasSensorConfigFragment extends BaseBleCommunicateFragment {
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
    Button btnConfirm;

    private DecimalFormat decimalFormat = new DecimalFormat();

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

    private boolean isRefresh = true;//是否第一次进入页面
    private BreakAlarmStatus breakAlarmStatus = BreakAlarmStatus.QUERY;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_das_sensor;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                    rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    if (!mSbDigitalOsmometerEnable.isChecked()) {
                        btnConfirm.setVisibility(View.GONE);
                    }
                    //发送关闭传感器指令
                    setSwitchSensorCmd(RainStation.CLOSE);
                } else {
                    rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                }
            } else if (id == R.id.radio_rain_gauge) {//雨量计单选按钮
                if (isChecked) {
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rainPrecisionLayout.setVisibility(View.VISIBLE);
                    rbCloseSwitchSensor.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    btnConfirm.setVisibility(View.VISIBLE);
                    setSwitchSensorCmd(RainStation.OPEN);
                } else {
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    rainPrecisionLayout.setVisibility(View.GONE);
                }
            } else if (id == R.id.radio_break_alarm) {  //断线报警器单选按钮
                if (isChecked) {
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rgBreakAlarmItems.setVisibility(View.VISIBLE);
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setChecked(false);
                    if (!mSbDigitalOsmometerEnable.isChecked()) {
                        btnConfirm.setVisibility(View.GONE);
                    }
                    setSwitchSensorCmd(RainStation.ALARM_OPEN);
                } else {
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    rgBreakAlarmItems.setVisibility(View.GONE);
                }
            }
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
     * 数字式渗压计启用开关事件
     */
    private void setSwitchViewListener() {
        //数字式渗压计启用开关事件
        mSbDigitalOsmometerEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_OPEN);
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
                    btnConfirm.setVisibility(View.VISIBLE);
                } else {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_CLOSE);
                    digitalOsmometerChildsLayout.setVisibility(View.GONE);

                    //恢复刚进入页面时的渗压计原始数据
                    mEtOsmometerAddress.setText(osmometerAddress);
                    mEtWaterAlarmValue.setText(depthTriggerValue);
                    mEtWaterRevised.setText(depthCorrection);
                    mEtOsmometerCord.setText(osmometerLength);
                    mEtNozzelHeight.setText(nozzelHeight);

                    //判断是否禁用保存按钮
                    if (!rbRainGauge.isChecked()) {
                        btnConfirm.setVisibility(View.GONE);
                    }
                }
            }
        });
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
                isRefresh = true;
                querySwitchSensorInfo();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_10000_MILLIS);
            }
        });
    }

    /**
     * 查询开关量传感器信息
     */
    private void querySwitchSensorInfo() {
        String command = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommand(command);
        Timber.d("获取基础配置信息指令===%s", command);
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
     * 查询 数字式渗压计配置信息  ##400
     */
    private void queryDigitalOsmometerCmd() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER, null);
        sendCommand(command);
        Timber.d("查询数字式渗压计配置信息===%s", command);
    }

    /**
     * 设置数字式渗压计启用状态 ##401
     */
    private void enableOrDisableDigitalOsmometerCmd(OsmometerStatus status) {
        DigitalOsmometerFunctionEntity entity = new DigitalOsmometerFunctionEntity(status.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.DIGITAL_OSMOMETER_FUNCTION, entity);
        sendCommand(command);
        Timber.d("设置数字式渗压计指令==%s", command);
    }

    @OnClick({R.id.tvPrecision, R.id.extendSensorLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }
        int id = view.getId();
        if (id == R.id.tvPrecision) {
            String[] values = getResources().getStringArray(R.array.rain_value);
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
            KeyBordUtils.hideSoftKeyboard(view);
            cmdRainPrecision = "";
            cmdOsmometerAddress = "";
            cmdDepthTriggerValue = "";
            cmdDepthCorrection = "";
            cmdOsmometerLength = "";
            cmdNozzelHeight = "";
            if (!checkRainPrecisionParam() || !checkDigitalOsmometerParam()) {
                return;
            }
            processSave();
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
        cmdRainPrecision = CommandManager.getInstance().getCommand(CommandType.SETTING_RAIN_PRECISION, entity);
        return true;
    }

    /**
     * 处理数字式渗压计参数
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
            ToastUtils.show("请输入渗压计地址");
            mEtOsmometerAddress.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(osmometerAddress);
            if (value < 0 || value > 255) {
                ToastUtils.show("请输入正确的渗压计地址!");
                mEtOsmometerAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的渗压计地址!");
            mEtOsmometerAddress.requestFocus();
            return false;
        }
        SetOsmometerAddressEntity addressEntity = new SetOsmometerAddressEntity(Integer.parseInt(osmometerAddress));
        cmdOsmometerAddress = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETER_ADDRESS, addressEntity);

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
            cmdDepthTriggerValue = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETER_TRIGGER, triggerEntity);
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
            cmdDepthCorrection = CommandManager.getInstance().getCommand(CommandType.SET_OSMOMETR_CORRECT, correctEntity);
        }

        if (!decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getCordLenght())).equals(osmometerLength)) {
            if (TextUtils.isEmpty(osmometerLength)) {
                ToastUtils.show("请输入渗压计绳长");
                return false;
            }
            try {
                double value = Double.parseDouble(osmometerLength);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的渗压计绳长!");
                mEtOsmometerCord.requestFocus();
                return false;
            }
            SetOsmometerCordLengthEntity cordLengthEntity = new SetOsmometerCordLengthEntity(osmometerLength);
            cmdOsmometerLength = CommandManager.getInstance().getCommand(CommandType.SET_CORD_LENGTH, cordLengthEntity);
        }

        if (!decimalFormat.format(Double.parseDouble(queryOsmometerParameterInfo.getInstallHeight())).equals(nozzelHeight)) {
            if (TextUtils.isEmpty(nozzelHeight)) {
                ToastUtils.show("请输入管口高程值");
                return false;
            }
            try {
                double value = Double.parseDouble(nozzelHeight);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的管口高程值!");
                mEtNozzelHeight.requestFocus();
                return false;
            }
            SetOsmometerNozzelHeightEntity nozzelHeightEntity = new SetOsmometerNozzelHeightEntity(nozzelHeight);
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
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
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
        sendCommand(command);
        Timber.d("设置雨量计或渗压计配置参数指令===%s", command);
        //移除已发送的指令
        cmdList.remove(0);
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
                break;

            case BREAK_ALARM_STATUS: //查询或设置断线报警器状态 227
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
                    //第一次进入页面，查询数字式渗压计
                    if (isRefresh) {
                        isRefresh = false;
                        queryDigitalOsmometerCmd();
                    }
                }
                break;

            case QUERY_OSMOMETER_PARAMETER://查询数字式渗压计参数 400
                mRefreshLayout.finishRefresh(true);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询数字式渗压计参数指令出错!");
                    return;
                }
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
                if (!cmdList.isEmpty()) {
                    //保存配置信息指令
                    sendParamConfigCmd();
                } else {
                    doAfterSetting();
                }
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

            case SET_OSMOMETER_ADDRESS://设置数字渗压计地址 402
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计地址配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                if (!cmdList.isEmpty()) {
                    //保存配置信息指令
                    sendParamConfigCmd();
                } else {
                    doAfterSetting();
                }
                break;

            case SET_OSMOMETER_TRIGGER://设置数字渗压计水位报警值 403
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计水位报警值配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                if (!cmdList.isEmpty()) {
                    //保存配置信息指令
                    sendParamConfigCmd();
                } else {
                    doAfterSetting();
                }
                break;

            case SET_OSMOMETR_CORRECT://设置数字渗压计水深修正值 404
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计水深修正值配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                if (!cmdList.isEmpty()) {
                    //保存配置信息指令
                    sendParamConfigCmd();
                } else {
                    doAfterSetting();
                }
                break;

            case SET_CORD_LENGTH://设置数字渗压计绳长 405
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数字渗压计绳长配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                if (!cmdList.isEmpty()) {
                    //保存配置信息指令
                    sendParamConfigCmd();
                } else {
                    doAfterSetting();
                }
                break;

            case SET_OSMOMETR_NOZZEL_HEIGHT://设置数字渗压计安装高程 406
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    ToastUtils.show("数字渗压计安装高程配置错误!");
                    return;
                }
                if (!cmdList.isEmpty()) {
                    //保存配置信息指令
                    sendParamConfigCmd();
                } else {
                    doAfterSetting();
                }
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
        collectorModel = baseConfigInfo.getCollectorModel().toString();
        switch (baseConfigInfo.getRainStation()) {
            case CLOSE:
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbCloseSwitchSensor.setOnCheckedChangeListener(null);
                rbCloseSwitchSensor.setChecked(true);
                rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rainPrecisionLayout.setVisibility(View.GONE);
                rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbCloseSwitchSensor.setOnCheckedChangeListener(onCheckedChangeListener);
                //查询数字式渗压计
                queryDigitalOsmometerCmd();
                break;

            case OPEN://雨量站开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbRainGauge.setOnCheckedChangeListener(null);
                rbRainGauge.setChecked(true);
                rainPrecisionLayout.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbRainGauge.setOnCheckedChangeListener(onCheckedChangeListener);
                btnConfirm.setVisibility(View.VISIBLE);
                try {
                    decimalFormat.applyPattern("#.#");
                    rainPrecision = decimalFormat.format((double) baseConfigInfo.getRainAccuracy() / 10000);
                    mTvPrecision.setText(rainPrecision);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //查询数字式渗压计
                queryDigitalOsmometerCmd();
                break;

            case ALARM_OPEN://断线报警器开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbBreakAlarm.setOnCheckedChangeListener(null);
                rbBreakAlarm.setChecked(true);
                rgBreakAlarmItems.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rainPrecisionLayout.setVisibility(View.GONE);
                rbBreakAlarm.setOnCheckedChangeListener(onCheckedChangeListener);
                //查询断线报警器状态
                breakAlarmStatus = BreakAlarmStatus.QUERY;
                queryOrSetBreakAlarmCmd();
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
     * 初始化数字渗压计参数
     */
    private void initDigitalOsmometerInfo() {
        if (queryOsmometerParameterInfo == null) {
            Timber.e("数字渗压计信息为空!");
            queryOsmometerParameterInfo = new QueryOsmometerParameterInfo();
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            if (!rbRainGauge.isChecked()) {
                btnConfirm.setVisibility(View.GONE);
            }
            return;
        }
        if (queryOsmometerParameterInfo.getOsmometerStatus() == OsmometerStatus.OSMOMETER_CLOSE) {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            if (!rbRainGauge.isChecked()) {
                btnConfirm.setVisibility(View.GONE);
            }
        } else {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(true);
            digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
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

    private void doAfterSetting() {
        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
//        isExitMode = true;
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
