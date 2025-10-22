package com.shmedo.mcloudapp.utils

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.AdmeModuleErrorType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UProductCurrentStateInfo

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

                "MEMS" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("倾角加速度模块故障")
                    }
                }

                "LORA" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("LORA模块故障")
                    }
                }

                "BT" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("蓝牙模块故障")
                    }
                }

                "LD" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("雷达模块故障")
                    }
                }

                "RADIO" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("电台模块故障")
                    }
                }

                "CAM" -> {
                    if (errors.size > 1 && errors[1] == "0") {
                        deviceAbnormalList.add("摄像头模块故障")
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

    fun checkDeviceAbnormal(currentStateInfo: UProductCurrentStateInfo): ArrayList<String> {
        val deviceAbnormalList: ArrayList<String> = ArrayList()

        if (currentStateInfo._4g.uppercase().contains("FAIL")
        ) {
            deviceAbnormalList.add("4G模块故障")
        }
        if (currentStateInfo.scl.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("倾角加速度模块故障")
        }
        //lora LORA模块
        if (currentStateInfo.lora.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("LORA模块故障")
        }
        //bt 蓝牙模块
        if (currentStateInfo.bt.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("蓝牙模块故障")
        }
        //ld 雷达
        if (currentStateInfo.ld.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("雷达模块故障")
        }
        //radio 电台模块
        if (currentStateInfo.radio.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("电台模块故障")
        }
        //cam相机
        if (currentStateInfo.cam.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("摄像头模块故障")
        }
        //gnss 
        if (currentStateInfo.gnss.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("GNSS模块故障")
        }
        if (currentStateInfo.adc.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("电压采集故障")
        }
        if (currentStateInfo.emmc.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("存储卡故障")
        }
        if (currentStateInfo.sht21.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("温湿度模块故障")
        }
        if (currentStateInfo.qmc5883.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("磁力计故障")
        }
        //battery 电池
        if (currentStateInfo.battery.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("电池故障")
        }
        //simCard sim卡
        if (currentStateInfo.simCard.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("SIM卡故障")
        }
        //flash
        if (currentStateInfo.flash.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("FLASH故障")
        }
        //fram FRAM
        if (currentStateInfo.fram.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("铁电存储器故障")
        }
        //rtc RTC状态
        if (currentStateInfo.rtc.uppercase().contains("FAIL")) {
            deviceAbnormalList.add("系统时钟故障")
        }

        return deviceAbnormalList
    }

    /**
     * 兼容方法：通过JSON字符串检查设备异常状态
     * @param statusInfo  JSON 字符串
     * @return 设备异常信息列表
     */
    fun checkM50Abnormal(statusInfo: String): ArrayList<String> {
        return try {
            // 将 JSON 字符串解析为 Map 对象
            val statusMap = MoshiUtil.fromJson<Map<String, Any>>(statusInfo)
            if (statusMap != null) {
                checkM50AbnormalFromMap(statusMap)
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
    private fun checkM50AbnormalFromMap(statusMap: Map<String, Any>): ArrayList<String> {
        val deviceAbnormalList: ArrayList<String> = ArrayList()

        // 检查4G状态
        val _4g = statusMap["4g"] as? String
        if (_4g?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("4G模块故障")
        }

        // 检查倾角加速度状态
        val scl = statusMap["scl"] as? String
        if (scl?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("倾角加速度模块故障")
        }

        // 检查LORA模块
        val lora = statusMap["lora"] as? String
        if (lora?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("LORA模块故障")
        }

        // 检查蓝牙模块
        val bt = statusMap["bt"] as? String
        if (bt?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("蓝牙模块故障")
        }

        // 检查雷达
        val ld = statusMap["ld"] as? String
        if (ld?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("雷达模块故障")
        }

        // 检查电台模块
        val radio = statusMap["radio"] as? String
        if (radio?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("电台模块故障")
        }

        // 检查相机
        val cam = statusMap["cam"] as? String
        if (cam?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("摄像头模块故障")
        }

        // 检查GNSS
        val gnss = statusMap["gnss"] as? String
        if (gnss?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("GNSS模块故障")
        }

        // 检查电压采集
        val adc = statusMap["adc"] as? String
        if (adc?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("电压采集故障")
        }

        // 检查存储器
        val emmc = statusMap["emmc"] as? String
        if (emmc?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("存储卡故障")
        }

        // 检查存储器
        val sdCard = statusMap["sd"] as? String
        if (sdCard?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("存储卡故障")
        }

        // 检查温湿度
        val sht21 = statusMap["sht21"] as? String
        if (sht21?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("温湿度模块故障")
        }

        // 检查电池信息
        val batteryList = statusMap["battery"] as? List<*>
        if (batteryList != null && batteryList.isNotEmpty()) {
            // 检查内部电池（第一个元素）
            val internalBattery = batteryList[0] as? Map<*, *>
            if (internalBattery != null) {
                val internalBatteryStatus = internalBattery["battery_status"]?.toString() ?: "0"
                if (internalBatteryStatus == "-1") {
                    deviceAbnormalList.add("内部电池故障")
                }
            }

            // 检查备用电池（第二个元素）
            if (batteryList.size > 1) {
                val backupBattery = batteryList[1] as? Map<*, *>
                if (backupBattery != null) {
                    val backupBatteryStatus = backupBattery["battery_status"]?.toString() ?: "0"
                    if (backupBatteryStatus == "-1") {
                        deviceAbnormalList.add("备用电池故障")
                    }
                }
            }
        }

        // 检查SIM卡
        val simCard = statusMap["simCard"] as? String
        if (simCard?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("SIM卡故障")
        }

        // 检查FLASH
        val flash = statusMap["flash"] as? String
        if (flash?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("FLASH故障")
        }

        // 检查系统时钟
        val rtc = statusMap["rtc"] as? String
        if (rtc?.uppercase()?.contains("FAIL") ?: false) {
            deviceAbnormalList.add("系统时钟故障")
        }

        return deviceAbnormalList
    }

    /**
     * 通过JSON字符串检查M50设备告警状态
     * @param statusInfo M50CurrentStateInfo 的 JSON 字符串
     * @return 设备告警信息列表
     */
    fun checkM50Warn(statusInfo: String): ArrayList<String> {
        return try {
            // 将 JSON 字符串解析为 Map 对象
            val statusMap = MoshiUtil.fromJson<Map<String, Any>>(statusInfo)
            if (statusMap != null) {
                checkM50WarnFromMap(statusMap)
            } else {
                ArrayList<String>()
            }
        } catch (e: Exception) {
            // 异常处理：返回空列表
            ArrayList<String>()
        }
    }
    /**
     * 通过Map检查M50设备告警状态
     * @param statusMap 状态信息Map
     * @return 设备告警信息列表
     */
    private fun checkM50WarnFromMap(statusMap: Map<String, Any>): ArrayList<String> {
        val deviceWarnList: ArrayList<String> = ArrayList()

        // 1. 检查光伏板电压 (solar_volt)
        val solarVolt = statusMap["solar_volt"] as? String
        val solarVoltage = solarVolt?.toDoubleOrNull() ?: 0.0
        if (solarVoltage > 0 && solarVoltage < 9) {
            deviceWarnList.add("光伏板电压过低")
        }

        // 2. 检查外部电压 (ext_power_volt)
        val extPowerVolt = statusMap["ext_power_volt"] as? String
        val externalVoltage = extPowerVolt?.toDoubleOrNull() ?: 0.0
        if (externalVoltage > 0 && externalVoltage < 11) {
            deviceWarnList.add("外部输入电压过低")
        }

        // 3. 检查电池信息
        val batteryList = statusMap["battery"] as? List<*>
        if (batteryList != null && batteryList.isNotEmpty()) {
            // 检查内部电池（第一个元素）
            val internalBattery = batteryList[0] as? Map<*, *>
            if (internalBattery != null) {
                val internalBatteryStatus = internalBattery["battery_status"]?.toString() ?: "0"
                val isInternalBatteryFailed = internalBatteryStatus == "-1"

                if (!isInternalBatteryFailed) {
                    // 内部电池电量检查
                    val internalBatteryCap =
                        internalBattery["battery_cap"]?.toString()?.toDoubleOrNull() ?: 100.0
                    if (internalBatteryCap < 20) {
                        deviceWarnList.add("内部电池电量过低")
                    }

                    // 内部电池健康度检查
                    val internalBatteryHealth =
                        internalBattery["battery_health"]?.toString()?.toDoubleOrNull() ?: 100.0
                    if (internalBatteryHealth < 80) {
                        deviceWarnList.add("内部电池健康度过低")
                    }
                }
            }

            // 检查备用电池（第二个元素）
            if (batteryList.size > 1) {
                val backupBattery = batteryList[1] as? Map<*, *>
                if (backupBattery != null) {
                    val backupBatteryStatus = backupBattery["battery_status"]?.toString() ?: "0"
                    val isBackupBatteryFailed = backupBatteryStatus == "-1"

                    if (!isBackupBatteryFailed) {
                        // 备用电池电量检查
                        val backupBatteryCap =
                            backupBattery["battery_cap"]?.toString()?.toDoubleOrNull() ?: 100.0
                        if (backupBatteryCap < 20) {
                            deviceWarnList.add("备用电池电量过低")
                        }

                        // 备用电池健康度检查
                        val backupBatteryHealth =
                            backupBattery["battery_health"]?.toString()?.toDoubleOrNull() ?: 100.0
                        if (backupBatteryHealth < 80) {
                            deviceWarnList.add("备用电池健康度过低")
                        }
                    }
                }
            }
        }

        return deviceWarnList
    }
    /**
     * 合并故障和告警信息，并进行过滤
     * @param abnormalList 故障信息列表
     * @param warnList 告警信息列表
     * @return 过滤后的合并列表
     */
    fun mergeM50StatusInfo(abnormalList: ArrayList<String>, warnList: ArrayList<String>): ArrayList<String> {
        val mergedList = ArrayList<String>()
        
        // 添加所有故障信息
        mergedList.addAll(abnormalList)
        
        // 检查电池故障状态
        val hasInternalBatteryFault = abnormalList.any { it.contains("内部电池故障") }
        val hasBackupBatteryFault = abnormalList.any { it.contains("备用电池故障") }
        
        // 过滤告警信息
        warnList.forEach { warn ->
            val shouldAdd = when (warn) {
                "内部电池电量过低", "内部电池健康度过低" -> !hasInternalBatteryFault
                "备用电池电量过低", "备用电池健康度过低" -> !hasBackupBatteryFault
                else -> true
            }
            
            if (shouldAdd) {
                mergedList.add(warn)
            }
        }
        
        return mergedList
    }

    /**
     * 通过JSON字符串检查M20设备告警状态
     * @param statusInfo CommonCurrentStateInfo 的 JSON 字符串
     * @return 设备告警信息列表
     */
    fun checkM20Warn(statusInfo: String): ArrayList<String> {
        return try {
            // 将 JSON 字符串解析为 Map 对象
            val statusMap = MoshiUtil.fromJson<Map<String, Any>>(statusInfo)
            if (statusMap != null) {
                checkM20WarnFromMap(statusMap)
            } else {
                ArrayList<String>()
            }
        } catch (e: Exception) {
            // 异常处理：返回空列表
            ArrayList<String>()
        }
    }

    /**
     * 通过Map检查M20设备告警状态
     * @param statusMap 状态信息Map
     * @return 设备告警信息列表
     */
    private fun checkM20WarnFromMap(statusMap: Map<String, Any>): ArrayList<String> {
        val deviceWarnList: ArrayList<String> = ArrayList()

        // 检查外部电压 (ext_power_volt)
        val extPowerVolt = statusMap["ext_power_volt"] as? String
        val externalVoltage = extPowerVolt?.toDoubleOrNull() ?: 0.0
        if (externalVoltage > 0 && externalVoltage < 11) {
            deviceWarnList.add("外部输入电压过低")
        }

        return deviceWarnList
    }

    /**
     * 合并故障和告警信息，并进行过滤
     * @param abnormalList 故障信息列表
     * @param warnList 告警信息列表
     * @return 过滤后的合并列表
     */
    fun mergeM20StatusInfo(abnormalList: ArrayList<String>, warnList: ArrayList<String>): ArrayList<String> {
        val mergedList = ArrayList<String>()

        // 添加所有故障信息
        mergedList.addAll(abnormalList)

        // 添加所有告警信息
        mergedList.addAll(warnList)

        return mergedList
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