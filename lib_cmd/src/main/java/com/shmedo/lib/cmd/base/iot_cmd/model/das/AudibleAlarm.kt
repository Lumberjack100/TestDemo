package com.shmedo.lib.cmd.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：     声光报警器信息
 */
data class AudibleAlarm(
    var alarmstatus: String = "0",//声光报警器开启状态 0 关闭 1 开启
    var screenstatus: String = "0", //电子点阵屏开启状态 0 关闭 1 开启
    var alarmtype: String = "0",//类型  0  降雨量  1 水位
    var alarmaddr: String = "", //声光报警器地址
    var level1: String = "", //1级预警值
    var level2: String = "", //2级预警值
    var level3: String = "", //3级预警值
    var playtime: String = "", //播放时长 秒
    var playgap: String = "", //播放间隙 秒
    var volume: String = "0", //音量大小
    var screenaddr: String = "", //电子屏地址
    var showtime: String = "", //电子屏显示时长 秒
    var showgap: String = "", //电子屏熄屏时长 秒
    var mcuaddr: String = "", //MCU 地址
)