package com.shmedo.mcloudapp.ui.viewmodel.request

import com.shmedo.core.data.repository.AdmeConfigRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.model.AdmeConfigInfo
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel

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
        return admeConfigRepositoryImp.queryConfigByDeviceSN(deviceToken)
    }

    /**
     * 根据孔号查询配置信息
     */
    suspend fun queryConfigByHoleNumber(
        projectID: String,
        areaNumber: String,
        holeNumber: String
    ): AdmeConfigInfo? {
        return admeConfigRepositoryImp.queryConfigByHoleNumber(projectID, areaNumber, holeNumber)
    }

    /**
     * 查询所有项目编号
     */
    suspend fun queryAllProjectID(): List<String>? {
        return admeConfigRepositoryImp.queryAllProjectID()
    }

    /**
     * 查询所有区域编号
     */
    suspend fun queryAllAreaID(projectID: String): List<String>? {
        return admeConfigRepositoryImp.queryAllAreaID(projectID)
    }

    /**
     * 查询所有孔编号
     */
    suspend fun queryAllHoleNumber(
        projectID: String,
        areaNumber: String
    ): List<String>? {
        return admeConfigRepositoryImp.queryAllHoleNumber(projectID, areaNumber)
    }

    /**
     * 管理配置
     */
    suspend fun manageConfig(jsonParam: String): String? {
        return admeConfigRepositoryImp.manageConfig(jsonParam)
    }

    /**
     * 删除设备配置
     */
    suspend fun deleteDeviceConfig(deviceToken: String): String? {
        return admeConfigRepositoryImp.deleteDeviceConfig(deviceToken)
    }

}