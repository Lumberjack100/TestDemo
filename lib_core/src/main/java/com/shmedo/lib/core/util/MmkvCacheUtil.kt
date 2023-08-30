package com.shmedo.lib.core.util

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

    fun getUserName(): String {
        val kv = MMKV.defaultMMKV()
        val _value = kv.decodeString("username")
        return _value ?: ""
    }

    fun setUserName(_value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("username", _value)
    }

    fun getPassword(): String {
        val kv = MMKV.defaultMMKV()
        val _value = kv.decodeString("password")
        return _value ?: ""
    }

    fun setPassword(_value: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("password", _value)
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

//    /**
//     * 获取保存的用户信息
//     */
//    fun getUser(): UserInfo? {
//        val kv = MMKV.defaultMMKV()
//        val userStr = kv.decodeString("user")
//        return if (TextUtils.isEmpty(userStr)) {
//            null
//        } else {
//            Gson().fromJson(userStr, UserInfo::class.java)
//        }
//    }
//
//    fun setUser(info: UserInfo?) {
//        val kv = MMKV.defaultMMKV()
//        if (info == null) {
//            kv.encode("user", "")
//            setIsLogin(false)
//        } else {
//            kv.encode("user", Gson().toJson(info))
//            setIsLogin(true)
//        }
//    }

}