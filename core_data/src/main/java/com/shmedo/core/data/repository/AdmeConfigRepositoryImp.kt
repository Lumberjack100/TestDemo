package com.shmedo.core.data.repository

import com.shmedo.core.model.AdmeConfigInfo
import com.shmedo.lib.network.util.BaseURL
import org.json.JSONObject

/**
 * 创建者：gonghe
 * 创建时间：2024/11/26
 * 描述： TODO
 */
class AdmeConfigRepositoryImp : BaseRepositoryImp() {

    companion object {
        const val authorization: String = "357d6378-2eff-4d1e-a8d9-acc11f898c25"
    }

    /**
     * 根据设备SN查询配置信息
     */
    suspend fun queryConfigByDeviceSN(
        deviceToken: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): AdmeConfigInfo? {
        val headers: Map<String, String> = mapOf("Authorization" to authorization)

        val jsonObject = JSONObject().apply {
            put("deviceSN", deviceToken)
        }

        return commonPostResponseString<AdmeConfigInfo>(
            baseUrl = BaseURL.ADME_CONFIG_ADDRESS.baseUrl,
            shortMethodUrl = "/QueryConfigByDeviceSN",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 根据孔号查询配置信息
     */
    suspend fun queryConfigByHoleNumber(
        projectID: String,
        areaNumber: String,
        holeNumber: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): AdmeConfigInfo? {
        val headers: Map<String, String> = mapOf("Authorization" to authorization)

        val jsonObject = JSONObject().apply {
            put("projectID", projectID)
            put("areaNumber", areaNumber)
            put("holeNumber", holeNumber)
        }

        return commonPostResponseString<AdmeConfigInfo>(
            baseUrl = BaseURL.ADME_CONFIG_ADDRESS.baseUrl,
            shortMethodUrl = "/QueryConfigByHoleNumber",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 查询所有项目编号
     */
    suspend fun queryAllProjectID(
        onCatch: ((Throwable) -> Unit)? = null
    ): List<String>? {
        val headers: Map<String, String> = mapOf("Authorization" to authorization)

        return commonGetResponseString<List<String>>(
            baseUrl = BaseURL.ADME_CONFIG_ADDRESS.baseUrl,
            shortMethodUrl = "/ListAllProjectID",
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 查询所有区域编号
     */
    suspend fun queryAllAreaID(
        projectID: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<String>? {
        val headers: Map<String, String> = mapOf("Authorization" to authorization)

        val jsonObject = JSONObject().apply {
            put("projectID", projectID)
        }

        return commonPostResponseString<List<String>>(
            baseUrl = BaseURL.ADME_CONFIG_ADDRESS.baseUrl,
            shortMethodUrl = "/ListAreaNumber",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 查询所有孔编号
     */
    suspend fun queryAllHoleNumber(
        projectID: String,
        areaNumber: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<String>? {
        val headers: Map<String, String> = mapOf("Authorization" to authorization)

        val jsonObject = JSONObject().apply {
            put("projectID", projectID)
            put("areaNumber", areaNumber)
        }

        return commonPostResponseString<List<String>>(
            baseUrl = BaseURL.ADME_CONFIG_ADDRESS.baseUrl,
            shortMethodUrl = "/ListHoleNumber",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 管理配置
     */
    suspend fun manageConfig(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val headers: Map<String, String> = mapOf("Authorization" to authorization)

        return commonPostResponseString<String>(
            baseUrl = BaseURL.ADME_CONFIG_ADDRESS.baseUrl,
            shortMethodUrl = "/ManageConfig",
            jsonParam = jsonParam,
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 删除设备配置
     */
    suspend fun deleteDeviceConfig(
        deviceToken: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val headers: Map<String, String> = mapOf("Authorization" to authorization)

        val jsonObject = JSONObject().apply {
            put("deviceSN", deviceToken)
        }

        return commonPostResponseString<String>(
            baseUrl = BaseURL.ADME_CONFIG_ADDRESS.baseUrl,
            shortMethodUrl = "/DeleteDeviceConfig",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }
}