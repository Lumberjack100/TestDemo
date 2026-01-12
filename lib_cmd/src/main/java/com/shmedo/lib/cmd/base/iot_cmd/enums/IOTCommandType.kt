package com.shmedo.lib.cmd.base.iot_cmd.enums

/** 创建者: gonghe <br></br> 创建时间: 2020/8/31 <br></br> 描述： 米度物联网设备指令 */
enum class IOTCommandType(val value: String) {
    // <editor-fold desc="设备通用指令">
    /** 获取设备终端时间 */
    QUERY_TERMINAL_TIME("reqtime"),

    /** 设置设备终端时间 */
    SET_TERMINAL_TIME("settime"),

    /** 获取设备状态 */
    QUERY_DEVICE_STATUS("getstatus"),

    /** 获取设备自检信息 */
    MD_GET_DEVICE_STATUS("md_getdevicesta"),

    /** 重启设备 */
    REBOOT("reboot"),

    /** 恢复出厂设置 */
    RESET("md_reset"),

    /** 格式化数据存储 */
    MD_FORMAT_DATA_STORAGE("md_cleaninfo"),

    /** 参数导出(上传) */
    MD_BACKUP_CONFIG("md_backupconfig"),

    /** 参数参数导入(恢复) */
    MD_RESTORE_CONFIG("md_restoreconfig"),

    /** 传感器遥测 */
    SAMPLE("sample"),

    /** 获取工作模式 */
    GET_WORK_MODE("getworkmode"),

    /** 设置工作模式 */
    SET_WORK_MODE("setworkmode"),

    /** 获取上报数据的间隔 */
    MD_GET_DATA_REPORT_TIME("md_getreportdatatime"),

    /** 设置上报数据的间隔 */
    MD_SET_DATA_REPORT_TIME("md_setreportdatatime"),

    /** 获取数据链路状态 */
    MD_GET_DATA_CENTER_STATUS("md_getdatacenterstatus"),

    /** 获取数据链路参数 */
    MD_GET_DATA_CENTER_PARAM("md_getdatacenter"),

    /** 设置数据链路参数 */
    MD_SET_DATA_CENTER_PARAM("md_setdatacenter"),

    /** 查询/设置安装位置 */
    MD_GET_INSTALL_LOCATION("md_getloc"),
    MD_SET_INSTALL_LOCATION("md_setloc"),

    /** 设备查找指令 */
    MD_SEARCH_DEVICE("md_searchdev"),

    /** 获取日志输出等级和输出方式 */
    MD_GET_LOG_OUTPUT_MODE_LEVEL("md_getlogoutput"),

    /** 日志输出等级和输出方式 */
    MD_SET_LOG_OUTPUT_MODE_LEVEL("md_setlogoutput"),

    /** 固件升级 */
    MD_UPGRADE("md_upgrade"),

    /** 保存配置参数 */
    MD_SAVE_CONFIG_PARAM("md_saveconfig"),

    /** 获取设备历史传感器数据 */
    MD_GET_DEVICE_SENSOR_HISTORY_DATA("md_getsensordata"),

    /** 上报周期 */
    MD_GET_DATA_REPORT_TYPE("md_getdatareporttype"),

    /** 设置上报周期 */
    MD_SET_DATA_REPORT_TYPE("md_setdatareporttype"),

    // </editor-fold>

    // <editor-fold desc="DAS 指令">
    /** 获取基本信息 */
    DAS_MD_GET_DEVICE_BASE("md_getdevicebase"),

    /** 获取数据链路状态 */
    DAS_MD_GET_NET_STATUS("md_getnetstatus"),

    /** 获取太阳能控制器状态 */
    DAS_MD_GET_SOLAR_STATUS("md_getsloarstatus"),

    /** 获取温湿度状态 */
    DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS("md_thmstatus"),

    /** 获取主传感器状态 */
    DAS_MD_GET_SENSOR_STATUS("md_getsensorstatus"),

    /** 获取辅传感器状态 */
    DAS_MD_GET_SUB_SENSOR_STATUS("md_getsubsensorstatus"),

