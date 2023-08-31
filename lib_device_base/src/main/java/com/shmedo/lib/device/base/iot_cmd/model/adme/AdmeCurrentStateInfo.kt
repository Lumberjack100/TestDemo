package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：     ADME 设备当前状态
 */
class AdmeCurrentStateInfo {
    var sn //设备SN号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var productid //产品型号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var simid //物联网卡号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var imeid // IMEI卡号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var firversion //固件版本
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ctrinputv // CTR输入电压
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var driveinputv //驱动器输入电压
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var inctype //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var incnum //测斜仪信道号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var incvoltage //测斜仪电压
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var temperature //设备温度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var humidity //设备湿度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var intertempe //测斜仪管温度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var signalstr //4G信号强度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var incloc // 测斜仪位置信息
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var abndiasis //设备异常诊断
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downnum //设备下降次数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var testway //工作模式(0:常规测量模式，1：特定点位模式，2：静态测量模式，3：设备停用模式)）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}