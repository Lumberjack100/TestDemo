package com.shmedo.mcloudapp.user.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AboutViewModel : ViewModel() {
    val appVersion = NonNullObservableField("")
}