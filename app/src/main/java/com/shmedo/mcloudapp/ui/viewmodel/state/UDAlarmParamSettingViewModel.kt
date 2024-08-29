package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDAlarmParamSettingViewModel : ViewModel() {
    val isOpened = NonNullObservableField(true)

    //监测点编号 [1~15] 默认01
    val monitorPoint = NonNullObservableField("1")
    //播报次数 [0~255] 其中0表示关闭当前报警，255表示一直报警，默认03
    val broadcastTimes = NonNullObservableField("3")
    //一级报警语音编号  [1~255] 默认 4
    val firstAlarmVoice = NonNullObservableField("4")
    //二级报警语音编号  [1~255] 默认 3
    val secondAlarmVoice = NonNullObservableField("3")
    //三级报警语音编号  [1~255] 默认 2
    val thirdAlarmVoice = NonNullObservableField("2")
    //四级报警语音编号  [1~255] 默认 1
    val fourthAlarmVoice = NonNullObservableField("1")


    val firstAlarmReportIntervalTitle = NonNullObservableField("一级报警间隔(秒)")
    val secondAlarmReportIntervalTitle = NonNullObservableField("二级报警间隔(秒)")
    val thirdAlarmReportIntervalTitle = NonNullObservableField("三级报警间隔(秒)")
    val fourthAlarmReportIntervalTitle = NonNullObservableField("四级报警间隔(秒)")

    //一级报警上报间隔 默认60,单位s
    val firstAlarmReportInterval = NonNullObservableField("60")
    //二级报警上报间隔 默认300,单位s
    val secondAlarmReportInterval = NonNullObservableField("300")
    //三级报警上报间隔 默认1800,单位s
    val thirdAlarmReportInterval = NonNullObservableField("1800")
    //四级报警上报间隔 默认3600,单位s
    val fourthAlarmReportInterval = NonNullObservableField("3600")
}