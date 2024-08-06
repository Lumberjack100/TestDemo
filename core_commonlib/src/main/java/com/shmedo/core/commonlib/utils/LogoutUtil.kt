package com.shmedo.core.commonlib.utils

import com.blankj.utilcode.util.ActivityUtils

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
        MmkvCacheUtil.setToken("")
        MmkvCacheUtil.setPassword("")
        MmkvCacheUtil.setUserId(0)
        MmkvCacheUtil.setUserCompanyId(0)
        MmkvCacheUtil.setUser(null)
        ActivityUtils.finishAllActivities()
    }
}