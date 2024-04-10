package com.shmedo.mcloudapp.di

import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.parser.das.AuthenticationInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.AuthenticationResultInfoParser
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
val mdCommandModule = module {
    factory { AuthenticationInfoParser() }
    factory { AuthenticationResultInfoParser() }

    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<MDCommandParser<*>> = listOf(
            get<AuthenticationInfoParser>(),
            get<AuthenticationResultInfoParser>()
        )
        MDParserManager.getInstance(parsers)
    }
}