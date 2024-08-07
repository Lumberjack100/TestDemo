package com.shmedo.core.commonlib.mmkv

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.model.AppConfigInfo
import com.shmedo.core.model.UserPermissionInfo
import com.tencent.mmkv.MMKV

object MmkvCacheUtil {

    fun isAgreePrivate(): Boolean {
        val kv = MMKV.defaultMMKV()
        return kv.decodeBool("private", false)
    }

    fun setAgreePrivate(first: Boolean): Boolean {
        val kv = MMKV.defaultMMKV()
        return kv.encode("private", first)
    }

    /**
     * 用户名
     */
    fun getAccount(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("username")
        return value ?: ""
    }

    fun setAccount(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("username", value)
    }

    /**
     * 密码
     */
    fun getPassword(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("password")
        return value ?: ""
    }

    fun setPassword(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("password", value)
    }

    fun getToken(): String {
        val kv = MMKV.defaultMMKV()
        val token = kv.decodeString("token")
        return token ?: ""
    }

    fun setToken(token: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("token", token)
    }

    fun setUserPermissionList(list: List<UserPermissionInfo>?) {
        val kv = MMKV.defaultMMKV()
        list?.let {
            kv.encode("user_permission_list", MoshiUtil.toJson(it))
        }
    }

    /**
     * 获取搜索历史缓存数据
     */
    fun getSearchHistoryData(): List<String> {
        val kv = MMKV.defaultMMKV()
        val searchCacheStr = kv.decodeString("device_search_history")
        return if (searchCacheStr.isNullOrEmpty()) arrayListOf()
        else MoshiUtil.fromJson<List<String>>(searchCacheStr) ?: arrayListOf()
    }

    fun setSearchHistoryData(searchResponseStr: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("device_search_history", searchResponseStr)
    }

    //<editor-fold desc="ADME ">
    /**
     * 获取 ADME 自动测孔深上一次电机下放速度
     */
    fun getAdmeAutoLastMotorDropSpeed(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("auto_last_motor_drop_speed")
        return value ?: ""
    }

    fun setAdmeAutoLastMotorDropSpeed(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("auto_last_motor_drop_speed", value)
    }

    /**
     * 获取 ADME 手动测孔深上一次电机上拉速度
     */
    fun getAdmeManualLastMotorPullUpSpeed(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("manual_last_motor_pull_up_speed")
        return value ?: ""
    }

    fun setAdmeManualLastMotorPullUpSpeed(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("manual_last_motor_pull_up_speed", value)
    }

    /**
     * 获取 ADME 手动测孔深上一次电机上拉距离
     */
    fun getAdmeManualLastMotorPullUpDistance(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("manual_last_motor_pull_up_distance")
        return value ?: ""
    }

    fun setAdmeManualLastMotorPullUpDistance(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("manual_last_motor_pull_up_distance", value)
    }

    /**
     * 获取 ADME 手动测孔深上一次电机下放速度
     */
    fun getAdmeManualLastMotorDropSpeed(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("manual_last_motor_drop_speed")
        return value ?: ""
    }

    fun setAdmeManualLastMotorDropSpeed(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("manual_last_motor_drop_speed", value)
    }

    /**
     * 获取 ADME 手动测孔深上一次电机下放距离
     */
    fun getAdmeManualLastMotorDropDistance(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("manual_last_motor_drop_distance")
        return value ?: ""
    }

    fun setAdmeManualLastMotorDropDistance(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("manual_last_motor_drop_distance", value)
    }
    // </editor-fold>

    fun getAmsToken(): String {
        val kv = MMKV.defaultMMKV()
        val token = kv.decodeString("ams_token")
        return token ?: ""
    }

    fun setAmsToken(token: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("ams_token", token)
    }

    fun getAppConfigInfo(): AppConfigInfo? {
        val kv = MMKV.defaultMMKV()
        val userStr = kv.decodeString("ams_app_config_info")

        return if (userStr.isNullOrEmpty()) null
        else MoshiUtil.fromJson<AppConfigInfo>(userStr)
    }

    fun setAppConfigInfo(info: AppConfigInfo?) {
        val kv = MMKV.defaultMMKV()
        info?.let {
            kv.encode("ams_app_config_info", MoshiUtil.toJson(it))
        }
    }

    fun setAppConfigInfo(jsonParam: String?) {
        val kv = MMKV.defaultMMKV()
        jsonParam?.let {
            kv.encode("ams_app_config_info", jsonParam)
        }
    }

}