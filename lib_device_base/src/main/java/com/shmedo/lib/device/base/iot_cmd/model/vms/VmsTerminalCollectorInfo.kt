package com.shmedo.lib.device.base.iot_cmd.model.vms

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/2/20 <br></br>
 * 描述：    Vms网关挂载的终端采集参数
 */
class VmsTerminalCollectorInfo {
    var reptgap //数据上报间隔，单位s
            : String? = null
    var repttype //数据上报方式，0：网关召测，1：主动上报
            : String? = null
    var filtertype //滤波类型，默认0（无滤波）1，中值滤波；2，算术平均滤波；3，中位值平均滤波；4，加权平均滤波
            : String? = null
    var filternum //样本大小，默认10
            : String? = null
    var collgap //采集间隔，默认10
            : String? = null
    var waitgap //激励前等待间隔，默认500，单位ms
            : String? = null
}