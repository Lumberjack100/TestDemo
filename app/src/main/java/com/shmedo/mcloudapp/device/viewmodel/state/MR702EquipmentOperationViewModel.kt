package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/6
 *
 * 描述： TODO
 *
 *
 */
class MR702EquipmentOperationViewModel : ViewModel() {
    val isResponseLoading = NonNullObservableField(true)
    val isResponseSuccess = NonNullObservableField(true)
    val responseContent = NonNullObservableField("")
}