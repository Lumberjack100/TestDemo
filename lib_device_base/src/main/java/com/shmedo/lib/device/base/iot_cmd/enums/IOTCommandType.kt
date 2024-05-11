package com.shmedo.lib.device.base.iot_cmd.enums

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：    米度物联网设备指令
 */
enum class IOTCommandType(private val value: String) {
    //<editor-fold desc="设备通用指令">
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
     * 获取设备状态
     */
    QUERY_DEVICE_EX_STATUS("md_getExstatus"),

    /**
     * 获取设备自检信息
     */
    MD_GET_DEVICE_STATUS("md_getdevicesta"),

    /**
     * 重启设备
     */
    REBOOT("reboot"),

    /**
     * 恢复出厂设置
     */
    RESET("md_reset"),

    /**
     * 参数导出(上传)
     */
    MD_BACKUP_CONFIG("md_backupconfig"),

    /**
     * 参数参数导入(恢复)
     */
    MD_RESTORE_CONFIG("md_restoreconfig"),

    /**
     * 获取接入传感器类型
     */
    QUERY_SENSOR_TYPE("getsensorID"),

    /**
     * 传感器遥测
     */
    QUERY_SAMPLE("sample"),

    /**
     * 获取工作模式
     */
    GET_WORK_MODE("getworkmode"),

    /**
     * 设置工作模式
     */
    SET_WORK_MODE("setworkmode"),

    /**
     * 获取上报数据的间隔
     */
    MD_GET_DATA_REPORT_TIME("md_getreportdatatime"),

    /**
     * 设置上报数据的间隔
     */
    MD_SET_DATA_REPORT_TIME("md_setreportdatatime"),

    /**
     * 获取数据中心状态
     */
    MD_GET_DATA_CENTER_STATUS("md_getdatacenterstatus"),

    /**
     * 获取数据中心参数
     */
    MD_GET_DATA_CENTER("md_getdatacenter"),

    /**
     * 设置数据中心参数
     */
    MD_SET_DATA_CENTER("md_setdatacenter"),

    /**
     * 获取日志输出等级和输出方式
     */
    GET_LOG_OUTPUT_MODE_LEVEL("md_getlogoutput"),

    /**
     * 日志输出等级和输出方式
     */
    SET_LOG_OUTPUT_MODE_LEVEL("md_setlogoutput"),

    /**
     * 固件升级
     */
    MD_UPGRADE("md_upgrade"),

    /**
     * 保存配置参数
     */
    MD_SAVE_CONFIG_PARAM("md_saveconfig"),
    // </editor-fold>

    //<editor-fold desc="DAS 指令">
    /**
     * 获取基本信息
     */
    DAS_MD_GET_DEVICE_BASE("md_getdevicebase"),

    /**
     * 获取数据中心状态
     */
    DAS_MD_GET_NET_STATUS("md_getnetstatus"),

    /**
     * 获取太阳能控制器状态
     */
    DAS_MD_GET_SOLAR_STATUS("md_getsloarstatus"),

    /**
     * 获取温湿度状态
     */
    DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS("md_thmstatus"),

    /**
     * 获取主传感器状态
     */
    DAS_MD_GET_SENSOR_STATUS("md_getsensorstatus"),

    /**
     * 获取辅传感器状态
     */
    DAS_MD_GET_SUB_SENSOR_STATUS("md_getsubsensorstatus"),

    /**
     * 获取激活模式
     */
    DAS_MD_GET_ACTIVE("md_getactive"),

    /**
     * 设置激活模式
     */
    DAS_MD_SET_ACTIVE("md_setactive"),

    /**
     * 获取采集控制相关参数
     */
    DAS_MD_GET_COLLECTOR_CONTROL("md_getcollctrl"),

    /**
     * 设置采集控制相关参数
     */
    DAS_MD_SET_COLLECTOR_CONTROL("md_setcollctrl"),

    /**
     * 获取开关量传感器信息
     */
    DAS_MD_GET_IO_SENSOR_INFO("md_getioctrl"),

    /**
     * 设置开关量传感器信息
     */
    DAS_MD_SET_IO_SENSOR_INFO("md_setioctrl"),

    /**
     * 获取数字水位计信息
     */
    DAS_MD_GET_DIGITAL_PIEZOMETER_INFO("md_getdigtalosm"),

    /**
     * 设置数字水位计信息
     */
    DAS_MD_SET_DIGITAL_PIEZOMETER_INFO("md_setdigtalosm"),

