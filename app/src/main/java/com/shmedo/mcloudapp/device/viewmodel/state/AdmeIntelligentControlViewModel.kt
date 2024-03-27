package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeIntelligentControlViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isPositiveAndNegativeTestSupport = NonNullObservableField(true)
    val isPositiveAndNegativeTestExceptionHandlingSupport = NonNullObservableField(true)
    val isLowPowerAlarmSupport = NonNullObservableField(true)
    val isAnthropomorphicMovementSupport = NonNullObservableField(true)
    val isBrakePadControlSupport = NonNullObservableField(true)
    val isMotorPowerSupport = NonNullObservableField(true)

    val positiveAndNegativeTest = NonNullObservableField(false)//正反测使能
    val isPositiveAndNegativeTestExceptionHandling = NonNullObservableField(false)//正反测异常智能处理
    val lowPowerAlarm = NonNullObservableField(false)//力矩电机继电器低功耗使能
    val anthropomorphicMovement = NonNullObservableField(false)//拟人运动使能
    val brakePadControl = NonNullObservableField("")//刹车片控制
    val motorPower = NonNullObservableField(false)//电机电源使能
}