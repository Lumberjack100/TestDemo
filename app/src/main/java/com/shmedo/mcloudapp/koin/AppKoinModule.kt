package com.shmedo.mcloudapp.koin

import com.shmedo.core.data.koin.coreKoinModule
import com.shmedo.lib.ble.koin.bleKoinModule
import com.shmedo.lib.cmd.base.koin.deviceBaseKoinModule
import com.shmedo.lib.tcp.koin.tcpKoinModule
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */

val appKoinModule = module {
    includes(
        coreKoinModule,
        deviceBaseKoinModule,
        viewModelModule,
        bleKoinModule,
        locationKoinModule,
        tcpKoinModule
    )
}