package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDSensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val measureInterval = NonNullObservableField("") //测量间隔
    val installAngleOffsetThreshold = NonNullObservableField("") //安装角度偏移阈值
    val altitude = NonNullObservableField("") //海拔


    val captureFrequency = NonNullObservableField("")//抓拍频率
    //图片分辨率 640*480、1920*1080、2560*1920；默认为1920*1080
    val imageResolution = NonNullObservableField("")
}