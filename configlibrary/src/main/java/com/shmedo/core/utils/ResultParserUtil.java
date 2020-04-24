package com.shmedo.core.utils;

import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.parser.ParseManager;
import com.shmedo.core.model.BreakAlarmStatusInfo;
import com.shmedo.core.model.DeviceStatusInfoOne;
import com.shmedo.core.model.DeviceStatusInfoThree;
import com.shmedo.core.model.DeviceStatusInfoTwo;
import com.shmedo.core.model.GetAllSensorConfigInfo;
import com.shmedo.core.model.QueryOsmometerParameterInfo;
import com.shmedo.core.model.VersionMessageInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    获取数据解析后生成的实体对象工具类
 */
public class ResultParserUtil {
    /**
     * 获取所有配置信息
     *
     * @param result
     * @return
     */
    public static GetAllSensorConfigInfo getAllConfigInfo(String result) {
        CommandResult<GetAllSensorConfigInfo> commandResult = ParseManager.getInstance().parse(result);
        GetAllSensorConfigInfo allInfo = new GetAllSensorConfigInfo();
        if (commandResult.isSuccess()) {
            allInfo = commandResult.getResult();
        }

        return allInfo;
    }

    /**
     * 获取版本信息
     *
     * @param result
     * @return
     */
    public static VersionMessageInfo getVersionMessage(String result) {
        CommandResult<VersionMessageInfo> commandResult = ParseManager.getInstance().parse(result);
        VersionMessageInfo versionInfo = new VersionMessageInfo();
        if (commandResult.isSuccess()) {
            versionInfo = commandResult.getResult();
        }

        return versionInfo;
    }

    /**
     * 获取渗压计信息
     *
     * @param result
     * @return
     */
    public static QueryOsmometerParameterInfo getQueryOsmometerParameterInfo(String result) {
        CommandResult<QueryOsmometerParameterInfo> commandResult = ParseManager.getInstance().parse(result);
        QueryOsmometerParameterInfo digtalInfo = new QueryOsmometerParameterInfo();
        if (commandResult.isSuccess()) {
            digtalInfo = commandResult.getResult();
        }

        return digtalInfo;
    }

    /**
     * 获取断线报警器状态
     *
     * @param result
     * @return
     */
    public static BreakAlarmStatusInfo getBreakAlarmStatus(String result) {
        CommandResult<BreakAlarmStatusInfo> commandResult = ParseManager.getInstance().parse(result);
        BreakAlarmStatusInfo info = new BreakAlarmStatusInfo();
        if (commandResult.isSuccess()) {
            info = commandResult.getResult();
        }

        return info;
    }

    /**
     * 获取安装位置
     *
     * @param result
     * @return
     */
    public static String getInstallPosition(String result) {
        String info = "";
        CommandResult<String> commandResult = ParseManager.getInstance().parse(result);
        if (commandResult.isSuccess()) {
            info = commandResult.getResult();
        }

        return info;
    }

    /**
     * 获取设备运行状态信息
     *
     * @param result
     * @return
     */
    public static DeviceStatusInfoOne getDeviceStatusOne(String result) {
        CommandResult<DeviceStatusInfoOne> commandResult = ParseManager.getInstance().parse(result);
        DeviceStatusInfoOne info = new DeviceStatusInfoOne();
        if (commandResult.isSuccess()) {
            info = commandResult.getResult();
        }

        return info;
    }

    /**
     * 获取设备运行状态信息
     *
     * @param result
     * @return
     */
    public static DeviceStatusInfoTwo getDeviceStatusTwo(String result) {
        CommandResult<DeviceStatusInfoTwo> commandResult = ParseManager.getInstance().parse(result);
        DeviceStatusInfoTwo info = new DeviceStatusInfoTwo();
        if (commandResult.isSuccess()) {
            info = commandResult.getResult();
        }

        return info;
    }

    /**
     * 获取设备运行状态信息
     *
     * @param result
     * @return
     */
    public static DeviceStatusInfoThree getDeviceStatusThree(String result) {
        CommandResult<DeviceStatusInfoThree> commandResult = ParseManager.getInstance().parse(result);
        DeviceStatusInfoThree info = new DeviceStatusInfoThree();
        if (commandResult.isSuccess()) {
            info = commandResult.getResult();
        }

        return info;
    }

    /**
     * 获取实体对象
     *
     * @param result
     * @return
     */
    public static <T> T getEntityObject(String result) {
        CommandResult<Class> commandResult = ParseManager.getInstance().parse(result);
        T info = null;
        if (commandResult.isSuccess()) {
            info = (T) commandResult.getResult();
        }

        return info;
    }

}
