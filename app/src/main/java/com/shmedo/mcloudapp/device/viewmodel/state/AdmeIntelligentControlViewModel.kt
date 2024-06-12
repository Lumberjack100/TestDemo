package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeIntelligentControlViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isPositiveAndNegativeTestSupport = NonNullObservableField(true)//正反测是否支持
    val isPositiveAndNegativeTestExceptionHandlingSupport = NonNullObservableField(true)//正反测异常智能处理是否支持
    val isLowPowerAlarmSupport = NonNullObservableField(true)//低功耗是否支持
    val isAnthropomorphicMovementSupport = NonNullObservableField(true)//拟人运动是否支持
    val isTorqueMotorPowerOffRestartSupport = NonNullObservableField(true)  //力矩电机断电重启是否支持
    val isBrakePadControlSupport = NonNullObservableField(true)//刹车片控制方式是否支持
    val isMotorPowerSupport = NonNullObservableField(true)//电机电源是否支持
    val isClearDeviceDropTimesSupport = NonNullObservableField(true)//清空设备下降次数是否支持
    val isClearRopeRunDistanceSupport = NonNullObservableField(true)//清空钢丝绳运行里程是否支持
    val isClearVerticalMagneticSwitchTriggerRecordSupport = NonNullObservableField(true)//清空竖向磁开关触发记录是否支持
    val isClearRotaryMagneticSwitchTriggerRecordSupport = NonNullObservableField(true)//清空旋转磁开关触发记录是否支持
    val isClearBrakePadOpenCloseRecordSupport = NonNullObservableField(true)//清空刹车片启闭记录是否支持


    val positiveAndNegativeTest = NonNullObservableField(false)//正反测使能
    val isPositiveAndNegativeTestExceptionHandling = NonNullObservableField(false)//正反测异常智能处理
    val lowPowerAlarm = NonNullObservableField(false)//力矩电机继电器低功耗使能
    val anthropomorphicMovement = NonNullObservableField(false)//拟人运动使能
    val brakePadControl = NonNullObservableField("")//刹车片控制
    val motorPower = NonNullObservableField(false)//电机电源使能
}