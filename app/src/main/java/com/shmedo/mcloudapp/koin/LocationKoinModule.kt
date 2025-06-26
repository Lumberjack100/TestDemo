package com.shmedo.mcloudapp.koin

import com.shmedo.mcloudapp.ui.viewmodel.request.LocationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/22
 * 描述： TODO
 */

val locationKoinModule = module {
    viewModelOf(::LocationViewModel)
}