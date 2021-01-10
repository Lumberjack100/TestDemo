package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeBasicConfigParamParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeBasicInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeCurrentStateInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeExecutiveAgencyInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeGuideGrooveCalibrationInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeInclinometerInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMeasuringHoleDepthInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMeterWheelParamParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMotionStateParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMotorMotionAngleInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeMotorMotionDistanceInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeStepperMotorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.adme.AdmeWorkModeParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsAisleInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsAisleTerminalInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsBasicInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalCollectorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalCommInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalSensorInfoParser;
import com.shmedo.configlibrary.iot.cmd.parser.vms.VmsTerminalTimeParser;
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
 * 描述：     TODO
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
        IOTCommandResult commandResult = new IOTCommandResult();
        String temp = result.replace("&&", "");
        //失败的指令处理
        if (temp.contains(IOTCommandResult.ERROR_FLAG)) {
            CommonSettingCmdResult cmdResult = CommonSettingCmdResultParser.getInstance().parse(result);
            commandResult.setSuccess(false);
            commandResult.setMessage(cmdResult.getReason());
            return commandResult;
        }

        IOTCommandType cmdType = IOTStringUtil.extractCommandType(temp);
        IOTResultParser parser = parserMap.get(cmdType);
        if (parser == null)
            throw new RuntimeException("未找到命令：" + cmdType + "的解析器");

        parser.validate(temp);
        T data = (T) parser.parse(temp);
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
        CommonSettingCmdResult data = CommonSettingCmdResultParser.getInstance().parse(result);

        return data;
    }

    /**
     * 这个方式只执行初级的格式校验，具体的逻辑校验由Validater接口和StringValidater接口执行
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
                VmsTerminalTimeParser.class,
                DeviceCurrentStateParser.class,
                TelemetryParser.class,
                VmsBasicInfoParser.class,
                VmsAisleTerminalInfoParser.class,
                VmsAisleInfoParser.class,
                DataCenterInfoParser.class,
                VmsTerminalSensorInfoParser.class,
                DataCenterStatusParser.class,
                VmsTerminalCollectorInfoParser.class,
                VmsTerminalCommInfoParser.class,
                AdmeBasicInfoParser.class,
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
                AdmeWorkModeParser.class
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
