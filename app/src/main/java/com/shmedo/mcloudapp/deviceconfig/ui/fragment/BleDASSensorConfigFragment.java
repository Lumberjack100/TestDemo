package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.BreakAlarmStatusEntity;
import com.shmedo.configlibrary.ble.cmd.entity.RainStationEntity;
import com.shmedo.configlibrary.ble.enums.BreakAlarmStatus;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.OsmometerStatus;
import com.shmedo.configlibrary.ble.enums.RainStation;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.BreakAlarmStatusInfo;
import com.shmedo.configlibrary.ble.model.QueryOsmometerParameterInfo;
import com.shmedo.configlibrary.ble.model.SetRainPrecisionInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * DAS 传感器配置页面
 */
public class BleDASSensorConfigFragment extends BaseBleConnectFragment {
    private static final int SWITCH_ENABLE = 0x0001;
    private static final int DIGITAL_PIEZOMETER_ENABLE = 0x0002;
    private static final int EXTEND_SENSOR_ENABLE = 0x0003;

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

    @BindView(R.id.extendedSensorEnableSBtn)
    SwitchButton mSbExtendedSensorEnable;

    private BaseConfigInfo baseConfigInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;

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
                    showCloseSwitchButtonDialog("确定要关闭开关量传感器？", SWITCH_ENABLE);
                } else {
                    switchSensorChildsLayout.setVisibility(View.VISIBLE);
                    rbRainGauge.setChecked(true);
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    rgBreakAlarmItems.setVisibility(View.GONE);
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
                    showCloseSwitchButtonDialog("确定要关闭数字渗压计？", SWITCH_ENABLE);
                } else {
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);

                }
            }
        });

        mSbExtendedSensorEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbExtendedSensorEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭开关量传感器？", SWITCH_ENABLE);
                } else {
                    switchSensorChildsLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

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
     * 设置断线报警器状态
     */
    private void queryOrSetBreakAlarm(BreakAlarmStatus breakAlarmStatus) {
        BreakAlarmStatusEntity entity = new BreakAlarmStatusEntity(breakAlarmStatus.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.BREAK_ALARM_STATUS, entity);
        sendCommonCommandImmediately(command);
        Timber.d("设置断线报警器指令==%s", command);
    }

    /**
     * 查询 数字式渗压计配置信息  ##400
     */
    private void queryDigitalOsmometerInfo() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER, null);
        sendCommonCommandImmediately(command);
        Timber.d("查询数字式渗压计配置信息===%s", command);
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content, final int index) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (index) {
                            case SWITCH_ENABLE:
                                switchSensorChildsLayout.setVisibility(View.GONE);
                                RainStationEntity entity = new RainStationEntity(RainStation.CLOSE.toInt());
                                String command = CommandManager.getInstance().getCommand(CommandType.RAIN_STATION, entity);
                                sendCommonCommandImmediately(command);
                                Timber.d("设置开关量指令==%s", command);
                                break;

                            case DIGITAL_PIEZOMETER_ENABLE:

                                break;

                            case EXTEND_SENSOR_ENABLE:

                                break;
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (index) {
                            case SWITCH_ENABLE:
                                mSbSwitchSensor.setCheckedImmediatelyNoEvent(true);
                                break;

                            case DIGITAL_PIEZOMETER_ENABLE:
                                mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(true);
                                break;

                            case EXTEND_SENSOR_ENABLE:
                                mSbExtendedSensorEnable.setCheckedImmediatelyNoEvent(true);
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
//            sendCollector();
        }
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
                queryDigitalOsmometerInfo();
                break;

            case BREAK_ALARM_STATUS: //断线报警器状态 227
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    ToastUtils.show("断线报警器配置错误!");
                    return;
                }
                BreakAlarmStatusInfo breakAlarmStatusInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (breakAlarmStatusInfo == null) {
                    Timber.e("断线报警器状态为空!");
                    return;
                }
                Timber.d("断线报警器状态: %s", breakAlarmStatusInfo.toString());
                switch (breakAlarmStatusInfo.getStatus()) {
                    case OPEN:
                        rbBreakAlarmOpen.setChecked(true);
                        break;
                    case CLOSE:
                        rbBreakAlarmClose.setChecked(true);
                        break;
                }
                break;

            case QUERY_OSMOMETER_PARAMETER://查询数字式渗压计参数 400
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    Timber.e("查询数字式渗压计参数指令出错!");
                    return;
                }
                stopProgressRunnable();
                queryOsmometerParameterInfo = ResultParserUtil.getEntityObject(cmdStr);
                initDigitalOsmometerInfo();
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
                rbBreakAlarm.setChecked(false);
                rgBreakAlarmItems.setVisibility(View.GONE);
                break;

            case ALARM_OPEN://断线报警器开启
                mSbSwitchSensor.setCheckedImmediatelyNoEvent(true);
                switchSensorChildsLayout.setVisibility(View.VISIBLE);
                rbRainGauge.setChecked(false);
                rbBreakAlarm.setChecked(true);
                rgBreakAlarmItems.setVisibility(View.VISIBLE);

                //查询断线报警器状态
                queryOrSetBreakAlarm(BreakAlarmStatus.QUERY);
                break;
        }
    }

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
