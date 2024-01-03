package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo

class AdmeMeasuringHoleDepthViewModel : ViewModel() {
    val lockedRotorDetectionInfoWrapper = NonNullObservableField(AdmeLockedRotorDetectionInfo())
    val isClearMotionDataVisible = NonNullObservableField(false)
    val isEditable = NonNullObservableField(false)
    val isAutoMode = NonNullObservableField(true)
    val measureWay = NonNullObservableField("")//测量方式
    val downEnable = NonNullObservableField(true)///进入页面默认自动测孔深，需要打开堵转检测使能
    val positiveAndNegativeTest = NonNullObservableField(false)//正反测使能
    val speed = NonNullObservableField("")//下放速度(r/min)

    //自动测孔深
    val realHoleDepth = NonNullObservableField("")// 实测孔深
    val recommendHoleDepth = NonNullObservableField("")//推荐孔深

    //手动测孔深
    val motionType = NonNullObservableField("")//运动类型
    val distanceGoal = NonNullObservableField("")//运动距离

    //自动/手动测孔深底部弹窗
    val isExitButtonVisible = NonNullObservableField(false)
    val pauseButtonText = NonNullObservableField("暂停")
    val motionPulse = NonNullObservableField("0")
    val motionDistance = NonNullObservableField("0")

}