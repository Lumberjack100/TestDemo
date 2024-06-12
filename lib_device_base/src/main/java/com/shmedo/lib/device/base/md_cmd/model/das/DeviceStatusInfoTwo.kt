package com.shmedo.lib.device.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 *
 * $$042,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(15),(16),(17),(18)\r\n
 * （1）SN号  150000L
 * （2）经度  0.000000
 * （3）纬度  0.000000
 * （4）设备内部电压   8.4
 * （5）设备外部电压   11.9
 * （6）太阳能控制器状态  0
 * （7）太阳能板电压   1.1
 * （8）电池电压   11.9
 * （9）日发电量   0.0
 * （10）日耗电量    0.0
 * （11）机箱内部温湿度状态   0
 * （12）机箱内部温度   23.1
 * （13）机箱内部湿度    35.9
 * （14）机箱外部温湿度状态  0
 * （15）机箱外部温度    23.8
 * （16）机箱外部湿度   35.1
 * （17）开关量类型，1：雨量计，2：关闭，3：断线报警器
 * （18）降雨量或断线报警器状态(1:断开，0：闭合)
 * 示例：
 * $$042,150000L,0.000000,0.000000,8.4,11.9,0,1.1,11.9,0.0,0.0,0,23.1,35.9,0,23.8,35.1,2,0.0
 */
data class DeviceStatusInfoTwo(
    val snNumber: String = "",
    val longitude: String = "",
    val latitude: String = "",
    val internalVoltage: String = "",
    val externalVoltage: String = "",
    val solarControllerStatus: String = "",// 状态 0正常，1异常，2 不展示
    val solarPanelVoltage: String = "",
    val batteryVoltage: String = "",
    val dailyPowerGeneration: String = "",
    val dailyPowerConsumption: String = "",
    val internalTempHumidityStatus: String = "",// 状态 0正常，1异常，2 不展示
    val internalTemperature: String = "",
    val internalHumidity: String = "",
    val externalTempHumidityStatus: String = "", // 状态 0正常，1异常，2 不展示
    val externalTemperature: String = "",
    val externalHumidity: String = "",
    val switchType: String = "",
    val rainfallStatus: String = ""
)
