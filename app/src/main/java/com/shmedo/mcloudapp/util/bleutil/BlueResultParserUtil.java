package com.shmedo.mcloudapp.util.bleutil;

import android.util.Log;

import com.shmedo.das.common.BaseConfigInfo;
import com.shmedo.das.common.BreakAlarmStatusInfo;
import com.shmedo.das.common.CollectorConfigInfo;
import com.shmedo.das.common.CollectorSensorParamsInfo;
import com.shmedo.das.common.DeviceLockStatusInfo;
import com.shmedo.das.common.DigitalOsmometerFunctionInfo;
import com.shmedo.das.common.GetAllSensorConfigInfo;
import com.shmedo.das.common.QueryOsmometerParameterInfo;
import com.shmedo.das.common.RainStationInfo;
import com.shmedo.das.common.RebootDeviceInfo;
import com.shmedo.das.common.SettingRainPrecisionInfo;
import com.shmedo.das.common.SystemRunStateInfo;
import com.shmedo.das.common.VersionMessageInfo;
import com.shmedo.das.common.enumerate.CollectorModel;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.das.das.cmd.parser.GetAllSensorConfigParser;
import com.shmedo.das.das.cmd.parser.ParseManager;
import com.shmedo.mcloudapp.entity.ble.BaseConfigInfoSub;
import com.shmedo.mcloudapp.entity.ble.BreakAlarmStatusSub;
import com.shmedo.mcloudapp.entity.ble.CollectorInfoSub;
import com.shmedo.mcloudapp.entity.ble.DeviceLockStatusSub;
import com.shmedo.mcloudapp.entity.ble.DigitalOsmometerFunctionSub;
import com.shmedo.mcloudapp.entity.ble.QueryOsmometerParameterSubInfo;
import com.shmedo.mcloudapp.entity.ble.RainStationSub;
import com.shmedo.mcloudapp.entity.ble.RebootDeviceSub;
import com.shmedo.mcloudapp.entity.ble.SettingRainPrecisionSub;
import com.shmedo.mcloudapp.entity.ble.SystemRunStateSub;
import com.shmedo.mcloudapp.entity.ble.VersionMessageSub;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.bleutil
 * 文件名:   BlueResultParserUtil
 * 创建者:   dpc
 * 创建时间:  2019/3/7 13:39
 * 描述：   蓝牙交互数据解析工具类
 */
public class BlueResultParserUtil {


    /**
     * 解析基础配置信息
     *
     * @param result
     * @return
     */
    public static BaseConfigInfoSub getBaseConfig(String result) {
        CommandResult<BaseConfigInfo> baseBean = ParseManager.getInstance().parse(result);
        BaseConfigInfo info = null;
        BaseConfigInfoSub infoSub = new BaseConfigInfoSub();
        if (baseBean.isSuccess()) {
            info = baseBean.getResult();
        } else {
            Log.d(LogTag.INFO_TAG, baseBean.getMessage());
            return infoSub;
        }

        return setBaseConfiginfoSub(info, infoSub);
    }

    /**
     * 获取基础配置信息
     *
     * @param info
     * @param infoSub
     */
    public static BaseConfigInfoSub setBaseConfiginfoSub(BaseConfigInfo info, BaseConfigInfoSub
            infoSub) {
        infoSub.setToken(info.getToken());//设备序列号
        switch (info.getEquipmentStatus()) {
            case STANDBY:
                infoSub.setEquipmentStatus("待机");
                break;
            case ACTIVATION:
                infoSub.setEquipmentStatus("激活");
                break;
            default:
                break;
        }
        switch (info.getRainfallStation()) {
            case RAIN_OPEN:
                infoSub.setRainfallStation("开启");
                break;
            case RAIN_CLOSE:
                infoSub.setRainfallStation("关闭");
                break;
        }
        switch (info.getDebugModel()) {
            case INITIALZE:
                infoSub.setDebugModel("初始化");
                break;
            case CLOSE:
                infoSub.setDebugModel("关闭");
                break;
            case INFO:
                infoSub.setDebugModel("INFO");
                break;
            case DEBUG:
                infoSub.setDebugModel("DEBUG");
                break;
        }
        infoSub.setCollectorModel(info.getCollectorModel().name());
        infoSub.setDataReportInterval(String.valueOf(info.getDataReportInterval()));
        infoSub.setDataCommunicateMode(info.getDataCommunicateMode().name());
        infoSub.setSensorInterfaceType(info.getSensorInterfaceType().name());
        infoSub.setSimChoose(info.getSIMChoose().name());
        switch (info.getSIMChoose()) {
            case SIM1:
                infoSub.setSimChoose("选择sim卡1");
                break;
            case SIM2:
                infoSub.setSimChoose("选择sim卡2");
                break;
        }
        return infoSub;
    }

