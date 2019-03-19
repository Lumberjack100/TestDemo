package com.shmedo.mcloudapp.util.bleutil;

import android.util.Log;
import com.shmedo.das.common.BaseConfigInfo;
import com.shmedo.das.common.GetAllSensorConfigInfo;
import com.shmedo.das.common.QueryOsmometerParameterInfo;
import com.shmedo.das.common.SystemRunStateInfo;
import com.shmedo.das.common.VersionMessageInfo;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.das.das.cmd.parser.GetAllSensorConfigParser;
import com.shmedo.das.das.cmd.parser.ParseManager;
import com.shmedo.mcloudapp.entity.ble.BaseConfigInfoSub;
import com.shmedo.mcloudapp.entity.ble.CollectorInfoSub;
import com.shmedo.mcloudapp.entity.ble.QueryOsmometerParameterSubInfo;
import com.shmedo.mcloudapp.entity.ble.SystemRunStateSub;
import com.shmedo.mcloudapp.entity.ble.VersionMessageSub;

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

        return  setBaseConfiginfoSub(info,infoSub);
    }

    /**
     * 获取基础配置信息
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
        switch (info.getRainfallStation()){
            case RAIN_OPEN:
                infoSub.setRainfallStation("开启");
                break;
            case RAIN_CLOSE:
                infoSub.setRainfallStation("关闭");
                break;
        }
        infoSub.setDebugModel(info.getDebugModel().name());
        infoSub.setCollectorModel(info.getCollectorModel().name());
        infoSub.setDataReportInterval(String.valueOf(info.getDataReportInterval()));
        infoSub.setDataCommunicateMode(info.getDataCommunicateMode().name());
        infoSub.setSensorInterfaceType(info.getSensorInterfaceType().name());
        infoSub.setSimChoose(info.getSIMChoose().name());
        switch (info.getSIMChoose()){
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
        return setStateInfoSub(stateInfo,stateInfoSub);
    }
    /**
     * 获取系统运行状态信息
     * @param stateInfo
     * @param stateInfoSub
     */
    public static SystemRunStateSub setStateInfoSub(SystemRunStateInfo stateInfo, SystemRunStateSub stateInfoSub) {
        stateInfoSub.setGprsSignal(stateInfo.getGprsSignal());
        stateInfoSub.setBatteryVoltage(stateInfo.getBatteryVoltage());
        stateInfoSub.setSimCCID(stateInfo.getSimCCID().equals("")?"无":stateInfo.getSimCCID());
        if (stateInfo.getOperator().equals("")){
            stateInfoSub.setOperator("未插卡");
        }else {
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
     * @param result
     * @return
     */
    public static QueryOsmometerParameterSubInfo getQueryOsmometerParameter(String result) {
        CommandResult<QueryOsmometerParameterInfo> shenyaBean = ParseManager.getInstance().parse(result);
        QueryOsmometerParameterInfo digtalInfo = null;
        QueryOsmometerParameterSubInfo digitalOsFuncSubInfo=new QueryOsmometerParameterSubInfo();
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
            Log.i("adu", shenyaBean.getMessage());
            return digitalOsFuncSubInfo;
        }
        return  setDigtalInfo(digtalInfo, digitalOsFuncSubInfo);
    }
    /**
     * 获取渗压计状态信息
     * @param digtalInfo
     * @param digitalOsFuncSubInfo
     */
    public static QueryOsmometerParameterSubInfo setDigtalInfo(QueryOsmometerParameterInfo digtalInfo, QueryOsmometerParameterSubInfo digitalOsFuncSubInfo) {
        switch (digtalInfo.getOsmometerStatus()){
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
        return  setVersionInfo(versionInfo, versionSub);
    }
    /**
     * 获取版本信息
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
     * @param result
     * @return
     */
    public static GetAllSensorConfigInfo getAllBlueMessage(String result) {
        GetAllSensorConfigParser allParser = new GetAllSensorConfigParser();
        GetAllSensorConfigInfo allInfo = allParser.parse(result);
        BaseConfigInfoSub infoSub = new BaseConfigInfoSub();
        setBaseConfiginfoSub(allInfo.getBaseConfig(),infoSub);
        return  allInfo;
    }

    /**
     * 解析获取采集器的信息
     * @param result
     * @return
     */
    public static CollectorInfoSub getCollectorInfo(GetAllSensorConfigInfo result) {
        CollectorInfoSub collectorInfoSub = new CollectorInfoSub();
        collectorInfoSub.setCollectorAddress(result.getCollectorConfig().getCollectorAddress());
        collectorInfoSub.setStandbyTime(result.getCollectorConfig().getStandbyTime());
        collectorInfoSub.setWorkTime(result.getCollectorConfig().getWorkTime());
        collectorInfoSub.setCollectorInterval(result.getCollectorConfig().getCollectorInterval());
        return collectorInfoSub;
    }

    /**
     * 解析所有配置信息中的基础信息
     * @param result
     * @return
     */
    public static BaseConfigInfoSub getBasicFromAllBlueMessage(String result) {
        GetAllSensorConfigParser allParser = new GetAllSensorConfigParser();
        GetAllSensorConfigInfo allInfo = allParser.parse(result);
        BaseConfigInfoSub infoSub = new BaseConfigInfoSub();
        setBaseConfiginfoSub(allInfo.getBaseConfig(),infoSub);
        infoSub.setServerAddressOne(allInfo.getServerAddressPortInfo1().getAddress()+" "+
            allInfo.getServerAddressPortInfo1().getPort());
        infoSub.setServerAddressTwo(allInfo.getServerAddressPortInfo2().getAddress()+" "+
            allInfo.getServerAddressPortInfo2().getPort());
        return  infoSub;
    }
}
