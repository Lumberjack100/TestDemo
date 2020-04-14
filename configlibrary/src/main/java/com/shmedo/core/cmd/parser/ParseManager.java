package com.shmedo.core.cmd.parser;

import android.text.TextUtils;

import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.utils.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shmedo.core.cmd.CommandResult.COMMAND_RESULT_HEADER;
import static com.shmedo.core.cmd.CommandResult.ERROR_END;
import static com.shmedo.core.cmd.CommandResult.RESULT_MIN_LENGTH;


/**
 * Created by Liudongdong on 17/12/12.
 */
public class ParseManager {
    public static final String PACKAGE_NAME = "com.shmedo.das.das.cmd.parser";
    private static final ParseManager instance = new ParseManager();

    private Map<CommandType, ResultParser> parserMap = new HashMap<>();

    private ParseManager() {
        registerParse();
    }

    public <T> CommandResult<T> parse(String result) {
        baseValidate(result);
        CommandResult commandResult = new CommandResult();
        String temp = result.replace("\r\n", "");
        if (temp.endsWith(ERROR_END)) {
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
        if (result.length() < RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("result长度过短:" + result);
        if (!result.startsWith(COMMAND_RESULT_HEADER))
            throw new IllegalArgumentException("result格式错误:" + result);
    }

    /**
     * 将本包下的Parse注册到parserMap中
     */
    private void registerParse() {
        /*List<Class<? extends ResultParser>> parsers = ClassUtil.getClass(PACKAGE_NAME, ResultParser.class);
        for (int i = 0; i < parsers.size(); i++) {
            try {
                ResultParser parser = parsers.get(i).newInstance();
                parserMap.put(parser.commandType(), parser);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }*/
        AuthenticationConfigParser authenticationConfigParser = new AuthenticationConfigParser();
        AuthorizePhoneNumberParser authorizePhoneNumberParser = new AuthorizePhoneNumberParser();
        BaseConfigParser baseConfigParser = new BaseConfigParser();
        BatchResultParser batchResultParser = new BatchResultParser();
        CellProtectionVoltageParser cellProtectionVoltageParser = new CellProtectionVoltageParser();
        CollectorConfigParser collectorConfigParser = new CollectorConfigParser();
        CollectorFrequencyParser collectorFrequencyParser = new CollectorFrequencyParser();
        CollectorSensorParamsParser collectorSensorParamsParser = new CollectorSensorParamsParser();
        CollectorSensorRevisedParser collectorSensorRevisedParser = new CollectorSensorRevisedParser();
        CollectorSensorThresholdParser collectorSensorThresholdParser = new CollectorSensorThresholdParser();
        CollectorSensorThresholdSoliParser collectorSensorThresholdSoliParser = new CollectorSensorThresholdSoliParser();
        CollectorSolutionFrequencyParser collectorSolutionFrequencyParser = new CollectorSolutionFrequencyParser();
        CollectorStandbyTimeParser collectorStandbyTimeParser = new CollectorStandbyTimeParser();
        DASSendAuthenticRequestParser dasSendAuthenticRequestParser = new DASSendAuthenticRequestParser();
        DASSendAuthenticResultParser dasSendAuthenticResultParser = new DASSendAuthenticResultParser();
        DASWorkModelParser dasWorkModelParser = new DASWorkModelParser();
        DataMessageModelParser dataMessageModelParser = new DataMessageModelParser();
        DataReportIntervalParser dataReportIntervalParser = new DataReportIntervalParser();
        DigitalOsmometerFunctionParser digitalOsmometerFunctionParser = new DigitalOsmometerFunctionParser();
        GetAllSensorConfigParser getAllSensorConfigParser = new GetAllSensorConfigParser();
        HeartbeatSendIntervalParser heartbeatSendIntervalParser = new HeartbeatSendIntervalParser();
        InstantCollectionParser instantCollectionParser = new InstantCollectionParser();
        LocalTimeParser localTimeParser = new LocalTimeParser();
        QueryOsmometerParameterParser queryOsmometerParameterParser = new QueryOsmometerParameterParser();
        RainStationParser rainStationParser = new RainStationParser();
        RebootDeviceParser rebootDeviceParser = new RebootDeviceParser();
        RestoreFactorySettingParser restoreFactorySettingParser = new RestoreFactorySettingParser();
        SaveConfigInfoParser saveConfigInfoParser = new SaveConfigInfoParser();
        SensorBaudRateParser sensorBaudRateParser = new SensorBaudRateParser();
        SensorInterfaceTypeParser sensorInterfaceTypeParser = new SensorInterfaceTypeParser();
        ServerAddressParser serverAddressParser = new ServerAddressParser();
        SetAuthorzePhoneParser setAuthorzePhoneParser = new SetAuthorzePhoneParser();
        SetCollectorAddressParser setCollectorAddressParser = new SetCollectorAddressParser();
        SetCollectorSensorParser setCollectorSensorParser = new SetCollectorSensorParser();
        SetCordLenghtParser setCordLenghtParser = new SetCordLenghtParser();
        SetGPRSOnlineTimeParser setGPRSOnlineTimeParser = new SetGPRSOnlineTimeParser();
        SetInclinometerLongParser setInclinometerLongParser = new SetInclinometerLongParser();
        SetOsmometerAddressParser setOsmometerAddressParser = new SetOsmometerAddressParser();
        SetOsmometerCorrectparser setOsmometerCorrectparser = new SetOsmometerCorrectparser();
        SetOsmometerTriggerParser setOsmometerTriggerParser = new SetOsmometerTriggerParser();
        SetServerAddressPortParser setServerAddressPortParser = new SetServerAddressPortParser();
        SettingGPSPositionParser settingGPSPositionParser = new SettingGPSPositionParser();
        SettingRainPrecisionParser settingRainPrecisionParser = new SettingRainPrecisionParser();
        SettingRemoteUpgradeParser settingRemoteUpgradeParser = new SettingRemoteUpgradeParser();
        SixTargerBDNumberParser sixTargerBDNumberParser = new SixTargerBDNumberParser();
        SystemRunStateParser systemRunStateParser = new SystemRunStateParser();
        VersionMessageParser versionMessageParser = new VersionMessageParser();
        VibratingSensorParameterParser vibratingSensorParameterParser = new VibratingSensorParameterParser();
        DeviceLockStatusParser deviceLockStatusParser = new DeviceLockStatusParser();
        BreakAlarmStatusParser breakAlarmStatusParser = new BreakAlarmStatusParser();

        parserMap.put(authenticationConfigParser.commandType(), authenticationConfigParser);
        parserMap.put(authorizePhoneNumberParser.commandType(), authorizePhoneNumberParser);
        parserMap.put(baseConfigParser.commandType(), baseConfigParser);
        parserMap.put(batchResultParser.commandType(),batchResultParser);
        parserMap.put(cellProtectionVoltageParser.commandType(),cellProtectionVoltageParser);
        parserMap.put(collectorConfigParser.commandType(),collectorConfigParser);
        parserMap.put(collectorFrequencyParser.commandType(),collectorFrequencyParser);
        parserMap.put(collectorSensorParamsParser.commandType(),collectorSensorParamsParser);
        parserMap.put(collectorSensorRevisedParser.commandType(),collectorSensorRevisedParser);
        parserMap.put(collectorSensorThresholdParser.commandType(),collectorSensorThresholdParser);
        parserMap.put(collectorSensorThresholdSoliParser.commandType(),collectorSensorThresholdSoliParser);
        parserMap.put(collectorSolutionFrequencyParser.commandType(),collectorSolutionFrequencyParser);
        parserMap.put(collectorStandbyTimeParser.commandType(),collectorStandbyTimeParser);
        parserMap.put(dasSendAuthenticRequestParser.commandType(),dasSendAuthenticRequestParser);
        parserMap.put(dasSendAuthenticResultParser.commandType(),dasSendAuthenticResultParser);
        parserMap.put(dasWorkModelParser.commandType(),dasWorkModelParser);
        parserMap.put(dataMessageModelParser.commandType(),dataMessageModelParser);
        parserMap.put(dataReportIntervalParser.commandType(),dataReportIntervalParser);
        parserMap.put(digitalOsmometerFunctionParser.commandType(),digitalOsmometerFunctionParser);
        parserMap.put(getAllSensorConfigParser.commandType(),getAllSensorConfigParser);
        parserMap.put(heartbeatSendIntervalParser.commandType(),heartbeatSendIntervalParser);
        parserMap.put(instantCollectionParser.commandType(),instantCollectionParser);
        parserMap.put(localTimeParser.commandType(),localTimeParser);
        parserMap.put(queryOsmometerParameterParser.commandType(),queryOsmometerParameterParser);
        parserMap.put(rainStationParser.commandType(),rainStationParser);
        parserMap.put(rebootDeviceParser.commandType(),rebootDeviceParser);
        parserMap.put(restoreFactorySettingParser.commandType(),restoreFactorySettingParser);
        parserMap.put(saveConfigInfoParser.commandType(),saveConfigInfoParser);
        parserMap.put(sensorBaudRateParser.commandType(),sensorBaudRateParser);
        parserMap.put(sensorInterfaceTypeParser.commandType(),sensorInterfaceTypeParser);
        parserMap.put(serverAddressParser.commandType(),serverAddressParser);
        parserMap.put(setAuthorzePhoneParser.commandType(),setAuthorzePhoneParser);
        parserMap.put(setCollectorAddressParser.commandType(),setCollectorAddressParser);
        parserMap.put(setCollectorSensorParser.commandType(),setCollectorSensorParser);
        parserMap.put(setCordLenghtParser.commandType(),setCordLenghtParser);
        parserMap.put(setGPRSOnlineTimeParser.commandType(),setGPRSOnlineTimeParser);
        parserMap.put(setInclinometerLongParser.commandType(),setInclinometerLongParser);
        parserMap.put(setOsmometerAddressParser.commandType(),setOsmometerAddressParser);
        parserMap.put(setOsmometerCorrectparser.commandType(),setOsmometerCorrectparser);
        parserMap.put(setOsmometerTriggerParser.commandType(),setOsmometerTriggerParser);
        parserMap.put(setServerAddressPortParser.commandType(),setServerAddressPortParser);
        parserMap.put(settingGPSPositionParser.commandType(),settingGPSPositionParser);
        parserMap.put(settingRainPrecisionParser.commandType(),settingRainPrecisionParser);
        parserMap.put(settingRemoteUpgradeParser.commandType(),settingRemoteUpgradeParser);
        parserMap.put(sixTargerBDNumberParser.commandType(),sixTargerBDNumberParser);
        parserMap.put(systemRunStateParser.commandType(),systemRunStateParser);
        parserMap.put(versionMessageParser.commandType(),versionMessageParser);
        parserMap.put(vibratingSensorParameterParser.commandType(),vibratingSensorParameterParser);
        parserMap.put(deviceLockStatusParser.commandType(),deviceLockStatusParser);
        parserMap.put(breakAlarmStatusParser.commandType(),breakAlarmStatusParser);

    }

    public void registerWithClass(List<Class> classes) {
        try {
            for (int i = 0; i < classes.size(); ++i) {
                ResultParser resultParser = (ResultParser) classes.get(i).newInstance();
                parserMap.put(resultParser.commandType(), resultParser);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ParseManager getInstance() {
        if(instance.parserMap==null||instance.parserMap.size()<=0)
        {
            SettingRainPrecisionParser temp=new SettingRainPrecisionParser();
            instance.registerParse();
        }
        return instance;
    }
}
