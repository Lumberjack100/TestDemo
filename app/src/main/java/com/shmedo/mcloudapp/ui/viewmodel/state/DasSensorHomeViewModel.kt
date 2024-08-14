package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DasSensorHomeViewModel : ViewModel() {
    val isBdOpened = NonNullObservableField(false)
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("")
}