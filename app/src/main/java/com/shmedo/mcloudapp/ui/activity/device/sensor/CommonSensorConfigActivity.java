package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.model.CollectorSensorParamsInfo;
import com.shmedo.core.model.SensorInclinometerInfo;
import com.shmedo.core.model.SensorInfrasoundInfo;
import com.shmedo.core.model.SensorRadarLevelInfo;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.core.model.SensorWireShiftInfo;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.interfaces.Extras;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/8 <br/>
 * 描述：    普通传感器配置页面
 */
public class CommonSensorConfigActivity extends BaseSensorConfigActivity {

    public static void startActivityForResultByFragment(Fragment context, String collectorSensorConfig, int requestCode) {
        Intent intent = new Intent(context.getActivity(), CommonSensorConfigActivity.class);
        intent.putExtra(Extras.SPLICE_SENSOR_PARAMS, collectorSensorConfig);
        context.startActivityForResult(intent, requestCode);
    }


    public static void startActivityForResult(Activity context, String collectorSensorConfig, int requestCode) {
        Intent intent = new Intent(context, CommonSensorConfigActivity.class);
        intent.putExtra(Extras.SPLICE_SENSOR_PARAMS, collectorSensorConfig);
        context.startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onPositiveClick(View view) {
        KeyBordUtils.hideSoftKeyboard(view);

        List<CollectorSensorParamsInfo> paramsInfoSubList = new ArrayList<>();
        paramsInfoSubList.addAll(collectorSensorHashMap.values());
        if (paramsInfoSubList.isEmpty()) {
            return false;
        }

        for (int k = 0; k < paramsInfoSubList.size() - 1; k++) {
            for (int j = k + 1; j < paramsInfoSubList.size(); j++) {
                if (paramsInfoSubList.get(k).getSensorAddress().equals(paramsInfoSubList.get(j).getSensorAddress())) {
                    ToastUtils.show("地址不能重复");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onNegativeClick(View view) {
        super.onNegativeClick(view);
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
        for (CollectorSensorParamsInfo paramsInfoSub : collectorSensorParamsInfoSubs) {
            spliceStringCollectorSensorParams(paramsInfoSub);
            builderFirst.append(StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) + StringUtil.formatStringTwo(paramsInfoSub.getSensorType().toString()));
        }
        builderFirst.append("\r\n");
        String command = String.valueOf(builderFirst);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
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
                        sensorInfrasoundInfo.getRevised() + "\r\n";
                break;

            case HD08://土壤湿度采集器
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorSoilMoistureInfo.getRevised() + "\r\n";
                break;

            case RD08://雷达采集器
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel().toString()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorRadarLevelInfo.getRevised() + "\r\n";
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
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SET_COLLECTOR_SENSOR://设置采集器接入的传感器 150
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show(collectorName + "接入传感器配置错误!");
                    stopProgressRunnable();
                    return;
                }
                setTriggerThreshold();
                break;

            case COLLECTOR_SENSOR_THRESHOLD_SOLI://传感器触发阈值 168
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show(collectorName + "的传感器触发阈值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                setCorrectionValue();
                break;

            case COLLECTOR_SENSOR_REVISED: //传感器修正值 165
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show(collectorName + "的传感器修正值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sensorIndex++;
                setTriggerThreshold();

                if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
                    stopProgressRunnable();
                    ToastUtils.show("设置完成");
                    hander.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = getIntent();
                            intent.putExtra(Extras.SPLICE_SENSOR_PARAMS, sbCollectorSensorConfig.toString());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }, 2000);
                }
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
