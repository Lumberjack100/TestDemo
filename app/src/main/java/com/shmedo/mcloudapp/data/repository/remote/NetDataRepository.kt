package com.shmedo.mcloudapp.data.repository.remote

import android.annotation.SuppressLint
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.data.model.bean.BasicUserInfo
import com.shmedo.mcloudapp.data.model.bean.UserWrapperInfo
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

    companion object {
        val instance = NetDataRepository()
    }
}