    /** 获取激活模式 */
    DAS_MD_GET_ACTIVE("md_getactive"),

    /** 设置激活模式 */
    DAS_MD_SET_ACTIVE("md_setactive"),

    /** 获取采集控制相关参数 */
    DAS_MD_GET_COLLECTOR_CONTROL("md_getcollctrl"),

    /** 设置采集控制相关参数 */
    DAS_MD_SET_COLLECTOR_CONTROL("md_setcollctrl"),

    /** 获取开关量传感器信息 */
    DAS_MD_GET_IO_SENSOR_INFO("md_getioctrl"),

    /** 设置开关量传感器信息 */
    DAS_MD_SET_IO_SENSOR_INFO("md_setioctrl"),

    /** 获取数字水位计信息 */
    DAS_MD_GET_DIGITAL_PIEZOMETER_INFO("md_getdigtalosm"),

    /** 设置数字水位计信息 */
    DAS_MD_SET_DIGITAL_PIEZOMETER_INFO("md_setdigtalosm"),

    /** 查询北斗数传终端 */
    DAS_MD_GET_BD_TERMINAL("md_getbdterminal"),

    /** 设置北斗数传终端 */
    DAS_MD_SET_BD_TERMINAL("md_setbdterminal"),

    /** 查询 MCU 地址 */
    DAS_MD_GET_MCU_ADDRESS("md_getmcuaddr"),

    /** 设置 MCU 地址 */
    DAS_MD_SET_MCU_ADDRESS("md_setmcuaddr"),

    /** 查询扩展传感器信息 */
    DAS_MD_GET_EXTERNAL_SENSOR("md_getsensorctrl"),

    /** 设置扩展传感器信息 */
    DAS_MD_SET_EXTERNAL_SENSOR("md_setsensorctrl"),

    /** 删除扩展传感器 */
    DAS_MD_DEL_EXTERNAL_SENSOR("md_delsensor"),

    /** 查询声光报警器信息 */
    DAS_MD_GET_AUDIBLE_ALARM("md_getalarm"),

    /** 设置声光报警器信息 */
    DAS_MD_SET_AUDIBLE_ALARM("md_setalarm"),
    // </editor-fold>

    // <editor-fold desc="ADME 指令">
    /** 获取ADME的基本信息 */
    ADME_MD_GET_EQUIPMENT_BASIS("md_getequipmentbasis"),

    /** 获取ADME的当前状态 */
    ADME_MD_GET_EQUIPMENT_STATE("md_getequipmentstate"),

    /** 获取ADME的电机运行状态 */
    ADME_MD_GET_MOTION_STATE("md_getmotionstate"),

    /** 设置ADME模式（0：设备配置模式，1：自动检测模式） */
    ADME_MD_SET_EQUIPMENT_MODEL("md_setequimodel"),

    /** 获取ADME的基础配置参数 */
    ADME_MD_GET_BASIC("md_getbasicparameters"),

    /** 设置ADME的基础配置参数 */
    ADME_MD_SET_BASIC("md_setbasicparameters"),

    /** 获取ADME的计米轮配置参数 */
    ADME_MD_GET_METER_WHEEL("md_getjmqparameter"),

    /** 设置ADME的计米轮配置参数 */
    ADME_MD_SET_METER_WHEEL("md_setjmqparameter"),

    /** 获取ADME的测斜仪配置参数 */
    ADME_MD_GET_INCLINOMETER("md_getinter"),

    /** 设置ADME的测斜仪配置参数 */
    ADME_MD_SET_INCLINOMETER("md_setinter"),

    /** 获取ADME的步进电机配置参数 */
    ADME_MD_GET_STEPPER_MOTOR("md_getsteppermotor"),

    /** 设置ADME的步进电机配置参数 */
    ADME_MD_SET_STEPPER_MOTOR("md_setsteppermotor"),

    /** 获取ADME的执行机构配置参数 */
    ADME_MD_GET_EXECUTIVE_AGENCY("md_getactuator"),

