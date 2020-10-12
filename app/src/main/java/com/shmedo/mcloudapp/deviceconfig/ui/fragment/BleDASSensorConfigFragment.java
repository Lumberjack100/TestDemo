package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.BreakAlarmStatusEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorConfigEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorSensorParamsEntity;
import com.shmedo.configlibrary.ble.cmd.entity.DigitalOsmometerFunctionEntity;
import com.shmedo.configlibrary.ble.cmd.entity.RainStationEntity;
import com.shmedo.configlibrary.ble.enums.BreakAlarmStatus;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.OsmometerStatus;
import com.shmedo.configlibrary.ble.enums.RainStation;
import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.BreakAlarmStatusInfo;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.ble.model.QueryOsmometerParameterInfo;
import com.shmedo.configlibrary.ble.model.SetRainPrecisionInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterServerConfigActivity;
import com.shmedo.mcloudapp.projects.adapter.DASSensorAdapter;
import com.shmedo.mcloudapp.projects.model.DASSensorItem;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * DAS 传感器配置页面
 */
public class BleDASSensorConfigFragment extends BaseBleConnectFragment {
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

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    private String collectorModel = "";//采集器类型
    private BaseConfigInfo baseConfigInfo;
    private SetRainPrecisionInfo setRainPrecisionInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;//数字式渗压计参数

    private String rainPrecision;
    private String osmometerAddress;
    private String depthTriggerValue;
    private String depthCorrection;
    private String osmometerLength;
    private String nozzelHeight;

    private String cmdOsmometerAddress;//渗压计地址
    private String cmdDepthTriggerValue;//深度触发值-水位报警值
    private String cmdDepthCorrection;//深度修正值
    private String cmdOsmometerLength;//渗压计绳长
    private String cmdNozzelHeight;//管口高程

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_d_a_s_sensor_config;
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
        mEtRainPrecision.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
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
                } else {
                    setSwitchSensorCmd(RainStation.ALARM_OPEN);
                    switchSensorChildsLayout.setVisibility(View.VISIBLE);
                    rbBreakAlarm.setChecked(true);
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    mEtRainPrecision.setEnabled(false);
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
                } else {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_OPEN);
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
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
                DataCenterServerConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_ONE);
                break;

            case R.id.btn_confirm:
                KeyBordUtils.hideSoftKeyboard(view);

                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
//                if (!checkValue()) {
//                    Timber.w("参数存在错误!");
//                    return;
//                }

                processSave();
                break;
        }
    }

//    private boolean checkValue() {
//        rainPrecision = mEtRainPrecision.getText().toString().trim();
//    }


    private void processSave() {

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
            case BASE_CONFIG://基础配置信息
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

            case QUERY_OSMOMETER_PARAMETER://查询数字式渗压计参数
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
        setRainPrecisionInfo = new SetRainPrecisionInfo();
        setRainPrecisionInfo.setPrecision((double) baseConfigInfo.getRainAccuracy() / 100);
        collectorModel = baseConfigInfo.getCollectorModel().toString();
        mEtRainPrecision.setText(String.valueOf((double) baseConfigInfo.getRainAccuracy() / 100));
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

}
