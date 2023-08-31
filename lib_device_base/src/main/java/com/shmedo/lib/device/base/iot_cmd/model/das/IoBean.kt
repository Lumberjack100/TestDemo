package com.shmedo.lib.device.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     DAS 状态页面开关量传感器状态
 */
class IoBean {
    var type = 0
    var vaule = 0.0
        private set
    var errno = 0
    fun setVaule(vaule: Int) {
        this.vaule = vaule.toDouble()
    }
}