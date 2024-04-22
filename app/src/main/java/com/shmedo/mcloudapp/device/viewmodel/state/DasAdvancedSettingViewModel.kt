package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/4/22
 * 描述： TODO
 */
class DasAdvancedSettingViewModel: ViewModel()  {
    val isRefreshingLocation = NonNullObservableField(false)
    val location = NonNullObservableField("")
    val address = NonNullObservableField("")

}