package com.shmedo.mcloudapp.deviceconfig.ui.fragment.sensor;

import android.os.Bundle;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.SensorInclinometerInfo;
import com.shmedo.configlibrary.ble.model.SensorInfrasoundInfo;
import com.shmedo.configlibrary.ble.model.SensorRadarLevelInfo;
import com.shmedo.configlibrary.ble.model.SensorSoilMoistureInfo;
import com.shmedo.configlibrary.ble.model.SensorWireShiftInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/15 <br/>
 * 描述：    外接数字式传感器
 */
public class BleDASExternalDigtalSensorFragment extends BaseBleDASExternalSensorFragment {

    public static BaseBleDASExternalSensorFragment newInstance(String collectorModel) {
        BaseBleDASExternalSensorFragment fragment = new BleDASExternalDigtalSensorFragment();
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
            Timber.e("%s 采集器接入的传感器信息为空!", collectorName);
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

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_LONG_DELAY_MILLIS);
        sendCommonCommandImmediately(command);
        Timber.d("设置 %s 接入的传感器指令===%s", collectorName, command);
    }

    /**
     * 设置采集器接入传感器触发阈值
     */
    private void setTriggerThreshold() {
        if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
            return;
        }

        String command = "";
        CollectorSensorParamsInfo paramsInfoSub = collectorSensorParamsInfoSubs.get(sensorIndex);
        CollectorModel collectorModel = paramsInfoSub.getCollectorModel();
        switch (collectorModel) {
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

        sendCommonCommandImmediately(command);
        Timber.d("设置 %s %s 通道号的传感器触发阈值参数===%s", collectorName, StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()), command);
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
        sendCommonCommandImmediately(command);
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

        sendCommonCommandImmediately(command);
        Timber.d("设置 %s 的测段长指令===%s", collectorName, command);
    }

    @Override
    protected void setResultData(CmdResponseMessage responseMessage) {
        String cmdStr = responseMessage.getResult();
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SET_COLLECTOR_SENSOR://设置采集器接入的传感器 150
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("接入传感器设置错误!");
                    stopProgressRunnable();
                    return;
                }
                setTriggerThreshold();
                break;

            case COLLECTOR_SENSOR_THRESHOLD_SOLI://传感器触发阈值 168
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器触发阈值设置错误!");
                    stopProgressRunnable();
                    return;
                }
                setCorrectionValue();
                break;

            case COLLECTOR_SENSOR_REVISED: //传感器修正值 165
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器修正值设置错误!");
                    stopProgressRunnable();
                    return;
                }
                sensorIndex++;
                setTriggerThreshold();

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
                    stopProgressRunnable();
                    return;
                }
                doAfterSetting();
                break;

            default:
                super.setResultData(responseMessage);
                break;
        }
    }

    private void doAfterSetting() {
        stopProgressRunnable();
        ToastUtils.show("已设置");
        MCloudApp.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivity.finish();
            }
        }, 2000);
    }
}
