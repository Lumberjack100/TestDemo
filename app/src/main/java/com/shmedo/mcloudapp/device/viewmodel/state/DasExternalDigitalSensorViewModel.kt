package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasExternalDigitalSensorViewModel : ViewModel() {
    val isModelSwitchSupport = NonNullObservableField(false)
    val modelType = NonNullObservableField("")//阵列测斜仪物模型模型切换

    val isChildSensorTypeSupport = NonNullObservableField(false)
    val childSensorTypeTitle = NonNullObservableField("传感器类型")//
    val childSensorType = NonNullObservableField("")//子雷达类型

    val address = NonNullObservableField("")//地址

    val isTriggerSupport = NonNullObservableField(true)//扩展1
    val triggerTitle = NonNullObservableField("触发值")//
    val triggerValue = NonNullObservableField("")//触发值
    val isTriggerTipBtnSupport = NonNullObservableField(false)//

    val isCorrectSupport = NonNullObservableField(true)//扩展1
    val correctTitle = NonNullObservableField("修正值")//
    val correctValue = NonNullObservableField("")//修正值
    val isCorrectTipBtnSupport = NonNullObservableField(false)//

    val isExtension1Support = NonNullObservableField(false)//扩展1
    val isExtension1TipBtnSupport = NonNullObservableField(false)//扩展1
    val extension1Title = NonNullObservableField("")//
    val extension1Value = NonNullObservableField("")//
    val extension1ValueEnable = NonNullObservableField(true)//
    val isExtension1ButtonSupport = NonNullObservableField(false)//扩展1

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
}