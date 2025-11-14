package com.shmedo.lib.wifi.koin

import android.os.Build
import androidx.annotation.RequiresApi
import com.shmedo.lib.wifi.connector.repository.WifiConnectorRepository
import com.shmedo.lib.wifi.connector.viewmodel.WifiConnectorViewModel
import com.shmedo.lib.wifi.permission.location.LocationStateManager
import com.shmedo.lib.wifi.permission.viewmodel.WifiPermissionViewModel
import com.shmedo.lib.wifi.permission.wifi.WifiStateManager
import com.shmedo.lib.wifi.scanner.repository.WifiDataStore
import com.shmedo.lib.wifi.scanner.repository.WifiScannerRepository
import com.shmedo.lib.wifi.scanner.viewmodel.WifiScannerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * WiFi 模块 Koin 配置
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: WiFi 模块的依赖注入配置
 */
@RequiresApi(Build.VERSION_CODES.Q)
val libWifiModule = module {
    
    // WiFi 数据存储
    single { WifiDataStore() }
    
    // WiFi 扫描相关
    single { WifiScannerRepository(androidContext(), get()) }
    viewModel { WifiScannerViewModel(get()) }
    
    // WiFi 连接相关
    single { WifiConnectorRepository(androidContext()) }
    viewModel { WifiConnectorViewModel(get()) }
    
    // WiFi 权限相关 - 新架构
    single { WifiStateManager(androidContext()) }
    single { LocationStateManager(androidContext()) }
    viewModel { WifiPermissionViewModel(get(), get()) }
}

