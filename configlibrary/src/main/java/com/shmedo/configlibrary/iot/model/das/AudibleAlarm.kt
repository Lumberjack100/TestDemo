package com.shmedo.configlibrary.iot.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：     声光报警器信息
 */
class AudibleAlarm {
    var channel: String = "" //通信信道

    var panid: String = "" //网络编号

    var groupid: String = ""//目标地址

    var alarmtype: String = ""//报警类型
}