package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class ChongQingRadioSettingsViewModel : BaseStateViewModel() {
    val receiveChannel = NonNullObservableField("")//报警接收频点
    val sendChannel = NonNullObservableField("")//广播发射频点
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率

    val telemetryStationNode1 = NonNullObservableField("")//测站1编号
    val telemetryStationNode2 = NonNullObservableField("")//测站2编号
    val telemetryStationNode3 = NonNullObservableField("")//测站3编号
    val telemetryStationNode4 = NonNullObservableField("")//测站4编号
    val telemetryStationNode5 = NonNullObservableField("")//测站5编号
    val telemetryStationNode6 = NonNullObservableField("")//测站6编号
    val telemetryStationNode7 = NonNullObservableField("")//测站7编号
    val telemetryStationNode8 = NonNullObservableField("")//测站8编号
    val telemetryStationNode9 = NonNullObservableField("")//测站9编号
    val telemetryStationNode10 = NonNullObservableField("")//测站10编号

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "receiveChannel" to receiveChannel.get(),
            "sendChannel" to sendChannel.get(),
            "transmitPower" to transmitPower.get(),
            "airSpeed" to airSpeed.get(),
            "telemetryStationNode1" to telemetryStationNode1.get(),
            "telemetryStationNode2" to telemetryStationNode2.get(),
            "telemetryStationNode3" to telemetryStationNode3.get(),
            "telemetryStationNode4" to telemetryStationNode4.get(),
            "telemetryStationNode5" to telemetryStationNode5.get(),
            "telemetryStationNode6" to telemetryStationNode6.get(),
            "telemetryStationNode7" to telemetryStationNode7.get(),
            "telemetryStationNode8" to telemetryStationNode8.get(),
            "telemetryStationNode9" to telemetryStationNode9.get(),
            "telemetryStationNode10" to telemetryStationNode10.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            receiveChannel,
            sendChannel,
            transmitPower,
            airSpeed,
            telemetryStationNode1,
            telemetryStationNode2,
            telemetryStationNode3,
            telemetryStationNode4,
            telemetryStationNode5,
            telemetryStationNode6,
            telemetryStationNode7,
            telemetryStationNode8,
            telemetryStationNode9,
            telemetryStationNode10
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
                "receiveChannel" -> receiveChannel.get() != value
                "sendChannel" -> sendChannel.get() != value
                "transmitPower" -> transmitPower.get() != value
                "airSpeed" -> airSpeed.get() != value
                "telemetryStationNode1" -> telemetryStationNode1.get() != value
                "telemetryStationNode2" -> telemetryStationNode2.get() != value
                "telemetryStationNode3" -> telemetryStationNode3.get() != value
                "telemetryStationNode4" -> telemetryStationNode4.get() != value
                "telemetryStationNode5" -> telemetryStationNode5.get() != value
                "telemetryStationNode6" -> telemetryStationNode6.get() != value
                "telemetryStationNode7" -> telemetryStationNode7.get() != value
                "telemetryStationNode8" -> telemetryStationNode8.get() != value
                "telemetryStationNode9" -> telemetryStationNode9.get() != value
                "telemetryStationNode10" -> telemetryStationNode10.get() != value
                else -> false
            }
        }
    }
}