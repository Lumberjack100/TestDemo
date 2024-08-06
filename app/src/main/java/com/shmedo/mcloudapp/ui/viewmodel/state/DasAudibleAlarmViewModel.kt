package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DasAudibleAlarmViewModel : ViewModel() {
    //声光报警器特有参数
    val isAudibleOpened = NonNullObservableField(false)
    val alarmType = NonNullObservableField("")
    val alarmAddress = NonNullObservableField("")
    val duration = NonNullObservableField("")//报警器语音播放时长
    val interval = NonNullObservableField("")//报警器切换间隔
    val volume = NonNullObservableField(20)
    val triggerValueLevel1 = NonNullObservableField("")//一级报警值
    val triggerValueLevel2 = NonNullObservableField("")//二级报警值
    val triggerValueLevel3 = NonNullObservableField("")//三级报警值

    //LED屏特有参数
    val isScreenOpened = NonNullObservableField(false)
    val screenAddress = NonNullObservableField("")//
    val showTime = NonNullObservableField("")//显示时长
    val showGap = NonNullObservableField("")//显示间隔
}