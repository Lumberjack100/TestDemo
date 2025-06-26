package com.shmedo.lib.ble.koin

import com.shmedo.lib.ble.permission.bluetooth.BluetoothStateManager
import com.shmedo.lib.ble.permission.location.LocationStateManager
import com.shmedo.lib.ble.permission.viewmodel.PermissionViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */

val blePermissionKoinModule = module {
    singleOf(::BluetoothStateManager)
    singleOf(::LocationStateManager)

    viewModelOf(::PermissionViewModel)
}