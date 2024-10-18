package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDSensorParamViewModel : BaseStateViewModel() {
    val isEditable = NonNullObservableField(true)

    val measureInterval = NonNullObservableField("") //雷达测量间隔
    val installAngleOffsetThreshold = NonNullObservableField("") //安装角度偏移阈值
    val airAltitudeInitialValue = NonNullObservableField("") //海拔

    val captureFrequency = NonNullObservableField("")//抓拍频率
    val imageResolution = NonNullObservableField("")//图片分辨率

    val locationInitialValue = NonNullObservableField("") //位置

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "measureInterval" to measureInterval.get(),
            "installAngleOffsetThreshold" to installAngleOffsetThreshold.get(),
            "airAltitudeInitialValue" to airAltitudeInitialValue.get(),
            "captureFrequency" to captureFrequency.get(),
            "imageResolution" to imageResolution.get(),
            "locationInitialValue" to locationInitialValue.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            measureInterval,
            installAngleOffsetThreshold,
            airAltitudeInitialValue,
            captureFrequency,
            imageResolution,
            locationInitialValue
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "measureInterval" -> measureInterval.get() != value
                "installAngleOffsetThreshold" -> installAngleOffsetThreshold.get() != value
                "airAltitudeInitialValue" -> airAltitudeInitialValue.get() != value
                "captureFrequency" -> captureFrequency.get() != value
                "imageResolution" -> imageResolution.get() != value
                "locationInitialValue" -> locationInitialValue.get() != value
                else -> false
            }
        }
    }

}