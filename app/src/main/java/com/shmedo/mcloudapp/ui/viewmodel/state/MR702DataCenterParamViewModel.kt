package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702DataCenterParamViewModel : BaseDataCenterParamViewModel() {
    val isDataNetOpened = NonNullObservableField(true) // 4G 是否开启
    val isWiredNetOpened = NonNullObservableField(true) // 有线 是否开启

    val communicateWay = NonNullObservableField("") // 通信方式
    val ipLeve = NonNullObservableField("")

    /** SL651 水文协议特有配置参数 */
    val isSL651ItemVisible = NonNullObservableField(false)
    val stationType = NonNullObservableField("") // 测站分类
    val centerStationAddr = NonNullObservableField("") // 中心站地址
    val password = NonNullObservableField("") // 密码
    val telemetryStationAddr = NonNullObservableField("") // 测站编码(遥测站地址)

    val isAdvancedItemVisible = NonNullObservableField(false) // 高级设置是否可见
    val hourlyReport = NonNullObservableField(false) // 小时报开启标识
    val timingReport = NonNullObservableField(true) // 定时报开启标识
    val addReport = NonNullObservableField(false) // 加报报开启标识
    val maintainReport = NonNullObservableField(true) // 维持报开启标识
    val maintainReportInterval = NonNullObservableField("") // 维持上报间隔（秒）
    val reissuingDataValidDays = NonNullObservableField("") // 数据补发有效天数
    val reissuingDataInterval = NonNullObservableField("") // 数据补发间隔（分钟）

    /** 广东水文平台特有配置参数 */
    val guangdongWaterPlatformStationType = NonNullObservableField("") // 测站分类
}
