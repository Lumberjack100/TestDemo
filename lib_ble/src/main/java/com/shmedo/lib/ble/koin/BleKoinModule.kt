package com.shmedo.lib.ble.koin

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
        bleCommunicateKoinModule
    )
}