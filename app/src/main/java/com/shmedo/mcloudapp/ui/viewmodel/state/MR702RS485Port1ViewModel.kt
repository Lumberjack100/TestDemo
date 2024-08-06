package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port1ViewModel : ViewModel() {
    val acquisitionFrequency = NonNullObservableField("500")//采集频率
    val collectionDuration = NonNullObservableField("5")//采集周期
    val collectionTimes = NonNullObservableField("1")//采集次数
    val noResponseTimes = NonNullObservableField("3")//无应答次数
    val delayDuration = NonNullObservableField("10")//延时时间
}