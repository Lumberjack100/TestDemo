package com.shmedo.core.data.repository

import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.model.DispatchCmdItem
import com.shmedo.core.model.QueryCmdResult
import com.shmedo.lib.network.util.BaseURL

/**
 * 创建者：gonghe
 * 创建时间：2024/10/24
 * 描述： TODO
 */

class DeviceInteractiveRepositoryImp : BaseRepositoryImp() {

    /**
     * 批量透明指令下发(限定同一产品)
     */
    suspend fun batchDispatchRawCmd(
        jsonParam: String
    ): List<DispatchCmdItem> {
        val headers: Map<String, String> = mapOf("Authorization" to MmkvCacheUtil.getToken())

        return commonPostResponseStringCallAwait<List<DispatchCmdItem>>(
            baseUrl = BaseURL.IOT_INTERACTIVE_SERVICE_ADDRESS.baseUrl,
            shortMethodUrl = "/BatchDispatchRawCmd",
            jsonParam = jsonParam,
            headers = headers,
        )
    }


    /**
     * 查询指令响应结果
     */
    suspend fun queryCmdResultByMsgID(
        jsonParam: String
    ): List<QueryCmdResult> {
        val headers: Map<String, String> = mapOf("Authorization" to MmkvCacheUtil.getToken())

        return commonPostResponseStringCallAwait<List<QueryCmdResult>>(
            baseUrl = BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl,
            shortMethodUrl = "/QueryCmdResultByMsgID",
            jsonParam = jsonParam,
            headers = headers,
        )
    }
}