package com.shmedo.configlibrary.iot.enums;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：    米度物联网设备指令
 */
public enum IOTCommandType {
    /**　米度物联网设备通用指令  **/
    /**
     * 获取设备终端时间
     */
    QUERY_TERMINAL_TIME("reqtime"),

    /**
     * 设置设备终端时间
     */
    SET_TERMINAL_TIME("settime"),

    /**
     * 获取设备状态
     */
    QUERY_DEVICE_STATUS("getstatus"),

    /**
     * 重启设备
     */
    REBOOT("reboot"),

    /**
     * 恢复出厂设置
     */
    RESET("md_reset"),

    /**
     * 获取接入传感器类型
     */
    QUERY_SENSOR_TYPE("getsensorID"),

    /**
     * 传感器遥测
     */
    QUERY_SAMPLE("sample"),

    /**
     * 设置工作模式
     */
    SET_WORK_MODE("setworkmode"),


    /*　VMS-LoRa网关指令  */
    /**
     * 获取网关的基本信息
     */
    VMS_MD_GET_GATEWAY_BASE("md_getgatewaybase"),

    /**
     * 获取网关不同通道下，挂载终端的运行情况
     */
    VMS_MD_GET_GATEWAY_STATUS("md_getgatewaystatus"),

    /**
     * 获取网关相关的控制参数
     */
    VMS_MD_GET_GATEWAY_PARAM("md_getgatewayparam"),

    /**
     * 设置网关相关的控制参数
     */
    VMS_MD_SET_GATEWAY_PARAM("md_setgatewayparam"),

    /**
     * 获取数据中心参数
     */
    MD_GET_DATA_CENTER("md_getdatacenter"),

    /**
     * 设置数据中心参数
     */
    MD_SET_DATA_CENTER("md_setdatacenter"),

    /**
     * 获取Vms数据中心状态
     */
    VMS_MD_GET_DATA_CENTER_STATUS("md_getdatacenterstatus"),

    /**
     * 删除Vms终端
     */
    VMS_MD_DELETE_TERMINAL("md_delterminal"),

    /**
     * 重启Vms终端
     */
    VMS_MD_REBOOT_TERMINAL("md_rebootterminal"),

    /**
     * 获取Vms终端传感器参数
     */
    VMS_MD_GET_TERMINAL_CHL("md_getterminalchl"),

    /**
     * 设置Vms终端传感器参数
     */
    VMS_MD_SET_TERMINAL_CHL("md_setterminalchl"),

    /**
     * 获取Vms终端采集参数
     */
    VMS_MD_GET_TERMINAL_COLLECTOR("md_getterminalcoll"),

    /**
     * 设置Vms终端采集参数
     */
    VMS_MD_SET_TERMINAL_COLLECTOR("md_setterminalcoll"),

    /**
     * 获取Vms终端通信参数
     */
    VMS_MD_GET_TERMINAL_COMMUNICATE("md_getterminalcom"),

    /**
     * 设置Vms终端通信参数
     */
    VMS_MD_SET_TERMINAL_COMMUNICATE("md_setterminalcom"),


    /*　 ADME 指令  */
    /**
     * 获取ADME的基本信息
     */
    ADME_MD_GET_EQUIPMENT_BASIS("md_getequipmentbasis"),

    /**
     * 获取ADME的运行状态
     */
    ADME_MD_GET_MOTION_STATE("md_getmotionstate"),

    /**
     * 设置ADME模式（0：设备配置模式，1：自动检测模式）
     */
    ADME_MD_SET_EQUIPMENT_MODEL("md_setequimodel"),

    /**
     * 获取ADME的基础配置参数
     */
    ADME_MD_GET_BASIC_PARAMETERS("md_getbasicparameters"),

    /**
     * 设置ADME的基础配置参数
     */
    ADME_MD_SET_BASIC_PARAMETERS("md_setbasicparameters"),

    /**
     * 获取ADME的计米轮配置参数
     */
    ADME_MD_GET_METER_WHEEL_PARAMETERS("md_getjmqparameter"),

    /**
     * 设置ADME的计米轮配置参数
     */
    ADME_MD_SET_METER_WHEEL_PARAMETERS("md_setjmqparameter"),

    /**
     * 获取ADME的测斜仪配置参数
     */
    ADME_MD_GET_INCLINOMETER_PARAMETERS("md_getinter"),

    /**
     * 设置ADME的测斜仪配置参数
     */
    ADME_MD_SET_INCLINOMETER_PARAMETERS("md_setinter"),

    /**
     * 获取ADME的步进电机配置参数
     */
    ADME_MD_GET_STEPPER_MOTOR_PARAMETERS("md_getsteppermotor"),

    /**
     * 设置ADME的步进电机配置参数
     */
    ADME_MD_SET_STEPPER_MOTOR_PARAMETERS("md_setsteppermotor"),

    /**
     * 获取ADME的执行机构配置参数
     */
    ADME_MD_GET_EXECUTIVE_AGENCY_PARAMETERS("md_getactuator"),

    /**
     * 设置ADME的执行机构配置参数
     */
    ADME_MD_SET_EXECUTIVE_AGENCY_PARAMETERS("md_setactuator"),

    /**
     * 获取ADME的测量孔深配置参数
     */
    ADME_MD_GET_MEASURING_HOLEDEPTH_PARAMETERS("md_getmhdmeasth"),

    /**
     * 设置ADME的测量孔深配置参数
     */
    ADME_MD_SET_MEASURING_HOLEDEPTH_PARAMETERS("md_setmhdmeasth"),

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离
     */
    ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE("md_getmhdpulsedistance"),

    /**
     * ADME测孔深运动停止
     */
    ADME_MD_STOP_MEASURING_HOLEDEPTH("md_setmhdmeasthstop"),

    /**
     * ADME测量孔深清空
     */
    ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA("md_setmhdempty"),

    /**
     * 自定义心跳包
     */
    HEART_BEAT("HeartBeatData");


    private String commandCode;

    IOTCommandType(String commandCode) {
        this.commandCode = commandCode;
    }

    @Override
    public String toString() {
        return this.commandCode;
    }
}
