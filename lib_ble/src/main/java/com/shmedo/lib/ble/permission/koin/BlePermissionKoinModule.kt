package com.shmedo.lib.ble.permission.koin

import com.shmedo.lib.ble.permission.bluetooth.BluetoothStateManager
import com.shmedo.lib.ble.permission.location.LocationStateManager
import com.shmedo.lib.ble.permission.viewmodel.PermissionViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */

val blePermissionKoinModule = module {
    single { BluetoothStateManager( androidApplication()) }
    single { LocationStateManager(androidApplication()) }

    viewModel { PermissionViewModel(get(),get()) }
}