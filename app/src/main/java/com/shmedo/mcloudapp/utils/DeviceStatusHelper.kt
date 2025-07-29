package com.shmedo.mcloudapp.utils

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.AdmeModuleErrorType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

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
                        deviceAbnormalList.add("4G模块故障")
                    }
                }

                "SCL" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("倾角加速度模块故障")
                    }
                }

                "LORA" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("LORA故障")
                    }
                }

                "BT" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("蓝牙模块故障")
                    }
                }

                "LD" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("雷达故障")
                    }
                }

                "RADIO" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("电台故障")
                    }
                }

                "CAM" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("摄像头故障")
                    }
                }

                "GNSS" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("GNSS模块故障")
                    }
                }

                "ADC" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("电压采集故障")
                    }
                }

                "EMMC" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("存储卡故障")
                    }
                }

                "SHT21" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("温湿度模块故障")
                    }
                }

                "QMC5883" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("磁力计故障")
                    }
                }

                "SOLAR485" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("太阳能控制器故障")
                    }
                }

                "BATTERY" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("电池故障")
                    }
                }

                "SIMCARD" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("SIM卡故障")
                    }
                }

                "FRAM" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("铁电存储器故障")
                    }
                }

                "RTC" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("系统故障")
                    }
                }

                else -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("${errors[0]} 故障")
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
            deviceAbnormalList.add("4G模块故障")
        }
        if (currentStateInfo.scl != IOTConstants.NULL_KEY && !currentStateInfo.scl.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("倾角加速度模块故障")
        }
        //lora LORA模块
        if (currentStateInfo.lora != IOTConstants.NULL_KEY && !currentStateInfo.lora.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("LORA故障")
        }
        //bt 蓝牙模块
        if (currentStateInfo.bt != IOTConstants.NULL_KEY && !currentStateInfo.bt.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("蓝牙模块故障")
        }
        //ld 雷达
        if (currentStateInfo.ld != IOTConstants.NULL_KEY && !currentStateInfo.ld.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("雷达故障")
        }
        //radio 电台模块
        if (currentStateInfo.radio != IOTConstants.NULL_KEY && !currentStateInfo.radio.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电台故障")
        }
        //cam相机
        if (currentStateInfo.cam != IOTConstants.NULL_KEY && !currentStateInfo.cam.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("摄像头故障")
        }
        //gnss 
        if (currentStateInfo.gnss != IOTConstants.NULL_KEY && !currentStateInfo.gnss.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("GNSS模块故障")
        }
        if (currentStateInfo.adc != IOTConstants.NULL_KEY && !currentStateInfo.adc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电压采集故障")
        }
        if (currentStateInfo.emmc != IOTConstants.NULL_KEY && !currentStateInfo.emmc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("存储卡故障")
        }
        if (currentStateInfo.sht21 != IOTConstants.NULL_KEY && !currentStateInfo.sht21.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("温湿度模块故障")
        }
        if (currentStateInfo.qmc5883 != IOTConstants.NULL_KEY && !currentStateInfo.qmc5883.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("磁力计故障")
        }
        //battery 电池
        if (currentStateInfo.battery != IOTConstants.NULL_KEY && !currentStateInfo.battery.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("电池故障")
        }
        //simCard sim卡
        if (currentStateInfo.simCard != IOTConstants.NULL_KEY && !currentStateInfo.simCard.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("SIM卡故障")
        }
        //flash
        if (currentStateInfo.flash != IOTConstants.NULL_KEY && !currentStateInfo.flash.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("FLASH故障")
        }
        //fram FRAM
        if (currentStateInfo.fram != IOTConstants.NULL_KEY && !currentStateInfo.fram.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("铁电存储器故障")
        }
        //rtc RTC状态
        if (currentStateInfo.rtc != IOTConstants.NULL_KEY && !currentStateInfo.rtc.uppercase()
                .contains("OK")
        ) {
            deviceAbnormalList.add("系统时钟故障")
        }

        return deviceAbnormalList
    }

    /**
     * 兼容方法：通过JSON字符串检查设备异常状态
     * @param statusInfo CommonCurrentStateInfo2 的 JSON 字符串
     * @return 设备异常信息列表
     */
    fun checkDeviceAbnormal2(statusInfo: String): ArrayList<String> {
        return try {
            // 将 JSON 字符串解析为 Map 对象
            val statusMap = MoshiUtil.fromJson<Map<String, Any>>(statusInfo)
            if (statusMap != null) {
                checkDeviceAbnormalFromMap(statusMap)
            } else {
                ArrayList<String>()
            }
        } catch (e: Exception) {
            // 异常处理：返回包含错误信息的列表
            ArrayList<String>()
        }
    }

    /**
     * 通过Map检查设备异常状态
     * @param statusMap 状态信息Map
     * @return 设备异常信息列表
     */
    private fun checkDeviceAbnormalFromMap(statusMap: Map<String, Any>): ArrayList<String> {
        val deviceAbnormalList: ArrayList<String> = ArrayList()

        // 检查4G状态
        val _4g = statusMap["4g"] as? String
        if (_4g != IOTConstants.NULL_KEY && !(_4g?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("4G模块故障")
        }

        // 检查倾角加速度状态
        val scl = statusMap["scl"] as? String
        if (scl != IOTConstants.NULL_KEY && !(scl?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("倾角加速度模块故障")
        }

        // 检查LORA模块
        val lora = statusMap["lora"] as? String
        if (lora != IOTConstants.NULL_KEY && !(lora?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("LORA故障")
        }

        // 检查蓝牙模块
        val bt = statusMap["bt"] as? String
        if (bt != IOTConstants.NULL_KEY && !(bt?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("蓝牙模块故障")
        }

        // 检查雷达
        val ld = statusMap["ld"] as? String
        if (ld != IOTConstants.NULL_KEY && !(ld?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("雷达故障")
        }

        // 检查电台模块
        val radio = statusMap["radio"] as? String
        if (radio != IOTConstants.NULL_KEY && !(radio?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("电台故障")
        }

        // 检查相机
        val cam = statusMap["cam"] as? String
        if (cam != IOTConstants.NULL_KEY && !(cam?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("摄像头故障")
        }

        // 检查GNSS
        val gnss = statusMap["gnss"] as? String
        if (gnss != IOTConstants.NULL_KEY && !(gnss?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("GNSS模块故障")
        }

        // 检查电压采集
        val adc = statusMap["adc"] as? String
        if (adc != IOTConstants.NULL_KEY && !(adc?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("电压采集故障")
        }

        // 检查存储器
        val emmc = statusMap["emmc"] as? String
        if (emmc != IOTConstants.NULL_KEY && !(emmc?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("存储卡故障")
        }

        // 检查存储器
        val sdCard = statusMap["sd"] as? String
        if (sdCard != IOTConstants.NULL_KEY && !(sdCard?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("存储卡故障")
        }

        // 检查温湿度
        val sht21 = statusMap["sht21"] as? String
        if (sht21 != IOTConstants.NULL_KEY && !(sht21?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("温湿度模块故障")
        }

        // 检查磁力计
        val qmc5883 = statusMap["qmc5883"] as? String
        if (qmc5883 != IOTConstants.NULL_KEY && !(qmc5883?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("磁力计故障")
        }

        // 检查电池
        val battery = statusMap["battery"] as? String
        if (battery != IOTConstants.NULL_KEY && !(battery?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("电池故障")
        }

        // 检查SIM卡
        val simCard = statusMap["simCard"] as? String
        if (simCard != IOTConstants.NULL_KEY && !(simCard?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("SIM卡故障")
        }

        // 检查FLASH
        val flash = statusMap["flash"] as? String
        if (flash != IOTConstants.NULL_KEY && !(flash?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("FLASH故障")
        }

        // 检查铁电存储器
        val fram = statusMap["fram"] as? String
        if (fram != IOTConstants.NULL_KEY && !(fram?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("铁电存储器故障")
        }

        // 检查系统时钟
        val rtc = statusMap["rtc"] as? String
        if (rtc != IOTConstants.NULL_KEY && !(rtc?.uppercase()?.contains("OK") ?: false)) {
            deviceAbnormalList.add("系统时钟故障")
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