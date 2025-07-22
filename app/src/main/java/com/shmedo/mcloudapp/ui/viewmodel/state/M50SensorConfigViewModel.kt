package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50SensorConfigViewModel : BaseStateViewModel() {
    // 当前角度值
    val xCurrentAngle = NonNullObservableField("") //x 轴当前角度
    val yCurrentAngle = NonNullObservableField("") //y 轴当前角度
    val zCurrentAngle = NonNullObservableField("") //z 轴当前角度

    // 初始角度值
    val xInitialAngle = NonNullObservableField("") //x 轴初始角度
    val yInitialAngle = NonNullObservableField("") //y 轴初始角度
    val zInitialAngle = NonNullObservableField("") //z 轴初始角度

    // 偏移角度值
    val xOffsetAngle = NonNullObservableField("") //x 轴偏移角度
    val yOffsetAngle = NonNullObservableField("") //y 轴偏移角度
    val zOffsetAngle = NonNullObservableField("") //z 轴偏移角度

    val isTriggerEnable = NonNullObservableField(false) //

    //角度触发值
    val angleTrigger = NonNullObservableField("") //角度触发值

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isTriggerEnable" to isTriggerEnable.get(),
            "angleTrigger" to angleTrigger.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isTriggerEnable,
            angleTrigger
        ).forEach {
            it.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
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
                "isTriggerEnable" -> isTriggerEnable.get() != value
                "angleTrigger" -> angleTrigger.get() != value
                else -> false
            }
        }
    }
} 