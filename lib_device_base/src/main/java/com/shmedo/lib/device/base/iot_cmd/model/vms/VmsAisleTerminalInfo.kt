package com.shmedo.lib.device.base.iot_cmd.model.vms

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/11/13 <br></br>
 * 描述：    Vms网关不同通道下，挂载终端的运行情况
 */
class VmsAisleTerminalInfo {
    /**
     * mode : 1
     * netid : 2
     * chl : 20
     * addr : 2
     * terminalnum : 1
     * airbaud : 3
     * terminal : [{"sn":"253333D","addr":64,"uprssi":-74,"downrssi":-72,"tx":160,"rx":36,"volt":4.3,"status":1,"sensor_errno":[{"sensoraddr":0,"sensortype":58,"errno":-4,"sensorval":0},{"sensoraddr":1,"sensortype":58,"errno":-4,"sensorval":-0.448},{"sensoraddr":2,"sensortype":58,"errno":0,"sensorval":0.06},{"sensoraddr":3,"sensortype":58,"errno":0,"sensorval":0}],"logintime":"2020/09/28 11:23:24","lastpackagetime":"2020/09/28 11:23:24"}]
     */
    var channel //通道号
            = 0
    var mode //通道工作模式 0：配置，1：数据
            = 0
    var netid //网络号
            = 0
    var chl //信道
            = 0
    var addr //地址
            = 0
    var terminalnum //终端接入数量
            = 0
    var airbaud //空中速率
            = 0
    var terminal //终端列表
            : List<VmsTerminalInfo>? = null
        get() = if (field == null) ArrayList() else field
}