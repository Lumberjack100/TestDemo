package com.shmedo.mcloudapp.deviceconfig.ui.fragment.sensor;

import android.os.Bundle;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanPercolateInfo;
import com.shmedo.configlibrary.ble.model.SensorJunXingZljInfo;
import com.shmedo.configlibrary.ble.model.SensorKangPercolateInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/15 <br/>
 * 描述：    外接振弦式传感器
 */
public class BleDASExternalVibratingWireSensorFragment extends BaseBleDASExternalSensorFragment {
    /**
     * 传感器除了触发阀值外其他修正参数配置指令集合
     */
    private List<String> correctValueCmdList = new LinkedList<>();

    /**
     * 传感器除了触发阀值外,其他参数配置项名称
     */
    private List<String> configItemNameList = new ArrayList<>();

    /**
     * 当前配置项名称
     */
    private String curConfigItemName;

    public static BaseBleDASExternalSensorFragment newInstance(String collectorModel) {
        BaseBleDASExternalSensorFragment fragment = new BleDASExternalVibratingWireSensorFragment();
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
        builderFirst.append(defaultCollectorSensorParamsInfo.getCollectorModel());
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
            builderFirst.append(getTriggerThresholdBySensorType(paramsInfoSub));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);
        sendCommonCommandImmediately(command);
        Timber.d("设置传感器触发阈值===%s", command);
    }

    private String getTriggerThresholdBySensorType(CollectorSensorParamsInfo infoSub) {
        String value = "";
        SensorType sensorType = infoSub.getSensorType();
        switch (sensorType) {
            case KANG_PERCOLATE: {//基康渗压计(BGK-4500)
                SensorKangPercolateInfo sensorInfo = (SensorKangPercolateInfo) infoSub.getSensorData();
                value = StringUtil.formatStringFour((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
            }
            break;

            case GUDAN_PERCOLATE: {//葛南渗压计(VWP-03)
                SensorGudanPercolateInfo sensorInfo = (SensorGudanPercolateInfo) infoSub.getSensorData();
                value = StringUtil.formatStringFour((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
            }
            break;

            case JUNXING_ZLJ_300T: {//轴力计(ZLJ-300T)
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
        configItemNameList.clear();
        correctValueCmdList.clear();
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
            getCorrectionValueParamCommands(paramsInfoSub);
        }
        sendCorrectionValueCmd();
    }

    private void getCorrectionValueParamCommands(CollectorSensorParamsInfo infoSub) {
        String channelNumber = StringUtil.formatStringTwo(infoSub.getSensorAddress());
        String cmdCorrectionValueFormat = "##167" + channelNumber + "{}\r\n";//修正参数
        String cmdInstallElevationFormat = "##169" + channelNumber + "{}\r\n";//安装高程
        SensorType sensorType = infoSub.getSensorType();
        switch (sensorType) {
            case KANG_PERCOLATE: {//基康渗压计(BGK-4500)
                SensorKangPercolateInfo sensorInfo = (SensorKangPercolateInfo) infoSub.getSensorData();
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "A" + sensorInfo.getPolynomialRatioA()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "B" + sensorInfo.getPolynomialRatioB()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "C" + sensorInfo.getPolynomialRatioC()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "K" + sensorInfo.getTemperatureCoefficientK()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "T" + sensorInfo.getCreateTemperature()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "M" + sensorInfo.getManualCorrection()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "F" + sensorInfo.getCordLenght()));
                correctValueCmdList.add(cmdInstallElevationFormat.replace("{}", sensorInfo.getInstallElevation()));

                configItemNameList.add("基康渗压计 多项式系数A");
                configItemNameList.add("基康渗压计 多项式系数B");
                configItemNameList.add("基康渗压计 多项式系数C");
                configItemNameList.add("基康渗压计 温度系数K");
                configItemNameList.add("基康渗压计 初始温度T0");
                configItemNameList.add("基康渗压计 手动纠偏");
                configItemNameList.add("基康渗压计 绳长");
                configItemNameList.add("基康渗压计 安装高程");
            }
            break;

            case GUDAN_PERCOLATE: {//葛南渗压计(VWP-03)
                SensorGudanPercolateInfo sensorInfo = (SensorGudanPercolateInfo) infoSub.getSensorData();
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "A" + sensorInfo.getSensitivityK()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "B" + sensorInfo.getTemperatureCoefficientB()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "F" + sensorInfo.getReferenceValue()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "T" + sensorInfo.getCreateTemperature()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "M" + sensorInfo.getManualCorrection()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "C" + sensorInfo.getCordLenght()));
                correctValueCmdList.add(cmdInstallElevationFormat.replace("{}", sensorInfo.getInstallElevation()));

                configItemNameList.add("葛南渗压计 灵敏度K");
                configItemNameList.add("葛南渗压计 温修系数b");
                configItemNameList.add("葛南渗压计 基准值F0");
                configItemNameList.add("葛南渗压计 初始温度T0");
                configItemNameList.add("葛南渗压计 手动纠偏");
                configItemNameList.add("葛南渗压计 绳长");
                configItemNameList.add("葛南渗压计 安装高程");
            }
            break;

            case JUNXING_ZLJ_300T: {//轴力计(ZLJ-300T)
                SensorJunXingZljInfo sensorInfo = (SensorJunXingZljInfo) infoSub.getSensorData();
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "A" + sensorInfo.getPolynomialRatioA()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "B" + sensorInfo.getTemperatureCoefficientB()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "F" + sensorInfo.getReferenceValue()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "T" + sensorInfo.getCreateTemperature()));
                correctValueCmdList.add(cmdCorrectionValueFormat.replace("{}", "M" + sensorInfo.getManualCorrection()));

                configItemNameList.add("轴力计 标定系数A");
                configItemNameList.add("轴力计 温修系数b");
                configItemNameList.add("轴力计 基准值F0");
                configItemNameList.add("轴力计 初始温度T0");
                configItemNameList.add("轴力计 手动纠偏");
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

        curConfigItemName = configItemNameList.get(0);
        configItemNameList.remove(0);
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
                    Timber.w("%s配置错误!", curConfigItemName);
                    ToastUtils.show(curConfigItemName + "配置错误!");
                    stopProgressRunnable();
                    return;
                }

//                if (isTimeOut) {
//                    Timber.w("达到发送配置指令超时时间!");
//                    return;
//                }

                if (!correctValueCmdList.isEmpty()) {
                    sendCorrectionValueCmd();
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