    /**
     * 解析系统运行状态信息
     *
     * @param result
     * @return
     */
    public static SystemRunStateSub getSystemRunState(String result) {
        CommandResult<SystemRunStateInfo> singalBean = ParseManager.getInstance().parse(result);
        SystemRunStateInfo stateInfo = null;
        SystemRunStateSub stateInfoSub = new SystemRunStateSub();
        if (singalBean.isSuccess()) {
            stateInfo = singalBean.getResult();
        } else {
            Log.i("adu", singalBean.getMessage());
            return stateInfoSub;
        }
        return setStateInfoSub(stateInfo, stateInfoSub);
    }

    /**
     * 获取系统运行状态信息
     *
     * @param stateInfo
     * @param stateInfoSub
     */
    public static SystemRunStateSub setStateInfoSub(SystemRunStateInfo stateInfo, SystemRunStateSub stateInfoSub) {
        stateInfoSub.setGprsSignal(stateInfo.getGprsSignal());
        stateInfoSub.setBatteryVoltage(stateInfo.getBatteryVoltage());
        stateInfoSub.setSimCCID(stateInfo.getSimCCID().equals("") ? "无" : stateInfo.getSimCCID());
        if (stateInfo.getOperator().equals("")) {
            stateInfoSub.setOperator("未插卡");
        } else {
            switch (stateInfo.getOperator()) {
                case "CMCC":
                    stateInfoSub.setOperator("中国移动");
                    break;
                case "CU":
                    stateInfoSub.setOperator("中国联通");
                    break;
                case "CT":
                    stateInfoSub.setOperator("中国电信");
                    break;
                default:
                    break;
            }
        }
        return stateInfoSub;
    }

    /**
     * 解析渗压计信息
     *
     * @param result
     * @return
     */
    public static QueryOsmometerParameterSubInfo getQueryOsmometerParameter(String result) {

        CommandResult<QueryOsmometerParameterInfo> shenyaBean = ParseManager.getInstance().parse(result);
        QueryOsmometerParameterInfo digtalInfo = null;
        QueryOsmometerParameterSubInfo digitalOsFuncSubInfo = new QueryOsmometerParameterSubInfo();
        if (shenyaBean.isSuccess()) {
            digtalInfo = shenyaBean.getResult();
            digitalOsFuncSubInfo.setDepthCorrect(digtalInfo.getDepthCorrect());
            digitalOsFuncSubInfo.setDepthTrigger(digtalInfo.getDepthTrigger());
            digitalOsFuncSubInfo.setOsmometerAddress(digtalInfo.getOsmometerAddress());
            digitalOsFuncSubInfo.setSyCordLength(String.valueOf(digtalInfo.getCordLenght()));
            digitalOsFuncSubInfo.setTemperatureCorrect(digtalInfo.getDepthCorrect());
            digitalOsFuncSubInfo.setTemperatureTrigger(digtalInfo.getTemperatureTrigger());
            digitalOsFuncSubInfo.setTemperatureCorrect(digtalInfo.getTemperatureCorrect());

        } else {
            Log.i(LogTag.INFO_TAG, "解析渗压计信息=" + shenyaBean.getMessage());
            return digitalOsFuncSubInfo;
        }
        return setDigtalInfo(digtalInfo, digitalOsFuncSubInfo);
    }

