package com.shmedo.core.data.repository

import com.shmedo.lib.network.util.BaseURL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rxhttp.tryAwait
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse

/**
 * 创建者：gonghe
 * 创建时间：2024/9/3
 * 描述： TODO
 */
open class BaseRepositoryImp {

    /**
     * 通用post请求,出现异常会 onCatch 回调
     */
    protected suspend inline fun <reified T> commonPostResponseString(
        baseUrl: String = BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl,
        shortMethodUrl: String,
        jsonParam: String,
        headers: Map<String, String> = mapOf(),
        noinline onCatch: ((Throwable) -> Unit)? = null
    ): T? =
        withContext(Dispatchers.IO) {
            RxHttp.postJson(shortMethodUrl)
                .setDomainIfAbsent(baseUrl)
                .addAllHeader(headers)
                .addAll(jsonParam)
                .toAwaitResponse<T>()
                .tryAwait(onCatch)
        }

    /**
     * 通用post请求,出现异常会抛出
     */
    protected suspend inline fun <reified T> commonPostResponseStringCallAwait(
        baseUrl: String = BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl,
        shortMethodUrl: String,
        jsonParam: String,
        headers: Map<String, String> = mapOf(),
    ): T =
        withContext(Dispatchers.IO) {
            RxHttp.postJson(shortMethodUrl)
                .setDomainIfAbsent(baseUrl)
                .addAllHeader(headers)
                .addAll(jsonParam)
                .toAwaitResponse<T>()
                .await()
        }
}