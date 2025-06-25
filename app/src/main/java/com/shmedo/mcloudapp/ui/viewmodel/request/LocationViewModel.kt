package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.baidu.location.BDLocation
import com.shmedo.core.data.repository.LocationError
import com.shmedo.core.data.repository.LocationRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/22
 * 描述： 位置服务 ViewModel - 适配优化后的 LocationRepositoryImp
 */
class LocationViewModel(
    private val locationRepositoryImp: LocationRepositoryImp,
    private val loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    private val _locationState = MutableSharedFlow<BDLocation>(replay = 1)
    val locationState: SharedFlow<BDLocation> = _locationState

    private val _errorState = MutableSharedFlow<LocationError>(replay = 1)
    val locationErrorState: SharedFlow<LocationError> = _errorState

    private val _isLocationServiceAvailable = MutableSharedFlow<Boolean>(replay = 1)
    val isLocationServiceAvailable: SharedFlow<Boolean> = _isLocationServiceAvailable

    init {
        setupLocationObservers()
    }

    /**
     * 设置位置相关的观察者
     */
    private fun setupLocationObservers() {
        // 收集位置状态流
        viewModelScope.launch {
            locationRepositoryImp.locationStateFlow.collect { location ->
                location?.let {
                    _locationState.emit(it)
                    Timber.d("位置更新: lat=${it.latitude}, lng=${it.longitude}")
                }
            }
        }

        // 收集错误状态流
        viewModelScope.launch {
            locationRepositoryImp.errorStateFlow.collect { error ->
                error?.let {
                    _errorState.emit(it)
                    Timber.w("位置服务错误: ${it.message}")
                }
            }
        }
    }

    /**
     * 立即请求位置更新
     * @param onResult 结果回调，可选
     */
    fun requestImmediateLocationUpdate(onResult: ((BDLocation?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val location = locationRepositoryImp.requestImmediateLocationUpdate()
                onResult?.invoke(location)
                
                if (location == null) {
                    Timber.d("立即定位请求：未获取到位置信息")
                } else {
                    Timber.d("立即定位请求成功: lat=${location.latitude}, lng=${location.longitude}")
                }
            } catch (e: Exception) {
                Timber.e(e, "立即定位请求失败")
                onResult?.invoke(null)
            }
        }
    }

    /**
     * 停止位置服务
     */
    fun stopLocation() {
        viewModelScope.launch {
            try {
                locationRepositoryImp.stopLocation()
                Timber.d("位置服务已停止")
            } catch (e: Exception) {
                Timber.e(e, "停止位置服务失败")
            }
        }
    }

    /**
     * 获取缓存的位置信息
     */
    fun getCachedLocation(): BDLocation? {
        return locationRepositoryImp.getCachedLocation()
    }

    /**
     * 检查位置服务是否可用
     */
    fun checkLocationServiceAvailability() {
        viewModelScope.launch {
            val isAvailable = locationRepositoryImp.isLocationServiceAvailable()
            _isLocationServiceAvailable.emit(isAvailable)
        }
    }

    /**
     * 清理资源
     */
    private fun clearLocationService() {
        viewModelScope.launch {
            try {
                locationRepositoryImp.clear()
            } catch (e: Exception) {
                Timber.e(e, "清理位置服务资源失败")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearLocationService()
    }
}