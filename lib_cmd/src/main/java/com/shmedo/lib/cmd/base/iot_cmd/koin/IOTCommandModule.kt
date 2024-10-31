package com.shmedo.lib.cmd.base.iot_cmd.koin

import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserRegistry
import org.koin.dsl.module

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

val iotCommandModule = module {
    // 直接使用注册表中的解析器
    single {
        IOTParserManager(IOTParserRegistry.getAllParsers())
    }
}