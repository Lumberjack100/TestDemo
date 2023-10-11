package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/27/21 <br></br>
 * 描述：     MR 设备的基本信息
 */
data class MRCommunicationData(
    var status1: String = "",//数据状态  0 未接入 1在线 2 离线
    var status2: String = "",//数据状态  0 未接入 1在线 2 离线
    var status3: String = "",//数据状态  0 未接入 1在线 2 离线
    var status4: String = "",//数据状态  0 未接入 1在线 2 离线
    var status5: String = "",//数据状态  0 未接入 1在线 2 离线
    var agreem1: String = "",//网络协议 1 IPV4 2 IPV6
    var agreem2: String = "",//网络协议 1 IPV4 2 IPV6
    var agreem3: String = "",//网络协议 1 IPV4 2 IPV6
    var agreem4: String = "",//网络协议 1 IPV4 2 IPV6
    var agreem5: String = "",//网络协议 1 IPV4 2 IPV6
    var sdata1: String = "",//发送数据
    var sdata2: String = "",//发送数据
    var sdata3: String = "",//发送数据
    var sdata4: String = "",//发送数据
    var sdata5: String = "",//发送数据
    var ndata1: String = "",//未发送数据
    var ndata2: String = "",//未发送数据
    var ndata3: String = "",//未发送数据
    var ndata4: String = "",//未发送数据
    var ndata5: String = "",//未发送数据
    var rate1: String = "",//在线率 数值(0-100)
    var rate2: String = "",//在线率 数值(0-100)
    var rate3: String = "",//在线率 数值(0-100)
    var rate4: String = "",//在线率 数值(0-100)
    var rate5: String = "",//在线率 数值(0-100)
)