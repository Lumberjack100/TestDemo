package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/7 <br/>
 * 描述：     TODO
 */
open class BaseDataCenterParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isCenterOpened = NonNullObservableField(true)
    val centerName = NonNullObservableField("")
    val centerStatus = NonNullObservableField("已开启")
    val centerServerAddress = NonNullObservableField("")//数据服务器地址
    val centerServerPort = NonNullObservableField("")//数据服务器端口
    val transferProtocol = NonNullObservableField("")//传输协议
    val dataProtocol = NonNullObservableField("")//数据协议
    val platformType = NonNullObservableField("")//平台类型

    /**
     * MQTT 协议特有配置参数
     */
    val isMqttItemVisible = NonNullObservableField(false)
    val productId = NonNullObservableField("")//产品ID
    val deviceId = NonNullObservableField("")//设备 Id
    val deviceKey = NonNullObservableField("")//设备Key
    val registerCode = NonNullObservableField("")//注册码
    val registerAddress = NonNullObservableField("")//注册地址
    val registerPort = NonNullObservableField("")//注册端口
}