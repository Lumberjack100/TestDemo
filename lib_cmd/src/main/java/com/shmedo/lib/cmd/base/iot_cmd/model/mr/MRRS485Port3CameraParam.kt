package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/20 <br/>
 * 描述：     RS485-3-摄像头参数
 */
data class MRRS485Port3CameraParam(
    var index: String = "",       // 序号  0，1，2对应序号1、2、3
    var status: String = "",      // 是否接入 0未接入 1已接入
    var switch: String = "",      // 功能开关 0关 1开
    var addr: String = "",        // 地址码
    var baud: String = "",        // 波特率
    var databit: String = "",     // 数据位
    var parity: String = "",      // 校验位 0:NONE 1:ODD 2:EVEN 3:MARK 4:SPACE
    var stopbit: String = "",     // 停止位 0:1位 1:1.5位 2:2位
    var type: String = "",        // 摄像头类型 0:SXH485-H200
    var resolut: String = "",     // 分辨率 1:640*480 2:1280*720 3:1920*1080
    var quality: String = "",     // 压缩比
    var interval: String = "",    // 拍照间隔（分钟）
    var workmode: String = ""     // 工作模式 0:正常 1:应急
)