    /** 设置ADME的执行机构配置参数 */
    ADME_MD_SET_EXECUTIVE_AGENCY("md_setactuator"),

    /** 获取ADME的电机运动堵转缓停参数 */
    ADME_MD_GET_LOCKED_ROTOR_DETECTION("md_getlocros"),

    /** 设置ADME的电机运动堵转缓停参数 */
    ADME_MD_SET_LOCKED_ROTOR_DETECTION("md_setlocros"),

    /** 获取ADME的测量孔深配置参数 */
    ADME_MD_GET_MEASURING_HOLEDEPTH("md_getmhdmeasth"),

    /** 设置ADME的测量孔深配置参数 */
    ADME_MD_SET_MEASURING_HOLEDEPTH("md_setmhdmeasth"),

    /** 设置ADME的自动测量孔深参数 */
    ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH("md_setautomhdmeasth"),

    /** 查询ADME测孔深运动的脉冲数、运动距离 */
    ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE("md_getmhdpulsedistance"),

    /** ADME测孔深运动停止 */
    ADME_MD_STOP_MEASURING_HOLEDEPTH("md_setmhdmeasthstop"),

    /** ADME测量孔深清空 */
    ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA("md_setmhdempty"),

    /** 获取ADME的导槽校准配置参数 */
    ADME_MD_GET_GUIDE_GROOVE_CALIBRATION("md_getmeasth"),

    /** 设置ADME的导槽校准配置参数 */
    ADME_MD_SET_GUIDE_GROOVE_CALIBRATION("md_setmeasth"),

    /** 查询ADME导槽校准的脉冲数、运动角度 */
    ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE("md_getpulsedistance"),

    /** ADME导槽校准动停止 */
    ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION("md_setstop"),

    /** ADME导槽校准清空 */
    ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA("md_setempty"),

    /** 获取ADME的工作模式 */
    ADME_MD_GET_WORK_MODE("md_getworkmode"),

    /** 设置ADME的工作模式 */
    ADME_MD_SET_WORK_MODE("md_setworkmode"),

    /** 获取ADME的力矩电机继电器低功耗使能 */
    ADME_MD_GET_LOW_ENERGY_MODE("md_getlowenergy"),

    /** 设置ADME的低功耗状态 */
    ADME_MD_SET_LOW_ENERGY_MODE("md_setlowenergy"),

    /** 获取ADME的电压配置参数 */
    ADME_MD_GET_VOLTAGE("md_getvolt"),

    /** 设置ADME的电压配置参数 */
    ADME_MD_SET_VOLTAGE("md_setvolt"),

    /** 获取ADME的拟人运动使能参数 */
    ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE("md_getanthrmove"),

    /** 设置ADME的拟人运动使能参数 */
    ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE("md_setanthrmove"),

    /** 力矩电机断电重启 */
    ADME_MD_TORQUE_MOTOR_REBOOT("md_torquemotorreboot"),

    /** 获取ADME的刹车片控制方式 */
    ADME_MD_GET_BRAKE_PAD_CONTROL("md_getbreakway"),

    /** 设置ADME的刹车片控制方式 */
    ADME_MD_SET_BRAKE_PAD_CONTROL("md_setbreakway"),

    /** 获取ADME的电机电源使能信息 */
    ADME_MD_GET_MOTOR_POWER("md_getmotorpower"),

    /** 设置ADME的电机电源使能 */
    ADME_MD_SET_MOTOR_POWER("md_setmotorpower"),

    /** 获取ADME的检校、数据处理信息 */
    ADME_MD_GET_CALIBRATION_PROCESSING("md_getaccuracy"),

    /** 设置ADME的检校、数据处理信息 */
    ADME_MD_SET_CALIBRATION_PROCESSING("md_setaccuracy"),

    /**
     * 清空设备运行数据 type=1 清空设备下降次数 type=2 清空设备里程 type=3 清空竖向磁开关触发次数 type=4 旋转磁开关触发次数 type=5 清空刹车片启闭次数
     */
    ADME_MD_CLEAR_DEVICE_RUNNING_DATA("md_clearrundata"),

