package com.shmedo.configlibrary.iot.cmd.entity.das

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：     声光报警器监测类型
 */
class AlarmTypeEntity(private val type: String) : Validater {
    override fun validate() {}
    override fun toString(): String {
        return "alarmtype=$type"
    }
}