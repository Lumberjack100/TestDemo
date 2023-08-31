package com.shmedo.lib.device.base.iot_cmd.model.e40

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/20 <br></br>
 * 描述：     TODO
 */
class CurrentExtendStateInfo {
    var base: BaseBean? = null
    var storage: StorageBean? = null
    var net: NetBean? = null
    var solar: SolarBean? = null
    var mems: MemsBean? = null
    var sensor: List<SensorBean>? = null
        get() = if (field == null) ArrayList() else field
    var gnss: GnssBean? = null
}