package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class UDProductSensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    //安装高度
    val installHeight = NonNullObservableField("")

    //测量间隔
    val measureInterval = NonNullObservableField("")

    //平均次数
    val averageTimes = NonNullObservableField("")

    //触发抓拍级别
    val triggerCaptureLevel = NonNullObservableField("")

    //图片分辨率
    val imageResolution = NonNullObservableField("")
}