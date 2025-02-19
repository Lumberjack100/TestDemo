package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
class DataCenterParamViewModel : BaseDataCenterParamViewModel() {
    val isDataTypeVisible = NonNullObservableField(false)
    val isRegisterVisible = NonNullObservableField(false)

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


    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isEditable" to isEditable.get(),
            "isCenterOpened" to isCenterOpened.get(),
            "centerName" to centerName.get(),
            "centerStatus" to centerStatus.get(),

            "centerServerAddress" to centerServerAddress.get(),
            "centerServerPort" to centerServerPort.get(),
            "transferProtocol" to transferProtocol.get(),
            "dataType" to dataType.get(),
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
            "reissuingDataInterval" to reissuingDataInterval.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isEditable,
            isCenterOpened,
            centerName,
            centerStatus,

            centerServerAddress,
            centerServerPort,
            transferProtocol,
            dataType,
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
            reissuingDataInterval
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
                "isEditable" -> isEditable.get() != value
                "isCenterOpened" -> isCenterOpened.get() != value
                "centerName" -> centerName.get() != value
                "centerStatus" -> centerStatus.get() != value

                "centerServerAddress" -> centerServerAddress.get() != value
                "centerServerPort" -> centerServerPort.get() != value
                "transferProtocol" -> transferProtocol.get() != value
                "dataType" -> dataType.get() != value
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
                else -> false
            }
        }
    }
}