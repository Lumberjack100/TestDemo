package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeGuideGrooveCalibrationViewModel : ViewModel() {
    val isClearMotionDataVisible = NonNullObservableField(false)
    val isEditable = NonNullObservableField(true)
    val motionType = NonNullObservableField("")//运动类型
    val speed = NonNullObservableField("")//速度(r/min)
    val pulseGoal = NonNullObservableField("")//运动脉冲数

    //底部弹窗
    val isStopQueryMotorState = NonNullObservableField(false)
    val isExitButtonVisible = NonNullObservableField(false)
    val isDoStopAction = NonNullObservableField(false)
    val pauseButtonText = NonNullObservableField("暂停")
    val motionPulse = NonNullObservableField("0")
    val motionAngle = NonNullObservableField("0")

}