    /** 获取HAC的预警配置参数 */
    ADME_HAC_MD_GET_WARN("md_hac_getwarn"),

    /** 设置HAC的预警配置参数 */
    ADME_HAC_MD_SET_WARN("md_hac_setwarn"),

    /** 获取HAC执⾏机构参数 */
    ADME_HAC_MD_GET_EXECUTIVE_AGENCY("md_hac_getactuator"),

    /** 设置HAC执⾏机构参数 */
    ADME_HAC_MD_SET_EXECUTIVE_AGENCY("md_hac_setactuator"),

    /** 获取HAC的孔深测量配置参数 */
    ADME_HAC_MD_GET_HOLE_MEASURE_PARAM("md_hac_getholemeasparame"),

    /** 设置HAC的孔深测量配置参数 */
    ADME_HAC_MD_SET_HOLE_MEASURE_PARAM("md_hac_setholemeasparame"),

    /** 查询HAC测孔深运动的脉冲数、运动距离 */
    ADME_HAC_MD_GET_HOLE_MEASURE_PULSE("md_hac_getmhdpulsedistance"),

    /** 查询HAC数据测量参数 */
    ADME_HAC_MD_GET_DATA_MEASURE_PARAM("md_hac_getdatameasparame"),

    /** 设置HAC数据测量参数 */
    ADME_HAC_MD_SET_DATA_MEASURE_PARAM("md_hac_setdatameasparame"),

    /** 查询电机当前运动状态 */
    ADME_HAC_MD_GET_MOTION_STATE("md_hac_getmotionstate"),

    /** 获取ADME的正反测异常智能处理参数 */
    ADME_MD_GET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING("md_getintelsw"),

    /** 设置ADME的正反测异常智能处理 */
    ADME_MD_SET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING("md_setintelsw"),
    // </editor-fold>

    // <editor-fold desc="M20 指令">
    /** 获取M20的基本信息 */
    M20_MD_GET_BASE_INFO("md_getbaseinfo"),

    /** M20水平初始化设置 */
    M20_MD_LEVEL_INITIAL("md_levelinit"),
    // </editor-fold>

    // <editor-fold desc="M50 指令">

    /** GNSS-RTK模式配置 */
    GM_MD_CFG_RTK("md_cfgrtkparam"),

    /** M50 电台参数 */
    M50_MD_RADIO_PARAM("md_cfgradioparam"),

    /** M50 串口参数 */
    M50_MD_SET_SERIAL_PORT("md_gm_setportparam"),

    /** 获取/设置 M50 的 CORS 服务参数 */
    M50_MD_GET_CORS("md_getcors"),
    M50_MD_SET_CORS("md_setcors"),

    /** 获取设备卫星状态 */
    M50_MD_GET_SATELITE_INFO("md_getskyinfo"),

    /** 查询/设置采样率 */
    MD_GET_SAMPLING_RATE("md_getgnssraw"),
    MD_SET_SAMPLING_RATE("md_setgnssraw"),

    /** 查询/设置截止高度角 */
    MD_GET_ELEVATION_ANGLE("md_getmoduleparam"),
    MD_SET_ELEVATION_ANGLE("md_setmoduleparam"),
    // </editor-fold>

    // <editor-fold desc="GT600 指令">
    /** 获取设备状态 */
    MD_GET_EXSTATUS("md_getExstatus"),

    /** 获取/设置ENMEA输出内容及输出频率 */
    MD_GET_NMEA_TIME("md_getnmeatime"),
    MD_SET_NMEA_TIME("md_setnmeatime"),

    /** 获取/设置 GNSS 控制参数（RTCM参数） */
    MD_GET_GNSS_CTL("md_getgnssctl"),
    MD_SET_GNSS_CTL("md_setgnssctl"),

    /** 配置双天线参数（VTG输出） */
    MD_CFG_NMEA_VTG_OUT("md_cfgnmeavtgout"),

