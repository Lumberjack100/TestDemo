package com.shmedo.lib.cmd.base.iot_cmd.parser

/**
 * 创建者：gonghe
 * 创建时间：2024/10/30
 * 描述：
 * 标记 IOT 命令解析器的注解
 * 被此注解标记的类会被自动注册到 IOTParserManager 中
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IOTParser