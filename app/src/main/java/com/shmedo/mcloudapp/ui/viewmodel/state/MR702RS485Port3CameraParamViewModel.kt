package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port3CameraParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    //通用参数
    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val sensorType = NonNullObservableField(1)//1 太阳能控制器 2 声光报警器 3 LED屏
    val sensorName = NonNullObservableField("")
    val address = NonNullObservableField("")

    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")//数据位
    val checkBit = NonNullObservableField("")//校验位
    val stopBit = NonNullObservableField("")//停止位

    //设备参数
    val cameraModel = NonNullObservableField("")
    val cameraResolution = NonNullObservableField("")
    val quality = NonNullObservableField("")
    val workModel = NonNullObservableField("")
}