    /**
     * 查询北斗数传终端
     */
    DAS_MD_GET_BD_TERMINAL("md_getbdterminal"),

    /**
     * 设置北斗数传终端
     */
    DAS_MD_SET_BD_TERMINAL("md_setbdterminal"),

    /**
     * 查询 MCU 地址
     */
    DAS_MD_GET_MCU_ADDRESS("md_getmcuaddr"),

    /**
     * 设置 MCU 地址
     */
    DAS_MD_SET_MCU_ADDRESS("md_setmcuaddr"),

    /**
     * 查询扩展传感器信息
     */
    DAS_MD_GET_EXTERNAL_SENSOR("md_getsensorctrl"),

    /**
     * 设置扩展传感器信息
     */
    DAS_MD_SET_EXTERNAL_SENSOR("md_setsensorctrl"),

    /**
     * 删除扩展传感器
     */
    DAS_MD_DEL_EXTERNAL_SENSOR("md_delsensor"),

    /**
     * 查询声光报警器信息
     */
    DAS_MD_GET_AUDIBLE_ALARM("md_getalarm"),

    /**
     * 设置声光报警器信息
     */
    DAS_MD_SET_AUDIBLE_ALARM("md_setalarm"),

    /**
     * 查询定时上报参数
     */
    DAS_MD_GET_DATA_REPORT_TYPE("md_getdatareporttype"),

    /**
     * 设置定时上报参数
     */
    DAS_MD_SET_DATA_REPORT_TYPE("md_setdatareporttype"),
    // </editor-fold>

    //<editor-fold desc="VMS-LoRa 指令">
    /**
     * 获取网关的基本信息
     */
    VMS_MD_GET_GATEWAY_BASE("md_getgatewaybase"),

    /**
     * 获取网关相关的控制参数
     */
    VMS_MD_GET_GATEWAY_PARAM("md_getgatewayparam"),

    /**
     * 设置网关相关的控制参数
     */
    VMS_MD_SET_GATEWAY_PARAM("md_setgatewayparam"),

    /**
     * 获取网关不同通道下，挂载终端的运行情况
     */
    VMS_MD_GET_TERMINAL_STATUS("md_getterminalstatus"),

    /**
     * 扫描添加新的终端
     */
    VMS_MD_SCAN_ADD_TERMINAL("md_addterminal"),

    /**
     * 获取扫码添加的终端添列表
     */
    VMS_MD_GET_TERMINAL_SN("md_getterminalsn"),

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

    /**
     * Vms终端遥测
     */
    VMS_TERMINAL_QUERY_SAMPLE("md_sampleterminal"),
    // </editor-fold>

    //<editor-fold desc="RN20 指令">
    /**
     * 获取终端基本信息
     */
    RN20_MD_GET_TERMINAL_BASE("md_getterminalbase"),

    /**
     * 获取设备模块状态信息
     */
    RN20_MD_GET_TERMINAL_MODULE_STATUS("md_getterminalmodule"),

    /**
     * 获取终端经纬度参数
     */
    RN20_MD_GET_TERMINAL_LOCAL("md_getterminallocal"),

    /**
     * 设置终端经纬度参数
     */
    RN20_MD_SET_TERMINAL_LOCAL("md_setterminallocal"),
    // </editor-fold>

    //<editor-fold desc="ADME 指令">
    /**
     * 获取ADME的基本信息
     */
    ADME_MD_GET_EQUIPMENT_BASIS("md_getequipmentbasis"),

    /**
     * 获取ADME的当前状态
     */
    ADME_MD_GET_EQUIPMENT_STATE("md_getequipmentstate"),

    /**
     * 获取ADME的电机运行状态
     */
    ADME_MD_GET_MOTION_STATE("md_getmotionstate"),

    /**
     * 设置ADME模式（0：设备配置模式，1：自动检测模式）
     */
    ADME_MD_SET_EQUIPMENT_MODEL("md_setequimodel"),

    /**
     * 获取ADME的基础配置参数
     */
    ADME_MD_GET_BASIC("md_getbasicparameters"),

    /**
     * 设置ADME的基础配置参数
     */
    ADME_MD_SET_BASIC("md_setbasicparameters"),

    /**
     * 获取ADME的计米轮配置参数
     */
    ADME_MD_GET_METER_WHEEL("md_getjmqparameter"),

    /**
     * 设置ADME的计米轮配置参数
     */
    ADME_MD_SET_METER_WHEEL("md_setjmqparameter"),

