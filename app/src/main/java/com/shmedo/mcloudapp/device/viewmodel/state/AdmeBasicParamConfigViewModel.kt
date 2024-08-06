package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeBasicConfigInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeExecutiveAgencyInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo

class AdmeBasicParamConfigViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val basicConfigInfoWrapper = NonNullObservableField(AdmeBasicConfigInfo())
    val address = NonNullObservableField("")//采集器地址/Mac 地址

    val executiveAgencyInfoWrapper = NonNullObservableField(AdmeExecutiveAgencyInfo())
    val measureMethodText = NonNullObservableField("")
    val measureMethod = NonNullObservableField(0)//测量方式 （0:实时测量，1:整时整点测量，2:定时定点测量)
    val dataSettlementMethod = NonNullObservableField("")//数据解算方式
    val waitingIntervalPerRound = NonNullObservableField("")//每轮等待时间
    val measurementIntervalPerRound = NonNullObservableField("")//每轮测量间隔
    val modifiedDate = NonNullObservableField("")//修改日期
    val intervalDays = NonNullObservableField("")//间隔时间
    val startTimePerRound = NonNullObservableField("")//每轮开始时间
    val inclinometerTubeHoleDepth = NonNullObservableField("")//测斜管孔深(m)
    val decentralizationSpeed = NonNullObservableField("")//下放速度(r/min)
    val decentralizationWaitingTime = NonNullObservableField("")//下放等待时间(min)

    val lockedRotorDetectionInfoWrapper = NonNullObservableField(AdmeLockedRotorDetectionInfo())
    val downEnable = NonNullObservableField(false)//下放堵转检测使能

    val positiveAndNegativeTest = NonNullObservableField(false)//正反测使能

}