package com.shmedo.mcloudapp.device.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.lib.core.base.model.DeviceDetailInfo
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.DeviceStatisticInfo
import com.shmedo.lib.core.base.model.ProductInfo
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.mcloudapp.data.repository.remote.NetDataRepository
import com.shmedo.mcloudapp.device.model.CloudDeviceData
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： TODO
 *
 *
 */
class DeviceRequestViewModel : BaseViewModel() {
    private val _deviceStatisticInfoResult = MutableResult<DataResult<DeviceStatisticInfo>>()
    val deviceStatisticInfoResult: Result<DataResult<DeviceStatisticInfo>> =
        _deviceStatisticInfoResult

    private val _productListResult = MutableResult<DataResult<List<ProductInfo>>>()
    val productListResult: Result<DataResult<List<ProductInfo>>> =
        _productListResult

    private val _deviceListResult = MutableResult<DataResult<List<DeviceInfo>>>()
    val deviceListResult: Result<DataResult<List<DeviceInfo>>> =
        _deviceListResult

    private val _deviceInfoResult = MutableResult<DataResult<DeviceInfo>>()
    val deviceInfoResult: Result<DataResult<DeviceInfo>> =
        _deviceInfoResult

    private val _cloudDeviceDataListResult = MutableResult<DataResult<List<CloudDeviceData>>>()
    val cloudDeviceDataListResult: Result<DataResult<List<CloudDeviceData>>> =
        _cloudDeviceDataListResult

    /**
     * 查询公司设备在线统计信息
     */
    fun getDeviceStatByCompanyID(companyID: Int, isHasListSuperInfoPermission: Boolean = false) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", companyID)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: DeviceStatisticInfo =
                NetDataRepository.instance.getDeviceStatByCompanyID(
                    jsonObjectRequest.toString(),
                    isHasListSuperInfoPermission
                ) { error: Throwable ->
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

    /**
     *  分页查询产品列表
     */
    fun getProductList(companyID: Int) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
//                jsonObjectRequest.put("companyID", companyID)
                jsonObjectRequest.put("pageSize", 100)
                jsonObjectRequest.put("currentPage", 1)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: PageList<ProductInfo> =
                NetDataRepository.instance.getUserCompanyProductList(jsonObjectRequest.toString()) { error: Throwable ->
                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _productListResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val tempList = mutableListOf<ProductInfo>()
            data.currentPageData?.filter { product ->
                product.deviceNum > 0 &&     //过滤掉设备数为0的产品
                        ProductType.valueByPrefix(product.productToken.uppercase()) !== ProductType.UnKnown //过滤掉未知产品类型
            }?.sortedBy { product ->
                product.productName
            }?.let {
                tempList.addAll(it)
            }
            val productInfo = ProductInfo(id = -1, productName = "全部", isChecked = true)
            tempList.add(0, productInfo)

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _productListResult.setValue(
                DataResult(
                    tempList,
                    responseStatus = responseStatus,
                    totalCount = data.totalCount,
                    totalPage = data.totalPage
                )
            )
        }
    }

    /**
     * 分页查询设备列表
     */
    fun getDeviceList(
        companyID: Int,
        productID: Int = -1,
        deviceToken: String = "",
        currentPage: Int,
        pageSize: Int,
        isHasListSuperInfoPermission: Boolean = false,
        onlineStatus: String = "",
    ) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", companyID)
                if (deviceToken.isNotEmpty())
                    jsonObjectRequest.put("deviceToken", deviceToken)//SN号,支持模糊查询
                jsonObjectRequest.put(
                    "productID",
                    if (productID == -1) "" else productID
                )//产品ID,null则不指定产品
                jsonObjectRequest.put("tokenAndVersion", false)//sn号和版本号之间得关系
                jsonObjectRequest.put("deviceStatus", "启用")//ull选择全部，启用选择启用设备，禁用用选择未启用设备
                jsonObjectRequest.put("sortSNAsc", true)//ture按SN正序，false按Sn逆序
                jsonObjectRequest.put("onlineStatus", onlineStatus)//在线状态
                if (isHasListSuperInfoPermission)
                    jsonObjectRequest.put("filterNoPermissionDevice", true)//过滤用户无权限设备
                jsonObjectRequest.put("currentPage", currentPage)
                jsonObjectRequest.put("pageSize", pageSize)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: PageList<DeviceInfo> =
                NetDataRepository.instance.queryDeviceList(
                    jsonObjectRequest.toString(),
                    isHasListSuperInfoPermission
                ) { error: Throwable ->
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
     * 获取设备详细信息
     */
    fun getDeviceDetailInfo(deviceToken: String = "") {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("deviceToken", deviceToken)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: DeviceDetailInfo =
                NetDataRepository.instance.getDeviceDetailInfo(jsonObjectRequest.toString()) { error: Throwable ->
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
        try {
            jsonObjectRequest.put("deviceToken", deviceToken)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return NetDataRepository.instance.getDeviceDetailInfo(jsonObjectRequest.toString(), onCatch)
    }

    suspend fun applyBackup(
        backupID: String = "",
        deviceID: String = "",
        onCatch: ((Throwable) -> Unit)? = null
    ): String? {
        val jsonObjectRequest = JSONObject()
        try {
            jsonObjectRequest.put("backupID", backupID)
            jsonObjectRequest.put("deviceID", deviceID)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
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
            try {
                jsonObjectRequest.put("sn", sn)
                jsonObjectRequest.put("begin", begin)
                jsonObjectRequest.put("end", end)
                jsonObjectRequest.put("condition", condition)
                jsonObjectRequest.put("dataType", dataType)
                jsonObjectRequest.put("iotData", iotData)
                jsonObjectRequest.put("timeSort", timeSort)
                jsonObjectRequest.put("currentPage", currentPage)
                jsonObjectRequest.put("pageSize", pageSize)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: PageList<CloudDeviceData> =
                NetDataRepository.instance.queryCloudDataExWithPage(
                    jsonObjectRequest.toString()
                ) { error: Throwable ->
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
}