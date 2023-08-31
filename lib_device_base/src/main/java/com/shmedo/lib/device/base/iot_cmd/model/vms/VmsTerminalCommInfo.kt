package com.shmedo.lib.device.base.iot_cmd.model.vms

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/2/20 <br></br>
 * 描述：    Vms网关挂载的终端通信参数
 */
class VmsTerminalCommInfo {
    var netid //网络号
            : String? = null
    var dstaddr //通道的地址
            : String? = null
    var channel //通信信道
            : String? = null
    var airbaud //空中波特率
            : String? = null
}