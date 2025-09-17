package com.shmedo.mcloudapp.ui.viewmodel.request

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ResourceUtils
import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.data.repository.ProductConfigRepositoryImp
import com.shmedo.core.model.AdmeConfigInfo
import com.shmedo.core.model.DeviceCmdOrderInfo
import com.shmedo.core.model.DeviceDebugAddress
import com.shmedo.core.model.SensorModel
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.BuildConfig
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/11/26
 * 描述： TODO
 */
class ProductConfigViewModel(
    private val productConfigRepositoryImp: ProductConfigRepositoryImp,
    loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    //<editor-fold desc="ADME 产品配置接口">
    /**
     * 根据设备SN查询配置信息
     */
    suspend fun queryADMEConfigInfoBySN(deviceToken: String): AdmeConfigInfo? {
        return productConfigRepositoryImp.queryADMEConfigInfoBySN(deviceToken) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/QueryConfigByDeviceSN"
            )
        }
    }

    /**
     * 根据孔号查询配置信息
     */
    suspend fun queryADMEConfigInfoByHoleNumber(
        projectID: String,
        areaNumber: String,
        holeNumber: String
    ): AdmeConfigInfo? {
        return productConfigRepositoryImp.queryADMEConfigInfoByHoleNumber(
            projectID,
            areaNumber,
            holeNumber
        ) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/QueryConfigByHoleNumber"
            )
        }
    }

    /**
     * 查询所有项目编号
     */
    suspend fun queryADMEAllProjectID(): List<String>? {
        return productConfigRepositoryImp.queryADMEAllProjectID { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/ListAllProjectID"
            )
        }
    }

    /**
     * 查询所有区域编号
     */
    suspend fun queryADMEAllAreaID(projectID: String): List<String>? {
        return productConfigRepositoryImp.queryADMEAllAreaID(projectID) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/ListAreaNumber"
            )
        }
    }

    /**
     * 查询所有孔编号
     */
    suspend fun queryADMEAllHoleNumber(
        projectID: String,
        areaNumber: String
    ): List<String>? {
        return productConfigRepositoryImp.queryADMEAllHoleNumber(
            projectID,
            areaNumber
        ) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/ListHoleNumber"
            )
        }
    }

    /**
     * 管理配置
     */
    suspend fun manageADMEConfig(
        deviceToken: String,
        projectID: String,
        areaNumber: String,
        holeNumber: String,
        configJson: String = "",
    ): String? {
        val jsonObject = JSONObject().apply {
            put("deviceSN", deviceToken)
            put("projectID", projectID)
            put("areaNumber", areaNumber)
            put("holeNumber", holeNumber)
            put("config", configJson)
            put("exValues", "")
        }

        return productConfigRepositoryImp.manageADMEConfig(jsonObject.toString()) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/ManageConfig"
            )
        }
    }

    /**
     * 删除设备配置
     */
    suspend fun deleteADMEConfigBySN(deviceToken: String): String? {
        return productConfigRepositoryImp.deleteADMEConfigBySN(deviceToken) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/DeleteDeviceConfig"
            )
        }
    }
    // </editor-fold>


    //<editor-fold desc="孙建伟通用配置接口">
    /**
     * 获取远程设备登录信息
     */
    suspend fun getRemoteDeviceLogin(
        deviceSn: String,
        deviceKey: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceDebugAddress? {
        val jsonObjectRequest = JSONObject()//接口请求参数
        jsonObjectRequest.put("appKey", BuildConfig.AMS_APP_KEY)
        jsonObjectRequest.put("appSecret", BuildConfig.AMS_APP_SECRET)
        jsonObjectRequest.put("deviceSn", deviceSn)
        jsonObjectRequest.put("deviceKey", deviceKey)
        jsonObjectRequest.put("reCreate", false)

        return productConfigRepositoryImp.getRemoteDebugDeviceServerInfo(jsonObjectRequest.toString()) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.AMS_CONFIG_ADDRESS.baseUrl}/DeviceLogin"
            )
            onCatch?.invoke(error)
        }
    }

    /**
     * 获取设备快速配置参数指令模版
     */
    suspend fun getDeviceGetCmdOrdersBySn(
        verificationSuffix: String,
        param: Map<String, String>,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceCmdOrderInfo? {
        return productConfigRepositoryImp.getDeviceGetCmdOrdersBySn(
            verificationSuffix,
            param
        ) { error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.AMS_CONFIG_ADDRESS.baseUrl}/GetCmdOrdersBySn"
            )
            onCatch?.invoke(error)
        }
    }

    /**
     * 加载MR702传感器配置
     */
    fun loadMR702SensorConfig() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // 先从服务器中获取传感器配置列表
                val remoteSensorConfigList: List<SensorModel> = queryRemoteMR702SensorConfigList()
                
                if (remoteSensorConfigList.isEmpty()) {
                    // 如果服务器中没有传感器配置列表，则从本地缓存中获取传感器配置列表获取
                    val sensorConfigList: List<SensorModel> =
                        MmkvCacheUtil.getMR702SensorConfigInfo()
                    if (sensorConfigList.isNotEmpty())
                        return@launch

                    // 如果本地缓存中没有传感器配置列表，则从本地资源文件中获取传感器配置列表
                    val localSensorConfigInfo =
                        ResourceUtils.readAssets2String("mr702_sensor_config.json")
                    val localSensorConfigList =
                        MoshiUtil.fromJson<List<SensorModel>>(localSensorConfigInfo)
                            ?: arrayListOf()

                    MmkvCacheUtil.setMR702SensorConfigInfo(localSensorConfigList)
                    return@launch
                }

                MmkvCacheUtil.setMR702SensorConfigInfo(remoteSensorConfigList)
            } catch (e: Exception) {
                Timber.e(e)
                val msg =
                    "call loadMR702SensorConfig() error: ${e.localizedMessage}" //这里的msg是网络请求的错误信息
                addLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = Log.ERROR,
                    data = msg
                )
            }
        }
    }

    /**
     * 获取远程 MR702 传感器配置列表
     */
    suspend fun queryRemoteMR702SensorConfigList(onCatch: ((Throwable) -> Unit)? = null): List<SensorModel> {
        val jsonObject = JSONObject().apply {
            put("productName", "")
            put("portName", "")
            put("sensorName", "")
        }
        val tempList =
            productConfigRepositoryImp.queryMR702SensorConfigList(jsonObject.toString()) { error: Throwable ->
                handleError(
                    MutableResult<DataResult<Unit>>(),
                    error,
                    "${BaseURL.AMS_CONFIG_ADDRESS.baseUrl}/QueryDeviceTemplates"
                )
            }

        return tempList ?: return arrayListOf()
    }
    // </editor-fold>
}