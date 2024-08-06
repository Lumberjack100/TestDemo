package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class BleScannerListViewModel : ViewModel() {
    val bluetoothNotAvailable = NonNullObservableField(false)
    val bluetoothDisabled = NonNullObservableField(false)
    val bluetoothMissPermission = NonNullObservableField(false)

    val keyWords = MutableLiveData<String>("MD")
}