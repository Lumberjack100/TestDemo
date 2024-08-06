package com.shmedo.lib.ble.koin

import com.shmedo.lib.ble.permission.koin.blePermissionKoinModule
import com.shmedo.lib.ble.scanner.koin.bleScannerKoinModule
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