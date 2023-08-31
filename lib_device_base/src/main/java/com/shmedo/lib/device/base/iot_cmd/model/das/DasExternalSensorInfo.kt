package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils
import java.io.Serializable

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/21 <br></br>
 * 描述：    DAS 扩展传感器参数
 */
class DasExternalSensorInfo : Serializable {
    var index //传感器接入顺序（第一支、第二支...）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var type //传感器类型
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var addr //传感器地址/通道
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var threshold //触发值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var corrval //修正值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var spacing //测段长
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var holenum //测孔编号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var tubealti //安装高程
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ropelen //安装绳长
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var poly_a //多项式系数A
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var poly_b //多项式系数B
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var poly_c //多项式系数C
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var temp_k //温度系数K
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var temp_t0 //初始温度T0
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var sens_k //灵敏度K
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var temp_b //温度系数b
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var referval_f //基准值F
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var elastic_mod //膨胀系数(应力计)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    //量水堰计特有
    var lsycsds //初始读数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lsyysst //初始堰上水头
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    //倾角仪特有
    var initvalx //X轴初始值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var initvaly //Y轴初始值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var initvalz //z轴初始值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var child_type //子传感器类型/采集器类型
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field

    //阵列测斜仪特有
    var datatype // 解算方式 0:顶部  1: 底部
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var measinval // 测量间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var model_type // 模型切换 0:坐标模型  1: ADME 模型
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}