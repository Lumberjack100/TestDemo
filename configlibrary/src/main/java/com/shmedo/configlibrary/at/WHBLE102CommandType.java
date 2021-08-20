package com.shmedo.configlibrary.at;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     TODO
 */
public enum WHBLE102CommandType {

    /**
     * 进入命令模式
     */
    ENTER_COMMAND("+++a"),

    /**
     * 查询/设置模块的名称
     */
    NAME("NAME"),

    /**
     * 查询/设置模块的工作模式
     */
    MODE("MODE"),

    /**
     * 查询模块 MAC 地址
     */
    MAC("MAC"),

    /**
     * 查询软件版本号
     */
    CIVER("CIVER"),

    /**
     * 查询/设置模块发射功率
     */
    TPL("TPL"),

    /**
     * 查询/设置是否使能连接验证，当开启使能后主机连接到使能验证的从机后需要在十秒钟内
     * 发送 6 位通讯密码，超时或者密码错误会被断开连接
     */
    PASSEN("PASSEN"),

    /**
     * 设置/查询模块串口参数
     */
    UART("UART"),

    /**
     * 设置进入超低功耗模式
     */
    DEEPSLEEP("DEEPSLEEP"),

    /**
     * 退出命令模式退出命令模式
     */
    ENTM("ENTM"),

    /**
     * 恢复出厂默认参数
     */
    RELOAD("RELOAD"),

    /**
     * 控制模块重启
     */
    Z("Z"),

    /**
     * 查询模块连接状态
     */
    LINK("LINK"),

    /**
     * 搜索周围的从机
     */
    SCAN("SCAN"),

    /**
     * 通过搜索到索引号快速建立连接
     */
    CONN("CONN"),

    /**
     * 设置/查询设备上电默认连接模块的 MAC 地址
     */
    CONNADD("CONNADD"),

    /**
     * 设置断开当前连接
     */
    DISCONN("DISCONN"),

    /**
     * 使能/禁用断线自动重连功能
     */
    AUTOCONN("AUTOCONN"),

    /**
     * 设置/查询设备串口服务 UUID
     */
    UUID("UUID"),

    /**
     * 允许连接
     */
    ALLOW_CONNECT("allow_connect"),

    /**
     * 允许蓝牙通讯
     */
    ALLOW_BLUETOOTH_COMMUNICATION("allow_bluetooth_communication"),

    /**
     * 查询测斜仪电池电压
     */
    QUERY_BATTERY_VOLTAGE("query_battery_voltage"),

    /**
     * 查询测量数据
     */
    QUERY_MEASUREMENT_DATA("query_measurement_data"),

    /**
     * 查询测斜仪工作模式
     */
    QUERY_WORK_MODE("query_work_mode"),

    /**
     * 设置测斜仪工作模式
     */
    SET_WORK_MODE("set_work_mode"),

    /**
     * 查询蓝牙无通讯休眠时间
     */
    QUERY_SLEEP_TIME("query_sleep_time"),

    /**
     * 设置蓝牙无通讯休眠时间
     */
    SET_SLEEP_TIME("set_sleep_time"),

    /**
     * 查询蓝牙开机等待连接时间
     */
    QUERY_WAITING_LINK_TIME("query_waiting_link_time"),

    /**
     * 设置蓝牙开机等待连接时间
     */
    SET_WAITING_LINK_TIME("set_waiting_link_time"),

    /**
     * 未知的命令类型
     */
    UNKNOWN_TYPE("unknown_type");


    private String value;

    WHBLE102CommandType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
