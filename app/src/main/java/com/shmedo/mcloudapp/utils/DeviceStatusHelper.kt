package com.shmedo.mcloudapp.utils

import com.shmedo.lib.device.base.iot_cmd.enums.AdmeModuleErrorType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： TODO
 */
object DeviceStatusHelper {
    fun checkDeviceAbnormal(selfCheck: String): ArrayList<String> {
        //"self_check": "GPS:1,eMMC:1,4g:1,RTC:1,solar485:0,G-Sensor:1,BT:1,GNSS:1,QMC:0,SHT21:1,product_time:20240411"
        val deviceAbnormalList: ArrayList<String> = ArrayList()
        //解析 self_check,根据逗号分隔，取出各个传感器的状态
        selfCheck.split(",".toRegex()).dropLastWhile { it.isEmpty() }.forEach { item ->
            val errors = item.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            when (errors[0].uppercase()) {
                "4G" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("4G 异常")
                    }
                }

                "SCL" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("倾角加速度异常")
                    }
                }

                "LORA" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("LoRa 异常")
                    }
                }

                "BT" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("蓝牙异常")
                    }
                }

                "LD" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("雷达异常")
                    }
                }

                "RADIO" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("电台异常")
                    }
                }

                "CAM" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("相机异常")
                    }
                }

                "GNSS" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("GNSS 异常")
                    }
                }

                "ADC" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("电压采集功能异常")
                    }
                }

                "EMMC" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("EMMC 异常")
                    }
                }

                "SHT21" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("温湿度异常")
                    }
                }

                "QMC5883" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("磁力异常")
                    }
                }

                "SOLAR485" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("太阳能控制器异常")
                    }
                }

                "battery" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("电池异常")
                    }
                }

                "SIMCARD" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("SIM 卡异常")
                    }
                }

                "fram" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("铁电存储器异常")
                    }
                }

                "RTC" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("系统异常")
                    }
                }

                else -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("${errors[0]} 异常")
                    }
                }

            }
        }
        return deviceAbnormalList
    }

    fun checkDeviceAbnormal(currentStateInfo: CommonCurrentStateInfo2): ArrayList<String> {
        val deviceAbnormalList: ArrayList<String> = ArrayList()

        if (currentStateInfo._4g != IOTConstants.NULL_KEY && !currentStateInfo._4g.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("4G 异常")
        }
        if (currentStateInfo.scl != IOTConstants.NULL_KEY && !currentStateInfo.scl.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("倾角加速度异常")
        }
        //lora LORA模块
        if (currentStateInfo.lora != IOTConstants.NULL_KEY && !currentStateInfo.lora.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("LoRa 异常")
        }
        //bt 蓝牙模块
        if (currentStateInfo.bt != IOTConstants.NULL_KEY && !currentStateInfo.bt.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("蓝牙异常")
        }
        //ld 雷达
        if (currentStateInfo.ld != IOTConstants.NULL_KEY && !currentStateInfo.ld.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("雷达异常")
        }
        //radio 电台模块
        if (currentStateInfo.radio != IOTConstants.NULL_KEY && !currentStateInfo.radio.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电台异常")
        }
        //cam相机
        if (currentStateInfo.cam != IOTConstants.NULL_KEY && !currentStateInfo.cam.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("相机异常")
        }
        //gnss 
        if (currentStateInfo.gnss != IOTConstants.NULL_KEY && !currentStateInfo.gnss.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("GNSS 异常")
        }
        if (currentStateInfo.adc != IOTConstants.NULL_KEY && !currentStateInfo.adc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电压采集异常")
        }
        if (currentStateInfo.emmc != IOTConstants.NULL_KEY && !currentStateInfo.emmc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("存储器异常")
        }
        if (currentStateInfo.sht21 != IOTConstants.NULL_KEY && !currentStateInfo.sht21.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("温湿度异常")
        }
        if (currentStateInfo.qmc5883 != IOTConstants.NULL_KEY && !currentStateInfo.qmc5883.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("磁力异常")
        }
        //battery 电池
        if (currentStateInfo.battery != IOTConstants.NULL_KEY && !currentStateInfo.battery.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电池异常")
        }
        //simCard sim卡
        if (currentStateInfo.simCard != IOTConstants.NULL_KEY && !currentStateInfo.simCard.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("SIM 卡异常")
        }
        //flash
        if (currentStateInfo.flash != IOTConstants.NULL_KEY && !currentStateInfo.flash.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("存储器异常")
        }
        //fram FRAM
        if (currentStateInfo.fram != IOTConstants.NULL_KEY && !currentStateInfo.fram.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("铁电存储器异常")
        }
        //rtc RTC状态
        if (currentStateInfo.rtc != IOTConstants.NULL_KEY && !currentStateInfo.rtc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("系统时钟异常")
        }

        return deviceAbnormalList
    }

    fun checkAdmeDeviceAbnormal(abndiasis: String): ArrayList<String> {
        if (abndiasis.isEmpty() || abndiasis == "0")
            return ArrayList()

        val deviceAbnormalList: ArrayList<String> = ArrayList()
        abndiasis.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.forEach { code ->
            val errorType = AdmeModuleErrorType.valueByCode(code)
            if (errorType != AdmeModuleErrorType.NORMAL && errorType != AdmeModuleErrorType.EMPTY_ERROR) {
                deviceAbnormalList.add(if (errorType == AdmeModuleErrorType.UNKNOWN_ERROR) "未知异常,异常代码: $code" else errorType.description)
            }
        }

        return deviceAbnormalList
    }
}