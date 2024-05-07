package com.shmedo.mcloudapp.utils

import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： TODO
 */
object DeviceStatusHelper {

    fun checkDeviceAbnormal(currentStateInfo: CommonCurrentStateInfo2): ArrayList<String> {
        val deviceAbnormalList: ArrayList<String> = ArrayList()

        if (currentStateInfo._4g != IOTConstants.NULL_KEY && !currentStateInfo._4g.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("4G 状态异常")
        }
        if (currentStateInfo.scl != IOTConstants.NULL_KEY && !currentStateInfo.scl.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("SCL 倾角加速度状态异常")
        }
        //lora LORA模块
        if (currentStateInfo.lora != IOTConstants.NULL_KEY && !currentStateInfo.lora.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("LORA 状态异常")
        }
        //bt 蓝牙模块
        if (currentStateInfo.bt != IOTConstants.NULL_KEY && !currentStateInfo.bt.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("蓝牙状态异常")
        }
        //ld 雷达状态
        if (currentStateInfo.ld != IOTConstants.NULL_KEY && !currentStateInfo.ld.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("雷达状态异常")
        }
        //radio 电台模块
        if (currentStateInfo.radio != IOTConstants.NULL_KEY && !currentStateInfo.radio.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电台状态异常")
        }
        //cam相机状态
        if (currentStateInfo.cam != IOTConstants.NULL_KEY && !currentStateInfo.cam.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("相机状态异常")
        }
        //gnss 状态
        if (currentStateInfo.gnss != IOTConstants.NULL_KEY && !currentStateInfo.gnss.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("GNSS 状态异常")
        }
        if (currentStateInfo.adc != IOTConstants.NULL_KEY && !currentStateInfo.adc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("ADC 电压采集功能状态异常")
        }
        if (currentStateInfo.emmc != IOTConstants.NULL_KEY && !currentStateInfo.emmc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("EMMC 状态异常")
        }
        if (currentStateInfo.sht21 != IOTConstants.NULL_KEY && !currentStateInfo.sht21.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("sht21 温湿度状态异常")
        }
        if (currentStateInfo.qmc5883 != IOTConstants.NULL_KEY && !currentStateInfo.qmc5883.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("qmc5883 磁力状态异常")
        }
        //battery 电池状态
        if (currentStateInfo.battery != IOTConstants.NULL_KEY && !currentStateInfo.battery.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电池状态异常")
        }
        //simCard sim卡状态
        if (currentStateInfo.simCard != IOTConstants.NULL_KEY && !currentStateInfo.simCard.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("SIM 卡状态异常")
        }
        //flash
        if (currentStateInfo.flash != IOTConstants.NULL_KEY && !currentStateInfo.flash.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("FLASH 状态异常")
        }
        //fram FRAM状态
        if (currentStateInfo.fram != IOTConstants.NULL_KEY && !currentStateInfo.fram.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("FRAM 状态异常")
        }
        //rtc RTC状态
        if (currentStateInfo.rtc != IOTConstants.NULL_KEY && !currentStateInfo.rtc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("RTC 状态异常")
        }

        return deviceAbnormalList
    }

}