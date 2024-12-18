package com.shmedo.mcloudapp.ui.viewmodel.request

import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.core.data.repository.AdmeConfigRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.model.AdmeConfigInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import org.json.JSONObject

/**
 * 创建者：gonghe
 * 创建时间：2024/11/26
 * 描述： TODO
 */
class AdmeConfigViewModel(
    private val admeConfigRepositoryImp: AdmeConfigRepositoryImp,
    loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    /**
     * 根据设备SN查询配置信息
     */
    suspend fun queryConfigByDeviceSN(deviceToken: String): AdmeConfigInfo? {
        return admeConfigRepositoryImp.queryConfigByDeviceSN(deviceToken) { error: Throwable ->
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
    suspend fun queryConfigByHoleNumber(
        projectID: String,
        areaNumber: String,
        holeNumber: String
    ): AdmeConfigInfo? {
        return admeConfigRepositoryImp.queryConfigByHoleNumber(
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
    suspend fun queryAllProjectID(): List<String>? {
        return admeConfigRepositoryImp.queryAllProjectID() { error: Throwable ->
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
    suspend fun queryAllAreaID(projectID: String): List<String>? {
        return admeConfigRepositoryImp.queryAllAreaID(projectID) { error: Throwable ->
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
    suspend fun queryAllHoleNumber(
        projectID: String,
        areaNumber: String
    ): List<String>? {
        return admeConfigRepositoryImp.queryAllHoleNumber(
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
    suspend fun manageConfig(
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

        return admeConfigRepositoryImp.manageConfig(jsonObject.toString()) { error: Throwable ->
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
    suspend fun deleteDeviceConfig(deviceToken: String): String? {
        return admeConfigRepositoryImp.deleteDeviceConfig(deviceToken){ error: Throwable ->
            handleError(
                MutableResult<DataResult<Unit>>(),
                error,
                "${BaseURL.ADME_CONFIG_ADDRESS.baseUrl}/DeleteDeviceConfig"
            )
        }
    }

}