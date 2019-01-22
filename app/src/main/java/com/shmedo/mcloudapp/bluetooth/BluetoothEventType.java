package com.shmedo.mcloudapp.bluetooth;

/**
 * Created by Liudongdong on 18/2/1.
 * 蓝牙事件类型
 */

public enum BluetoothEventType {
    /**
     * 蓝牙连接成功
     */
    CONNECTED,
    /**
     * 蓝牙连接断开
     */
    DISCONNECTED,
    /**
     * 扫描到蓝牙设备
     */
    DEVICE_FIND,
    /**
     * 从蓝牙读取到数据
     */
    DATA_AVAILABLE,
    /**
     * 等待消息响应超时
     */
    MESSAGE_RESPONSE_TIME_OUT,
    /**
     * 响应没有找到响应的消息
     */
    RESPONSE_WITH_NO_MESSAGE,
    /**
     * 蓝牙消息写入成功
     */
    MESSAGE_WRITE_SUCCESS,
    /**
     * 蓝牙消息写入失败
     */
    MESSAGE_WRITE_FAIL,
    /**
     * 请求设置MTU失败
     */
    REQUEST_MTU_FAIL,
    /**
     * 自动读取蓝牙数据设置成功
     */
    ENABLE_READ_SUCCESS,
    /**
     * 自动读取蓝牙数据设置失败
     */
    ENABLE_READ_FAIL,
    /**
     * 蓝牙服务发现失败
     */
    SERVICE_FIND_FAIL,
    /**
     * 特征读取失败
     */
    CHARACTERISTICS_FIND_FAIL,
    /**
     * 蓝牙状态异常，这种情况，通常需要断开设备，重新连接
     */
    STATE_EXCEPTION,
    /**
     * 等待写入响应超时
     */
    WRITE_TIME_OUT
}
