package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/4/22
 * 描述： TODO
 */
class AdvancedSettingViewModel: ViewModel()  {
    val isRefreshingLocation = NonNullObservableField(false)
    val latitude = NonNullObservableField("")
    val longitude = NonNullObservableField("")
    val location = NonNullObservableField("")
    val address = NonNullObservableField("")

}