    /** 获取/设置 串口配置置参数 */
    MD_GET_DB_GUART("md_getdbguart"),
    MD_SET_DB_GUART("md_setdbguart"),

    /** 获取/设置 有线网络配置置参数 */
    MD_GET_ETHERNET("md_geteth0"),
    MD_SET_ETHERNET("md_seteth0"),

    /** 获取/设置 4G网络使用开关 */
    MD_GET_NET_4G_USE("md_getnet4guse"),
    MD_SET_NET_4G_USE("md_setnet4guse"),

    /** 获取/设置 基站位置信息 */
    MD_GET_BASE_POSITION("md_getbaseposition"),
    MD_SET_BASE_POSITION("md_setbaseposition"),

    // </editor-fold>

    // <editor-fold desc="E40 指令">
    /** 获取E40 的 CORS 服务参数 */
    E40_MD_GET_CORS("md_getntrip"),

    /** 设置E40 的 CORS 服务参数 */
    E40_MD_SET_CORS("md_setntrip"),

    /** 获取E40 的板卡解算参数 */
    E40_MD_GET_BOARDSOLUTION("md_getembedams"),

    /** 设置E40 的板卡解算参数 */
    E40_MD_SET_BOARDSOLUTION("md_setembedams"),


    /** 获取E40 的GPS工作参数 */
    E40_MD_GET_GPS_PARAM("md_getgpsparam"),

    /** 设置E40 的GPS工作参数 */
    E40_MD_SET_GPS_PARAM("md_setgpsparam"),



    // </editor-fold>

    // <editor-fold desc="MR702 水利终端机通用指令">
    /** 基本信息 */
    MR_MD_GET_DEVICE_BASE_INFO("md_mrgetdevicebase"),

    /** 4G网络配置 */
    MR_MD_GET_DATA_NETWORK("md_mrgetdatanetwork"),
    MR_MD_SET_DATA_NETWORK("md_mrsetdatanetwork"),

    /** 有线网络配置 */
    MR_MD_GET_WIRED_NETWORK("md_mrgetwirednetwork"),
    MR_MD_SET_WIRED_NETWORK("md_mrsetwirednetwork"),

    /** 终端参数-上报方式 */
    MR_MD_GET_REPORT_TYPE("md_mrgetreporttype"),
    MR_MD_SET_REPORT_TYPE("md_mrsetreporttype"),

    /** 终端参数-本机屏幕 */
    MR_MD_GET_SCREEN_PARAM("md_mrgetscreen"),
    MR_MD_SET_SCREEN_PARAM("md_mrsetscreen"),

    /** 获取数据链路状态 */
    MR_MD_GET_DATA_CENTER_STATUS("md_mrgetcenterstatus"),
    MR_MD_GET_DATA_CENTER("md_mrgetdatacenter"),
    MR_MD_SET_DATA_CENTER("md_mrsetdatacenter"),

    /** RS485-端口1 获取采集控制参数配置 */
    MR_MD_GET_RS485_PORT1_COLL("md_get485port1_coll"),
    MR_MD_SET_RS485_PORT1_COLL("md_set485port1_coll"),

    /** 传感器状态获取 */
    MR_MD_GET_RS485_PORT1_SENSOR("md_get485port1_sensor"),
    MR_MD_DEL_RS485_PORT1_SENSOR("md_del485port1_param"),

    /** RS485-端口1 传感器参数获取 */
    MR_MD_GET_RS485_PORT1_SENSOR_PARAM("md_get485port1_param"),
    MR_MD_SET_RS485_PORT1_SENSOR_PARAM("md_set485port1_param"),

    /** RS485-端口2 获取采集控制参数配置 */
    MR_MD_GET_RS485_PORT2_COLL("md_get485port2_coll"),
    MR_MD_SET_RS485_PORT2_COLL("md_set485port2_coll"),

    /** RS485-端口2 获取串口参数配置 */
    MR_MD_GET_RS485_PORT2_UART("md_get485port2_uart"),
    MR_MD_SET_RS485_PORT2_UART("md_set485port2_uart"),

