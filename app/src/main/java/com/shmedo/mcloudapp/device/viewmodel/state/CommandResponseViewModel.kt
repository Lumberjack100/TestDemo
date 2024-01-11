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
open class CommandResponseViewModel : ViewModel() {
    val isResponseLoading = NonNullObservableField(false)
    val isResponseSuccess = NonNullObservableField(false)
    val responseContent = NonNullObservableField("")

    val deviceTime = NonNullObservableField("")
    val systemTime = NonNullObservableField("")
    val isCalibratingSuccess = NonNullObservableField(false)//是否校准时间成功
}