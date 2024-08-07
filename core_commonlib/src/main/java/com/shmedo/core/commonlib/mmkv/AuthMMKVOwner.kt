package com.shmedo.core.commonlib.mmkv

import com.shmedo.core.model.UserInfo


/**
 * 创建者：gonghe
 * 创建时间：2024/3/26
 * 描述： TODO
 */
object AuthMMKVOwner : MMKVOwner(mmapID = "auth_settings") {
    var account by mmkvString(default = "")
    var password by mmkvString(default = "")
    var token by mmkvString(default = "")
    var realName by mmkvString(default = "")
    var phone by mmkvString(default = "")
    var userID by mmkvInt(default = 0)
    var companyID by mmkvInt(default = 0)
    var userInfo by mmkvParcelable<UserInfo>()
    var listSuperInfoPermission by mmkvBool(default = false)

    fun clear() {
        password = ""
        token = ""
        realName = ""
        phone = ""
        userID = 0
        companyID = 0
        userInfo = null
        listSuperInfoPermission = false
    }
}