package com.shmedo.core.data.repository

import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.model.AdmeConfigInfo
import com.shmedo.core.model.DeviceCmdOrderInfo
import com.shmedo.core.model.DeviceDebugAddress
import com.shmedo.core.model.SensorModel
import com.shmedo.lib.network.util.BaseURL
import org.json.JSONObject
import rxhttp.tryAwait
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse

/**
 * 创建者：gonghe
 * 创建时间：2024/11/26
 * 描述： TODO
 */
class ProductConfigRepositoryImp : BaseRepositoryImp() {

    companion object {
        const val ADME_CONFIG_AUTHORIZATION: String = "357d6378-2eff-4d1e-a8d9-acc11f898c25"

    }

    //<editor-fold desc="ADME 产品配置接口">
    /**
     * 根据设备SN查询配置信息
     */
    suspend fun queryADMEConfigInfoBySN(
        deviceToken: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): AdmeConfigInfo? {
        val headers: Map<String, String> = mapOf("Authorization" to ADME_CONFIG_AUTHORIZATION)

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
    suspend fun queryADMEConfigInfoByHoleNumber(
        projectID: String,
        areaNumber: String,
        holeNumber: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): AdmeConfigInfo? {
        val headers: Map<String, String> = mapOf("Authorization" to ADME_CONFIG_AUTHORIZATION)

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
    suspend fun queryADMEAllProjectID(
        onCatch: ((Throwable) -> Unit)? = null
    ): List<String>? {
        val headers: Map<String, String> = mapOf("Authorization" to ADME_CONFIG_AUTHORIZATION)

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
    suspend fun queryADMEAllAreaID(
        projectID: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<String>? {
        val headers: Map<String, String> = mapOf("Authorization" to ADME_CONFIG_AUTHORIZATION)

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
    suspend fun queryADMEAllHoleNumber(
        projectID: String,
        areaNumber: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<String>? {
        val headers: Map<String, String> = mapOf("Authorization" to ADME_CONFIG_AUTHORIZATION)

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
    suspend fun manageADMEConfig(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val headers: Map<String, String> = mapOf("Authorization" to ADME_CONFIG_AUTHORIZATION)

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
    suspend fun deleteADMEConfigBySN(
        deviceToken: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val headers: Map<String, String> = mapOf("Authorization" to ADME_CONFIG_AUTHORIZATION)

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
    // </editor-fold>

    //<editor-fold desc="孙建伟通用配置接口">
    /**
     * 查询设备远程调试连接地址信息
     */
    suspend fun getRemoteDebugDeviceServerInfo(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceDebugAddress? =
        RxHttp.postJson("/DeviceLogin")
            .setDomainIfAbsent(BaseURL.AMS_CONFIG_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<DeviceDebugAddress>()
            .tryAwait(onCatch)

    /**
     * 获取设备快速配置参数指令模版
     */
    suspend fun getDeviceGetCmdOrdersBySn(
        verificationSuffix: String,
        param: Map<String, String>,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceCmdOrderInfo? =
        RxHttp.get("/GetCmdOrdersBySn/$verificationSuffix")
            .setDomainIfAbsent(BaseURL.AMS_CONFIG_ADDRESS.baseUrl)
            .addAll(param)
            .toAwaitResponse<DeviceCmdOrderInfo>()
            .tryAwait(onCatch)

    /**
     * 获取 MR702 传感器远程配置信息
     */
    suspend fun queryMR702SensorConfigList(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<SensorModel>? =
        RxHttp.postJson("/QueryDeviceTemplates")
            .setDomainIfAbsent(BaseURL.AMS_CONFIG_ADDRESS.baseUrl)
            .addHeader("Authorization", AuthMMKVOwner.token)
            .addAll(jsonParam)
            .toAwaitResponse<List<SensorModel>>()
            .tryAwait(onCatch)

    // </editor-fold>
}