    /**
     * 获取ADME的测斜仪配置参数
     */
    ADME_MD_GET_INCLINOMETER("md_getinter"),

    /**
     * 设置ADME的测斜仪配置参数
     */
    ADME_MD_SET_INCLINOMETER("md_setinter"),

    /**
     * 获取ADME的步进电机配置参数
     */
    ADME_MD_GET_STEPPER_MOTOR("md_getsteppermotor"),

    /**
     * 设置ADME的步进电机配置参数
     */
    ADME_MD_SET_STEPPER_MOTOR("md_setsteppermotor"),

    /**
     * 获取ADME的执行机构配置参数
     */
    ADME_MD_GET_EXECUTIVE_AGENCY("md_getactuator"),

    /**
     * 设置ADME的执行机构配置参数
     */
    ADME_MD_SET_EXECUTIVE_AGENCY("md_setactuator"),

    /**
     * 获取ADME的电机运动堵转缓停参数
     */
    ADME_MD_GET_LOCKED_ROTOR_DETECTION("md_getlocros"),

    /**
     * 设置ADME的电机运动堵转缓停参数
     */
    ADME_MD_SET_LOCKED_ROTOR_DETECTION("md_setlocros"),

    /**
     * 获取ADME的测量孔深配置参数
     */
    ADME_MD_GET_MEASURING_HOLEDEPTH("md_getmhdmeasth"),

    /**
     * 设置ADME的测量孔深配置参数
     */
    ADME_MD_SET_MEASURING_HOLEDEPTH("md_setmhdmeasth"),

    /**
     * 设置ADME的自动测量孔深参数
     */
    ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH("md_setautomhdmeasth"),

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
     * 获取ADME的导槽校准配置参数
     */
    ADME_MD_GET_GUIDE_GROOVE_CALIBRATION("md_getmeasth"),

    /**
     * 设置ADME的导槽校准配置参数
     */
    ADME_MD_SET_GUIDE_GROOVE_CALIBRATION("md_setmeasth"),

    /**
     * 查询ADME导槽校准的脉冲数、运动角度
     */
    ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE("md_getpulsedistance"),

    /**
     * ADME导槽校准动停止
     */
    ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION("md_setstop"),

    /**
     * ADME导槽校准清空
     */
    ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA("md_setempty"),

    /**
     * 获取ADME的工作模式
     */
    ADME_MD_GET_WORK_MODE("md_getworkmode"),

    /**
     * 设置ADME的工作模式
     */
    ADME_MD_SET_WORK_MODE("md_setworkmode"),

    /**
     * 获取ADME的力矩电机继电器低功耗使能
     */
    ADME_MD_GET_LOW_ENERGY_MODE("md_getlowenergy"),

    /**
     * 设置ADME的低功耗状态
     */
    ADME_MD_SET_LOW_ENERGY_MODE("md_setlowenergy"),

    /**
     * 获取ADME的电压配置参数
     */
    ADME_MD_GET_VOLTAGE("md_getvolt"),

    /**
     * 设置ADME的电压配置参数
     */
    ADME_MD_SET_VOLTAGE("md_setvolt"),

    /**
     * 获取ADME的拟人运动使能参数
     */
    ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE("md_getanthrmove"),

    /**
     * 设置ADME的拟人运动使能参数
     */
    ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE("md_setanthrmove"),

    /**
     * 力矩电机断电重启
     */
    TORQUE_MOTOR_REBOOT("md_torquemotorreboot"),

    /**
     * 获取ADME的刹车片控制方式
     */
    ADME_MD_GET_BRAKE_PAD_CONTROL("md_getbreakway"),

    /**
     * 设置ADME的刹车片控制方式
     */
    ADME_MD_SET_BRAKE_PAD_CONTROL("md_setbreakway"),

    /**
     * 获取ADME的电机电源使能信息
     */
    ADME_MD_GET_MOTOR_POWER("md_getmotorpower"),

    /**
     * 设置ADME的电机电源使能
     */
    ADME_MD_SET_MOTOR_POWER("md_setmotorpower"),

    /**
     * 获取ADME的检校、数据处理信息
     */
    ADME_MD_GET_CALIBRATION_PROCESSING("md_getaccuracy"),

    /**
     * 设置ADME的检校、数据处理信息
     */
    ADME_MD_SET_CALIBRATION_PROCESSING("md_setaccuracy"),

