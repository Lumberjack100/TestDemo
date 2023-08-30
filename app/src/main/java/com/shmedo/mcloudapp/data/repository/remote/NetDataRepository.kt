package com.shmedo.mcloudapp.data.repository.remote

import android.annotation.SuppressLint
import com.shmedo.lib.core.base.model.BasicUserInfo
import com.shmedo.lib.core.base.model.UserWrapperInfo
import com.shmedo.lib.network.util.BaseURL
import rxhttp.tryAwait
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/8/28 <br/>
 * 描述：     TODO
 */
@SuppressLint("CheckResult")
class NetDataRepository private constructor() {

    /**
     * 发送验证码
     */
    suspend fun sendSmsCode(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/SendSmsCode")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 账户密码登录
     */
    suspend fun loginByAccount(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/SignIn")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 手机验证码登录
     */
    suspend fun loginByPhone(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/SmsLogin")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 通过token获取用户基本信息
     */
    suspend fun getUserByToken(onCatch: ((Throwable) -> Unit)? = null): BasicUserInfo? =
        RxHttp.get("/GetUserByToken")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .toAwaitResponse<BasicUserInfo>()
            .tryAwait(onCatch)

    /**
     * 查询用户详细信息
     */
    suspend fun queryUserByID(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): UserWrapperInfo? =
        RxHttp.postJson("/QueryUserByID")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<UserWrapperInfo>()
            .tryAwait(onCatch)

    /**
     * 修改用户信息
     */
    suspend fun updateUserInfo(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/UpdateUser")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    suspend fun uploadUserAvatar(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/UploadUserAvatar")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    companion object {
        val instance = NetDataRepository()
    }
}