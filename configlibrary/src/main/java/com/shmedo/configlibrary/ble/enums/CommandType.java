package com.shmedo.configlibrary.ble.enums;

/**
 * Created by adu on 2017/12/11.
 */
public enum CommandType {

    /**1.系统参数配置与查询**/
    /**
     * 获取基础配置信息
     */
    BASE_CONFIG("000"),
    /**
     * 设置六位目标北斗卡号
     */
    SIX_TARGER_BD_NUMBER("001"),
    /**
     * 设置数据通讯模式
     */
    DATA_MASSAGE_MODEL("003"),
    /**
     * 工作模式
     */
    WORK_MODE("006"),
    /**
     * 北斗电源开关
     */
    BD_POWER_SWITCH("007"),
    /**
     * 重启设备
     */
    REBOOT_DEVICE("008"),
    /**
     * 设置本地时间，年月日时分秒（各两位）
     */
    LOCAL_TIME("010"),
    /**
     * 设置心跳包发送间隔
     */
    HEARTBEAT_SEND_INTERVAL("013"),
    /**
     * 系统运行状态
     */
    SYSTEM_RUN_STATE("014"),
    /**
     * 设置电池过放保护电压
     */
    CELL_PROTECTION_VOLTAGE("015"),
    /**
     * 设置GPS定位
     */
    SETTING_GPS_POSITION("016"),
    /**
     * 设置低功耗模式
     */
    LOW_ENERGY("018"),
    /**
     * 保存配置信息
     */
    SAVE_CONFIG_INFO("019"),
    /**
     * 设置传感器接口类型
     */
    SENSOR_INTERFACE_TYPE("020"),
    /**
     * 设置数据传输协议
     */
    DATA_TRANSFER_PROTOCOL("030"),
    /**
     * 获取版本信息
     */
    VERSION_MESSAGE("040"),
    /**
     * DAS 状态1
     */
    QUERY_DAS_STATUS_1("041"),
    /**
     * DAS 状态2
     */
    QUERY_DAS_STATUS_2("042"),
    /**
     * DAS 状态3
     */
    QUERY_DAS_STATUS_3("043"),
    /**
     * 查询网络状态
     */
    QUERY_NETWORK_STATUS("044"),
    /**
     * 查询太阳能控制器信息
     */
    QUERY_SOLAR_CONTROLLER_INFO("045"),
    /**
     * 即时采集
     */
    INSTANT_COLLEACTOR("110"),
    /**
     * 设置传感器接口波特率
     */
    SENSOR_BAUD_RATE("114"),
    /**
     * 恢复出厂设置
     */
    RESTORE_FACTORY_SETTING("119"),
    /**
     * 设置远程升级
     */
    SETTING_REMOTE_UPGRADE("120"),
    /**
     * 查询/同步安装位置
     */
    INSTALL_LOCATION("916"),