    /**
     * 获取渗压计状态信息
     *
     * @param digtalInfo
     * @param digitalOsFuncSubInfo
     */
    public static QueryOsmometerParameterSubInfo setDigtalInfo(QueryOsmometerParameterInfo digtalInfo, QueryOsmometerParameterSubInfo digitalOsFuncSubInfo) {
        switch (digtalInfo.getOsmometerStatus()) {
            case OSMOMETER_OPEN:
                digitalOsFuncSubInfo.setOsmometerStatus("开启");
                break;
            case OSMOMETER_CLOSE:
                digitalOsFuncSubInfo.setOsmometerStatus("关闭");
                break;
        }
        return digitalOsFuncSubInfo;
    }

    /**
     * 解析版本信息
     *
     * @param result
     * @return
     */
    public static VersionMessageSub getVersionMessage(String result) {
        CommandResult<VersionMessageInfo> versionBean = ParseManager.getInstance().parse(result);
        VersionMessageInfo versionInfo = null;
        VersionMessageSub versionSub = new VersionMessageSub();
        if (versionBean.isSuccess()) {
            versionInfo = versionBean.getResult();
        } else {
            Log.i("adu", versionBean.getMessage());
            return versionSub;
        }
        return setVersionInfo(versionInfo, versionSub);
    }

    /**
     * 获取版本信息
     *
     * @param versionInfo
     * @param versionSub
     */
    public static VersionMessageSub setVersionInfo(VersionMessageInfo versionInfo, VersionMessageSub versionSub) {
        versionSub.setFirmwareVersion(versionInfo.getFirmwareVersion());
        versionSub.setProduceDate(versionInfo.getProduceDate());
        versionSub.setProductID(versionInfo.getProductID());
        return versionSub;
    }

    /**
     * 解析所有配置信息
     *
     * @param result
     * @return
     */
    public static GetAllSensorConfigInfo getAllBlueMessage(String result) {
        GetAllSensorConfigParser allParser = new GetAllSensorConfigParser();
        GetAllSensorConfigInfo allInfo = allParser.parse(result);
        BaseConfigInfoSub infoSub = new BaseConfigInfoSub();
        setBaseConfiginfoSub(allInfo.getBaseConfig(), infoSub);
        return allInfo;
    }

    /**
     * 解析获取采集器的信息
     *
     * @param result
     * @return
     */
    public static CollectorInfoSub getCollectorInfos(GetAllSensorConfigInfo result) {
        CollectorInfoSub collectorInfoSub = new CollectorInfoSub();
        collectorInfoSub.setCollectorAddress(result.getCollectorConfig().getCollectorAddress());
        collectorInfoSub.setStandbyTime(result.getCollectorConfig().getStandbyTime());
        collectorInfoSub.setWorkTime(result.getCollectorConfig().getWorkTime());
        collectorInfoSub.setCollectorInterval(result.getCollectorConfig().getCollectorInterval());
        return collectorInfoSub;
    }

    public static CollectorInfoSub getCollectorInfo(String result) {
        CollectorInfoSub collectorInfoSub = new CollectorInfoSub();
        CommandResult<CollectorConfigInfo> commandResult = ParseManager.getInstance().parse(result);
        CollectorConfigInfo collectorConfigInfo = null;

        if (commandResult.isSuccess()) {
            collectorConfigInfo = commandResult.getResult();
            return setCollectorInfo(collectorConfigInfo, collectorInfoSub);

        } else {

            return collectorInfoSub;
        }
    }


