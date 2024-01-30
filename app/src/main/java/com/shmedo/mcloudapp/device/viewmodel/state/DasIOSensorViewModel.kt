package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasIOSensorViewModel : ViewModel() {
    val checkMode = NonNullObservableField(0)//
    val isSupportDumpMInTime = NonNullObservableField(false)//
    val rainResolution = NonNullObservableField("")//雨量计精度
    val dumpMinTime = NonNullObservableField("")//翻斗翻转最小间隔
    val isBreakAlarmOpen = NonNullObservableField(true)//
}