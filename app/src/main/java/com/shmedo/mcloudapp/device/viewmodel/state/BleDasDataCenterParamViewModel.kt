package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class BleDasDataCenterParamViewModel : BaseDataCenterParamViewModel() {
    val isMqttAutoRegister = NonNullObservableField(false)//是否自动注册

    val transferProtocolCode = NonNullObservableField("")//传输协议
    val keepAlive = NonNullObservableField("")// KeepAlive维持上报间隔
    val sn = NonNullObservableField("")//设备SN号

    /**
     * MQTT 手动注册配置参数
     */
    val account = NonNullObservableField("")//账号
    val password = NonNullObservableField("")//密码
}