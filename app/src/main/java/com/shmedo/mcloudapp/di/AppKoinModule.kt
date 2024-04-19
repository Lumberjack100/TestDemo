package com.shmedo.mcloudapp.di

import com.shmedo.lib.ble.di.bleKoinModule
import com.shmedo.lib.core.di.coreKoinModule
import com.shmedo.lib.device.base.di.deviceBaseKoinModule
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
        )
}