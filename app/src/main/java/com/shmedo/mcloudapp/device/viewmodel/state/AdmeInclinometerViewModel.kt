package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeInclinometerViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isCompensateWayVisible = NonNullObservableField(false)
    val isTorsionAngleVisible = NonNullObservableField(false)
    val inclinometerVersion = NonNullObservableField("")// 测斜仪版本
    val mode = NonNullObservableField("")// 测量工作模式
    val compensateWay = NonNullObservableField("")// 补偿方式
    val torsionAngle = NonNullObservableField("")// 扭转角γ
    val address = NonNullObservableField("")// 采集器地址/Mac 地址
    val collectionInterval = NonNullObservableField("")// 采集器采集间隔
    val solvingInterval = NonNullObservableField("")// 采集器解算间隔
    val sleepTime = NonNullObservableField("")// 休眠时间
    val correctionValue = NonNullObservableField("")// 测斜仪修正值

}