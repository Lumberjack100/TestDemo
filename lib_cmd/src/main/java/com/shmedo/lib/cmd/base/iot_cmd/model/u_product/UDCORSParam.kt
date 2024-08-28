package com.shmedo.lib.cmd.base.iot_cmd.model.u_product

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述：一体化雷达泥位计CORS参数
 */
@JsonClass(generateAdapter = true)
data class UDCORSParam(
    var use: String = "", //服务启用 0：不启用；1：启用
    var host: String = "", //域名 可为ip或域名
    var port: String = "", //端口
    var username: String = "", //差分账号
    var password: String = "", //密码
) 