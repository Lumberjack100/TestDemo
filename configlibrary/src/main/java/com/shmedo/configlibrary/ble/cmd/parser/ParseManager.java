package com.shmedo.configlibrary.ble.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.utils.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Liudongdong on 17/12/12.
 * 解析数据管理器
 */
public class ParseManager {
    private static final ParseManager instance = new ParseManager();

    private Map<CommandType, ResultParser> parserMap = new HashMap<>();

    public static ParseManager getInstance() {
        if (instance.parserMap == null || instance.parserMap.size() <= 0) {
            instance.registerParse();
        }
        return instance;
    }

    private ParseManager() {
        registerParse();
    }

    public <T> CommandResult<T> parse(String result) {
        baseValidate(result);
        CommandResult commandResult = new CommandResult();
        String temp = result.replace("\r\n", "");
        if (temp.endsWith(CommandResult.ERROR_END)) {
            commandResult.setSuccess(false);
            commandResult.setMessage(temp);
            return commandResult;
        }

        CommandType cmdType = StringUtil.extractCommandType(temp);
        ResultParser parser = parserMap.get(cmdType);
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
     * 这个方式只执行初级的格式校验，具体的逻辑校验由Validater接口和StringValidater接口执行
     *
     * @param result
     */
    private void baseValidate(String result) {
        if (TextUtils.isEmpty(result))
            throw new IllegalArgumentException("result为空或者null");
        if (result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("result长度过短:" + result);
        if (!result.startsWith(CommandResult.COMMAND_RESULT_HEADER))
            throw new IllegalArgumentException("result格式错误:" + result);
    }

    /**
     * 将本包下的Parse注册到parserMap中
     */
    private void registerParse() {
        List<Class> clazzes = Arrays.asList(new Class[]{
                BaseConfigParser.class,
                BatchResultParser.class,
                CellProtectionVoltageParser.class,
                CollectorConfigParser.class,
                CollectorFrequencyParser.class,
                CollectorSensorParamsParser.class,
                CollectorSensorRevisedParser.class,
                CollectorSensorThresholdParser.class,
                CollectorSensorThresholdSoliParser.class,
                CollectorSolutionFrequencyParser.class,
                CollectorStandbyTimeParser.class,
                DASSendAuthenticRequestParser.class,
                DASSendAuthenticResultParser.class,
                LowEnergyModelParser.class,
                DataCommunicateModeParser.class,
                DataReportIntervalParser.class,
                DigitalOsmometerFunctionParser.class,
                HeartbeatSendIntervalParser.class,
                InstantCollectionParser.class,
                LocalTimeParser.class,
                QueryOsmometerParameterParser.class,
                RebootDeviceParser.class,
                RestoreFactorySettingParser.class,
                SaveConfigInfoParser.class,
                SensorBaudRateParser.class,
                SensorInterfaceTypeParser.class,
                SetCollectorSensorParser.class,
                ServerAddressInfoParser.class,
                SystemRunStateParser.class,
                VersionMessageParser.class,
                DeviceLockStatusParser.class,
                BreakAlarmStatusParser.class,
                InstallLocationParser.class,
                DeviceStatusInfoOneParse.class,
                DeviceStatusInfoTwoParse.class,
                DeviceStatusInfoThreeParse.class,
                DeviceNetStatusParser.class,
                MqttConfigInfoParser.class,
                InclinometerInfoParser.class
        });

//        List<Class> temp = ClassUtil.getAllClassByInterface("com.shmedo.core.cmd.parser", ResultParser.class);

        registerWithClass(clazzes);
    }

    private void registerWithClass(List<Class> classes) {
        try {
            for (int i = 0; i < classes.size(); ++i) {
                ResultParser resultParser = (ResultParser) classes.get(i).newInstance();
                if (!parserMap.containsKey(resultParser.commandType())) {
                    parserMap.put(resultParser.commandType(), resultParser);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
