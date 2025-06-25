package com.shmedo.lib.ble.koin

import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.ble.communicate.service.base.ServiceManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */
val bleCommunicateKoinModule = module {
    singleOf(::ServiceManager)
    singleOf(::MedoBleRepository)
}