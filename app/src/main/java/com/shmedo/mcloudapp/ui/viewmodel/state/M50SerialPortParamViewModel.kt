package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50SerialPortParamViewModel : ViewModel() {
    val isExternalPower = NonNullObservableField(false) //外部供电

    val rs232ExternalDevice = NonNullObservableField("")//232 外接设备
    val captureFrequency = NonNullObservableField("")//抓拍频率
    val imageResolution = NonNullObservableField("")//图片分辨率
    val rs485ExternalDevice = NonNullObservableField("")//485 外接设备
    val rs485BaudRate = NonNullObservableField("")//485 波特率
    val rs485ExternalDeviceAddr = NonNullObservableField("")//485 外接设备地址

}