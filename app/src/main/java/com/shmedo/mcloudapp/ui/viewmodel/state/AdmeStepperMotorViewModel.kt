package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AdmeStepperMotorViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val accuracyCorrectionValue = NonNullObservableField("0")//绝对精度修正值
    val movementSpeed = NonNullObservableField("0")//步进电机运动速度(r/min)
    val motorTorque = NonNullObservableField("0")//步进电机力矩
}