package com.shmedo.lib.device.base.di

import com.shmedo.lib.device.base.iot_cmd.di.iotCommandModule
import com.shmedo.lib.device.base.md_cmd.di.mdCommandModule
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */

val deviceBaseKoinModule= module {
    includes(
        mdCommandModule,
        iotCommandModule
    )
}