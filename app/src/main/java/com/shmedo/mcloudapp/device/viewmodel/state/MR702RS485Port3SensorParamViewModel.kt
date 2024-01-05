package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS485Port3SensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    //通用参数
    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val sensorType = NonNullObservableField(1)//1 太阳能控制器 2 声光报警器 3 LED屏
    val sensorName = NonNullObservableField("")
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("9600")
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
    val volume = NonNullObservableField(20)
    val alarmType = NonNullObservableField("")//报警类型
    val triggerLevel1Hint = NonNullObservableField("请输入")//一级报警提示
    val triggerLevel2Hint = NonNullObservableField("请输入")//二级报警提示
    val triggerLevel3Hint = NonNullObservableField("请输入")//三级报警提示
    val triggerValueLevel1 = NonNullObservableField("")//一级报警值
    val triggerValueLevel2 = NonNullObservableField("")//二级报警值
    val triggerValueLevel3 = NonNullObservableField("")//三级报警值

    //LED屏特有参数
    val ledType = NonNullObservableField("")
    val screenTime = NonNullObservableField("")//熄屏时长
}