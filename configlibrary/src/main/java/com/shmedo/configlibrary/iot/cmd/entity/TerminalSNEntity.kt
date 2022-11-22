package com.shmedo.configlibrary.iot.cmd.entity

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/19/20 <br></br>
 * 描述：    终端设备 Sn 号码
 */
class TerminalSNEntity(private val sn: String) : Validater {
    override fun validate() {}
    override fun toString(): String {
        return "sn=$sn"
    }
}