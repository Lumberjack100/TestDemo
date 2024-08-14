package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/22
 *
 * 描述： 孙建伟通用配置信息实体类
 *
 *
 */
@JsonClass(generateAdapter = true)
data class AppConfigInfo(
    val id: Int,//自增长ID
    val appName: String = "",//应用系统名称
    val userName: String = "",//用户姓名
    var configPara: String = "",//配置Json文件
    val identyID: Int,//身份ID
    var desc: String,//描述
    var lastTime: String,//创建时间
)
