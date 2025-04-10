package com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m

/**
 * 创建者：gonghe
 * 创建时间：2025/4/10
 * 描述： TODO
 */
data class M50CorsParam(
    var sw: String = "", //开关  0 关闭 1 开启
    var host: String = "", //域名
    var port: String = "", //端口
    var username: String = "", //差分账号
    var password: String = "", //密码
)
