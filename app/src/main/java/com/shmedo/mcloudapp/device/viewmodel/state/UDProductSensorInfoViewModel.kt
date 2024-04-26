package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class UDProductSensorInfoViewModel : ViewModel() {
    //摄像头状态
    val cameraErrNo = NonNullObservableField("1")
    //雷达状态
    val radarErrNo = NonNullObservableField("1")
    //安装高度
    val installHeight = NonNullObservableField("")
    //雷达测量值
    val radarMeasureValue = NonNullObservableField("")
}