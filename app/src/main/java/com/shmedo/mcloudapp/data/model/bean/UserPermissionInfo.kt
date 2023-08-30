package com.shmedo.mcloudapp.data.model.bean

import android.text.TextUtils
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/1/11 <br></br>
 * 描述：     查询用户在某公司某服务中的所有权限
 */
@JsonClass(generateAdapter = true)
 class UserPermissionInfo {
    var id //权限id
            = 0
    var name //权限名
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var permissionToken //权限token
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var serviceName //权限所属服务
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var permissionDesc //权限描述
            : String? = null
    var exValues //权限拓展
            : String? = null
}