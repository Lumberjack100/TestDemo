package com.shmedo.core.data.repository

import com.blankj.utilcode.util.AppUtils
import com.shmedo.core.model.CheckSoftModel
import com.shmedo.lib.network.parser.PgyerApiResponseParser
import com.shmedo.lib.network.util.BaseURL

import rxhttp.toAwait
import rxhttp.tryAwait
import rxhttp.wrapper.param.RxHttp

/**
 * 创建者：gonghe
 * 创建时间：2024/4/1
 * 描述： TODO
 */
class AppUpdateRepositoryImp {

    suspend fun checkAppVersion(
        pgyApiKey: String ,
        pgyAppKey: String ,
        onCatch: ((Throwable) -> Unit)? = null
    ): CheckSoftModel? =
        RxHttp.postForm("/check")
            .setDomainIfAbsent(BaseURL.PGYER_SERVICE_ADDRESS.baseUrl)
            .add("_api_key", pgyApiKey)
            .add("appKey", pgyAppKey)
            .add("buildVersion", AppUtils.getAppVersionName())
            .toAwait(object : PgyerApiResponseParser<CheckSoftModel>() {})
            .tryAwait(onCatch)
}