    private static CollectorInfoSub setCollectorInfo(CollectorConfigInfo collectorConfigInfo, CollectorInfoSub collectorInfoSub) {
        collectorInfoSub.setCollectorAddress(collectorConfigInfo.getCollectorAddress());
        collectorInfoSub.setStandbyTime(collectorConfigInfo.getStandbyTime());
        collectorInfoSub.setWorkTime(collectorConfigInfo.getWorkTime());
        collectorInfoSub.setCollectorInterval(collectorConfigInfo.getCollectorInterval());
        collectorInfoSub.setAccessSum(collectorConfigInfo.getAccessSum());
        return collectorInfoSub;
    }


    /**
     * 解析所有配置信息中的基础信息
     *
     * @param result
     * @return
     */
    public static BaseConfigInfoSub getBasicFromAllBlueMessage(String result) {
        GetAllSensorConfigParser allParser = new GetAllSensorConfigParser();
        GetAllSensorConfigInfo allInfo = allParser.parse(result);
        BaseConfigInfoSub infoSub = new BaseConfigInfoSub();
        setBaseConfiginfoSub(allInfo.getBaseConfig(), infoSub);
        infoSub.setServerAddressOne(allInfo.getServerAddressPortInfo1().getAddress() + " " +
                allInfo.getServerAddressPortInfo1().getPort());
        infoSub.setServerAddressTwo(allInfo.getServerAddressPortInfo2().getAddress() + " " +
                allInfo.getServerAddressPortInfo2().getPort());
        return infoSub;
    }

    /**
     * 获取重启设备信息
     *
     * @param result
     * @return
     */
    public static RebootDeviceSub getRebootDeviceMessage(String result) {
        CommandResult<RebootDeviceInfo> bean = ParseManager.getInstance().parse(result);
        RebootDeviceInfo info = null;
        RebootDeviceSub deviceSub = new RebootDeviceSub();
        if (bean.isSuccess()) {
            info = bean.getResult();
        } else {
            return deviceSub;
        }
        return setRebootDeviceInfo(info, deviceSub);
    }

    /**
     * 得到设置激活设备等待的时间
     *
     * @param info
     * @param deviceSub
     * @return
     */
    private static RebootDeviceSub setRebootDeviceInfo(RebootDeviceInfo info, RebootDeviceSub deviceSub) {
        deviceSub.setTime(info.getTime());
        return deviceSub;
    }

    /**
     * 获取断线报警器状态
     *
     * @param result
     * @return
     */
    public static BreakAlarmStatusSub getBreakAlarmStatus(String result) {
        CommandResult<BreakAlarmStatusInfo> bean = ParseManager.getInstance().parse(result);
        BreakAlarmStatusInfo info = null;
        BreakAlarmStatusSub statusSub = new BreakAlarmStatusSub();
        if (bean.isSuccess()) {
            info = bean.getResult();
        } else {
            return statusSub;
        }
        return setBreakAlarmInfo(info, statusSub);
    }

    /**
     * 设置断线报警器状态
     *
     * @param info
     * @param statusSub
     * @return
     */
    private static BreakAlarmStatusSub setBreakAlarmInfo(BreakAlarmStatusInfo info, BreakAlarmStatusSub statusSub) {
        switch (info.getStatus()) {
            case OPEN:
                statusSub.setAlarmStatus(1);
                break;
            case CLOSE:
                statusSub.setAlarmStatus(2);
                break;
        }
        return statusSub;
    }


    /**
     * 获取雨量计开关
     *
     * @param result
     * @return
     */
    public static RainStationSub getRainStationInfo(String result) {
        CommandResult<RainStationInfo> bean = ParseManager.getInstance().parse(result);
        RainStationInfo info = null;
        RainStationSub rainStationSub = new RainStationSub();
        if (bean.isSuccess()) {
            info = bean.getResult();
        } else {
            return rainStationSub;
        }
        return setRainStationInfo(info, rainStationSub);
    }

    /**
     * 设置雨量计开关
     *
     * @param info
     * @param rainStationSub
     * @return
     */
    private static RainStationSub setRainStationInfo(RainStationInfo info, RainStationSub rainStationSub) {
        switch (info.getRainStation()) {
            case OPEN: {
                rainStationSub.setRainStation("1");
                break;
            }
            case CLOSE: {
                rainStationSub.setRainStation("2");
                break;
            }
            case ALARM_OPEN: {
                rainStationSub.setRainStation("3");
                break;
            }
        }
        return rainStationSub;
    }


