package com.shmedo.mcloudapp.di

import com.shmedo.mcloudapp.data.repository.SharedLocationRepositoryImp
import com.shmedo.mcloudapp.device.viewmodel.request.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/22
 * 描述： TODO
 */

val locationKoinModule = module {
    single { SharedLocationRepositoryImp(get()) }

    viewModel { LocationViewModel(get(), get()) }
}