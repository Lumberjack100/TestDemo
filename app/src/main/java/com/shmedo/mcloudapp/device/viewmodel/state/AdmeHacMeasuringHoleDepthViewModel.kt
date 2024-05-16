package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeHacMeasuringHoleDepthViewModel : ViewModel() {

    val isEditable = NonNullObservableField(true)
    val isAutoMode = NonNullObservableField(true)
    val decentralizedEnable = NonNullObservableField(true)//进入页面默认自动测孔深，需要打开堵转检测使能

    val runButtonText = NonNullObservableField("启动")//孔号

    val address = NonNullObservableField("")//MAC 地址
    val holeno = NonNullObservableField("")//孔号
    val areano = NonNullObservableField("")//区号
    val downSpeed = NonNullObservableField("")//下放速度(r/min)
    val speed = NonNullObservableField("")//速度(r/min)
    val measureWay = NonNullObservableField("")//测量方式

    //自动测孔深
    val realHoleDepth = NonNullObservableField("")// 实测孔深
    val recommendHoleDepth = NonNullObservableField("")//推荐孔深

    //手动测孔深
    val motionType = NonNullObservableField("")//运动类型
    val distanceGoal = NonNullObservableField("")//运动距离

    //自动/手动测孔深底部弹窗
    val isExitButtonVisible = NonNullObservableField(false)
    val isStopAction = NonNullObservableField(false)
    val pauseButtonText = NonNullObservableField("暂停")
    val motionPulse = NonNullObservableField("0")
    val motionDistance = NonNullObservableField("0")

    val isMotorInfoNormal = NonNullObservableField(true)
    val motorInfo = NonNullObservableField("正常")
}