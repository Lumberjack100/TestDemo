package com.shmedo.configlibrary.iot.cmd.entity.lr200

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/2/25 <br></br>
 * 描述：     LR200 设置位置指令参数
 */
class LR200PositionEntity : Validater {
    var lng: String? = null
    var lat: String? = null


    override fun validate() {}
    override fun toString(): String {
        return "lng=" + lng +
                "&" +
                "lat=" + lat
    }
}