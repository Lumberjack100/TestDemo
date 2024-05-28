/*
 * Designed and developed by 2020 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shmedo.mcloudapp.di

import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.mcloudapp.common.viewmodel.request.AppUpdateViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.NetIOTCommandViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.RequestSearchViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.TcpViewModel
import com.shmedo.mcloudapp.user.viewmodel.request.LoginRequestViewModel


import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { LogViewModel(get()) }
    viewModel { BleViewModel(get()) }
    viewModel { TcpViewModel(get(), get()) }
    viewModel { NetIOTCommandViewModel(get()) }
    viewModel { DeviceRequestViewModel(get()) }
    viewModel { RequestSearchViewModel(get()) }
    viewModel { AppUpdateViewModel(get()) }
    viewModel { LoginRequestViewModel(get()) }

//  viewModel { (posterId: Long) -> PosterDetailViewModel(posterId, get()) }
}
