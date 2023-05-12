package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.CommonDigitalSensorInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/15 <br/>
 * 描述：    外接数字式传感器
 *
 * @deprecated 后面将用物联网指令模式取代
 */
public class BleDasExternalDigtalSensorListListFragment extends BaseBleDasExternalSensorListFragment {

    public static BaseBleDasExternalSensorListFragment newInstance(String collectorCode) {
        BaseBleDasExternalSensorListFragment fragment = new BleDasExternalDigtalSensorListListFragment();
        Bundle args = new Bundle();
        args.putString(AppContants.Extras.COLLECTOR_MODE, collectorCode);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 设置采集器接入的传感器<br/>
     * 指令格式: ##150zzxxXXXX\r\n<br/>
     * zz 采集器型号<br/>
     * xx 的取值范围为：01~08，表示接入传感器的个数<br/>
     * 1）当传感器个数为01时XXXX（4个字节）的含义：前两位表示地址或者通道号，后两位表示接入传感器类型<br/>
     * 2）当传感器个数为02时XXXXXXXX（8个字节）的含义：前四位表示第一个地址和对应的传感器类型，后四位表示第二个地址和对应的传感器类型……以此类推。<br/>
     * 该指令不定长，根据接入传感器的个数而定，地址为01~99,通道为00~07<br/>
     */
    @Override
    protected void sendInstruction() {
        collectorSensorParamsInfoSubs.clear();
        collectorSensorParamsInfoSubs.addAll(sensorHashMap.values());
        if (collectorSensorParamsInfoSubs.isEmpty()) {
            Timber.i("%s 采集器接入的传感器信息为空!", collectorName);
            sendCloseCollectorCmd();
            return;
        }
        //##150zzxxXXXX\r\n：设置采集器接入的传感器
        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##150");
        builderFirst.append(defaultSensorParamsInfo.getCollectorModel() + StringUtil.formatStringTwo(String.valueOf(collectorSensorParamsInfoSubs.size())));
        for (CollectorSensorParamsInfo paramsInfoSub : sensorHashMap.values()) {
            builderFirst.append(StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) + StringUtil.formatStringTwo(paramsInfoSub.getSensorType().toString()));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);

        commandItems.clear();
        commandItems.add(command);

        //超声波物位计使用多传感器触发阈值配置指令
        if (SensorType.value(collectorCode) == SensorType.ULTRASONIC_LEVEL_GAUGE) {
            //获取触发值
            command = getMultiTriggerThreshold();
            if (!TextUtils.isEmpty(command)) {
                commandItems.add(command);
            }
            for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
                //获取修正值
                command = getCorrectionValue(paramsInfoSub);
                if (!TextUtils.isEmpty(command)) {
                    commandItems.add(command);
                }
            }
        } else {
            for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
                //获取触发值
                command = getSingleTriggerThreshold(paramsInfoSub);
                if (!TextUtils.isEmpty(command)) {
                    commandItems.add(command);
                }
                //获取修正值
                command = getCorrectionValue(paramsInfoSub);
                if (!TextUtils.isEmpty(command)) {
                    commandItems.add(command);
                }
                //静力水准需要额外设置高程
                if (SensorType.value(collectorCode) == SensorType.STATIC_LEVEL) {
                    command = getElevationValue(paramsInfoSub);
                    if (!TextUtils.isEmpty(command)) {
                        commandItems.add(command);
                    }
                }
            }
            //测斜仪需要额外设置测段长
            if (SensorType.value(collectorCode) == SensorType.INCLINOMETER) {
                command = getMeasureLongValue();
                if (!TextUtils.isEmpty(command)) {
                    commandItems.add(command);
                }
            }
        }
        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_30000_MILLIS);
        Timber.d("设置 %s 接入的传感器指令===%s", collectorName, commandItems.getFirst());
        sendCommandFromCmdList(this::saveConfigInfoNoReboot);
    }

    /**
     * 获取 超声波物位计 接入传感器触发阈值指令<br/>
     * 指令格式: ##162xxX…X\r\n<br/>
     * xx表示采集器类型，X…X表示阀值，X…X由接入传感器数量N决定（4*N）<br/>
     * 例如：裂缝采集器接入两只拉线位移计，报警值分别30mm、40mm<br/>
     * 设置举例：##1620200300040\r\n<br/>
     * 返回信息：$$1620200300040\r\n<br/>
     */
    private String getMultiTriggerThreshold() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("##162");
        stringBuilder.append(defaultSensorParamsInfo.getCollectorModel());
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
            CommonDigitalSensorInfo commonDigitalSensorInfo = (CommonDigitalSensorInfo) paramsInfoSub.getSensorData();
            String value = StringUtil.formatStringFour((int) Double.parseDouble(commonDigitalSensorInfo.getTriggerThreshold()) + "");
            stringBuilder.append(value);
        }
        stringBuilder.append("\r\n");
        String command = String.valueOf(stringBuilder);
        commandItems.add(command);

        return command;
    }

    /**
     * 获取采集器接入传感器触发阈值(通用)指令
     */
    private String getSingleTriggerThreshold(CollectorSensorParamsInfo paramsInfoSub) {
        if (paramsInfoSub == null) {
            return "";
        }
        String command = "";
        SensorType sensorType = paramsInfoSub.getSensorType();
        switch (sensorType) {
            case RAIN_GAUGE://雨量计
            case WIRE_SHIFT://裂缝计
            case SOIL_MOISTURE://管式含水率计
            case INCLINOMETER: //测斜仪
            case ULTRASONIC_LEVEL_GAUGE://超声波物位计
            case RADAR_LEVEL_GAUGE://雷达物位计
            case INFRASOUND://次声仪
            case STATIC_LEVEL://静力水准
            case WEATHER_STATION://气象计
            case DIGITAL_WATER_LEVEL_GAUGE://数字式水位计
                CommonDigitalSensorInfo commonDigitalSensorInfo = (CommonDigitalSensorInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        commonDigitalSensorInfo.getTriggerThreshold() + "\r\n";
                break;
        }
        return command;
    }

    /**
     * 获取采集器接入传感器修正值(只有墒情计用到3个修正值，其他传感器只用到一个修正值)指令
     */
    private String getCorrectionValue(CollectorSensorParamsInfo paramsInfoSub) {
        if (paramsInfoSub == null) {
            return "";
        }
        String command = "";
        SensorType sensorType = paramsInfoSub.getSensorType();
        switch (sensorType) {
            case RAIN_GAUGE://雨量计
            case WIRE_SHIFT://裂缝计
            case SOIL_MOISTURE://管式含水率计
            case INCLINOMETER: //测斜仪
            case ULTRASONIC_LEVEL_GAUGE://超声波物位计
            case RADAR_LEVEL_GAUGE://雷达物位计
            case INFRASOUND://次声仪
            case STATIC_LEVEL://静力水准
            case WEATHER_STATION://气象计
            case DIGITAL_WATER_LEVEL_GAUGE://数字式水位计
                CommonDigitalSensorInfo commonDigitalSensorInfo = (CommonDigitalSensorInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        commonDigitalSensorInfo.getCorrectionValue() + "\r\n";
                break;
        }
        return command;
    }

    /**
     * 获取测斜仪的测段长指令
     */
    private String getMeasureLongValue() {
        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##166");
        builderFirst.append(defaultSensorParamsInfo.getSensorType());
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
            CommonDigitalSensorInfo commonDigitalSensorInfo = (CommonDigitalSensorInfo) paramsInfoSub.getSensorData();
            builderFirst.append(StringUtil.formatStringFive(commonDigitalSensorInfo.getExValue1()));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);
        return command;
    }

    /**
     * 获取静力水准高程指令
     */
    private String getElevationValue(CollectorSensorParamsInfo paramsInfoSub) {
        String command = "";
        CommonDigitalSensorInfo commonDigitalSensorInfo = (CommonDigitalSensorInfo) paramsInfoSub.getSensorData();
        command = "##159" +
                StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                commonDigitalSensorInfo.getExValue1() + "\r\n";

        return command;
    }

    @Override
    protected void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SET_COLLECTOR_SENSOR://设置采集器接入的传感器 150
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("接入传感器设置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case COLLECTOR_SENSOR_THRESHOLD_SOLI://传感器触发阈值(单传感器设置) 168
            case COLLECTOR_SENSOR_THRESHOLD://传感器触发阈值(多传感器设置) 162
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器触发阈值设置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case COLLECTOR_SENSOR_REVISED: //传感器修正值 165
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器修正值设置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SET_INCLINOMETER_LONG: //设置测斜仪测段长 166
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("测段长设置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case STATIC_LEVEL_ELEVATION: //设置静力水准高程
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("静力水准设置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            default:
                super.setResultData(cmdStr);
                break;
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
}
