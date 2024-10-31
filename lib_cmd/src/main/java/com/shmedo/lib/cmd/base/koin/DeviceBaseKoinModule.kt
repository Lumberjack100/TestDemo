package com.shmedo.lib.cmd.base.koin

import com.shmedo.lib.cmd.base.iot_cmd.koin.iotCommandModule
import com.shmedo.lib.cmd.base.md_cmd.koin.mdCommandModule
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