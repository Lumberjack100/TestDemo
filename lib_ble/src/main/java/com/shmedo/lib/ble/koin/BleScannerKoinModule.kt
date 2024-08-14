package com.shmedo.lib.ble.koin

import com.shmedo.lib.ble.scanner.repository.DevicesDataStore
import com.shmedo.lib.ble.scanner.repository.ScannerRepository
import com.shmedo.lib.ble.scanner.viewmodel.ScannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */
val bleScannerKoinModule = module {
    single { DevicesDataStore() }
    single { ScannerRepository(get()) }

    viewModel { ScannerViewModel(get()) }
}