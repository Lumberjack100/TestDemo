package com.shmedo.lib.tcp.koin

import com.shmedo.lib.tcp.MedoTcpRepository
import com.shmedo.lib.tcp.TcpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述： TODO
 */

val tcpKoinModule = module {
    single { TcpManager() }
    single { MedoTcpRepository(get(), get()) }

    factory { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
}