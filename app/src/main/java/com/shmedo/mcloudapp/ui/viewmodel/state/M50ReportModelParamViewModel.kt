package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50ReportModelParamViewModel : ViewModel() {
    val reportModel = NonNullObservableField("")//上报模式
    val workModel = NonNullObservableField("")//工作模式
    val memsThreshold = NonNullObservableField("")//MEMS 触发阈值
    val alarmEnable = NonNullObservableField(true)//四级报警启用

    val firstAlarmThresholdTitle = NonNullObservableField("一级报警阈值(毫米)")
    val secondAlarmThresholdTitle = NonNullObservableField("二级报警阈值(毫米)")
    val thirdAlarmThresholdTitle = NonNullObservableField("三级报警阈值(毫米)")
    val fourthAlarmThresholdTitle = NonNullObservableField("四级报警阈值(毫米)")

    val firstAlarmThreshold = NonNullObservableField("")//一级报警阈值 默认40
    val secondAlarmThreshold = NonNullObservableField("")//二级报警阈值 默认20
    val thirdAlarmThreshold = NonNullObservableField("")//三级报警阈值 默认10
    val fourthAlarmThreshold = NonNullObservableField("")//四级报警阈值 默认5
}