package com.shmedo.lib.core.util

import com.shmedo.lib.core.base.model.UserInfo
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
    fun getUserName(): String {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("username")
        return value ?: ""
    }

    fun setUserName(value: String) {
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

    /**
     * 获取保存的用户信息
     */
    fun getUser(): UserInfo? {
        val kv = MMKV.defaultMMKV()
        val userStr = kv.decodeString("user_info")

        return if (userStr.isNullOrEmpty()) null
        else MoshiUtil.fromJson<UserInfo>(userStr)
    }

    fun setUser(info: UserInfo?) {
        val kv = MMKV.defaultMMKV()
        info?.let {
            kv.encode("user_info", MoshiUtil.toJson(info))
        }
    }

}