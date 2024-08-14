package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotionState

class AdmeHacMeasuringDataProcedureViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isFirstQueryMotorState = NonNullObservableField(true)
    val isStopQueryMotorState = NonNullObservableField(false)

    val motionStateWrapper = NonNullObservableField(HacMotionState())

    val isCheckReverse = NonNullObservableField(false)//测斜仪反转自检
    val measureMode = NonNullObservableField("")// 测量模式
    val motorInfo = NonNullObservableField("")
    val inclinometerBattery = NonNullObservableField("") //测斜仪电量
    val deviceBattery = NonNullObservableField("")//设备电量
    val inclinometerBatteryColorRes = NonNullObservableField(0)//测斜仪电量颜色
    val deviceBatteryColorRes = NonNullObservableField(0)//设备电量颜色

    val isWaitTimeVisible = NonNullObservableField(false)
    val waittimedesc = NonNullObservableField("")
    val waittime = NonNullObservableField("")//预计等待时间

    val isRunButtonVisible = NonNullObservableField(true)
    val runButtonText = NonNullObservableField("结束测量")

    val isVerticalProgressBarVisible = NonNullObservableField(true)
    val isCurDepthVisible = NonNullObservableField(false)
    val verticalProgress = NonNullObservableField(0)
    val verticalMaxProgress = NonNullObservableField(0)
    val curDepth = NonNullObservableField("")
    val holeDepth = NonNullObservableField("")
    val holeDepthValue = NonNullObservableField(0.0)

    //
    val isHorizontalProgressBarReadingData = NonNullObservableField(true)
    val horizontalProgress = NonNullObservableField(0)
    val horizontalMaxProgress = NonNullObservableField(0)
    val processDataNum = NonNullObservableField("")
    val processDataPercent = NonNullObservableField("")

}