package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/4/28
 * 描述： TODO
 */
class UniversalDataCenterHomeViewModel: ViewModel() {
    val isSupportedReportInterval = NonNullObservableField(false)

    val reportInterval = NonNullObservableField("")
}