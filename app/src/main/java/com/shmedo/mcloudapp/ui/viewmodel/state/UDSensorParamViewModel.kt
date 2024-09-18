package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDSensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val measureInterval = NonNullObservableField("") //雷达测量间隔
    val installAngleOffsetThreshold = NonNullObservableField("") //安装角度偏移阈值
    val altitude = NonNullObservableField("") //海拔

    val captureFrequency = NonNullObservableField("")//抓拍频率
    val imageResolution = NonNullObservableField("")//图片分辨率

    val altitudeMeasureMode = NonNullObservableField("")//模式 自动 手动

}