    /**2.传感器参数配置与查询**/
    /**
     * 设置开关量
     */
    RAIN_STATION("005"),
    /**
     * 获取采集器配置
     */
    COLLECTOR_CONFIG("100"),
    /**
     * 获取XX采集器YY通道的传感器参数
     */
    COLLECTOR_CHANNEL_SENSOR_PARAMETER("101"),
    /**
     * 设置雨量计精度
     */
    SETTING_RAIN_PRECISION("121"),
    /**
     * 设置数据上报间隔
     */
    DATA_REPORT_INTERVAL("143"),
    /**
     * 设置采集器地址
     */
    SET_COLLECTOR_ADDRESS("147"),
    /**
     * 设置采集器接入的传感器
     */
    SET_COLLECTOR_SENSOR("150"),
    /**
     * 设置采集器待机时长（单位s）
     */
    COLLECTOR_STANDBY_TIME("160"),
    /**
     * 设置采集器采集频度（单位ms）
     */
    COLLECTOR_FREQUENCY("161"),
    /**
     * 设置采集器接入传感器的报警值（单位由传感器类型决定）。
     */
    COLLECTOR_SENSOR_THRESHOLD("162"),
    /**
     * 设置采集器解算频度（单位s）
     */
    COLLECTOR_SOLUTION_FREQUENCY("163"),
    /**
     * 设置采集器接入传感器修正值（只有墒情计用到3个修正值，其他传感器只用到一个修正值）
     */
    COLLECTOR_SENSOR_REVISED("165"),
    /**
     * 设置测斜仪测段长（单位MM）（测斜采集器特有参数）
     */
    SET_INCLINOMETER_LONG("166"),
    /**
     * 设置振弦式传感器修正参数 (模拟量采集器特有参数)
     */
    VIBRATING_SENSOR_PARAMETER("167"),
    /**
     * 设置采集器接入传感器触发阈值（目前仅适用于墒情采集器）
     */
    COLLECTOR_SENSOR_THRESHOLD_SOLI("168"),
    /**
     * 设置振弦式传感器安装高程
     */
    SENSOR_INSTALLELEVATION("169"),
    /**
     * DAS发送认证请求
     */
    DAS_SEND_AUTHENTICATION_REQUEST("222"),
    /**
     * DAS发送认证结果
     */
    DAS_SEND_AUTHENTICATION_RESULT("223"),
    /**
     * 平台收到认证请求若需要配置，发送验证码
     */
    AUTHENTICATION_CONFIG("224"),
    /**
     * 设备锁定状态
     */
    DEVICE_LOCK_STATUS("225"),
    /**
     * 日志输出状态
     */
    LOG_OUTPUT_STATUS("226"),
    /**
     * 断线报警器状态
     */
    BREAK_ALARM_STATUS("227"),
    /**
     * 获取所有采集器配置
     */
    GET_ALL_SENSOR_CONFIG("333"),
    /**
     * 设置GPRS持续在线时长
     */
    SET_GPRS_ONLINE_TIME("334"),
    /**
     * 批处理开始指令
     */
    BATCH_BEGIN("335"),
    /**
     * 查询数字式渗压计参数
     */
    QUERY_OSMOMETER_PARAMETER("400"),
    /**
     * 开启/关闭数字式渗压计功能
     */
    DIGITAL_OSMOMETER_FUNCTION("401"),
    /**
     * 设置数字渗压计地址
     */
    SET_OSMOMETER_ADDRESS("402"),
    /**
     * 设置数字渗压计深度触发值，温度触发值
     */
    SET_OSMOMETER_TRIGGER("403"),
    /**
     * 设置数字渗压计深度修正值，温度修正值
     */
    SET_OSMOMETR_CORRECT("404"),
    /**
     * 设置渗压计绳长
     */
    SET_CORD_LENGTH("405"),
    /**
     * 设置管口高程
     */
    SET_OSMOMETR_NOZZEL_HEIGHT("406"),


    /**3.数据中心参数配置和查询**/
    /**
     * 获取服务器地址
     */
    SERVER_ADDRESS("200"),
    /**
     * 设置服务器地址、端口（x，Y，z之间由空格隔开）
     */
    SET_SERVER_ADDRESS_PORT("201"),
    /**
     * 网络链路通信协议查询与设置
     */
    NET_LINK_COMMUN_PROTOCOL("202"),
    /**
     * 设置授权手机号码（最大支持3个，号码之间用逗号隔开）
     */
    SET_AUTHORIZE_PHONE("203"),
    /**
     * 获取授权手机号码
     */
    AUTHORIZE_PHONE_NUMBER("204"),
    /**
     * 自动注册平台参数设置
     */
    SET_AUTO_REGISTRATION_PLATFORM_PARAM("803"),
    /**
     * 自动注册平台参数查询
     */
    QUERY_AUTO_REGISTRATION_PLATFORM_PARAM("804"),
    /**
     * 手动注册平台参数设置
     */
    SET_MANUAL_REGISTRATION_PLATFORM_PARAM("805"),
    /**
     * 手动注册平台参数查询
     */
    QUERY_MANUAL_REGISTRATION_PLATFORM_PARAM("806"),
    /**
     * 自动注册服务器地址、端口设置
     */
    SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT("807"),
    /**
     * 自动注册服务器地址、端口查询
     */
    QUERY_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT("808"),
    /**
     * MQTTKeepAlive值查询与设置
     */
    MQTT_KEEP_ALIVE("809"),
    /**
     * 自动注册平台查询与设置
     */
    AUTO_REGISTRATION_PLATFORM("810"),
    /**
     * appKey(米度/北京平台特有)设置
     */
    SET_MEDO_PLATFORM_APPKEY("811"),
    /**
     * appKey(米度/北京平台特有)查询
     */
    QUERY_MEDO_PLATFORM_APPKEY("812"),
    /**
     * 查询数据中心参数
     */
    QUERY_DATA_CENTER_PARAM("889"),

    /**
     * 自定义心跳包指令
     */
    HEARTBEAT("888"),

    UNKNOWN(""),
    /**
     * 批处理结束指令
     */
    BATCH_END("999336");


    private String commandCode;

    CommandType(String commandCode) {
        this.commandCode = commandCode;
    }

    @Override
    public String toString() {
        return this.commandCode;
    }
}