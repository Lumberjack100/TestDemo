package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port3SensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    //通用参数
    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val sensorType = NonNullObservableField(1)//1 太阳能控制器 2 声光报警器 3 LED屏
    val sensorName = NonNullObservableField("")
    val address = NonNullObservableField("")

    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")//数据位
    val checkBit = NonNullObservableField("")//校验位
    val stopBit = NonNullObservableField("")//停止位

    //太阳能控制器特有参数
    val solarVoltage = NonNullObservableField("")
    val batteryVoltage = NonNullObservableField("")
    val solarPower = NonNullObservableField("")
    val loadPower = NonNullObservableField("")

    //声光报警器特有参数
    val duration = NonNullObservableField("")//报警器语音播放时长、LED屏显示时长
    val interval = NonNullObservableField("")//报警器切换间隔、LED屏更新间隔
    val volume = NonNullObservableField("")//报警器音量
    val rainTriggerValueLevel1 = NonNullObservableField("")//降雨量一级报警值
    val rainTriggerValueLevel2 = NonNullObservableField("")//降雨量二级报警值
    val rainTriggerValueLevel3 = NonNullObservableField("")//降雨量三级报警值
    val waterTriggerValueLevel1 = NonNullObservableField("")//水位一级报警值
    val waterTriggerValueLevel2 = NonNullObservableField("")//水位二级报警值
    val waterTriggerValueLevel3 = NonNullObservableField("")//水位三级报警值

    //LED屏特有参数
    val ledType = NonNullObservableField("")
    val screenTime = NonNullObservableField("")//熄屏时长
}