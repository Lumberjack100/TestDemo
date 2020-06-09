package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.model.SensorGudanPercolateInfo;
import com.shmedo.core.model.SensorJunXingZljInfo;
import com.shmedo.core.model.SensorKangPercolateInfo;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.interfaces.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/8 <br/>
 * 描述：    正弦传感器配置页面
 */
public class VibratingWireSensorConfigActivity extends BaseSensorConfigActivity {
    /**
     * 传感器除了触发阀值外其他修正参数配置指令集合
     */
    private List<String> correctValueCmdList = new LinkedList<>();


    public static void startActivity(Context context, String collectorSensorConfig) {
        Intent intent = new Intent(context, VibratingWireSensorConfigActivity.class);
        intent.putExtra(Extras.SENSOR_PARAMS, collectorSensorConfig);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##150");
        builderFirst.append(defaultCollectorSensorParamsInfoSub.getCollectorModel() + StringUtil.formatStringTwo(String.valueOf(collectorSensorParamsInfoSubs.size())));
        for (CollectorSensorParamsInfoSub paramsInfoSub : collectorSensorParamsInfoSubs) {
            builderFirst.append(StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) + StringUtil.formatStringTwo(paramsInfoSub.getSensorType()));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_DELAY_MILLIS);
        sendCommonCommandImmediately(command);
        Timber.d("设置接入的传感器指令===%s", command);
    }

    /**
     * 设置采集器接入传感器触发阈值<br/>
     * 指令格式: ##162xxX…X\r\n<br/>
     * xx表示采集器类型，X…X表示阀值，X…X由接入传感器数量N决定（4*N）<br/>
     * 例如：裂缝采集器接入两只拉线位移计，触发阀值分别30mm、40mm<br/>
     * 设置举例：##1620200300040\r\n<br/>
     * 返回信息：$$1620200300040\r\n<br/>
     */
    private void setTriggerThreshold() {
        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##162");
        builderFirst.append(defaultCollectorSensorParamsInfoSub.getCollectorModel());
        for (CollectorSensorParamsInfoSub paramsInfoSub : collectorSensorParamsInfoSubs) {
            builderFirst.append(getTriggerThresholdBySensorType(paramsInfoSub));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);
        sendCommonCommandImmediately(command);
        Timber.d("设置传感器触发阈值===%s", command);
    }

    private String getTriggerThresholdBySensorType(CollectorSensorParamsInfoSub infoSub) {
        String value = "";
        String sensorType = infoSub.getSensorType();
        switch (sensorType) {
            case "50": {//基康渗压计(BGK-4500)
                SensorKangPercolateInfo sensorInfo = (SensorKangPercolateInfo) infoSub.getSensorData();
                value = StringUtil.formatStringFour((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
            }
            break;

            case "51": {//葛南渗压计(VWP-03)
                SensorGudanPercolateInfo sensorInfo = (SensorGudanPercolateInfo) infoSub.getSensorData();
                value = StringUtil.formatStringFour((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
            }
            break;

            case "58": {//军星轴力计(ZLJ-300T)
                SensorJunXingZljInfo sensorInfo = (SensorJunXingZljInfo) infoSub.getSensorData();
                value = StringUtil.formatStringFour((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
            }
            break;
        }

        return value;
    }

    /**
     * 设置振弦式传感器修正参数<br/>
     * ##167xxXx…x\r\n<br/>
     * xx表示模拟量传感器接入的采集器的通道号取值00~07<br/>
     * X：表示修正参数类型取值'A'，'B'，'C'，'K'，'M'：这些值可以为小数<br/>
     * x…x：为长度不确定的参数<br/>
     */
    private void setCorrectionValue() {
        for (CollectorSensorParamsInfoSub paramsInfoSub : collectorSensorParamsInfoSubs) {
            getCorrectionValueParamCommands(paramsInfoSub);
        }
        sendCorrectionValueCmd();
    }

    private void getCorrectionValueParamCommands(CollectorSensorParamsInfoSub infoSub) {
        String address = StringUtil.formatStringTwo(infoSub.getSensorAddress());
        String cmdCorrectionValueFormat = "##167" + address + "{}\r\n";//修正参数
        String cmdInstallElevationFormat = "##169" + address + "{}\r\n";//安装高程
        String sensorType = infoSub.getSensorType();
        switch (sensorType) {
            case "50": {//基康渗压计(BGK-4500)
                SensorKangPercolateInfo sensorInfo = (SensorKangPercolateInfo) infoSub.getSensorData();
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "A" + sensorInfo.getPolynomialRatioA()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "B" + sensorInfo.getPolynomialRatioB()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "C" + sensorInfo.getPolynomialRatioC()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "K" + sensorInfo.getTemperatureCoefficientK()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "F" + sensorInfo.getCordLenght()));
                correctValueCmdList.add(cmdInstallElevationFormat.replace("{}", sensorInfo.getInstallElevation()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "M" + sensorInfo.getManualCorrection()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "T" + sensorInfo.getCreateTemperature()));
            }
            break;

            case "51": {//葛南渗压计(VWP-03)
                SensorGudanPercolateInfo sensorInfo = (SensorGudanPercolateInfo) infoSub.getSensorData();
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "A" + sensorInfo.getSensitivityK()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "B" + sensorInfo.getTemperatureCoefficientB()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "C" + sensorInfo.getCordLenght()));
                correctValueCmdList.add(cmdInstallElevationFormat.replace("{}", sensorInfo.getInstallElevation()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "M" + sensorInfo.getManualCorrection()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "T" + sensorInfo.getCreateTemperature()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "F" + sensorInfo.getReferenceValue()));
            }
            break;

            case "58": {//军星轴力计(ZLJ-300T)
                SensorJunXingZljInfo sensorInfo = (SensorJunXingZljInfo) infoSub.getSensorData();
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "A" + sensorInfo.getSensitivityK()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "M" + sensorInfo.getManualCorrection()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "F" + sensorInfo.getReferenceValue()));
            }
            break;
        }
    }

    /**
     * 从修正参数指令集合中逐个取出发送
     */
    private void sendCorrectionValueCmd() {
        if (correctValueCmdList.isEmpty()) {
            Timber.d("修正参数指令已发送完毕");
            return;
        }

        String command = correctValueCmdList.get(0);
        sendCommonCommandImmediately(command);
        Timber.d("设置传感器修正参数===%s", command);
        //移除已发送的指令
        correctValueCmdList.remove(0);
    }

    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SET_COLLECTOR_SENSOR://设置采集器接入的传感器 150
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("接入传感器配置错误!");
                    stopProgressRunnable();
                    return;
                }
                setTriggerThreshold();
                break;

            case COLLECTOR_SENSOR_THRESHOLD://传感器触发阈值 162
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("传感器触发阈值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                setCorrectionValue();
                break;

            case VIBRATING_SENSOR_PARAMETER://传感器修正值 167
            case SENSOR_INSTALLELEVATION://传感器安装高程 169
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show(collectorName + "的传感器修正值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (!correctValueCmdList.isEmpty()) {
                    sendCorrectionValueCmd();
                    return;
                }

                stopProgressRunnable();
                ToastUtils.show("设置完成");
                hander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }
}
