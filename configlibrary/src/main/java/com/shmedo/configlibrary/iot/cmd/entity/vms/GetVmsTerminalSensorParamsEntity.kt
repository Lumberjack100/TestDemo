package com.shmedo.configlibrary.iot.cmd.entity.vms

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/23/20 <br></br>
 * 描述：     获取Vms终端某个通道下传感器参数
 */
class GetVmsTerminalSensorParamsEntity(private val sn: String, private val channel: Int) :
    Validater {
    override fun validate() {}
    override fun toString(): String {
        return "sn=$sn&channel=$channel"
    }
}