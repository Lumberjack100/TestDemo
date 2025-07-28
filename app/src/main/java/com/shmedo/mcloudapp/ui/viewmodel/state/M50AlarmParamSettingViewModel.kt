package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: M50报警参数设置 ViewModel
 */
class M50AlarmParamSettingViewModel : BaseStateViewModel() {
    // 电台模块状态，用于控制UI启用状态
    val isRadioEnable = NonNullObservableField(true)

    val isOpened = NonNullObservableField(true) // 本地报警启用

    //监测点编号 [1~15] 默认01
    val monitorPoint = NonNullObservableField("1")

    //播报次数 [0~255] 其中0表示关闭当前报警，255表示一直报警，默认03
    val broadcastTimes = NonNullObservableField("3")

    //一级报警语音编号  [1~255] 默认 4
    val firstAlarmVoice = NonNullObservableField("4")

    //二级报警语音编号  [1~255] 默认 3
    val secondAlarmVoice = NonNullObservableField("3")

    //三级报警语音编号  [1~255] 默认 2
    val thirdAlarmVoice = NonNullObservableField("2")

    //四级报警语音编号  [1~255] 默认 1
    val fourthAlarmVoice = NonNullObservableField("1")

    val firstAlarmReportIntervalTitle = NonNullObservableField("一级报警间隔（秒）")
    val secondAlarmReportIntervalTitle = NonNullObservableField("二级报警间隔（秒）")
    val thirdAlarmReportIntervalTitle = NonNullObservableField("三级报警间隔（秒）")
    val fourthAlarmReportIntervalTitle = NonNullObservableField("四级报警间隔（秒）")

    //一级报警上报间隔 默认60,单位s
    val firstAlarmReportInterval = NonNullObservableField("60")
    //二级报警上报间隔 默认300,单位s
    val secondAlarmReportInterval = NonNullObservableField("300")
    //三级报警上报间隔 默认1800,单位s
    val thirdAlarmReportInterval = NonNullObservableField("1800")
    //四级报警上报间隔 默认3600,单位s
    val fourthAlarmReportInterval = NonNullObservableField("3600")

    // 报警阈值相关字段
    val firstAlarmThreshold = NonNullObservableField("800") // 一级报警阈值（毫米）
    val secondAlarmThreshold = NonNullObservableField("400") // 二级报警阈值（毫米）
    val thirdAlarmThreshold = NonNullObservableField("200") // 三级报警阈值（毫米）
    val fourthAlarmThreshold = NonNullObservableField("100") // 四级报警阈值（毫米）

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "monitorPoint" to monitorPoint.get(),
            "broadcastTimes" to broadcastTimes.get(),
            "firstAlarmVoice" to firstAlarmVoice.get(),
            "secondAlarmVoice" to secondAlarmVoice.get(),
            "thirdAlarmVoice" to thirdAlarmVoice.get(),
            "fourthAlarmVoice" to fourthAlarmVoice.get(),
            "firstAlarmReportInterval" to firstAlarmReportInterval.get(),
            "secondAlarmReportInterval" to secondAlarmReportInterval.get(),
            "thirdAlarmReportInterval" to thirdAlarmReportInterval.get(),
            "fourthAlarmReportInterval" to fourthAlarmReportInterval.get(),
            "firstAlarmThreshold" to firstAlarmThreshold.get(),
            "secondAlarmThreshold" to secondAlarmThreshold.get(),
            "thirdAlarmThreshold" to thirdAlarmThreshold.get(),
            "fourthAlarmThreshold" to fourthAlarmThreshold.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isOpened,
            monitorPoint,
            broadcastTimes,
            firstAlarmVoice,
            secondAlarmVoice,
            thirdAlarmVoice,
            fourthAlarmVoice,
            firstAlarmReportInterval,
            secondAlarmReportInterval,
            thirdAlarmReportInterval,
            fourthAlarmReportInterval,
            firstAlarmThreshold,
            secondAlarmThreshold,
            thirdAlarmThreshold,
            fourthAlarmThreshold

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
                "isOpened" -> isOpened.get() != value
                "monitorPoint" -> monitorPoint.get() != value
                "broadcastTimes" -> broadcastTimes.get() != value
                "firstAlarmVoice" -> firstAlarmVoice.get() != value
                "secondAlarmVoice" -> secondAlarmVoice.get() != value
                "thirdAlarmVoice" -> thirdAlarmVoice.get() != value
                "fourthAlarmVoice" -> fourthAlarmVoice.get() != value
                "firstAlarmReportInterval" -> firstAlarmReportInterval.get() != value
                "secondAlarmReportInterval" -> secondAlarmReportInterval.get() != value
                "thirdAlarmReportInterval" -> thirdAlarmReportInterval.get() != value
                "fourthAlarmReportInterval" -> fourthAlarmReportInterval.get() != value
                "firstAlarmThreshold" -> firstAlarmThreshold.get() != value
                "secondAlarmThreshold" -> secondAlarmThreshold.get() != value
                "thirdAlarmThreshold" -> thirdAlarmThreshold.get() != value
                "fourthAlarmThreshold" -> fourthAlarmThreshold.get() != value

                else -> false
            }
        }
    }
}