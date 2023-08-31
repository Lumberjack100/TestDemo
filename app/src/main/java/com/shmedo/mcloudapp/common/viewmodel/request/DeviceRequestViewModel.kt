package com.shmedo.mcloudapp.common.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
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

    /**
     * 查询公司设备在线统计信息
     */
    fun getDeviceStatByCompanyID(companyID: Int) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", companyID)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: DeviceStatisticInfo =
                NetDataRepository.instance.getDeviceStatByCompanyID(jsonObjectRequest.toString()) { error: Throwable ->
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
     * 分页查询产品列表
     */
    fun getProductList(companyID: Int) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", companyID)
                jsonObjectRequest.put("pageSize", 100)
                jsonObjectRequest.put("currentPage", 1)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: PageList<ProductInfo> =
                NetDataRepository.instance.getProductList(jsonObjectRequest.toString()) { error: Throwable ->
                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _productListResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val tempList = mutableListOf<ProductInfo>()
            data.currentPageData?.filter { product ->
                product.deviceNum > 0 &&
                        ProductType.valueByPrefix(product.productToken.uppercase()) !== ProductType.UnKnown
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
    fun getDeviceList(companyID: Int, productID: Int, currentPage: Int, pageSize: Int) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", companyID)
                jsonObjectRequest.put("productID", if (productID == -1) "" else productID)
                jsonObjectRequest.put("tokenAndVersion", false)
                jsonObjectRequest.put("deviceStatus", "启用")
                jsonObjectRequest.put("currentPage", currentPage)
                jsonObjectRequest.put("pageSize", pageSize)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val data: PageList<DeviceInfo> =
                NetDataRepository.instance.queryDeviceList(jsonObjectRequest.toString()) { error: Throwable ->
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

}