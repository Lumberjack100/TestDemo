package com.shmedo.mcloudapp.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/18 <br/>
 * 描述：    MR702 串口传感器配置信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class MR702PortSensorConfig(
    @Json(name = "sensorList")
    var sensorModelList: List<SensorModel> = listOf(),
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class SensorModel(
    var port: String = "",
    var sensorID: String = "",
    var sensorType: String = "",
    var sensorName: String = "",
    var modelToken: String = "",
    var modelName: String = "",
    var modelFieldList: List<ModelField> = listOf(),
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ModelField(
    val fieldName: String = "",//采集项名称
    val engUnit: String = "",//采集项单位
    val hydrologicalIdentification: String = "",//水文标识
    val collectionInstructions: String = "",//采集指令
    val ratio: String = "",//倍率
    val dataFormat: String = "",//数据类型
    val solutionMethod: String = "",//解算方法
    val triggerValue: String = "",//触发值
    val upperLimit: String = "",//上限
    val lowerLimit: String = "",//下限
    val correctValue: String = "",//修正值
    val ngateval: String = "",//阈值次数
) : Parcelable
