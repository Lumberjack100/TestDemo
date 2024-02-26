package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeSportsCalibrationProcessingViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val isSportsCalibrationSupport = NonNullObservableField(false)//设备固件是否支持运动检校功能
    val isSportsCalibration = NonNullObservableField(false)//运动检校使能
    val zeroDifference = NonNullObservableField("")//归零差值
    val positioningErrorInterval = NonNullObservableField("")//定位误差区间值
    val tryNumber = NonNullObservableField("")   //尝试次数

    val isKValueCalibrationSupport = NonNullObservableField(false) //设备固件是否支持K值检校功能
    val isKValueCalibration = NonNullObservableField(false)//K值检校使能
    val kValueThreshold = NonNullObservableField("")//k值阈值
    val middleErrorThreshold = NonNullObservableField("") //中误差阈值
    val kTryNumber = NonNullObservableField("")//尝试次数


    val isDataProcessingSupport = NonNullObservableField(false)//设备固件是否支持数据处理功能
    val isDataProcessing = NonNullObservableField(false)//数据处理使能
    val accumulatedDifference = NonNullObservableField("") //累积值差值
}