    /**
     * 清空设备运行数据
     * type=1 清空设备下降次数  type=2 清空设备里程 type=3 清空竖向磁开关触发次数 type=4 旋转磁开关触发次数 type=5 清空刹车片启闭次数
     */
    ADME_MD_CLEAR_DEVICE_RUNNING_DATA("md_clearrundata"),

    // </editor-fold>

    //<editor-fold desc="ADME HAC 指令">
    /**
     * 获取HAC的预警配置参数
     */
    ADME_HAC_MD_GET_WARN("md_hac_getwarn"),

    /**
     * 设置HAC的预警配置参数
     */
    ADME_HAC_MD_SET_WARN("md_hac_setwarn"),

    /**
     * 获取HAC执⾏机构参数
     */
    ADME_HAC_MD_GET_EXECUTIVE_AGENCY("md_hac_getactuator"),

    /**
     * 设置HAC执⾏机构参数
     */
    ADME_HAC_MD_SET_EXECUTIVE_AGENCY("md_hac_setactuator"),

    /**
     * 获取HAC的孔深测量配置参数
     */
    ADME_HAC_MD_GET_HOLE_MEASURE_PARAM("md_hac_getholemeasparame"),

    /**
     * 设置HAC的孔深测量配置参数
     */
    ADME_HAC_MD_SET_HOLE_MEASURE_PARAM("md_hac_setholemeasparame"),

    /**
     * 查询HAC测孔深运动的脉冲数、运动距离
     */
    ADME_HAC_MD_GET_HOLE_MEASURE_PULSE("md_hac_getmhdpulsedistance"),

    /**
     * 查询HAC数据测量参数
     */
    ADME_HAC_MD_GET_DATA_MEASURE_PARAM("md_hac_getdatameasparame"),

    /**
     * 设置HAC数据测量参数
     */
    ADME_HAC_MD_SET_DATA_MEASURE_PARAM("md_hac_setdatameasparame"),

    /**
     * 查询电机当前运动状态
     */
    ADME_HAC_MD_GET_MOTION_STATE("md_hac_getmotionstate"),

    /**
     * 获取ADME的正反测异常智能处理参数
     */
    ADME_MD_GET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING("md_getintelsw"),

    /**
     * 设置ADME的正反测异常智能处理
     */
    ADME_MD_SET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING("md_setintelsw"),
    // </editor-fold>

    //<editor-fold desc="M20 指令">
    /**
     * 获取M20的基本信息
     */
    M20_MD_GET_BASE_INFO("md_getbaseinfo"),

    /**
     * M20水平初始化设置
     */
    M20_MD_LEVEL_INITIAL("md_levelinit"),
    // </editor-fold>

    //<editor-fold desc="E40 指令">
    /**
     * 获取设备卫星状态
     */
    E40_MD_GET_SATELITTE("md_getsatelitte"),

    /**
     * 获取E40 的 RTK
     */
    E40_MD_GET_RTK("md_getrtk"),

    /**
     * 设置E40 的 RTK
     */
    E40_MD_SET_RTK("md_setrtk"),

    /**
     * 获取E40 的 CORS 服务参数
     */
    E40_MD_GET_CORS("md_getntrip"),

    /**
     * 设置E40 的 CORS 服务参数
     */
    E40_MD_SET_CORS("md_setntrip"),

    /**
     * 获取E40 的板卡解算参数
     */
    E40_MD_GET_BOARDSOLUTION("md_getembedams"),

    /**
     * 设置E40 的板卡解算参数
     */
    E40_MD_SET_BOARDSOLUTION("md_setembedams"),

    /**
     * 获取E40 的有线网络配置置参数
     */
    E40_MD_GET_ETHERNET("md_geteth0"),

    /**
     * 设置E40 的有线网络配置置参数
     */
    E40_MD_SET_ETHERNET("md_seteth0"),

    /**
     * 获取E40 的串口配置置参数
     */
    E40_MD_GET_DB_GUART("md_getdbguart"),

    /**
     * 设置E40 的串口配置置参数
     */
    E40_MD_SET_DB_GUART("md_setdbguart"),

    /**
     * 获取E40 的GPS工作参数
     */
    E40_MD_GET_GPS_PARAM("md_getgpsparam"),

    /**
     * 设置E40 的GPS工作参数
     */
    E40_MD_SET_GPS_PARAM("md_setgpsparam"),

    /**
     * 获取E40 的基站位置信息
     */
    E40_MD_GET_BASE_POSITION("md_getbaseposition"),

    /**
     * 设置E40 的基站位置信息
     */
    E40_MD_SET_BASE_POSITION("md_setbaseposition"),

