package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AppPermissionSettingViewModel : ViewModel() {
    val isAllowLocation = NonNullObservableField(false)
    val isAllowBluetooth = NonNullObservableField(false) //是否
    val isAllowCamera = NonNullObservableField(false) //是否
    val isAllowStorage = NonNullObservableField(false) //是否
}