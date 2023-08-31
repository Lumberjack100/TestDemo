package com.shmedo.mcloudapp

import com.blankj.utilcode.util.ActivityUtils
import com.shmedo.lib.core.util.MmkvCacheUtil

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： TODO
 *
 *
 */
object MCloudUtil {

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