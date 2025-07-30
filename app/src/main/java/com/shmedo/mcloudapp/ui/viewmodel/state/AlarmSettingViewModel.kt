package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AlarmSettingViewModel : ViewModel() {
    // 电台模块状态，用于控制UI启用状态
    val isRadioEnable = NonNullObservableField(true)

    val isOpened = NonNullObservableField(false)
}