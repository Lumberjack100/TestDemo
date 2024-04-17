package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasExternalSensorListViewModel <T> : ViewModel() {
    val isSubmitBtnVisible = NonNullObservableField(false)//是否显示提交按钮
    val isVibratingWireSensor = NonNullObservableField(false)//是否振弦式传感器
    val collectorType = NonNullObservableField("")//采集器型号
    val sensorModelMap = mutableMapOf<String, T>() // key: 地址或通道号

    //是否刷新传感器列表
    private val _isRefreshSensorList = MutableResult<Boolean>()
    val isRefreshSensorList: Result<Boolean> = _isRefreshSensorList

    fun updateIsRefreshSensorList(open: Boolean) {
        _isRefreshSensorList.postValue(open)
    }
}