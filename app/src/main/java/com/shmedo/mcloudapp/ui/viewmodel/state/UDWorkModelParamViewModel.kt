package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDWorkModelParamViewModel : ViewModel() {
    val alarmEnable = NonNullObservableField(true)//报警启用

    val model = NonNullObservableField("")//模式
    val firstAlarmThresholdTitle = NonNullObservableField("一级报警阈值(毫米)")
    val secondAlarmThresholdTitle = NonNullObservableField("二级报警阈值(毫米)")
    val thirdAlarmThresholdTitle = NonNullObservableField("三级报警阈值(毫米)")
    val fourthAlarmThresholdTitle = NonNullObservableField("四级报警阈值(毫米)")
    //一级报警阈值 默认40
    val firstAlarmThreshold = NonNullObservableField("40")
    //二级报警阈值 默认20
    val secondAlarmThreshold = NonNullObservableField("20")
    //三级报警阈值 默认10
    val thirdAlarmThreshold = NonNullObservableField("10")
    //四级报警阈值 默认5
    val fourthAlarmThreshold = NonNullObservableField("5")

    //上报频率
    val reportFrequency = NonNullObservableField("")
}