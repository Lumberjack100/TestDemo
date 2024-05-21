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
    val id: Int = 0,//设备ID
    val companyID: Int = 0,//所在公司编号
    val companyName: String = "",//设备ID
    var deviceToken: String = "",//设备SN
    val deviceName: String = "",//设备名称
    val deviceDesc: String = "",//设备描述信息
    val installLocation: String = "",//设备安装位置
    val gpsLocation: String = "",//设备GPS位置
    val onlineStatus: Boolean = false,//在线状态
    val deviceStatus: String = "",//启用状态
    val allowWarn: Boolean = false,//是否推送预警
    val exValues: String = "",//设备拓展属性
    val apikey: String = "",//设备密钥
    val productID: Int = 0,//所属产品编号
    val productToken: String = "",//所属产品标识
    val productName: String = "",//所属产品名称
    val productType: String = "",//所属产品的产品类型
    val productKey: String = "",//所属产品密钥（产品注册码）
    val firmwareVersion: String = "",//固件版本
    val lastActiveTime: String = "",//最后交互时间
    val createTime: String = "",//创建时间
    val followTime: String = "",//用户收藏时间
    val deviceSn: String = "",//设备SN
) : Parcelable