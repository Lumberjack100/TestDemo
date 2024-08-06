package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType

class DasExternalVibratingSensorViewModel : ViewModel() {
    val sensorType = NonNullObservableField(IOTSensorType.UNKNOWN_TYPE)//
    val sensorTypeName = NonNullObservableField("")//传感器类型
    val channel = NonNullObservableField("")//通道


    val isExtension1Support = NonNullObservableField(false)//扩展1
    val isExtension1TipBtnSupport = NonNullObservableField(false)//扩展1
    val extension1Title = NonNullObservableField("")//
    val extension1Value = NonNullObservableField("0")//
    val extension1ValueEnable = NonNullObservableField(true)//

    val isExtension2Support = NonNullObservableField(false)//扩展2
    val isExtension2TipBtnSupport = NonNullObservableField(false)//扩展2
    val extension2Title = NonNullObservableField("")//
    val extension2Value = NonNullObservableField("")//
    val extension2ValueEnable = NonNullObservableField(true)//

    val isExtension3Support = NonNullObservableField(false)//扩展3
    val isExtension3TipBtnSupport = NonNullObservableField(false)//扩展3
    val extension3Title = NonNullObservableField("")//
    val extension3Value = NonNullObservableField("")//
    val extension3ValueEnable = NonNullObservableField(true)//

    val isExtension4Support = NonNullObservableField(false)//扩展4
    val isExtension4TipBtnSupport = NonNullObservableField(false)//扩展4
    val extension4Title = NonNullObservableField("")//
    val extension4Value = NonNullObservableField("")//
    val extension4ValueEnable = NonNullObservableField(true)//

    val isExtension5Support = NonNullObservableField(false)//扩展5
    val isExtension5TipBtnSupport = NonNullObservableField(false)//扩展5
    val extension5Title = NonNullObservableField("")//
    val extension5Value = NonNullObservableField("")//
    val extension5ValueEnable = NonNullObservableField(true)//

    val isExtension6Support = NonNullObservableField(false)
    val isExtension6TipBtnSupport = NonNullObservableField(false)
    val extension6Title = NonNullObservableField("")//
    val extension6Value = NonNullObservableField("")//
    val extension6ValueEnable = NonNullObservableField(true)//

    val isExtension7Support = NonNullObservableField(false)
    val isExtension7TipBtnSupport = NonNullObservableField(false)
    val extension7Title = NonNullObservableField("")//
    val extension7Value = NonNullObservableField("")//
    val extension7ValueEnable = NonNullObservableField(true)//

    val isExtension8Support = NonNullObservableField(false)
    val isExtension8TipBtnSupport = NonNullObservableField(false)
    val extension8Title = NonNullObservableField("")//
    val extension8Value = NonNullObservableField("")//
    val extension8ValueEnable = NonNullObservableField(true)//

    val isExtension9Support = NonNullObservableField(false)
    val isExtension9TipBtnSupport = NonNullObservableField(false)
    val extension9Title = NonNullObservableField("")//
    val extension9Value = NonNullObservableField("")//
    val extension9ValueEnable = NonNullObservableField(true)//

    val isExtension10Support = NonNullObservableField(false)
    val isExtension10TipBtnSupport = NonNullObservableField(false)
    val extension10Title = NonNullObservableField("")//
    val extension10Value = NonNullObservableField("")//
    val extension10ValueEnable = NonNullObservableField(true)//
}