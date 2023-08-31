package com.shmedo.lib.device.base.iot_cmd.entity.adme
import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/24/20 <br></br>
 * 描述：     ADME 设备模式
 */
class AdmeEquipModelEntity(private val model: String) : Validater {
    override fun validate() {}
    override fun toString(): String {
        return "equimodel=$model"
    }
}