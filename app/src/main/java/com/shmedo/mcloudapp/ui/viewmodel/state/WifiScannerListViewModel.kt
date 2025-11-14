package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * WiFi 扫描列表 ViewModel State
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: WiFi 扫描列表页面的状态管理
 */
class WifiScannerListViewModel : ViewModel() {
    
    // WiFi 是否未开启
    val wifiDisabled = ObservableBoolean(false)
    
    // 定位服务是否未开启
    val locationDisabled = ObservableBoolean(false)
    
    // 定位权限是否被拒绝
    val permissionDenied = ObservableBoolean(false)
    
    // 是否仅显示 IoT 设备
    val filterIoTOnly = ObservableBoolean(false)
    
    // 搜索关键词
    val keyWords = MutableLiveData<String>("")
}

