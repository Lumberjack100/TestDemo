package com.shmedo.lib.core.base.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/1/11 <br></br>
 * 描述：     查询用户在某公司某服务中的所有权限
 */
@JsonClass(generateAdapter = true)
class UserPermissionInfo(
    val id: Int = 0,   //权限id
    val name: String = "",//权限名
    val permissionToken: String = "", //权限token
    val serviceName: String = "", //权限所属服务
    val permissionDesc: String = "", //权限描述
    val exValues: String = "" //权限拓展
)