    /**
     * 设置雨量站精度
     *
     * @param result
     * @return
     */
    public static SettingRainPrecisionSub getRainPrecisionInfo(String result) {
        CommandResult<SettingRainPrecisionInfo> bean = ParseManager.getInstance().parse(result);
        SettingRainPrecisionInfo info = null;
        SettingRainPrecisionSub rainPrecisionSub = new SettingRainPrecisionSub();
        if (bean.isSuccess()) {
            info = bean.getResult();
        } else {
            return rainPrecisionSub;
        }
        return setRainPrecisionInfo(info, rainPrecisionSub);
    }

    private static SettingRainPrecisionSub setRainPrecisionInfo(SettingRainPrecisionInfo info, SettingRainPrecisionSub rainPrecisionSub) {
        rainPrecisionSub.setPrecision(info.getPrecision());
        return rainPrecisionSub;
    }


    /**
     * 开启、关闭 数字渗压计功能
     *
     * @param result
     * @return
     */
    public static DigitalOsmometerFunctionSub getOsmoeterFunctionInfo(String result) {
        CommandResult<DigitalOsmometerFunctionInfo> bean = ParseManager.getInstance().parse(result);
        DigitalOsmometerFunctionInfo info = null;
        DigitalOsmometerFunctionSub functionSub = new DigitalOsmometerFunctionSub();
        if (bean.isSuccess()) {
            info = bean.getResult();
        } else {
            return functionSub;
        }
        return setOsmoeterFunctionInfo(info, functionSub);
    }

    private static DigitalOsmometerFunctionSub setOsmoeterFunctionInfo(DigitalOsmometerFunctionInfo info, DigitalOsmometerFunctionSub functionSub) {
        switch (info.getOsmometerStatus()) {
            case OSMOMETER_OPEN:
                functionSub.setOsmometerStatus(1);
                break;
            case OSMOMETER_CLOSE:
                functionSub.setOsmometerStatus(2);
                break;
        }
        return functionSub;
    }

    //设备锁状态&&2250
    public static DeviceLockStatusSub getDeviceLockStatusInfo(String result) {
        CommandResult<DeviceLockStatusInfo> bean = ParseManager.getInstance().parse(result);
        DeviceLockStatusInfo info = null;
        DeviceLockStatusSub statusSub = new DeviceLockStatusSub();
        if (bean.isSuccess()) {
            info = bean.getResult();
        } else {
            return statusSub;
        }
        return setDeviceLockStatusInfo(info, statusSub);
    }


    private static DeviceLockStatusSub setDeviceLockStatusInfo(DeviceLockStatusInfo info, DeviceLockStatusSub statusSub) {
        switch (info.getStatus()) {
            case UNLOCK:
                statusSub.setLockStatus(0);
                break;
            case LOCK:
                statusSub.setLockStatus(1);
                break;
        }
        return statusSub;
    }

    //"$$1010200,2,2,10,0.000000\r\n";
    public static CollectorSensorParamsInfoSub setCollectorParams(String result) {
        CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
        CommandResult<CollectorSensorParamsInfo> bean = ParseManager.getInstance().parse(result);
        CollectorSensorParamsInfo collectorSensorParamsInfo = null;

        if (bean.isSuccess()) {
            collectorSensorParamsInfo = bean.getResult();
            collectorSensorParamsInfoSub.setSensorAddress(collectorSensorParamsInfo.getSensorAddress());
            collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfo.getSensorData());
            collectorSensorParamsInfoSub.setChannelNumber(collectorSensorParamsInfo.getChannelNumber());
            collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfo.getCollectorModel().toString());

