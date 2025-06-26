package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/10/21
 * 描述： TODO
 */
class FindDeviceBeepViewModel : ViewModel() {
    val deviceLogoResId = NonNullObservableField(R.drawable.device_logo_default)
}