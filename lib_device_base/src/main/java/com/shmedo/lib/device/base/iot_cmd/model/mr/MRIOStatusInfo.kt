package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     MR 设备的IO状态
 */
data class MRIOStatusInfo(
    var k1: String = "",//输出   1 开启  0 关闭
    var k2: String = "",//输出   1 开启  0 关闭
    var k3: String = "",//输出   1 开启  0 关闭
    var k4: String = "",//输出   1 开启  0 关闭
    var k5: String = "",//输出   1 开启  0 关闭
    var k6: String = "",//输出   1 开启  0 关闭
    var k7: String = "",//输出   1 开启  0 关闭
    var k8: String = "",//输出   1 开启  0 关闭
    var in1: String = "",//输入   1 开启  0 关闭
    var in2: String = "",//输入   1 开启  0 关闭
    var in3: String = "",//输入   1 开启  0 关闭
    var in4: String = "",//输入   1 开启  0 关闭
    var in5: String = "",//输入   1 开启  0 关闭
    var in6: String = "",//输入   1 开启  0 关闭
    var in7: String = "",//输入   1 开启  0 关闭
    var in8: String = "",//输入   1 开启  0 关闭
    var rain: String = "",//雨量   1 开启  0 关闭
    var dry: String = "",//干接点   1 开启  0 关闭
)
