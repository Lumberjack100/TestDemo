package com.shmedo.configlibrary.iot.cmd.entity.vms

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/23/20 <br></br>
 * 描述：     设置Vms终端某个通道下传感器参数
 */
class SetVmsTerminalSensorParamsEntity : Validater {
    var sn: String? = null
    var channel //传感器所在通道
            : String? = null
    var insert //接入判断，0：未接入，1：接入
            : String? = null
    var freqtype //激励类型，默认4（频率反馈固定频率扫频法）
            : String? = null
    var freqmax //频率上限，默认2000
            : String? = null
    var freqmin //频率下限，默认1000
            : String? = null
    var volttype //激励电压类型，0：低压，1：高压，默认0
            : String? = null
    var expvolt //期望电压,高压激励时的期望电压，默认150
            : String? = null
    var type //传感器类型，默认55，振弦式裂缝计
            : String? = null
    var name //传感器名称，默认102_x，x为通道号
            : String? = null
    var gateval //触发阈值，默认10
            : String? = null
    var corral //修正值，默认为0
            : String? = null
    var fixsite //安装高程,默认为0，单位m
            : String? = null
    var ropelen //绳长，默认为0，单位m
            : String? = null
    var parama //修正系数A
            : String? = null
    var paramb //修正系数B
            : String? = null
    var paramc //修正系数C
            : String? = null
    var paramk //修正系数K
            : String? = null
    var paramm //修正系数M
            : String? = null
    var paramf //基准值
            : String? = null
    var paramt //初始温度
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            stringBuilder.append("sn")
            stringBuilder.append("=")
            stringBuilder.append(sn)
            stringBuilder.append("&")
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (f.name != "sn" && value != null && value != "NullKey") {
                    stringBuilder.append(f.name)
                    stringBuilder.append("=")
                    stringBuilder.append(value)
                    stringBuilder.append("&")
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        }
        return stringBuilder.toString()
    }
}