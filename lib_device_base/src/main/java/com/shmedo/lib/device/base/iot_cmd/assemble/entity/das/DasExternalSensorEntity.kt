package com.shmedo.lib.device.base.iot_cmd.assemble.entity.das

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/2/1
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DasExternalSensorEntity(
    var index: String = "",//传感器接入顺序（第一支、第二支...）
    var type: String = IOTConstants.NULL_KEY,//传感器类型
    var addr: String = IOTConstants.NULL_KEY,//传感器地址/通道
    var threshold: String = IOTConstants.NULL_KEY,//触发值
    var corrval: String = IOTConstants.NULL_KEY,//修正值
    var spacing: String = IOTConstants.NULL_KEY,//测段长
    var holenum: String = IOTConstants.NULL_KEY,//测孔编号
    var tubealti: String = IOTConstants.NULL_KEY,//安装高程
    var ropelen: String = IOTConstants.NULL_KEY,//安装绳长
    var poly_a: String = IOTConstants.NULL_KEY,//多项式系数A
    var poly_b: String = IOTConstants.NULL_KEY,//多项式系数B
    var poly_c: String = IOTConstants.NULL_KEY,//多项式系数C
    var temp_k: String = IOTConstants.NULL_KEY,//温度系数K
    var temp_t0: String = IOTConstants.NULL_KEY,//初始温度T0
    var sens_k: String = IOTConstants.NULL_KEY,//灵敏度K
    var temp_b: String = IOTConstants.NULL_KEY,//温度系数b
    var referval_f: String = IOTConstants.NULL_KEY,//基准值F
    var elastic_mod: String = IOTConstants.NULL_KEY,//膨胀系数(应力计)

    //量水堰计特有
    var lsycsds: String = IOTConstants.NULL_KEY,//初始读数
    var lsyysst: String = IOTConstants.NULL_KEY,//初始堰上水头

    //倾角仪特有
    var initvalx: String = IOTConstants.NULL_KEY,//X轴初始值
    var initvaly: String = IOTConstants.NULL_KEY,//Y轴初始值
    var initvalz: String = IOTConstants.NULL_KEY,//z轴初始值

    var child_type: String = IOTConstants.NULL_KEY,//子传感器类型/采集器类型

    //阵列测斜仪特有
    var datatype: String = IOTConstants.NULL_KEY,// 解算方式 0:顶部  1: 底部
    var measinval: String = IOTConstants.NULL_KEY,// 测量间隔
    var model_type: String = IOTConstants.NULL_KEY,// 模型切换 0:坐标模型  1: ADME 模型
    var initval: String = IOTConstants.NULL_KEY, // 初始值

    var caddr: String = IOTConstants.NULL_KEY,//测点编码
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
