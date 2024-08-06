package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class QueryDeviceDataViewModel : ViewModel() {
    val sn = NonNullObservableField("")
    val platformType = NonNullObservableField("")
    val dataType = NonNullObservableField("")
    val periodDate = NonNullObservableField("")
    val regexContent = NonNullObservableField("")
}