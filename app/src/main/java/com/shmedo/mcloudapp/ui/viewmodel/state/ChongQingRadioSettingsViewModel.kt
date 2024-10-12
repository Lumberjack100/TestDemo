package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class ChongQingRadioSettingsViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val receiveChannel = NonNullObservableField("")//报警接收频点
    val sendChannel = NonNullObservableField("")//广播发射频点
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率

    val telemetryStationNodeBack1 = NonNullObservableField("")//测站1编号
    val telemetryStationNodeBack2 = NonNullObservableField("")//测站2编号
    val telemetryStationNodeBack3 = NonNullObservableField("")//测站3编号
    val telemetryStationNodeBack4 = NonNullObservableField("")//测站4编号
    val telemetryStationNodeBack5 = NonNullObservableField("")//测站5编号
    val telemetryStationNodeBack6 = NonNullObservableField("")//测站6编号
    val telemetryStationNodeBack7 = NonNullObservableField("")//测站7编号
    val telemetryStationNodeBack8 = NonNullObservableField("")//测站8编号
    val telemetryStationNodeBack9 = NonNullObservableField("")//测站9编号
    val telemetryStationNodeBack10 = NonNullObservableField("")//测站10编号

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
}