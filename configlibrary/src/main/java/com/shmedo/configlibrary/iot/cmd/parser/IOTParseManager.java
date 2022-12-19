package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeAnthropomorphicMovementInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeBaseInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeBasicConfigParamParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeCurrentStateInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeExecutiveAgencyInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeGuideGrooveCalibrationInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeInclinometerInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeLockedRotorDetectionInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeLowEnergyModeInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMeasuringHoleDepthInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMeterWheelParamParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMotionStateParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMotorMotionAngleInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMotorMotionDistanceInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeStepperMotorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeStepperMotorRelayModeInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeVoltageConfigParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeWorkModeParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.AlarmLevelParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.AudibleAlarmParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasBaseInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasBdTerminalInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasCollectorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasDataReportInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasDigitalPiezometerInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasExternalSensorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasFixedPointReportInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasIOSensorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasNetStatusInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasSensorStatusInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasSolarStatusInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasSubSensorStatusInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.das.DasTemperatureAndHumidityStatusinfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40BasePositionInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40BoardSolutionInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40CORSInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40EthernetInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40GpsWorkInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40NmeaTimeInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40RTKModeInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40SatelitteInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.e40.E40SerialPortInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.hac.HacExecutiveAgencyInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.hac.HacMeasuringDataInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.hac.HacMeasuringHoleDepthInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.hac.HacMotionStateParser;
import com.shmedo.configlibrary.iot.cmd.parser.hac.HacMotorMotionDistanceInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.hac.HacWarningValueParser;
import com.shmedo.configlibrary.iot.cmd.parser.lr200.LR200PositionInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.m20.M20BaseInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.rn20.Rn20BaseInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.rn20.Rn20ModuleStatusParser;
import com.shmedo.configlibrary.iot.cmd.parser.rn20.Rn20PositionInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.TerminalTelemetryParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsAisleInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsAisleTerminalInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsBasicInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalCollectorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalCommInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalSensorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalSnParser;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     TODO #gh#
 */
public class IOTParseManager {
    private static final IOTParseManager ourInstance = new IOTParseManager();

    private Map<IOTCommandType, IOTResultParser> parserMap = new HashMap<>();

    public static IOTParseManager getInstance() {
        if (ourInstance.parserMap == null || ourInstance.parserMap.size() <= 0) {
            ourInstance.registerParse();
        }
        return ourInstance;
    }

    private IOTParseManager() {
        registerParse();
    }

    public <T> IOTCommandResult<T> parse(String result) {
        baseValidate(result);
        String tempCmd = result.replace("&&", "");
        IOTCommandResult<T> commandResult = new IOTCommandResult<>();

        //失败的指令处理
        if (tempCmd.contains(IOTCommandResult.ERROR_FLAG)) {
            CommonSettingCmdResult settingCmdResult = CommonSettingCmdResultParser.getInstance().parse(result);
            commandResult.setSuccess(false);
            commandResult.setMessage(settingCmdResult.getReason());
            return commandResult;
        }
        IOTCommandType cmdType = IOTStringUtil.extractCommandType(tempCmd);
        IOTResultParser parser = parserMap.get(cmdType);
        if (parser == null) {
            commandResult.setSuccess(false);
            commandResult.setMessage("未找到命令：" + cmdType.toString() + "的解析器");
            commandResult.setCommandType(cmdType);
            return commandResult;
        }

        parser.validate(tempCmd);
        T data = (T) parser.parse(tempCmd);
        commandResult.setSuccess(true);
        commandResult.setCommandType(cmdType);
        commandResult.setResult(data);

        return commandResult;
    }

    /**
     * 解析设置类指令
     *
     * @param result
     * @return
     */
    public CommonSettingCmdResult parseSettingCmd(String result) {
        if (TextUtils.isEmpty(result)) {
            return null;
        }
        if (result.length() < IOTCommandResult.RESULT_MIN_LENGTH) {
            return null;
        }
        if (!result.startsWith(IOTCommandResult.COMMAND_HEADER)) {
            return null;
        }
        CommonSettingCmdResultParser.getInstance().validate(result);
        CommonSettingCmdResult settingCmdResult = CommonSettingCmdResultParser.getInstance().parse(result);
        return settingCmdResult;
    }

