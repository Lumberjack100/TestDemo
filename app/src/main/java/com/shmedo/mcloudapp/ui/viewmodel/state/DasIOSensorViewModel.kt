package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTRainStation

class DasIOSensorViewModel : ViewModel() {
    val switchType = NonNullObservableField(IOTRainStation.CLOSE)//
    val isSupportDumpMInTime = NonNullObservableField(false)//
    val rainResolution = NonNullObservableField("")//雨量计精度
    val dumpMinTime = NonNullObservableField("")//翻斗翻转最小间隔
    val isBreakAlarmOpen = NonNullObservableField(true)//
}