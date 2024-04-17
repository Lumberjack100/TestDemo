package com.shmedo.lib.device.base.md_cmd.model.das

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 * 创建时间：2024/4/17
 * 描述： TODO
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class MDDasExternalSensorInfo(
    var collectorModel: String = "",//采集器类型
    var sensorAddress: String = "",//传感器地址/通道
    var sensorType: String = "",//传感器类型
    var triggerThreshold: String = "", //触发阈值
    var correctionValue: String = "", //修正值
    var wireRopeLength: String = "", //绳长
    var installElevation: String = "", //安装高程
    var measuringSectionLength: String = "", //测段长
    var polynomialRatioA: String = "",//多项式系数A
    var polynomialRatioB: String = "",//多项式系数B
    var polynomialRatioC: String = "",//多项式系数C
    var temperatureCoefficientK: String = "",//温度系数K
    var temperatureCoefficientT0: String = "",//初始温度T0
    var sensitivityK: String = "",//灵敏度K
    var temperatureCoefficientB: String = "",//温度系数b
    var referenceValueF: String = "",//基准值F
    var expansionCoefficient: String = "",//膨胀系数(应力计)

    //量水堰计特有
    var lsycsds: String = "",//初始读数
    var lsyysst: String = "",//初始堰上水头

    //倾角仪特有
    var initvalx: String = "",//X轴初始值
    var initvaly: String = "",//Y轴初始值
    var initvalz: String = "",//z轴初始值

    //静力水准仪特有
    var initialValue: String = "", // 初始值

): Parcelable
