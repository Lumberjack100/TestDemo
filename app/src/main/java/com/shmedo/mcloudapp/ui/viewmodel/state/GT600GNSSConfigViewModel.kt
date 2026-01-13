package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: GT600 GNSS 配置页面 ViewModel
 *
 * 包含四个分组的数据字段:
 * 1. 卫星信息 - 截至高度角（暂不支持配置）
 * 2. 双天线参数 - 功能开关、上报频率、天线间距
 * 3. RTCM参数 - 观测值(OBS)频率、星历值(EHP)频率
 * 4. NMEA参数 - GPGGA/GPRMC/GPVGT/GPGSV/GPGSA 五项频率
 */
class GT600GNSSConfigViewModel : BaseStateViewModel() {

    // ========== 1. 卫星信息 ==========
    /** 截至高度角（单位：度） */
    val elevationAngle = NonNullObservableField("15°")

    // ========== 2. 双天线参数 ==========
    /** 功能开关: true-开启, false-关闭 */
    val dualAntennaSwitch = NonNullObservableField(false)

    /** 上报频率(秒) */
    val dualAntennaReportFreq = NonNullObservableField("")

    /** 天线间距(cm) */
    val antennaDistance = NonNullObservableField("")

    // ========== 3. RTCM 参数 ==========
    /** 观测值(OBS)输出频率 */
    val rtcmObsTime = NonNullObservableField("")

    /** 星历值(EHP)输出频率 */
    val rtcmEphTime = NonNullObservableField("")

    // ========== 4. NMEA 参数 ==========
    /** GPGGA 位置信息频率 */
    val gpggaFreq = NonNullObservableField("")

    /** GPRMC 最简导航传输信息频率 */
    val gprmcFreq = NonNullObservableField("")

    /** GPVGT 地面速度信息频率 */
    val gpvgtFreq = NonNullObservableField("")

    /** GPGSV 可视卫星状态频率 */
    val gpgsvFreq = NonNullObservableField("")

    /** GPGSA 参与定位卫星以及DOP值等信息频率 */
    val gpgsaFreq = NonNullObservableField("")

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    /**
     * 保存初始状态，用于检测数据是否被修改
     */
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            // 卫星信息-截至高度角
            "elevationAngle" to elevationAngle.get(),
            // 双天线参数
            "dualAntennaSwitch" to dualAntennaSwitch.get().toString(),
            "dualAntennaReportFreq" to dualAntennaReportFreq.get(),
            "antennaDistance" to antennaDistance.get(),
            // RTCM 参数
            "rtcmObsTime" to rtcmObsTime.get(),
            "rtcmEphTime" to rtcmEphTime.get(),
            // NMEA 参数
            "gpggaFreq" to gpggaFreq.get(),
            "gprmcFreq" to gprmcFreq.get(),
            "gpvgtFreq" to gpvgtFreq.get(),
            "gpgsvFreq" to gpgsvFreq.get(),
            "gpgsaFreq" to gpgsaFreq.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    /**
     * 注册字段变更监听器
     */
    override fun registerField() {
        val allFields = listOf(
            elevationAngle,
            dualAntennaSwitch,
            dualAntennaReportFreq,
            antennaDistance,
            rtcmObsTime,
            rtcmEphTime,
            gpggaFreq,
            gprmcFreq,
            gpvgtFreq,
            gpgsvFreq,
            gpgsaFreq
        )

        allFields.forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    /**
     * 更新数据修改状态
     */
    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "elevationAngle" -> elevationAngle.get() != value
                "dualAntennaSwitch" -> dualAntennaSwitch.get().toString() != value
                "dualAntennaReportFreq" -> dualAntennaReportFreq.get() != value
                "antennaDistance" -> antennaDistance.get() != value
                "rtcmObsTime" -> rtcmObsTime.get() != value
                "rtcmEphTime" -> rtcmEphTime.get() != value
                "gpggaFreq" -> gpggaFreq.get() != value
                "gprmcFreq" -> gprmcFreq.get() != value
                "gpvgtFreq" -> gpvgtFreq.get() != value
                "gpgsvFreq" -> gpgsvFreq.get() != value
                "gpgsaFreq" -> gpgsaFreq.get() != value
                else -> false
            }
        }
    }
}
