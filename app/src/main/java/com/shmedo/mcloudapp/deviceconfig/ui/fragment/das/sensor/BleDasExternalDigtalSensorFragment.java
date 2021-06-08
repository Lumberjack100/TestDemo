package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.os.Bundle;
import android.os.Message;

import androidx.annotation.NonNull;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.SensorInclinometerInfo;
import com.shmedo.configlibrary.ble.model.SensorInfrasoundInfo;
import com.shmedo.configlibrary.ble.model.SensorPiezoelectricRainGauge;
import com.shmedo.configlibrary.ble.model.SensorRadarLevelInfo;
import com.shmedo.configlibrary.ble.model.SensorSoilMoistureInfo;
import com.shmedo.configlibrary.ble.model.SensorUltrasonicLevelInfo;
import com.shmedo.configlibrary.ble.model.SensorWireShiftInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/15 <br/>
 * 描述：    外接数字式传感器
 */
public class BleDasExternalDigtalSensorFragment extends BaseBleDasExternalSensorFragment {

    public static BaseBleDasExternalSensorFragment newInstance(String collectorModel) {
        BaseBleDasExternalSensorFragment fragment = new BleDasExternalDigtalSensorFragment();
        Bundle args = new Bundle();
        args.putString(AppContants.Extras.COLLECTOR_MODE, collectorModel);
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
        collectorSensorParamsInfoSubs.addAll(collectorSensorHashMap.values());
        if (collectorSensorParamsInfoSubs.isEmpty()) {
            Timber.i("%s 采集器接入的传感器信息为空!", collectorName);
            sendCloseCollectorCmd();
            return;
        }

        //##150zzxxXXXX\r\n：设置采集器接入的传感器
        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##150");
        builderFirst.append(defaultCollectorSensorParamsInfo.getCollectorModel() + StringUtil.formatStringTwo(String.valueOf(collectorSensorParamsInfoSubs.size())));
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorHashMap.values()) {
            builderFirst.append(StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) + StringUtil.formatStringTwo(paramsInfoSub.getSensorType().toString()));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);

        startProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, WRITE_TIME_OUT_LONG_MILLIS);
        sendCommand(command);
        Timber.d("设置 %s 接入的传感器指令===%s", collectorName, command);
    }

    /**
     * 设置采集器接入传感器触发阈值(通用)
     */
    private void setSingleTriggerThreshold() {
        if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
            return;
        }

        String command = "";
        CollectorSensorParamsInfo paramsInfoSub = collectorSensorParamsInfoSubs.get(sensorIndex);
        CollectorModel collectorModel = paramsInfoSub.getCollectorModel();
        switch (collectorModel) {
            case RAIN08://雨量采集器
                SensorPiezoelectricRainGauge sensorPiezoelectricRainGauge = (SensorPiezoelectricRainGauge) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorPiezoelectricRainGauge.getTriggerThreshold() + "\r\n";
                break;

            case DS08://裂缝计采集器
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorWireShiftInfo.getTriggerThreshold() + "\r\n";
                break;

            case CS08://次声采集器
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInfrasoundInfo.getTriggerThreshold() + "\r\n";
                break;

            case HD08://土壤湿度采集器
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorSoilMoistureInfo.getTriggerThreshold() + "\r\n";
                break;

            case UDS08://超声波采集器
                SensorUltrasonicLevelInfo sensorUltrasonicLevelInfo = (SensorUltrasonicLevelInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorUltrasonicLevelInfo.getTriggerThreshold() + "\r\n";
                break;

            case RD08://雷达采集器
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorRadarLevelInfo.getTriggerThreshold() + "\r\n";
                break;

            case CX08://测斜仪采集器
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInclinometerInfo.getTriggerThreshold() + "\r\n";
                break;
        }

        sendCommand(command);
        Timber.d("设置 %s %s 通道号的传感器触发阈值参数===%s", collectorName, StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()), command);
    }

    /**
     * 设置 超声波物位计 接入传感器触发阈值<br/>
     * 指令格式: ##162xxX…X\r\n<br/>
     * xx表示采集器类型，X…X表示阀值，X…X由接入传感器数量N决定（4*N）<br/>
     * 例如：裂缝采集器接入两只拉线位移计，报警值分别30mm、40mm<br/>
     * 设置举例：##1620200300040\r\n<br/>
     * 返回信息：$$1620200300040\r\n<br/>
     */
    private void setMultiTriggerThreshold() {
        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##162");
        builderFirst.append(defaultCollectorSensorParamsInfo.getCollectorModel());
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
            builderFirst.append(getTriggerThresholdBySensorType(paramsInfoSub));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);
        sendCommand(command);
        Timber.d("设置传感器触发阈值===%s", command);
    }

    /**
     * 设置采集器接入传感器修正值（只有墒情计用到3个修正值，其他传感器只用到一个修正值）
     */
    private void setCorrectionValue() {
        if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
            return;
        }

        String command = "";
        CollectorSensorParamsInfo paramsInfoSub = collectorSensorParamsInfoSubs.get(sensorIndex);
        CollectorModel collectorModel = paramsInfoSub.getCollectorModel();
        switch (collectorModel) {
            case RAIN08://雨量采集器
                SensorPiezoelectricRainGauge sensorPiezoelectricRainGauge = (SensorPiezoelectricRainGauge) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorPiezoelectricRainGauge.getCorrectionValue() + "\r\n";
                break;

            case DS08://裂缝计采集器
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorWireShiftInfo.getCorrectionValue() + "\r\n";
                break;

            case CS08://次声采集器
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInfrasoundInfo.getCorrectionValue() + "\r\n";
                break;

            case HD08://土壤湿度采集器
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorSoilMoistureInfo.getCorrectionValue() + "\r\n";
                break;

            case UDS08://超声波采集器
                SensorUltrasonicLevelInfo sensorUltrasonicLevelInfo = (SensorUltrasonicLevelInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorUltrasonicLevelInfo.getCorrectionValue() + "\r\n";
                break;

            case RD08://雷达采集器
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorRadarLevelInfo.getCorrectionValue() + "\r\n";
                break;

            case CX08://测斜仪采集器
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInclinometerInfo.getCorrectionValue() + "\r\n";
                break;
        }
        sendCommand(command);
        Timber.d("设置 %s 采集器 %s 地址的传感器修正值参数===%s", collectorModel, StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()), command);
    }

    /**
     * 设置测斜仪的测段长
     */
    private void setMeasureLongValue() {
        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##166");
        builderFirst.append(defaultCollectorSensorParamsInfo.getSensorType());
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
            SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) paramsInfoSub.getSensorData();
            builderFirst.append(StringUtil.formatStringFive(sensorInclinometerInfo.getMeasureLength()));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);

        sendCommand(command);
        Timber.d("设置 %s 的测段长指令===%s", collectorName, command);
    }

    private String getTriggerThresholdBySensorType(CollectorSensorParamsInfo infoSub) {
        String value = "";
        SensorType sensorType = infoSub.getSensorType();
        switch (sensorType) {
            case ULTRASONIC_LEVEL_GAUGE: {//超声波采集器
                SensorUltrasonicLevelInfo sensorInfo = (SensorUltrasonicLevelInfo) infoSub.getSensorData();
                value = StringUtil.formatStringFour((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
            }
            break;
        }

        return value;
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (msg.what == AppContants.MsgWhat.MSG_DEFAULT) {
            ToastUtils.show("响应超时,请稍后尝试");
        }
    }

    @Override
    protected void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SET_COLLECTOR_SENSOR://设置采集器接入的传感器 150
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("接入传感器设置错误!");
                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sensorIndex = 0;
                //超声波物位计使用多传感器触发阈值配置指令
                if (CollectorModel.value(collectorModelValue) == CollectorModel.UDS08) {
                    setMultiTriggerThreshold();
                } else {
                    setSingleTriggerThreshold();
                }
                break;

            case COLLECTOR_SENSOR_THRESHOLD_SOLI://传感器触发阈值(单传感器设置) 168
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器触发阈值设置错误!");
                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                setCorrectionValue();
                break;

            case COLLECTOR_SENSOR_THRESHOLD://传感器触发阈值(多传感器设置) 162
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器触发阈值设置错误!");
                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                setCorrectionValue();
                break;

            case COLLECTOR_SENSOR_REVISED: //传感器修正值 165
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器修正值设置错误!");
                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sensorIndex++;
                if (CollectorModel.value(collectorModelValue) == CollectorModel.UDS08) {
                    setCorrectionValue();
                } else {
                    setSingleTriggerThreshold();
                }

                if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
                    //测斜仪需要设置测段长
                    if (CollectorModel.value(collectorModelValue) == CollectorModel.CX08) {
                        setMeasureLongValue();
                    } else {
                        doAfterSetting();
                    }
                }
                break;

            case SET_INCLINOMETER_LONG: //设置测斜仪测段长 166
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("测段长设置错误!");
                    stopProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                doAfterSetting();
                break;

            default:
                super.setResultData(cmdStr);
                break;
        }
    }

}
