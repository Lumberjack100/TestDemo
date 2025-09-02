package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702DataCenterParamViewModel : BaseDataCenterParamViewModel() {
    val isDataNetOpened = NonNullObservableField(true) // 4G 是否开启
    val isWiredNetOpened = NonNullObservableField(true) // 有线 是否开启

    val communicateWay = NonNullObservableField("") // 通信方式
    val ipLeve = NonNullObservableField("")// IP 网络协议

    /** SL651 水文协议特有配置参数 */
    val stationType = NonNullObservableField("") //SL651 测站分类
    val centerStationAddr = NonNullObservableField("") //SL651 中心站地址
    val password = NonNullObservableField("") //SL651 密码
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
    val guangdongWaterPlatformStationType = NonNullObservableField("") // 测站类型

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isCenterOpened" to isCenterOpened.get(),
            "centerServerAddress" to centerServerAddress.get(),
            "centerServerPort" to centerServerPort.get(),
            "communicateWay" to communicateWay.get(),
            "ipLeve" to ipLeve.get(),
            "transferProtocol" to transferProtocol.get(),
            "dataProtocol" to dataProtocol.get(),
            "platformType" to platformType.get(),

            "productId" to productId.get(),
            "deviceId" to deviceId.get(),
            "deviceKey" to deviceKey.get(),
            "registerCode" to registerCode.get(),
            "registerAddress" to registerAddress.get(),
            "registerPort" to registerPort.get(),

            "stationType" to stationType.get(),
            "centerStationAddr" to centerStationAddr.get(),
            "password" to password.get(),
            "telemetryStationAddr" to telemetryStationAddr.get(),
            "hourlyReport" to hourlyReport.get(),
            "timingReport" to timingReport.get(),
            "addReport" to addReport.get(),
            "maintainReport" to maintainReport.get(),
            "maintainReportInterval" to maintainReportInterval.get(),
            "reissuingDataValidDays" to reissuingDataValidDays.get(),
            "reissuingDataInterval" to reissuingDataInterval.get(),

            "guangdongWaterPlatformStationType" to guangdongWaterPlatformStationType.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isCenterOpened,

            centerServerAddress,
            centerServerPort,
            communicateWay,
            ipLeve,
            transferProtocol,
            dataProtocol,
            platformType,

            productId,
            deviceId,
            deviceKey,
            registerCode,
            registerAddress,
            registerPort,

            stationType,
            centerStationAddr,
            password,
            telemetryStationAddr,
            hourlyReport,
            timingReport,
            addReport,
            maintainReport,
            maintainReportInterval,
            reissuingDataValidDays,
            reissuingDataInterval,

            guangdongWaterPlatformStationType
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "isCenterOpened" -> isCenterOpened.get() != value

                "centerServerAddress" -> centerServerAddress.get() != value
                "centerServerPort" -> centerServerPort.get() != value
                "communicateWay" -> communicateWay.get() != value
                "ipLeve" -> ipLeve.get() != value
                "transferProtocol" -> transferProtocol.get() != value
                "dataProtocol" -> dataProtocol.get() != value
                "platformType" -> platformType.get() != value

                "productId" -> productId.get() != value
                "deviceId" -> deviceId.get() != value
                "deviceKey" -> deviceKey.get() != value
                "registerCode" -> registerCode.get() != value
                "registerAddress" -> registerAddress.get() != value
                "registerPort" -> registerPort.get() != value

                "stationType" -> stationType.get() != value
                "centerStationAddr" -> centerStationAddr.get() != value
                "password" -> password.get() != value
                "telemetryStationAddr" -> telemetryStationAddr.get() != value
                "hourlyReport" -> hourlyReport.get() != value
                "timingReport" -> timingReport.get() != value
                "addReport" -> addReport.get() != value
                "maintainReport" -> maintainReport.get() != value
                "maintainReportInterval" -> maintainReportInterval.get() != value
                "reissuingDataValidDays" -> reissuingDataValidDays.get() != value
                "reissuingDataInterval" -> reissuingDataInterval.get() != value

                "guangdongWaterPlatformStationType" -> guangdongWaterPlatformStationType.get() != value
                else -> false
            }
        }
    }
}
