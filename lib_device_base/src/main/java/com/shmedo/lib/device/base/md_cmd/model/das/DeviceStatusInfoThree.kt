package com.shmedo.lib.device.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 *
 * 应答:$$043,(1),(2),(3),(4),(5)<br/>
 * （1）SN号<br/>
 * （2）采集器型号<br/>
 * （3）采集器地址(当采集器地址为0时，关闭采集功能)<br/>
 * （4）传感器状态，用冒号分隔的字符串<br/>
 * ①:②:③，其中 ①：传感器地址，②：传感器状态，0正常，1异常，2 不展示 ③：传感器数据<br/>
 * （5）传感器状态，和（2）格式相同，<br/>
 * 注：传感器状态可能有很多个，有接入传感器个数决定。<br/>
 */
data class DeviceStatusInfoThree(
    val snNumber: String = "",
    val collectorModel: String = "",
    val collectorAddress: String = "",
    val sensorStatus: List<String> = emptyList()
)
