package com.shmedo.mcloudapp.ui.viewmodel.request

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.data.repository.DeviceManageRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.data.repository.NetDataRepository
import com.shmedo.core.model.CloudDeviceData
import com.shmedo.core.model.DeviceDebugAddress
import com.shmedo.core.model.DeviceDetailInfo
import com.shmedo.core.model.DeviceInfo
import com.shmedo.core.model.DeviceSensorBasicInfo
import com.shmedo.core.model.DeviceStatisticInfo
import com.shmedo.core.model.EmptyInfo
import com.shmedo.core.model.FirmWareInfo
import com.shmedo.core.model.ProductInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.BuildConfig
import com.shmedo.mcloudapp.model.SingleSelectionItem
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    private val _deviceStatisticInfoResult = MutableResult<DataResult<DeviceStatisticInfo>>()
    val deviceStatisticInfoResult: Result<DataResult<DeviceStatisticInfo>> =
        _deviceStatisticInfoResult

    private val _allProductTabResultFlow: MutableSharedFlow<DataResult<List<SingleSelectionItem>>> =
        MutableSharedFlow()
    val allProductTabResultFlow = _allProductTabResultFlow.asSharedFlow()

    private val _productListResult = MutableResult<DataResult<List<ProductInfo>>>()
    val productListResult: Result<DataResult<List<ProductInfo>>> =
        _productListResult

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

    private val _sensorDataListResult = MutableResult<DataResult<List<EmptyInfo>>>()
    val sensorDataListResult: Result<DataResult<List<EmptyInfo>>> =
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
                NetDataRepository.instance.getDeviceStatByCompanyID(
                    jsonObjectRequest.toString(),
                    isHasListSuperInfoPermission
                ) { error: Throwable ->
                    Timber.e(error)
                    addLogItem(CommonMMKVOwner.appLogSessionId, Log.ERROR, error.errorMsg)

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _deviceStatisticInfoResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _deviceStatisticInfoResult.setValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    fun getAllProductTabList() {
        viewModelScope.launch {
            val pageSize = 100
            val tempList = mutableListOf<ProductInfo>()
            val filterList = mutableListOf<SingleSelectionItem>()

            val responseStatus = ResponseStatus().apply {
                isSuccess = true
                responseCode = "0"
                source = ResultSource.NETWORK
            }

            try {
                var currentPage = 1 // Start with the first page

                while (true) { // Keep fetching pages until there are no more pages left
                    val jsonObjectRequest = JSONObject().apply {
                        put("pageSize", pageSize)
                        put("currentPage", currentPage)
                    }

                    val data: PageList<ProductInfo>? = withContext(Dispatchers.IO) {
                        try {
                            NetDataRepository.instance.getUserCompanyProductList(jsonObjectRequest.toString())
                        } catch (error: Throwable) {
                            Timber.e(error)
                            addLogItem(
                                CommonMMKVOwner.appLogSessionId,
                                Log.ERROR,
                                error.errorMsg
                            )
                            responseStatus.apply {
                                isSuccess = false
                                errorMessage = error.errorMsg
                            }
                            null
                        }
                    }

                    data?.currentPageData?.let { tempList.addAll(it) }

                    if (data == null || currentPage >= data.totalPage) {
                        // Break if data is null or we've reached the last page
                        break
                    }
                    currentPage++
                }

                if (responseStatus.isSuccess) {
                    filterList.apply {
                        clear()
                        add(SingleSelectionItem(name = "全部产品", isChecked = true))

                        tempList.filter { product ->
                            product.deviceNum > 0  // Filter out products with 0 devices
                        }.sortedBy { it.productName }
                            .mapTo(this) { product ->
                                SingleSelectionItem(
                                    name = product.productName,
                                    extValue = product.id.toString()
                                )
                            }
                    }
                }

                _allProductTabResultFlow.emit(
                    DataResult(
                        result = if (responseStatus.isSuccess) filterList else null,
                        responseStatus = responseStatus,
                        totalCount = filterList.size
                    )
                )
            } catch (error: Throwable) {
                Timber.e(error)
                addLogItem(CommonMMKVOwner.appLogSessionId, Log.ERROR, error.errorMsg)
                responseStatus.apply {
                    isSuccess = false
                    errorMessage = error.errorMsg
                }
                _allProductTabResultFlow.emit(DataResult(responseStatus = responseStatus))
            }
        }
    }

    /**
     * 分页查询设备列表
     */
    fun getDeviceList(
        companyID: Int,
        productID: String = "",
        deviceToken: String = "",
        currentPage: Int,
        pageSize: Int,
        isHasListSuperInfoPermission: Boolean = false,
        onlineStatus: String = "",
    ) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("companyID", companyID)
            if (deviceToken.isNotEmpty())
                jsonObjectRequest.put("deviceToken", deviceToken)//SN号,支持模糊查询
            if (productID.isNotEmpty() && productID != "-1")
                jsonObjectRequest.put("productID", productID)//产品ID,null则不指定产品
            if (onlineStatus.isNotEmpty())
                jsonObjectRequest.put("onlineStatus", onlineStatus)//在线状态
            if (isHasListSuperInfoPermission)
                jsonObjectRequest.put("filterNoPermissionDevice", true)//过滤用户无权限设备
            jsonObjectRequest.put("deviceStatus", "启用")//ull选择全部，启用选择启用设备，禁用选择未启用设备
            //jsonObjectRequest.put("sortSNAsc", true)//ture按SN正序，false按Sn逆序
            jsonObjectRequest.put("tokenAndVersion", false)//sn号和版本号之间得关系
            jsonObjectRequest.put("currentPage", currentPage)
            jsonObjectRequest.put("pageSize", pageSize)

            val data: PageList<DeviceInfo> =
                NetDataRepository.instance.queryDeviceList(
                    jsonObjectRequest.toString(),
                    isHasListSuperInfoPermission
                ) { error: Throwable ->
                    Timber.e(error)
                    addLogItem(CommonMMKVOwner.appLogSessionId, Log.ERROR, error.errorMsg)

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _deviceListResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _deviceListResult.setValue(
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
                NetDataRepository.instance.queryFollowDeviceList(
                    jsonObjectRequest.toString()
                ) { error: Throwable ->
                    Timber.e(error)
                    addLogItem(CommonMMKVOwner.appLogSessionId, Log.ERROR, error.errorMsg)

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _followDeviceListResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _followDeviceListResult.setValue(
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

            val data: String = NetDataRepository.instance.addUserFollowDevice(
                jsonObjectRequest.toString(),
            ) { error: Throwable ->
                error.printStackTrace()
                val msg =
                    "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/AddUserFollowDevice error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
                addLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = Log.ERROR,
                    data = msg
                )

                val responseStatus = ResponseStatus()
                responseStatus.isSuccess = false
                responseStatus.errorMessage = error.errorMsg
                responseStatus.source = ResultSource.NETWORK
                _followDeviceResult.setValue(DataResult(responseStatus = responseStatus))
            } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _followDeviceResult.setValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    fun cancelUserFollowDevice(deviceSn: String = "") {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            jsonObjectRequest.put("deviceSn", deviceSn)

            val data: String = NetDataRepository.instance.cancelUserFollowDevice(
                jsonObjectRequest.toString(),
            ) { error: Throwable ->
                error.printStackTrace()
                val msg =
                    "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/CancelUserFollowDevice error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
                addLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = Log.ERROR,
                    data = msg
                )

                val responseStatus = ResponseStatus()
                responseStatus.isSuccess = false
                responseStatus.errorMessage = error.errorMsg
                responseStatus.source = ResultSource.NETWORK
                _cancelFollowDeviceResult.setValue(DataResult(responseStatus = responseStatus))
            } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _cancelFollowDeviceResult.setValue(DataResult(data, responseStatus = responseStatus))
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
                NetDataRepository.instance.getDeviceDetailInfo(jsonObjectRequest.toString()) { error: Throwable ->
                    Timber.e(error)
                    addLogItem(CommonMMKVOwner.appLogSessionId, Log.ERROR, error.errorMsg)

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _deviceInfoResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _deviceInfoResult.setValue(
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

        return NetDataRepository.instance.getDeviceDetailInfo(jsonObjectRequest.toString(), onCatch)
    }

    suspend fun applyBackup(
        backupID: String = "",
        deviceID: String = "",
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val jsonObjectRequest = JSONObject()
        jsonObjectRequest.put("backupID", backupID)
        jsonObjectRequest.put("deviceID", deviceID)

        return NetDataRepository.instance.applyBackup(jsonObjectRequest.toString(), onCatch)
    }

    /**
     * 分页查询设备列表
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
                NetDataRepository.instance.queryCloudDataExWithPage(
                    jsonObjectRequest.toString()
                ) { error: Throwable ->
                    Timber.e(error)
                    addLogItem(CommonMMKVOwner.appLogSessionId, Log.ERROR, error.errorMsg)

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _cloudDeviceDataListResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _cloudDeviceDataListResult.setValue(
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
                NetDataRepository.instance.queryFirmwareListByProductIDWithPage(
                    jsonObjectRequest.toString()
                ) { error: Throwable ->
                    Timber.e(error)
                    addLogItem(CommonMMKVOwner.appLogSessionId, Log.ERROR, error.errorMsg)

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _firmWareListResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _firmWareListResult.setValue(
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

        return NetDataRepository.instance.applyFirmwareUpgrade(
            jsonObjectRequest.toString(),
            onCatch
        )
    }

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
        return NetDataRepository.instance.getRemoteDeviceLogin(jsonObjectRequest.toString()) { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AMS_CONFIG_ADDRESS.baseUrl}/Login error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = CommonMMKVOwner.appLogSessionId,
                priority = Log.ERROR,
                data = msg
            )
            onCatch?.invoke(error)
        }
    }


    //<editor-fold desc="获取传感器数据">
    suspend fun queryDeviceSensor(
        deviceToken: String = "",
        currentPage: Int = 1,
        pageSize: Int = 100,
    ): List<DeviceSensorBasicInfo> = withContext(Dispatchers.Default) {
        val sensorList = deviceManageRepositoryImp.queryDeviceSensorListWithPage(
            deviceToken = deviceToken,
            currentPage = currentPage,
            pageSize = pageSize
        )?.currentPageData ?: emptyList()

        sensorList
    }

    /**
     * 分页查询设备监测数据
     */
    fun queryMonitorDataListWithPage(
        sensorIDList: List<String>,
        begin: String = "",
        end: String = "",
        currentPage: Int = 1,
        pageSize: Int = 100,
    ) = viewModelScope.launch(Dispatchers.IO) {
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
                methodUrl = "${BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl}/QueryDeviceFilePage"
            )
        }?.pageResult?.currentPageData ?: return@launch
    }

    /**
     * 分页查询设备文件
     */
    fun queryDeviceFileListWithPage(
        deviceToken: String = "",
        begin: String = "",
        end: String = "",
        currentPage: Int = 1,
        pageSize: Int = 100,
    ) = viewModelScope.launch(Dispatchers.IO) {
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

        val dataList: MutableList<EmptyInfo> =
            remoteResult.currentPageData?.toMutableList() ?: arrayListOf()

        if (currentPage == 1)
            dataList.add(0, EmptyInfo())//添加一个空数据，用于显示文件上传按钮

        _sensorDataListResult.postValue(
            DataResult(
                dataList,
                responseStatus = ResponseStatus().apply {
                    isSuccess = true
                    responseCode = "0"
                    source = ResultSource.NETWORK
                }, totalPage = remoteResult.totalPage, totalCount = remoteResult.totalCount
            )
        )
    }
    //</editor-fold>

    override fun onCleared() {
        Timber.i("DeviceRequestViewModel onCleared")
        super.onCleared()
    }
}