package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class UDProductSensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    //安装高度 单位是米
    val installHeight = NonNullObservableField("")

    //测量间隔 1s、2s、5s、10s；默认为1s，当前置灰不可配置
    val measureInterval = NonNullObservableField("")

    //平均次数 2、3、5、10；默认为5，当前置灰不可配置
    val averageTimes = NonNullObservableField("")

    //触发抓拍级别 无触发、一级报警、二级报警、三级报警、四级报警；默认为四级报警
    val triggerCaptureLevel = NonNullObservableField("")

    //图片分辨率 640*480、1920*1080、2560*1920；默认为1920*1080
    val imageResolution = NonNullObservableField("")
}