    /**
     * 这个方式只执行初级的格式校验，具体的逻辑校验由Validator接口和StringValidator接口执行
     *
     * @param result
     */
    private void baseValidate(String result) {
        if (TextUtils.isEmpty(result))
            throw new IllegalArgumentException("result为空或者null");
        if (result.length() < IOTCommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("result长度过短:" + result);
        if (!result.startsWith(IOTCommandResult.COMMAND_HEADER))
            throw new IllegalArgumentException("result格式错误:" + result);
    }

    /**
     * 将本包下的Parse注册到parserMap中
     */
    private void registerParse() {
        List<Class> clazzes = Arrays.asList(new Class[]{
                DeviceTimeParser.class,
                DeviceCurrentStateParser.class,
                DeviceCurrentStateExParser.class,
                TelemetryParser.class,
                DataCenterStatusParser.class,
                DataCenterInfoParser.class,
                VmsBasicInfoParser.class,
                VmsAisleTerminalInfoParser.class,
                VmsAisleInfoParser.class,
                VmsTerminalSensorInfoParser.class,
                VmsTerminalCollectorInfoParser.class,
                VmsTerminalCommInfoParser.class,
                VmsTerminalSnParser.class,
                TerminalTelemetryParser.class,
                AdmeBaseInfoParser.class,
                AdmeBasicConfigParamParser.class,
                AdmeMeterWheelParamParser.class,
                AdmeInclinometerInfoParser.class,
                AdmeMotionStateParser.class,
                AdmeStepperMotorInfoParser.class,
                AdmeExecutiveAgencyInfoParser.class,
                AdmeMeasuringHoleDepthInfoParser.class,
                AdmeMotorMotionDistanceInfoParser.class,
                AdmeGuideGrooveCalibrationInfoParser.class,
                AdmeMotorMotionAngleInfoParser.class,
                AdmeCurrentStateInfoParser.class,
                AdmeWorkModeParser.class,
                AdmeLowEnergyModeInfoParser.class,
                AdmeLockedRotorDetectionInfoParser.class,
                AdmeVoltageConfigParser.class,
                AdmeAnthropomorphicMovementInfoParser.class,
                AdmeStepperMotorRelayModeInfoParser.class,
                M20BaseInfoParser.class,
                E40SatelitteInfoParser.class,
                E40RTKModeInfoParser.class,
                E40BasePositionInfoParser.class,
                E40CORSInfoParser.class,
                E40BoardSolutionInfoParser.class,
                E40EthernetInfoParser.class,
                E40SerialPortInfoParser.class,
                E40GpsWorkInfoParser.class,
                E40NmeaTimeInfoParser.class,
                DasCollectorInfoParser.class,
                DasBaseInfoParser.class,
                DasNetStatusInfoParser.class,
                DasSolarStatusInfoParser.class,
                DasTemperatureAndHumidityStatusinfoParser.class,
                DasSensorStatusInfoParser.class,
                DasSubSensorStatusInfoParser.class,
                DasIOSensorInfoParser.class,
                DasDigitalPiezometerInfoParser.class,
                DasDataReportInfoParser.class,
                DasBdTerminalInfoParser.class,
                DasExternalSensorInfoParser.class,
                DasFixedPointReportInfoParser.class,
                Rn20BaseInfoParser.class,
                Rn20ModuleStatusParser.class,
                Rn20PositionInfoParser.class,
                LogOutputInfoParser.class,
                LR200PositionInfoParser.class,
                AudibleAlarmParser.class,
                AlarmLevelParser.class,
                HacMeasuringDataInfoParser.class,
                HacMeasuringHoleDepthInfoParser.class,
                HacMotionStateParser.class,
                HacWarningValueParser.class,
                HacMotorMotionDistanceInfoParser.class,
                HacExecutiveAgencyInfoParser.class
        });

        registerWithClass(clazzes);
    }

    private void registerWithClass(List<Class> classes) {
        try {
            for (int i = 0; i < classes.size(); ++i) {
                IOTResultParser resultParser = (IOTResultParser) classes.get(i).newInstance();
                if (!parserMap.containsKey(resultParser.commandType())) {
                    parserMap.put(resultParser.commandType(), resultParser);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
