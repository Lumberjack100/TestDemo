package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class SettingViewModel : ViewModel() {
    val isAllowNotification = NonNullObservableField(false) //是否
}