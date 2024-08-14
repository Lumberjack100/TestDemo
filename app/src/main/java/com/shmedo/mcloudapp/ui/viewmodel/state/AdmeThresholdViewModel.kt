package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AdmeThresholdViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isAntifreezeSupport = NonNullObservableField(false)//防冻距离是否支持

    val driveStandardVoltageThreshold = NonNullObservableField("0")//驱动器标压阈值
    val driveLowVoltageThreshold = NonNullObservableField("0")//驱动器低压阈值
    val driveUnderVoltageThreshold = NonNullObservableField("0")//驱动器欠压阈值
    val inclinometerStandardVoltageThreshold = NonNullObservableField("0")//测斜仪标压阈值
    val inclinometerLowVoltageThreshold = NonNullObservableField("0")//测斜仪低压阈值
    val inclinometerUnderVoltageThreshold = NonNullObservableField("0")//测斜仪欠压阈值
    val wireRopeLength = NonNullObservableField("0")//钢丝绳长度
    val antifreezeDistance = NonNullObservableField("0")//防冻距离
}