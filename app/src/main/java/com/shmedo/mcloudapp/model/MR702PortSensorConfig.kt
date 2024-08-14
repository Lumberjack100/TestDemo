package com.shmedo.mcloudapp.model

import android.os.Parcelable
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
    var portName: String = "",
    var sensors: List<SensorModel> = listOf(),
): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class SensorModel(
    var sensorName: String = "",
    var sensorType: String = "",
    var modelToken: String = "",
    var modelFieldList: List<ModelField> = listOf(),
): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ModelField(
    val fieldName: String = "",
    val engUnit: String = "",
): Parcelable