    /** 传感器状态获取 */
    MR_MD_GET_RS485_PORT2_SENSOR("md_get485port2_sensor"),
    MR_MD_DEL_RS485_PORT2_SENSOR("md_del485port2_param"),

    /** RS485-端口2 传感器参数获取 */
    MR_MD_GET_RS485_PORT2_SENSOR_PARAM("md_get485port2_param"),
    MR_MD_SET_RS485_PORT2_SENSOR_PARAM("md_set485port2_param"),

    /** RS485-3-模块状态 */
    MR_MD_GET_RS485_PORT3_MODULE_STATUS("md_mrgetrs485p3status"),

    /** RS485-端口3 太阳能控制器/声光报警器/LED 屏参数获取 */
    MR_MD_GET_RS485_PORT3_SENSOR_PARAM("md_mrgetrs485p3param"),
    MR_MD_SET_RS485_PORT3_SENSOR_PARAM("md_mrsetrs485p3param"),

    /** RS485-端口3 摄像头参数获取 */
    MR_MD_GET_RS485_PORT3_CAMERA_PARAM("md_getrs485cam"),
    MR_MD_SET_RS485_PORT3_CAMERA_PARAM("md_setrs485cam"),

    /** RS232-1-摄像头 参数获取 */
    MR_MD_GET_RS232_PORT1_PARAM("md_mrgetrs232p1param"),
    MR_MD_SET_RS232_PORT1_PARAM("md_mrsetrs232p1param"),

    /** RS232-2-北斗数据终端 参数获取 */
    MR_MD_GET_RS232_PORT2_PARAM("md_get_rdssparam"),
    MR_MD_SET_RS232_PORT2_PARAM("md_set_rdssparam"),

    /** 雨量计 参数获取 */
    MR_MD_GET_RAIN_GAUGE_PORT_PARAM("md_mrgetraingauge"),
    MR_MD_SET_RAIN_GAUGE_PORT_PARAM("md_mrsetraingauge"),

    /** 获取、设置脉冲端口参数 */
    MR_MD_GET_PULSE_PORT_PARAM("md_mrgetdrygauge"),
    MR_MD_SET_PULSE_PORT_PARAM("md_mrsetdrygauge"),

    /** DO 参数获取 */
    MR_MD_GET_DO_PORT_PARAM("md_mrgetdostatus"),
    MR_MD_SET_DO_PORT_PARAM("md_mrsetdostatus"),

    /** DI 参数获取 */
    MR_MD_GET_DI_PORT_PARAM("md_mrgetdistatus"),

    /** 人工置数 */
    MR_MD_ARTIFICIAL("md_mrartificial"),

    /** 文件上传 */
    MR_MD_FILE_UPLOAD("md_mruploadfile"),

    /** 手动拍照 */
    MR_MD_TAKE_PHOTOS("md_mrtakephotos"),

    /** 远程消警 */
    MR_MD_RS485_CLEAR_ALARM("md_clearalarm"),

    /** 清除消警 */
    MR_MD_CLEAN_CLEAR_ALARM("md_clean_clearalarm"),

    /** 雨量清零 */
    MR_MD_RS485_CLEAR_RAIN_GAUGE("md_mrclrraingauge"),

    /** 库容计算 参数获取 */
    MR_MD_GET_RESERVOIR_CAPACITY("md_getkurongparam"),
    MR_MD_SET_RESERVOIR_CAPACITY("md_setkurongparam"),

    /** 获取、设置报警参数 */
    MR_MD_GET_ALARM_MODULE("md_getalarmmodule"),
    MR_MD_SET_ALARM_MODULE("md_setalarmmodule"),

    // </editor-fold>

    /** 语音播报 */
    BROADCAST("broadcast"),

    /** 获取终端ID */
    MD_GET_TERMINAL_ID("md_getcqterminalid"),

    /** 终端ID设置 */
    MD_SET_TERMINAL_ID("md_setcqterminalid"),

    /** 终端ID删除 */
    MD_DEL_TERMINAL_ID("md_delcqterminalid"),

