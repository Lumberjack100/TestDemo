package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocation
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import com.shmedo.mcloudapp.data.repository.SharedLocationRepositoryImp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/22
 * 描述： TODO
 */
class LocationViewModel(
    private val locationRepositoryImp: SharedLocationRepositoryImp,
    private val loggerRepositoryImp: LoggerRepositoryImp
) :
    BaseRequestViewModel(loggerRepositoryImp) {

    private val _locationState = MutableSharedFlow<AMapLocation>(replay = 1)
    val locationState: SharedFlow<AMapLocation> = _locationState


    init {
        locationRepositoryImp.getLocationFlow().onEach { location ->
            //_locationState.value = location
            _locationState.emit(location)
        }.catch { exception ->
            Timber.e("定位流异常: ${exception.localizedMessage}")
        }.launchIn(viewModelScope)
    }

    fun refreshLocation() {
        try {
            locationRepositoryImp.requestImmediateLocationUpdate()
        } catch (e: Exception) {
            Timber.e("请求即时定位更新失败: ${e.localizedMessage}")
        }
    }

    fun stopLocation() {
        locationRepositoryImp.stopLocation()
    }

    fun clearLocation() {
        locationRepositoryImp.clear()
    }

    override fun onCleared() {
        super.onCleared()
        locationRepositoryImp.clear()
    }
}