package com.shmedo.lib.cmd.base.md_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： TODO
 */
data class ServerAddressInfo(
    var centerid: String = "",
    var addr: String = "", //数据链路地址,addr和port设置为空时，关闭该数据链路
    var port: String = "", //数据链路端口
)
