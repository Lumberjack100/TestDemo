package com.shmedo.mcloudapp.utils

/**
 * 一体化雷达设备状态处理工具类
 */
object UDDeviceStatusHelper {

    /**
     * 处理设备异常信息
     * @param deviceError 设备故障信息
     * @param deviceWarn 设备告警信息
     * @return 异常信息列表
     */
    fun processAbnormalInfo(
        deviceError: Map<String, String>? = null,
        deviceWarn: Map<String, String>? = null
    ): List<String> {
        val errorInfoList = mutableListOf<String>()

        deviceError?.let { resultMap ->
            resultMap["bat"]?.let { errorInfoList.add("电池故障") }
            resultMap["ld"]?.let { errorInfoList.add("雷达故障") }
            resultMap["cam"]?.let { errorInfoList.add("摄像头故障") }
            resultMap["gnss"]?.let { errorInfoList.add("GNSS故障") }
            resultMap["qj"]?.let { errorInfoList.add("加速度计故障") }
            resultMap["4G"]?.let { errorInfoList.add("4G故障") }
            resultMap["bt"]?.let { errorInfoList.add("蓝牙故障") }
            resultMap["radio"]?.let { errorInfoList.add("电台故障") }
            resultMap["flash"]?.let { errorInfoList.add("存储故障") }
            resultMap["ath"]?.let { errorInfoList.add("温湿度故障") }
        }

        deviceWarn?.let { resultMap ->
            resultMap["loc_offset"]?.let { errorInfoList.add("位置偏移") }
            resultMap["angle_offset"]?.let { errorInfoList.add("角度偏移") }
            resultMap["extern_volt"]?.let { volt -> errorInfoList.add(if (volt == "-1") "外部电压过高" else "外部电压过低") }
            resultMap["bat_cap"]?.let { errorInfoList.add("电池电量过低") }
            resultMap["bat_temp"]?.let { errorInfoList.add("电池温度过高") }
            resultMap["bat_health"]?.let { errorInfoList.add("电池容量过低") }
            resultMap["inside_temp"]?.let { temp -> errorInfoList.add(if (temp == "-1") "内部温度过高" else "内部温度过低") }
            resultMap["sim_card"]?.let { errorInfoList.add("无SIM卡") }
        }

        return errorInfoList
    }
} 