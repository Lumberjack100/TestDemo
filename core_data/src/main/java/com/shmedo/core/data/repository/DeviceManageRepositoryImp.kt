package com.shmedo.core.data.repository

import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.model.DeviceSensorBasicInfo
import com.shmedo.core.model.DeviceSensorDataPageInfo
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.util.BaseURL
import org.json.JSONArray
import org.json.JSONObject

/**
 * 创建者：gonghe
 * 创建时间：2024/9/3
 * 描述： TODO
 */
class DeviceManageRepositoryImp : BaseRepositoryImp() {

    /**
     * 查询查询设备下的传感器
     */
    suspend fun queryDeviceSensorListWithPage(
        deviceToken: String = "",//SN号
        iotSensorType: String = "",//传感器类型
        currentPage: Int = 1,
        pageSize: Int = 100,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<DeviceSensorBasicInfo>? {
        val jsonObject = JSONObject().apply {
            put("deviceToken", deviceToken)
            if (iotSensorType.isNotEmpty())
                put("iotSensorType", iotSensorType)
            put("sensorValid", true)
            put("currentPage", currentPage)
            put("pageSize", pageSize)
        }
        val headers: Map<String, String> = mapOf("Authorization" to MmkvCacheUtil.getToken())

        return commonPostResponseString<PageList<DeviceSensorBasicInfo>>(
            baseUrl = BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl,
            shortMethodUrl = "/QueryDeviceSensor",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 查询单个传感器最新数据
     * @param sensorID 传感器编号。如果传感器有设备，则校验设备权限。无，则校验公司权限。两种情况都有一起校验。
     */
    suspend fun querySensorNewData(
        sensorID: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<Map<String, String>>? {
        val jsonObject = JSONObject().apply {
            put("sensorID", sensorID)
        }
        val headers: Map<String, String> = mapOf("Authorization" to MmkvCacheUtil.getToken())

        return commonPostResponseString<List<Map<String, String>>>(
            baseUrl = BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl,
            shortMethodUrl = "/DescribeSensorNewData",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 批量分页查询传感器最新数据列表
     * @param sensorIDList 传感器编号列表。所有传感器必须属于同一个公司,且属于同一个物联网传感器类型。如果传感器有设备，则校验设备权限。无，则校验公司权限。两种情况都有一起校验
     * @param begin 开始时间
     * @param end 结束时间
     * @param density 数据密度
     * @param dateTimeSort true为正序，false为逆序
     */
    suspend fun querySensorDataListExWithPage(
        sensorIDList: List<String>,
        begin: String = "",
        end: String = "",
        density: String = "0",
        dateTimeSort: Boolean = false,
        currentPage: Int = 1,
        pageSize: Int = 100,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceSensorDataPageInfo? {
        val jsonObject = JSONObject().apply {
            put("sensorIDList", JSONArray(sensorIDList))
            put("begin", begin)
            put("end", end)
            put("density", density)
            put("dateTimeSort", dateTimeSort)
            put("currentPage", currentPage)
            put("pageSize", pageSize)
        }
        val headers: Map<String, String> = mapOf("Authorization" to MmkvCacheUtil.getToken())

        return commonPostResponseString<DeviceSensorDataPageInfo>(
            baseUrl = BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl,
            shortMethodUrl = "/ListPageSensorDataListEx",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }

    /**
     * 分页查询设备文件
     * @param deviceToken 设备编号
     * @param begin 开始时间
     * @param end 结束时间
     * @param fileType 文件类型 枚举 1.图片 2.文本 3.压缩包 4.二进制
     * @param orderType 排序 1.按时间降序排序(默认) 2.按时间升序排序
     */
    suspend fun queryDeviceFileListWithPage(
        deviceToken: String = "",
        begin: String = "",
        end: String = "",
        fileType: String = "1",
        orderType: String = "1",
        currentPage: Int = 1,
        pageSize: Int = 100,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<Map<String, String>>? {
        val jsonObject = JSONObject().apply {
            put("companyID", AuthMMKVOwner.companyID)
            put("deviceToken", deviceToken)
            put("startTime", begin)
            put("endTime", end)
            put("fileType", fileType)
            put("orderType", orderType)
            put("currentPage", currentPage)
            put("pageSize", pageSize)
        }
        val headers: Map<String, String> = mapOf("Authorization" to MmkvCacheUtil.getToken())

        return commonPostResponseString<PageList<Map<String, String>>>(
            baseUrl = BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl,
            shortMethodUrl = "/QueryDeviceFilePage",
            jsonParam = jsonObject.toString(),
            headers = headers,
            onCatch = onCatch
        )
    }
}