    /**
     * 获取E40 NMEA输出内容及输出频率
     */
    E40_MD_GET_NMEA_TIME("md_getnmeatime"),

    /**
     * 设置E40 NMEA输出内容及输出频率
     */
    E40_MD_SET_NMEA_TIME("md_setnmeatime"),
    // </editor-fold>

    //<editor-fold desc="MR702水利终端机通用指令">
    /**
     * 基本信息
     */
    MD_MR_GET_DEVICE_BASE_INFO("md_mrgetdevicebase"),

    /**
     * 4G网络配置
     */
    MD_MR_GET_DATA_NETWORK("md_mrgetdatanetwork"),
    MD_MR_SET_DATA_NETWORK("md_mrsetdatanetwork"),

    /**
     *  有线网络配置
     */
    MD_MR_GET_WIRED_NETWORK("md_mrgetwirednetwork"),
    MD_MR_SET_WIRED_NETWORK("md_mrsetwirednetwork"),

    /**
     *  终端参数-上报方式
     */
    MD_MR_GET_REPORT_METHOD("md_mrgetreporttype"),
    MD_MR_SET_REPORT_METHOD("md_mrsetreporttype"),

    /**
     *  终端参数-本机屏幕
     */
    MD_MR_GET_SCREEN_PARAM("md_mrgetscreen"),
    MD_MR_SET_SCREEN_PARAM("md_mrsetscreen"),

    /**
     * 获取数据中心状态
     */
    MD_MR_GET_DATA_CENTER_STATUS("md_mrgetcenterstatus"),
    MD_MR_GET_DATA_CENTER("md_mrgetdatacenter"),
    MD_MR_SET_DATA_CENTER("md_mrsetdatacenter"),

    /**
     * RS485-端口1 获取采集控制参数配置
     */
    MD_MR_GET_RS485_PORT1_COLL("md_get485port1_coll"),
    MD_MR_SET_RS485_PORT1_COLL("md_set485port1_coll"),

    /**
     * 传感器状态获取
     */
    MD_MR_GET_RS485_PORT1_SENSOR("md_get485port1_sensor"),
    MD_MR_DEL_RS485_PORT1_SENSOR("md_del485port1_param"),

    /**
     * RS485-端口1 传感器参数获取
     */
    MD_MR_GET_RS485_PORT1_SENSOR_PARAM("md_get485port1_param"),
    MD_MR_SET_RS485_PORT1_SENSOR_PARAM("md_set485port1_param"),

    /**
     * RS485-端口2 获取采集控制参数配置
     */
    MD_MR_GET_RS485_PORT2_COLL("md_get485port2_coll"),
    MD_MR_SET_RS485_PORT2_COLL("md_set485port2_coll"),

    /**
     * RS485-端口2 获取串口参数配置
     */
    MD_MR_GET_RS485_PORT2_UART("md_get485port2_uart"),
    MD_MR_SET_RS485_PORT2_UART("md_set485port2_uart"),

    /**
     * 传感器状态获取
     */
    MD_MR_GET_RS485_PORT2_SENSOR("md_get485port2_sensor"),
    MD_MR_DEL_RS485_PORT2_SENSOR("md_del485port2_param"),

    /**
     * RS485-端口2 传感器参数获取
     */
    MD_MR_GET_RS485_PORT2_SENSOR_PARAM("md_get485port2_param"),
    MD_MR_SET_RS485_PORT2_SENSOR_PARAM("md_set485port2_param"),

    /**
     * RS485-3-模块状态
     */
    MD_MR_GET_RS485_PORT3_SENSOR_STATUS("md_mrgetrs485p3status"),

    /**
     * RS485-端口3 传感器参数获取
     */
    MD_MR_GET_RS485_PORT3_SENSOR_PARAM("md_mrgetrs485p3param"),
    MD_MR_SET_RS485_PORT3_SENSOR_PARAM("md_mrsetrs485p3param"),

    /**
     * RS232-1-摄像头 参数获取
     */
    MD_MR_GET_RS232_PORT1_PARAM("md_mrgetrs232p1param"),
    MD_MR_SET_RS232_PORT1_PARAM("md_mrsetrs232p1param"),

    /**
     * RS232-2-北斗数据终端 参数获取
     */
    MD_MR_GET_RS232_PORT2_PARAM("md_mrgetrs232p2param"),
    MD_MR_SET_RS232_PORT2_PARAM("md_mrsetrs232p2param"),

