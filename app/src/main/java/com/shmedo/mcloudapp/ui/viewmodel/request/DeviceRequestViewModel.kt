package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.data.repository.DeviceManageRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.model.CloudDeviceData
import com.shmedo.core.model.DeviceBackupInfo
import com.shmedo.core.model.DeviceDetailInfo
import com.shmedo.core.model.DeviceInfo
import com.shmedo.core.model.DeviceSensorBasicInfo
import com.shmedo.core.model.DeviceStatisticInfo
import com.shmedo.core.model.FirmWareInfo
import com.shmedo.lib.cmd.base.iot_cmd.enums.MonitoringType
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.model.HoverHeaderModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： TODO
 *
 *
 */
class DeviceRequestViewModel(
    private val deviceManageRepositoryImp: DeviceManageRepositoryImp,
    loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    private val _deviceStatisticInfoResult = MutableResult<DataResult<DeviceStatisticInfo>>()
    val deviceStatisticInfoResult: Result<DataResult<DeviceStatisticInfo>> =
        _deviceStatisticInfoResult

    private val _deviceListResult = MutableResult<DataResult<List<DeviceInfo>>>()
    val deviceListResult: Result<DataResult<List<DeviceInfo>>> =
        _deviceListResult

    private val _followDeviceListResult = MutableResult<DataResult<List<DeviceInfo>>>()
    val followDeviceListResult: Result<DataResult<List<DeviceInfo>>> =
        _followDeviceListResult

    private val _followDeviceResult = MutableResult<DataResult<String>>()
    val followDeviceResult: Result<DataResult<String>> = _followDeviceResult

    private val _cancelFollowDeviceResult = MutableResult<DataResult<String>>()
    val cancelFollowDeviceResult: Result<DataResult<String>> = _cancelFollowDeviceResult

    private val _deviceInfoResult = MutableResult<DataResult<DeviceInfo>>()
    val deviceInfoResult: Result<DataResult<DeviceInfo>> =
        _deviceInfoResult

    private val _cloudDeviceDataListResult = MutableResult<DataResult<List<CloudDeviceData>>>()
    val cloudDeviceDataListResult: Result<DataResult<List<CloudDeviceData>>> =
        _cloudDeviceDataListResult

    private val _sensorDataListResult = MutableResult<DataResult<List<Any>>>()
    val sensorDataListResult: Result<DataResult<List<Any>>> =
        _sensorDataListResult

    private val _firmWareListResult = MutableResult<DataResult<List<FirmWareInfo>>>()
    val firmWareListResult: Result<DataResult<List<FirmWareInfo>>> =
        _firmWareListResult

    /**
     * 查询公司设备在线统计信息
     */
    fun getDeviceStatByCompanyID(companyID: Int, isHasListSuperInfoPermission: Boolean = false) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("companyID", companyID)

            val data: DeviceStatisticInfo =
                deviceManageRepositoryImp.getDeviceStatByCompanyID(
                    jsonObjectRequest.toString(),
                    isHasListSuperInfoPermission
                ) { error: Throwable ->
                    handleError(
                        _deviceStatisticInfoResult,
                        error,
                        "getDeviceStatByCompanyID"
                    )
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _deviceStatisticInfoResult.postValue(DataResult(data, responseStatus = responseStatus))
        }
    }


    /**
     * 分页查询设备列表
     */
    fun getDeviceList(
        companyID: Int,
        productIDList: List<Int>? = emptyList(), // 新增参数，逗号分隔的产品ID列表
        deviceToken: String = "",
        currentPage: Int,
        pageSize: Int,
        isHasListSuperInfoPermission: Boolean = false,
        onlineStatus: String = "",
    ) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject().apply {
                put("companyID", companyID)
                if (deviceToken.isNotEmpty())
                    put("deviceToken", deviceToken)//SN号,支持模糊查询
                if (!productIDList.isNullOrEmpty()) {
                    // 优先使用 productIDList
                    put("productIDList", JSONArray(productIDList))//产品ID列表,逗号分隔
                }
                if (onlineStatus.isNotEmpty())
                    put("onlineStatus", onlineStatus)//在线状态
                if (isHasListSuperInfoPermission)
                    put("filterNoPermissionDevice", true)//过滤用户无权限设备
                put("deviceStatus", "启用")//null选择全部，启用选择启用设备，禁用选择未启用设备
                //put("sortSNAsc", true)//ture按SN正序，false按Sn逆序
                put("tokenAndVersion", false)//sn号和版本号之间得关系
                put("currentPage", currentPage)
                put("pageSize", pageSize)
            }

            val data: PageList<DeviceInfo> =
                deviceManageRepositoryImp.queryDeviceList(
                    jsonObjectRequest.toString(),
                    isHasListSuperInfoPermission
                ) { error: Throwable ->
                    handleError(_deviceListResult, error, "queryDeviceList")
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _deviceListResult.postValue(
                DataResult(
                    data.currentPageData,
                    responseStatus = responseStatus,
                    totalCount = data.totalCount,
                    totalPage = data.totalPage
                )
            )
        }
    }

    /**
     * 分页查询收藏设备列表
     */
    fun getFollowDeviceList(
        deviceSn: String = "",
        deviceName: String = "",
        currentPage: Int,
        pageSize: Int,
    ) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            if (deviceSn.isNotEmpty())
                jsonObjectRequest.put("deviceSn", deviceSn)//SN号,支持模糊查询
            if (deviceName.isNotEmpty())
                jsonObjectRequest.put("deviceName", deviceName)//在线状态
            jsonObjectRequest.put("currentPage", currentPage)
            jsonObjectRequest.put("pageSize", pageSize)

            val data: PageList<DeviceInfo> =
                deviceManageRepositoryImp.queryFollowDeviceList(
                    jsonObjectRequest.toString()
                ) { error: Throwable ->
                    handleError(_followDeviceListResult, error, "queryFollowDeviceList")

                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _followDeviceListResult.postValue(
                DataResult(
                    data.currentPageData,
                    responseStatus = responseStatus,
                    totalCount = data.totalCount,
                    totalPage = data.totalPage
                )
            )
        }
    }

    fun addUserFollowDevice(deviceSn: String = "") {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("deviceSn", deviceSn)

            val data: String = deviceManageRepositoryImp.addUserFollowDevice(
                jsonObjectRequest.toString(),
            ) { error: Throwable ->
                handleError(
                    _followDeviceResult,
                    error,
                    "${BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl}/AddUserFollowDevice"
                )

            } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _followDeviceResult.postValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    fun cancelUserFollowDevice(deviceSn: String = "") {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("deviceSn", deviceSn)

            val data: String = deviceManageRepositoryImp.cancelUserFollowDevice(
                jsonObjectRequest.toString(),
            ) { error: Throwable ->
                handleError(
                    _cancelFollowDeviceResult,
                    error,
                    "${BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl}/CancelUserFollowDevice"
                )

            } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _cancelFollowDeviceResult.postValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    /**
     * 获取设备详细信息
     */
    fun getDeviceDetailInfo(deviceToken: String = "") {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("deviceToken", deviceToken)

            val data: DeviceDetailInfo =
                deviceManageRepositoryImp.getDeviceDetailInfo(jsonObjectRequest.toString()) { error: Throwable ->
                    handleError(
                        _deviceInfoResult,
                        error,
                        "${BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl}/GetDeviceDetail"
                    )

                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _deviceInfoResult.postValue(
                DataResult(
                    data.deviceInfo,
                    responseStatus = responseStatus
                )
            )
        }
    }

    suspend fun getDeviceDetailInfo(
        deviceToken: String = "",
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceDetailInfo? {
        val jsonObjectRequest = JSONObject()
        jsonObjectRequest.put("deviceToken", deviceToken)

        return deviceManageRepositoryImp.getDeviceDetailInfo(jsonObjectRequest.toString(), onCatch)
    }

    suspend fun queryDeviceBackupList(
        deviceID: String,
        currentPage: Int = 1,
        pageSize: Int = 100,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<DeviceBackupInfo>? {
        val jsonObjectRequest = JSONObject()
        jsonObjectRequest.put("deviceID", deviceID)
        jsonObjectRequest.put("currentPage", currentPage)
        jsonObjectRequest.put("pageSize", pageSize)

        return deviceManageRepositoryImp.queryDeviceBackupListWithPage(
            jsonObjectRequest.toString(),
            onCatch
        )
    }

    suspend fun applyBackup(
        backupID: String = "",
        deviceID: String = "",
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val jsonObjectRequest = JSONObject()
        jsonObjectRequest.put("backupID", backupID)
        jsonObjectRequest.put("deviceID", deviceID)

        return deviceManageRepositoryImp.applyBackup(jsonObjectRequest.toString(), onCatch)
    }

    /**
     * 更换设备，将拉旧设备的最新配置数据同步给新设备（如果老设备没有相关备份文件，就不同步），并且自动旧设备的传感器关联标识更改生成一个新的，新设备的传感器标识改成旧设备之前的传感器关联标识，旧设备的；并且将新设备自动加入到之前旧设备的相关分组中
     * @param companyID 公司ID
     * @param oldDeviceToken 旧设备Token
     * @param newDeviceToken 新设备Token
     * @param configEnable 是否同步配置
     * @param failureMainType 故障主类型
     * @param failureChildType 故障子类型
     * @param failureDesc 故障描述
     * @param replaceTime 更换时间
     * @param replacePerson 更换人
     * @param onCatch 异常捕获
     * @return 返回结果
     */
    suspend fun replaceDevice(
        companyID: Int,
        oldDeviceToken: String = "",
        newDeviceToken: String = "",
        configEnable: Boolean = false,
        failureMainType: Int = 1,
        failureChildType: Int = 1,
        failureDesc: String = "",
        replaceTime: String = "",
        replacePerson: String = "",
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val jsonObjectRequest = JSONObject()
        jsonObjectRequest.put("companyID", companyID)
        jsonObjectRequest.put("oldDeviceToken", oldDeviceToken)
        jsonObjectRequest.put("newDeviceToken", newDeviceToken)
        jsonObjectRequest.put("configEnable", configEnable)
        jsonObjectRequest.put("failureMainType", failureMainType)
        jsonObjectRequest.put("failureChildType", failureChildType)
        jsonObjectRequest.put("failureDesc", failureDesc)
        jsonObjectRequest.put("replaceTime", replaceTime)
        jsonObjectRequest.put("replacePerson", replacePerson)

        return deviceManageRepositoryImp.replaceDevice(jsonObjectRequest.toString(), onCatch)
    }

    /**
     * 根据产品ID查询固件列表
     */
    fun queryFirmwareListByProductIDWithPage(
        productID: Int,//
        companyID: Int,//
        fwStatus: String = "",//固件环境代码
        fwName: String = "",//固件名称，支持模糊查询
        fwVersion: String = "",//固件版本号,支持模糊查询
        nameAndVersion: Boolean = false,//固件名和版本号之间关系
        currentPage: Int = 1,
        pageSize: Int = 20,
    ) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("productID", productID)
            jsonObjectRequest.put("companyID", companyID)
            jsonObjectRequest.put("fwStatus", fwStatus)
            jsonObjectRequest.put("fwName", fwName)
            jsonObjectRequest.put("fwVersion", fwVersion)
            jsonObjectRequest.put("nameAndVersion", nameAndVersion)
            jsonObjectRequest.put("currentPage", currentPage)
            jsonObjectRequest.put("pageSize", pageSize)

            val data: PageList<FirmWareInfo> =
                deviceManageRepositoryImp.queryFirmwareListByProductIDWithPage(
                    jsonObjectRequest.toString()
                ) { error: Throwable ->
                    handleError(
                        _firmWareListResult,
                        error,
                        "${BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl}/GetFirmwareListByProductID"
                    )

                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _firmWareListResult.postValue(
                DataResult(
                    data.currentPageData,
                    responseStatus = responseStatus,
                    totalCount = data.totalCount,
                    totalPage = data.totalPage
                )
            )
        }
    }

    suspend fun applyFirmwareUpgrade(
        deviceToken: String,
        firmwareID: Int,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val jsonObjectRequest = JSONObject()
        jsonObjectRequest.put("deviceToken", deviceToken)
        jsonObjectRequest.put("firmwareID", firmwareID)

        return deviceManageRepositoryImp.applyFirmwareUpgrade(
            jsonObjectRequest.toString(),
            onCatch
        )
    }

    //<editor-fold desc="获取传感器数据">
    suspend fun queryDeviceSensor(
        deviceToken: String = "",
        iotSensorType: String = "",//传感器类型
        currentPage: Int = 1,
        pageSize: Int = 100,
    ): List<DeviceSensorBasicInfo> = withContext(Dispatchers.Default) {
        val sensorList = deviceManageRepositoryImp.queryDeviceSensorListWithPage(
            deviceToken = deviceToken,
            iotSensorType = iotSensorType,
            currentPage = currentPage,
            pageSize = pageSize
        )?.currentPageData ?: emptyList()

        sensorList
    }

    /**
     * 查询设备最新监测数据
     */
    suspend fun queryLatestSensorData(
        deviceToken: String = "",
        iotSensorTypeList: List<String> = emptyList(),
    ): Map<String, String> {
        if (iotSensorTypeList.isEmpty()) return emptyMap()

        // 根据 iotSensorTypeList 获取对应的传感器ID列表
        val sensorInfoList = iotSensorTypeList.mapNotNull { sensorType ->
            queryDeviceSensor(deviceToken, sensorType).firstOrNull()?.let { sensorType to it.id }
        }
        if (sensorInfoList.isEmpty()) return emptyMap()

        // 使用协程并发查询每个传感器的最新数据，并携带传感器类型信息
        val resultSensorDataList = coroutineScope {
            sensorInfoList.map { (sensorType, sensorID) ->
                async {
                    deviceManageRepositoryImp.querySensorNewData(sensorID)?.firstOrNull()
                        ?.let { sensorType to it }
                }
            }.awaitAll().filterNotNull()
        }

        // 将所有传感器第一条数据合并成一个映射，针对 value 字段按照传感器类型拆分
        val resultMap = mutableMapOf<String, String>()
        for ((sensorType, sensorData) in resultSensorDataList) {
            for ((rawKey, value) in sensorData) {
                val mappedKey = when {
                    rawKey != "value" -> rawKey
                    sensorType == MonitoringType.FLOW_METER.code -> "flow_velocity"
                    sensorType == MonitoringType.SPRING_WATER_FLOW_METER.code -> "flow_rate"
                    else -> rawKey
                }

                if (mappedKey !in resultMap) {
                    resultMap[mappedKey] = value
                }

                if (rawKey == "value" && rawKey !in resultMap) {
                    resultMap[rawKey] = value
                }
            }
        }

        return resultMap
    }

    /**
     * 分页查询设备监测数据
     */
    fun queryMonitorDataListWithPage(
        isQueryFile: Boolean = false,
        deviceToken: String = "",
        sensorIDList: List<String> = emptyList(),
        begin: String = "",
        end: String = "",
        currentPage: Int = 1,
        pageSize: Int = 100,
    ) = viewModelScope.launch(Dispatchers.IO) {
        var totalCount: Int = 0
        var totalPage: Int = 0
        val dataList: MutableList<Any> = arrayListOf()

        if (isQueryFile) {
            val remoteResult = deviceManageRepositoryImp.queryDeviceFileListWithPage(
                deviceToken = deviceToken,
                begin = begin,
                end = end,
                fileType = "1",
                orderType = "1",
                currentPage = currentPage,
                pageSize = pageSize
            ) { error ->
                handleError(
                    _sensorDataListResult,
                    error,
                    methodUrl = "${BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl}/QueryDeviceFilePage"
                )
            } ?: return@launch

            totalPage = remoteResult.totalPage
            totalCount = remoteResult.totalCount
            dataList.addAll(remoteResult.currentPageData ?: emptyList())

        } else {
            val remoteResult = deviceManageRepositoryImp.querySensorDataListExWithPage(
                sensorIDList = sensorIDList,
                begin = begin,
                end = end,
                density = "0",
                dateTimeSort = false,
                currentPage = currentPage,
                pageSize = pageSize
            ) { error ->
                handleError(
                    _sensorDataListResult,
                    error,
                    methodUrl = "${BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl}/ListPageSensorDataListEx"
                )
            }?.pageResult ?: return@launch

            totalPage = remoteResult.totalPage
            totalCount = remoteResult.totalCount
            dataList.addAll(remoteResult.currentPageData ?: emptyList())
        }

        if (currentPage == 1 && dataList.isNotEmpty()) {
            dataList.add(0, HoverHeaderModel())
        }
        _sensorDataListResult.postValue(
            DataResult(
                dataList,
                responseStatus = ResponseStatus().apply {
                    isSuccess = true
                    responseCode = "0"
                    source = ResultSource.NETWORK
                }, totalCount = totalCount, totalPage = totalPage
            )
        )
    }
    //</editor-fold>

    /**
     * 分页查询设备原始数据列表
     */
    fun queryCloudDataExWithPage(
        sn: String = "",//SN号
        begin: String = "",//
        end: String = "",//
        condition: String = "",//正则表达式
        dataType: String = "",//数据类型 "" 全部 "0"传感器数据 "1"设备状态数据
        iotData: Boolean = false,//true 物联网平台数据 false MDNET平台数据
        timeSort: Boolean = false,
        currentPage: Int = 1,
        pageSize: Int = 50,
    ) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("sn", sn)
            jsonObjectRequest.put("begin", begin)
            jsonObjectRequest.put("end", end)
            jsonObjectRequest.put("condition", condition)
            jsonObjectRequest.put("dataType", dataType)
            jsonObjectRequest.put("iotData", iotData)
            jsonObjectRequest.put("timeSort", timeSort)
            jsonObjectRequest.put("currentPage", currentPage)
            jsonObjectRequest.put("pageSize", pageSize)

            val data: PageList<CloudDeviceData> =
                deviceManageRepositoryImp.queryCloudDataExWithPage(
                    jsonObjectRequest.toString()
                ) { error: Throwable ->
                    handleError(
                        _cloudDeviceDataListResult,
                        error,
                        "${BaseURL.CLOUD_PLATFORM_DATA_ADDRESS.baseUrl}/QueryCloudDataEx"
                    )

                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _cloudDeviceDataListResult.postValue(
                DataResult(
                    data.currentPageData,
                    responseStatus = responseStatus,
                    totalCount = data.totalCount,
                    totalPage = data.totalPage
                )
            )
        }
    }

    override fun onCleared() {
        Timber.i("DeviceRequestViewModel onCleared")
        super.onCleared()
    }
}
