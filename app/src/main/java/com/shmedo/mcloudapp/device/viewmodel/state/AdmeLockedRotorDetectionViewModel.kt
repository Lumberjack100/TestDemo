package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeLockedRotorDetectionViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val downEnable = NonNullObservableField(false)//下放堵转检测使能
    val downPulsesPerUnitTime = NonNullObservableField("")//下放单位时间脉冲数
    val downPulseDetectionTime = NonNullObservableField("")//下放脉冲检测判断时间
    val downSlowStartIntervalEndValue = NonNullObservableField("")//下放缓起区间终值(加速阶段)
    val downSlowStopIntervalStartValue = NonNullObservableField("")//下放缓停区间起始值(减速阶段)
    val downStallDetectionInterval = NonNullObservableField("")//堵转检测区间
    val downStallDetectionIntervalStartValue = NonNullObservableField("")//堵转检测区间起始值
    val downStallDetectionIntervalEndValue = NonNullObservableField("")//堵转检测区间终值
    val downTorqueStallThreshold = NonNullObservableField("")//下放力矩堵转阈值
    val downTorqueDetectionTime = NonNullObservableField("")//下放力矩检测判断时间

    val upEnable = NonNullObservableField(false)//上拉堵转检测使能
    val pullUpSlowStartIntervalEndValue = NonNullObservableField("")//上拉缓起区间终值(加速阶段)
    val pullUpSlowStopIntervalStartValue = NonNullObservableField("")//上拉缓停区间起始值(减速阶段)
    val pullUpTorqueStallThreshold = NonNullObservableField("")//上拉力矩堵转阈值
    val pullUpTorqueDetectionTime = NonNullObservableField("")//上拉力矩检测判断时间

}