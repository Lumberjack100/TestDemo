package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702DataCenterParamViewModel : DataCenterParamViewModel() {
    /**
     * SL651 水文协议特有配置参数
     */
    val isSL651ItemVisible = NonNullObservableField(false)
    val stationType = NonNullObservableField("")//测站分类码
    val centerStationAddr = NonNullObservableField("")//中心站地址
    val password = NonNullObservableField("")//密码
    val telemetryStationAddr = NonNullObservableField("")//测站编码(遥测站地址)
    val isAdvancedItemVisible = NonNullObservableField(false)//测站编码
    val hourlyReport = NonNullObservableField(false)//小时报开启标识
    val timingReport = NonNullObservableField(false)//定时报开启标识
    val addReport = NonNullObservableField(false)//加报报开启标识
    val maintainReport = NonNullObservableField(false)//维持报开启标识
    val maintainReportInterval = NonNullObservableField("")//维持上报间隔
    val reissuingDataValidDays = NonNullObservableField("")//数据补发有效天数
    val reissuingDataInterval = NonNullObservableField("")//数据补发间隔
}