            return setCollectorSeneorInfo(collectorSensorParamsInfo, collectorSensorParamsInfoSub);
        } else {

            return collectorSensorParamsInfoSub;
        }
    }


    /**
     * 设置传感器类型
     */
    private static CollectorSensorParamsInfoSub setCollectorSeneorInfo(CollectorSensorParamsInfo info, CollectorSensorParamsInfoSub infoSub) {
        switch (info.getSensorType()) {
            case WIRE_SHIFT:
                infoSub.setSensorType("02");
                break;

            case SOIL_MOISTURE:
                infoSub.setSensorType("03");
                break;

            case INCLINOMETER:
                infoSub.setSensorType("04");
                break;

            case ULTRASONIC_LEVEL_GAUGE:
                infoSub.setSensorType("06");
                break;

            case RADAR_LEVEL_GAUGE:
                infoSub.setSensorType("07");
                break;

            case MOISTURE_METER:
                infoSub.setSensorType("08");
                break;

            case TEMPERATURE_HUMIDITY_METER:
                infoSub.setSensorType("12");
                break;

            case UPLIFT_PRESSURE_GAUGE:
                infoSub.setSensorType("15");
                break;

            case KANG_PERCOLATE:
                infoSub.setSensorType("50");
                break;

            case GUDAN_PERCOLATE:
                infoSub.setSensorType("51");
                break;

            case GUDAN_SOIL_PRESSURE:
                infoSub.setSensorType("52");
                break;

            case GUDAN_STRESS:
                infoSub.setSensorType("53");
                break;

            case GUDAN_NOT_STRESS:
                infoSub.setSensorType("54");
                break;

            case GUDAN_DISPLACEMENT_METER:
                infoSub.setSensorType("55");
                break;

            case INFRASOUND_SENSOR:
                infoSub.setSensorType("21");
                break;

            default:
                infoSub.setSensorType("00");//未知的传感器类型
        }

        return infoSub;
    }


    /**
     * 解析采集器的信息
     *
     * @param result
     * @return
     */
    public static CollectorConfigInfo getCollectorConfigInfo(String result) {
        CommandResult<CollectorConfigInfo> commandResult = ParseManager.getInstance().parse(result);
        CollectorConfigInfo collectorConfigInfo = null;
        if (commandResult.isSuccess()) {
            collectorConfigInfo = commandResult.getResult();
        } else {
            collectorConfigInfo = new CollectorConfigInfo();
        }

        return collectorConfigInfo;
    }

    /**
     * 解析渗压计信息
     *
     * @param result
     * @return
     */
    public static QueryOsmometerParameterInfo getQueryOsmometerParameterInfo(String result) {
        CommandResult<QueryOsmometerParameterInfo> shenyaBean = ParseManager.getInstance().parse(result);
        QueryOsmometerParameterInfo digtalInfo = null;
        if (shenyaBean.isSuccess()) {
            digtalInfo = shenyaBean.getResult();
        } else {
            digtalInfo = new QueryOsmometerParameterInfo();
        }

        return digtalInfo;
    }

    public static String getCollectorName(CollectorModel collectorModel) {
        String collectorName = "";
        switch (collectorModel) {
            case VW08:
                collectorName = "采集器";
                break;

            case DS08:
                collectorName = "裂缝计采集器";
                break;

            case HD08:
                collectorName = "土壤湿度采集器";
                break;

            case CX08:
                collectorName = "测斜仪采集器";
                break;

            case UDS08:
                collectorName = "超声波采集器";
                break;

            case RD08:
                collectorName = "雷达采集器";
                break;

            case SMC08:
                collectorName = "墒情采集器";
                break;

            case TH08:
                collectorName = "温湿度采集器";
                break;

            case DVWP:
                collectorName = "数字式渗压计采集器";
                break;

            case QJY08:
                collectorName = "倾角仪采集器";
                break;

            case CS08:
                collectorName = "次声采集器";
                break;

            case VW01:
                collectorName = "单通道采集器";
                break;

            default:
                break;
        }

        return collectorName;
    }
}
