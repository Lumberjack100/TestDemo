package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AdmeHacMeasuringDataViewModel : BaseStateViewModel() {
    val isEditable = NonNullObservableField(true)

    val equipmodel = NonNullObservableField("0")//电机工作标识  0：停止  1：正常 2: 异常
    val address = NonNullObservableField("")//MAC 地址
    val projectNum = NonNullObservableField("")//项目编号
    val areaNum = NonNullObservableField("")//区号
    val holeNum = NonNullObservableField("")//孔号
    val realHoleDepth = NonNullObservableField("")//测斜管孔深阈值
    val recommendHoleDepth = NonNullObservableField("")//推荐孔深

    val decentralizationWaitingTime = NonNullObservableField("")//下放等待时间(s)
    val dataSettlementMethod = NonNullObservableField("")//数据解算方式
    val isSingleWayTest = NonNullObservableField(false)//单向测量
    val isCheckReverse = NonNullObservableField(false)//测斜仪反转自检

    val isRunButtonEnable = NonNullObservableField(true)//测量按钮是否可用
    val runButtonText = NonNullObservableField("正向测量")//

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "projectNum" to projectNum.get(),
            "areaNum" to areaNum.get(),
            "holeNum" to holeNum.get(),
            "realHoleDepth" to realHoleDepth.get(),
            "recommendHoleDepth" to recommendHoleDepth.get(),
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            projectNum,
            areaNum,
            holeNum,
            realHoleDepth,
            recommendHoleDepth
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
                "projectNum" -> projectNum.get() != value
                "areaNum" -> areaNum.get() != value
                "holeNum" -> holeNum.get() != value
                "realHoleDepth" -> realHoleDepth.get() != value
                "recommendHoleDepth" -> recommendHoleDepth.get() != value
                else -> false
            }
        }
    }
}