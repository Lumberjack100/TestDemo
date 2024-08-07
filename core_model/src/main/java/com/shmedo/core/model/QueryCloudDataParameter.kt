package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 创建者:   dpc
 * 创建时间:  2019-12-16
 * 描述：   查询设备参数实体类
 */
@JsonClass(generateAdapter = true)
data class QueryCloudDataParameter(
    var sn: String = "",
    var begin: String = "",
    var end: String = "",
    var number: String = "",
    var iotData: String = ""
)