    /**
     * 雨量计 参数获取
     */
    MD_MR_GET_RAIN_GAUGE_PORT_PARAM("md_mrgetraingauge"),
    MD_MR_SET_RAIN_GAUGE_PORT_PARAM("md_mrsetraingauge"),

    /**
     * DO 参数获取
     */
    MD_MR_GET_DO_PORT_PARAM("md_mrgetdostatus"),
    MD_MR_SET_DO_PORT_PARAM("md_mrsetdostatus"),

    /**
     * DI 参数获取
     */
    MD_MR_GET_DI_PORT_PARAM("md_mrgetdistatus"),

    /**
     * 人工置数
     */
    MD_MR_ARTIFICIAL("md_mrartificial"),

    /**
     * 文件上传
     */
    MD_MR_UPLOAD_FILE("md_mruploadfile"),

    /**
     * 手动拍照
     */
    MD_MR_TAKE_PHOTOS("md_mrtakephotos"),

    // </editor-fold>

    /**
     * 获取设备位置
     */
    MD_GET_LOCATION("md_getloc"),

    /**
     * 设置设备位置
     */
    MD_SET_LOCATION("md_setloc"),

    /**
     * 语音播报
     */
    MD_BROADCAST("broadcast"),

    /**
     * 获取终端ID
     */
    MD_GET_TERMINAL_ID("md_getcqterminalid"),

    /**
     * 终端ID设置
     */
    MD_SET_TERMINAL_ID("md_setcqterminalid"),

    /**
     * 终端ID删除
     */
    MD_DEL_TERMINAL_ID("md_delcqterminalid"),

    /**
     *  Lora通讯参数设置
     */
    MD_GET_LORA_CTRL("md_getcqloractrl"),
    MD_SET_LORA_CTRL("md_setcqloractrl"),

    /**
     * 电台参数设置
     */
    MD_GET_RADIO_CTRL("md_getcqradioctrl"),
    MD_SET_RADIO_CTRL("md_setcqradioctrl"),

    /**
     * 预警报警开关（仅适用于监测设备）
     */
    MD_GET_ALRAM_BROADCAST_SWITCH("md_getcqbroadcastswitch"),
    MD_SET_ALRAM_BROADCAST_SWITCH("md_setcqbroadcastswitch"),


    /**
     * 预警广播参数配置
     */
    MD_GET_ALRAM_BROADCAST_CTRL("md_getcqalarmctrl"),
    MD_SET_ALRAM_BROADCAST_CTRL("md_setcqalarmctrl"),

    /**
     * 预警广播触发值配置
     */
    MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE("md_getcqgateval"),
    MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE("md_setcqgateval"),

    /**
     * 预警广播上报间隔配置
     */
    MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL("md_getcqreptgap"),
    MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL("md_setcqreptgap"),

    /**
     * 预警广播测试
     */
    MD_TEST_ALRAM_BROADCAST("md_testbroadcast"),

    /**
     * 配置参数初始值指令
     */
    MD_SET_SENSOR_INITIAL("md_cfginitval"),

    /**
     * 泥位计配置
     */
    MD_GET_MUD_LEVEL_METER_SENSOR("md_getsense"),
    MD_SET_MUD_LEVEL_METER_SENSOR("md_setsense"),

    /**
     * 米度一体式裂缝计零位校准
     */
    MD_GET_LF_ZERO_VALUE("md_getlfzerovalue"),
    MD_SET_LF_ZERO_VALUE("md_setlfzerovalue"),

    /**
     * 米度一体式裂缝计初始化
     */
    MD_SET_LF_INITIAL_VALUE("md_setlfinitial"),

    /**
     * GNSS-RTK模式配置
     */
    MD_CFG_RTK("md_cfgrtkparam"),

    /**
     * 关闭语音播报
     */
    MD_SET_VOICE_BROADCAST_VOLUME_OFF("volumeoff"),

    /**
     * 设置语音播报音量
     */
    MD_SET_VOICE_BROADCAST_VOLUME("volume"),

    /**
     * 自定义心跳包
     */
    HEART_BEAT("ble_keepalive"),

    /**
     * 长度不够指令头最低长度要求
     */
    LENGTH_INVALID("length_invalid"),

    /**
     * 设置指令类型，在解析设置指令响应结果时使用
     */
    COMMON_SETTING_COMMAND("common_setting_command"),

    /**
     * 未知的命令类型
     */
    UNKNOWN_TYPE("unknown_type");

    override fun toString(): String {
        return value
    }
}