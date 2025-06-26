package com.shmedo.core.commonlib.utils

import com.blankj.utilcode.util.ActivityUtils
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner

/**
 * @Author      : gonghe
 * @Email       : xxxxx@qq.com
 * @Date        : on 2023-11-16 11:19.
 * @Description :描述
 */
object LogoutUtil {

    /**
     * 注销用户登录。
     */
    fun logout() {
        AuthMMKVOwner.clear()
        ActivityUtils.finishAllActivities()
    }
}