package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702PlusePortViewModel : ViewModel() {
    val isOpened = NonNullObservableField(true)//功能开关
    val workMode = NonNullObservableField("已接入")//功能选择 计数 消警
    val pulseResolution = NonNullObservableField("")//脉冲分辨率 默认1，整型，大于0，最大9999
    val debounceCoefficient = NonNullObservableField("")//消抖系数 默认5，整型，大于0，最大60.单位s
}