package com.shmedo.lib.core.base.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/18 <br></br>
 * 描述：     查询设备列表接口返回的设备信息实体
 */
@Parcelize
@JsonClass(generateAdapter = true)
class DeviceInfo(
    val id: Int = 0,
    val companyID: Int = 0,
    val companyName: String = "",
    val deviceToken: String = "",
    val deviceName: String = "",
    val deviceDesc: String = "",
    val installLocation: String = "",
    val gpsLocation: String = "",
    val onlineStatus: Boolean = false,
    val deviceStatus: String = "",
    val allowWarn: Boolean = false,
    val exValues: String = "",
    val apiKey: String = "",
    val productID: Int = 0,
    val productToken: String = "",
    val productName: String = "",
    val productType: String = "",
    val firmwareVersion: String = "",
    val lastActiveTime: String = "",
) : Parcelable