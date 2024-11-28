package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AdmeHacMeasuringHoleDepthViewModel : BaseStateViewModel() {
    val isAutoMeasuringMode = NonNullObservableField(true)
    val decentralizedEnable = NonNullObservableField(true)//进入页面默认自动测孔深，需要打开堵转检测使能

    val address = NonNullObservableField("")//MAC 地址
    val projectNum = NonNullObservableField("")//项目编号
    val areaNum = NonNullObservableField("")//区号
    val holeNum = NonNullObservableField("")//孔号

    val downSpeed = NonNullObservableField("")//下放速度(r/min)
    val speed = NonNullObservableField("")//速度(r/min)
    val measureWay = NonNullObservableField("")//测量方式

    val runButtonText = NonNullObservableField("启动")//

    //自动测孔深
    val realHoleDepth = NonNullObservableField("")// 实测孔深
    val recommendHoleDepth = NonNullObservableField("")//推荐孔深

    //手动测孔深
    val motionType = NonNullObservableField("")//运动类型
    val distanceGoal = NonNullObservableField("")//运动距离

    //自动/手动测孔深底部弹窗
    val isStopQueryMotorState = NonNullObservableField(false)
    val isExitButtonVisible = NonNullObservableField(false)
    val isDoManualStopAction = NonNullObservableField(false)//是否点击停止按钮操作
    val pauseButtonText = NonNullObservableField("暂停")
    val motionPulse = NonNullObservableField("0")
    val motionDistance = NonNullObservableField("0")

    val isMotorInfoNormal = NonNullObservableField(true)
    val motorInfo = NonNullObservableField("正常")


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