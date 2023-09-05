package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class BleScannerListViewModel : ViewModel() {
    val searchPlaceholderVisible = NonNullObservableField(true)
    val bluetoothNotAvailable = NonNullObservableField(false)
    val bluetoothDisabled = NonNullObservableField(false)
    val bluetoothMissPermission = NonNullObservableField(false)
    val scanNoResultsVisible = NonNullObservableField(false)
    val keyWords = NonNullObservableField("")
    val deviceCount = NonNullObservableField(0)
    val scanState = NonNullObservableField("刷新")
    val refreshing = NonNullObservableField(false)
}