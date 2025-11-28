package com.shmedo.lib.rtsp.koin

import com.shmedo.lib.rtsp.RtspManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 模块的 Koin 依赖注入配置
 * 
 * 注册 RTSP 相关的依赖，供应用层使用
 */
val rtspKoinModule = module {
    /**
     * 注册 RtspManager 为单例
     * 整个应用共享一个 RtspManager 实例，统一管理播放器资源
     */
    single { RtspManager(androidContext()) }
}

