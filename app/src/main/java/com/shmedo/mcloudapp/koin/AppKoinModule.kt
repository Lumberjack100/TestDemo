package com.shmedo.mcloudapp.koin

import com.shmedo.core.data.koin.coreDataKoinModule
import com.shmedo.lib.ble.koin.bleKoinModule
import com.shmedo.lib.cmd.base.koin.libCmdKoinModule
import com.shmedo.lib.rtsp.koin.rtspKoinModule
import com.shmedo.lib.tcp.koin.libTcpKoinModule
import com.shmedo.lib.wifi.koin.libWifiModule
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */

val appKoinModule = module {
    includes(
        coreDataKoinModule,
        libCmdKoinModule,
        bleKoinModule,
        libTcpKoinModule,
        rtspKoinModule,
        libWifiModule,
        viewModelModule

    )
}