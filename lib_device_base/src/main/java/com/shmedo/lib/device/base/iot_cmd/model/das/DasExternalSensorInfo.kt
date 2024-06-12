package com.shmedo.lib.device.base.iot_cmd.model.das

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/21 <br></br>
 * 描述：    DAS 扩展传感器参数
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class DasExternalSensorInfo(
    var index: String = "",//传感器接入顺序（第一支、第二支...）
    var type: String = "",//传感器类型
    var addr: String = "",//传感器地址/通道
    var threshold: String = "",//触发值
    var corrval: String = "",//修正值
    var spacing: String = "",//测段长
    var holenum: String = "",//测孔编号
    var tubealti: String = "",//安装高程
    var ropelen: String = "",//安装绳长
    var poly_a: String = "",//多项式系数 A
    var poly_b: String = "",//多项式系数 B
    var poly_c: String = "",//多项式系数 C
    var temp_k: String = "",//温度系数 K
    var temp_t0: String = "",//初始温度 T0
    var sens_k: String = "",//灵敏度 K
    var temp_b: String = "",//温度系数 B
    var referval_f: String = "0",//基准值 F
    var elastic_mod: String = "",//膨胀系数(应力计)

    //倾角仪
    var initvalx: String = "",//X轴初始值
    var initvaly: String = "",//Y轴初始值
    var initvalz: String = "",//z轴初始值

    //量水堰计
    var lsycsds: String = "",//初始读数
    var lsyysst: String = "",//初始堰上水头
    var caddr: String = "",//测点编码

    //静力水准/沉降仪
    var initval: String = "", //初始值

    var child_type: String = "",//子传感器类型/采集器类型

    //阵列测斜仪
    var datatype: String = "",// 解算方式 0:顶部  1: 底部
    var measinval: String = "",// 测量间隔
    var model_type: String = "",// 模型切换 0:坐标模型  1: ADME 模型

    var collectorModel: String = "",//采集器类型

) : Parcelable