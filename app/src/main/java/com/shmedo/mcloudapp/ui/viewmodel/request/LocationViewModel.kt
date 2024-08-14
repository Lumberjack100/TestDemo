package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocation
import com.shmedo.core.data.repository.LocationRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 * 创建时间：2024/4/22
 * 描述： TODO
 */
class LocationViewModel(
    private val locationRepositoryImp: LocationRepositoryImp,
    private val loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    private val _locationState = MutableSharedFlow<AMapLocation>(replay = 1)
    val locationState: SharedFlow<AMapLocation> = _locationState


    init {
        // 收集 LocationRepositoryImp 提供的 `locationStateFlow`
        viewModelScope.launch {
            locationRepositoryImp.locationStateFlow.collect { location ->
                location?.let {
                    _locationState.emit(it)
                }
            }
        }
    }

    fun requestImmediateLocationUpdate() {
        locationRepositoryImp.requestImmediateLocationUpdate()
    }

    fun stopLocation() {
        locationRepositoryImp.stopLocation()
    }

    override fun onCleared() {
        super.onCleared()
        locationRepositoryImp.clear()
    }
}