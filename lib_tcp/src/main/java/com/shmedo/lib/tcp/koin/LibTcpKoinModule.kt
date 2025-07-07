package com.shmedo.lib.tcp.koin

import com.shmedo.lib.tcp.MedoTcpRepository
import com.shmedo.lib.tcp.TcpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述： TODO
 */

val libTcpKoinModule = module {
    // TCP Manager 单例
    single { TcpManager() }

    // TCP 专用的 CoroutineScope，使用 single 确保同一个实例
    single<CoroutineScope>(qualifier = named("tcpScope")) {
        object : CoroutineScope {
            override val coroutineContext = Dispatchers.IO + SupervisorJob()

            fun cancel() {
                coroutineContext.cancel()
            }
        }
    }

    // TCP Repository 单例，明确指定依赖类型
    single {
        MedoTcpRepository(
            tcpManager = get(),
            scope = get(qualifier = named("tcpScope"))
        )
    }
}