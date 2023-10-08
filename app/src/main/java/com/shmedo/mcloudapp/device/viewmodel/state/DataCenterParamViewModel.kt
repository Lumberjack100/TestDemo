package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/7 <br/>
 * 描述：     TODO
 */
open class DataCenterParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isCenterOpened = NonNullObservableField(true)

    val centerName = NonNullObservableField("数据中心1")
    val centerStatus = NonNullObservableField("已开启")
    val centerServerAddress = NonNullObservableField("")//数据服务器地址
    val centerServerPort = NonNullObservableField("")//数据服务器端口
    val communicateWay = NonNullObservableField("有线")//通信方式
    val ipType = NonNullObservableField("IPV4")
    val transferProtocol = NonNullObservableField("")//传输协议
    val dataProtocol = NonNullObservableField("")//数据协议
    val platformType = NonNullObservableField("")//平台类型

    /**
     * MQTT 协议特有配置参数
     */
    val productId = NonNullObservableField("")//产品ID
    val deviceId = NonNullObservableField("")//设备 Id
    val deviceKey = NonNullObservableField("")//设备Key
    val registerCode = NonNullObservableField("")//注册码
    val registerAddress = NonNullObservableField("")//注册地址
    val registerPort = NonNullObservableField("")//注册端口

    /**
     * SL651 水文协议特有配置参数
     */
    val stationType = NonNullObservableField("")//测站分类
    val centerStationAddr = NonNullObservableField("")//中心站地址
    val password = NonNullObservableField("")//密码
    val stationCode = NonNullObservableField("")//测站编码
    val isAdvancedItemVisible = NonNullObservableField(false)//测站编码
    val hourlyReport = NonNullObservableField(false)//小时报开启标识
    val timingReport = NonNullObservableField(false)//定时报开启标识
    val addReport = NonNullObservableField(false)//加报报开启标识
    val maintainReport = NonNullObservableField(false)//维持报开启标识
    val maintainReportInterval = NonNullObservableField("")//维持上报间隔
    val reissuingDataValidDays = NonNullObservableField("")//数据补发有效天数
    val reissuingDataInterval = NonNullObservableField("")//数据补发间隔
}