    /** Lora通讯参数设置 */
    MD_GET_LORA_CTRL("md_getcqloractrl"),
    MD_SET_LORA_CTRL("md_setcqloractrl"),

    /** 电台参数设置 */
    MD_GET_RADIO_CTRL("md_getcqradioctrl"),
    MD_SET_RADIO_CTRL("md_setcqradioctrl"),

    /** 预警报警开关（仅适用于监测设备） */
    MD_GET_ALRAM_BROADCAST_SWITCH("md_getcqbroadcastswitch"),
    MD_SET_ALRAM_BROADCAST_SWITCH("md_setcqbroadcastswitch"),

    /** 预警广播参数配置 */
    MD_GET_ALRAM_BROADCAST_CTRL("md_getcqalarmctrl"),
    MD_SET_ALRAM_BROADCAST_CTRL("md_setcqalarmctrl"),

    /** 预警广播触发值配置 */
    MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE("md_getcqgateval"),
    MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE("md_setcqgateval"),

    /** 预警广播上报间隔配置 */
    MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL("md_getcqreptgap"),
    MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL("md_setcqreptgap"),

    /** 预警广播测试 */
    MD_TEST_ALRAM_BROADCAST("md_testbroadcast"),

    /** 配置参数初始值指令 */
    MD_SET_SENSOR_INITIAL("md_cfginitval"),

    /** 米度一体式泥位计流量计算参数查询 */
    MD_GET_MUD_LEVEL_METER_SENSOR("md_getsense"),

    /** 米度一体式泥位计上报模式配置 */
    MD_SET_MUD_LEVEL_METER_SENSOR("md_setsense"),

    /** 米度一体式泥位计差分定位模式 */
    MD_DIFF_LOCATE("md_updatealt"),

    /** 米度一体式泥位计 */
    UD_MD_SET_MODULE_GAP("md_setmodulegap"),

    /** 一体式设备 485 端口参数配置 */
    UD_MD_GET_RS485_PARAM("md_getrs485"),
    UD_MD_SET_RS485_PARAM("md_setrs485"),

    /** 一体式设备雨量计参数配置 */
    UD_MD_GET_RAIN_GAUGE_PARAM("md_getrain"),
    UD_MD_SET_RAIN_GAUGE_PARAM("md_setrain"),

    /** 米度一体式裂缝计零位校准 */
    MD_GET_LF_ZERO_VALUE("md_getlfzerovalue"),
    MD_SET_LF_ZERO_VALUE("md_setlfzerovalue"),

    /** 获取裂缝初始值 */
    LF_MD_GET_INITIAL_VALUE("md_getlfinitial"),

    /** 手动设置裂缝初始值 */
    LF_MD_MANUAL_SET_INITIAL_VALUE("md_setlfinitial"),

    /** 自动设置裂缝初始值 */
    LF_MD_AUTO_SET_INITIAL_VALUE("md_setlfinitialauto"),

    /** 关闭语音播报 */
    SET_VOICE_BROADCAST_VOLUME_OFF("volumeoff"),

    /** 设置语音播报音量 */
    SET_VOICE_BROADCAST_VOLUME("volume"),
    SET_VOICE_BROADCAST_VOLUME_LEVEL("volumelevel"),


    /** 指令透传 */
    MD_RAW("md_raw"),

    /** 自定义心跳包 */
    HEART_BEAT("ble_keepalive"),

    /** 长度不够指令头最低长度要求 */
    LENGTH_INVALID("length_invalid"),

    /** 设置指令类型，在解析设置指令响应结果时使用 */
    COMMON_SETTING_COMMAND("common_setting_command"),

    /** 未知的指令类型 */
    UNKNOWN_TYPE("unknown_type");

    override fun toString(): String {
        return value
    }

    companion object {
        /**
         * 从字符串解析指令类型
         * @param value 指令字符串
         * @return 对应的指令类型,如果未找到则返回Unknown
         */
        fun fromString(value: String): IOTCommandType {
            return entries.firstOrNull { it.value == value } ?: UNKNOWN_TYPE
        }
    }
}
