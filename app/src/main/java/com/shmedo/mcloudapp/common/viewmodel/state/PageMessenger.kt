package com.shmedo.mcloudapp.common.viewmodel.state

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.common.model.CustomActivityResult
import com.shmedo.mcloudapp.device.common.MRPortSensorRefreshResult

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/30
 *
 * 描述： TODO
 *
 *
 */
class PageMessenger : ViewModel(){
    //状态栏颜色
    private val _statusBarColor = MutableResult<Int>()
    val statusBarColor: Result<Int> = _statusBarColor

    //状态栏颜色
    private val _mr702Rs485PortSensorRefresh = MutableResult<MRPortSensorRefreshResult>()
    val mr702Rs485PortSensorRefresh: Result<MRPortSensorRefreshResult> = _mr702Rs485PortSensorRefresh

    // 是否同意隐私政策
    private val _isAgreePolicy = MutableResult<Boolean>()
    val isAgreePolicy: Result<Boolean> = _isAgreePolicy

    //
    private val _activityResultDispatcher = MutableResult<CustomActivityResult>()
    val activityResultDispatcher: Result<CustomActivityResult> = _activityResultDispatcher

    private val _scanSNResult = MutableResult<String>()
    val scanSNResult: Result<String> = _scanSNResult

    //ADME 设备模式 0：设备配置模式，1：自动检测模式，2：异常保护模式
    val admeDeviceMode = NonNullObservableField("0")

    fun requestStatusBarColor(resId: Int) {
        _statusBarColor.postValue(resId)
    }

    fun updateIsAgreePolicy(open: Boolean) {
        _isAgreePolicy.postValue(open)
    }

    fun dispatchActivityResult(result: CustomActivityResult) {
        _activityResultDispatcher.postValue(result)
    }

    fun dispatchScanSNResult(sn: String) {
        _scanSNResult.postValue(sn)
    }

    fun requestMR702Rs485PortSensorRefresh(port: MRPortSensorRefreshResult) {
        _mr702Rs485PortSensorRefresh.postValue(port)
    }
}