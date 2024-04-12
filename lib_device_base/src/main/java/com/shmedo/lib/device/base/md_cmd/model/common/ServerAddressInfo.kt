package com.shmedo.lib.device.base.md_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： TODO
 */
data class ServerAddressInfo(
    var centerid: String = "",
    var addr: String = "", //数据中心地址,addr和port设置为空时，关闭该数据中心
    var port: String = "", //数据中心端口
)
