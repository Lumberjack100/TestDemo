package com.shmedo.lib.core.util

import com.shmedo.lib.core.base.model.AppConfigInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.base.model.UserPermissionInfo
import com.tencent.mmkv.MMKV

object MmkvCacheUtil {

    /**
     * 是否是第一次打开 APP
     */
    fun isFirst(): Boolean {
        val kv = MMKV.defaultMMKV()
        return kv.decodeBool("first", true)
    }

    /**
     * 是否是第一次打开 APP
     */
    fun setFirst(first: Boolean): Boolean {
        val kv = MMKV.defaultMMKV()
        return kv.encode("first", first)
    }

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

    fun getUserId(): Int {
        val kv = MMKV.defaultMMKV()
        return kv.decodeInt("user_id", 0)
    }

    fun setUserId(value: Int) {
        val kv = MMKV.defaultMMKV()
        kv.encode("user_id", value)
    }

    fun getUserCompanyId(): Int {
        val kv = MMKV.defaultMMKV()
        return kv.decodeInt("user_company_id", 0)
    }

    fun setUserCompanyId(value: Int) {
        val kv = MMKV.defaultMMKV()
        kv.encode("user_company_id", value)
    }

    fun getUserRealName(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("user_real_name")
        return value ?: ""
    }

    fun setUserRealName(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("user_real_name", value)
    }

    /**
     * 获取保存的用户信息
     */
    fun getUser(): UserInfo? {
        val kv = MMKV.defaultMMKV()
        val userStr = kv.decodeString("user_info")

        return if (userStr.isNullOrEmpty()) UserInfo()
        else MoshiUtil.fromJson<UserInfo>(userStr)
    }

    fun setUser(info: UserInfo?) {
        val kv = MMKV.defaultMMKV()
        info?.let {
            kv.encode("user_info", MoshiUtil.toJson(it))
        }
    }

    fun getUserPermissionList(): List<UserPermissionInfo>? {
        val kv = MMKV.defaultMMKV()
        val userStr = kv.decodeString("user_permission_list")

        return if (userStr.isNullOrEmpty()) null
        else MoshiUtil.fromJson<List<UserPermissionInfo>>(userStr)
    }

    fun setUserPermissionList(list: List<UserPermissionInfo>?) {
        val kv = MMKV.defaultMMKV()
        list?.let {
            kv.encode("user_permission_list", MoshiUtil.toJson(it))
        }
    }

    fun isHasListSuperInfoPermission(): Boolean {
        val kv = MMKV.defaultMMKV()
        return kv.decodeBool("iot_listsuperinfo_permission", false)
    }

    fun setHasListSuperInfoPermission(flag: Boolean): Boolean {
        val kv = MMKV.defaultMMKV()
        return kv.encode("iot_listsuperinfo_permission", flag)
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

    /**
     * 获取保存的应用日志 session id
     */
    fun getAppLogSessionId(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("app_log_session_id")
        return value ?: ""
    }

    fun setAppLogSessionId(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("app_log_session_id", value)
    }

    /**
     * 获取保存的物联网设备日志 session id
     */
    fun getIOTDeviceLogSessionId(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("iot_device_log_session_id")
        return value ?: ""
    }

    fun setIOTDeviceLogSessionId(value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("iot_device_log_session_id", value)
    }
}