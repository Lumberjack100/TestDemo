package com.shmedo.lib.ble.di

import com.shmedo.lib.ble.permission.di.blePermissionKoinModule
import com.shmedo.lib.ble.scanner.di.bleScannerKoinModule
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */

val bleKoinModule = module {
    includes(
        blePermissionKoinModule,
        bleScannerKoinModule,

        )
}