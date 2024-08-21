package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
class DataCenterParamViewModel: BaseDataCenterParamViewModel() {
    val isDataTypeVisible = NonNullObservableField(false)
    val isRigisterVisible = NonNullObservableField(false)


    /**
     * SL651 水文协议特有配置参数
     */
    val isSL651ItemVisible = NonNullObservableField(false)
    val stationType = NonNullObservableField("")//测站分类
    val centerStationAddr = NonNullObservableField("")//中心站地址
    val password = NonNullObservableField("")//密码
    val telemetryStationAddr = NonNullObservableField("")//测站编码(遥测站地址)
    val isAdvancedItemVisible = NonNullObservableField(false)//高级设置是否可见
    val hourlyReport = NonNullObservableField(false)//小时报开启标识
    val timingReport = NonNullObservableField(false)//定时报开启标识
    val addReport = NonNullObservableField(false)//加报报开启标识
    val maintainReport = NonNullObservableField(false)//维持报开启标识
    val maintainReportInterval = NonNullObservableField("")//维持上报间隔
    val reissuingDataValidDays = NonNullObservableField("")//数据补发有效天数
    val reissuingDataInterval = NonNullObservableField("")//数据补发间隔
}