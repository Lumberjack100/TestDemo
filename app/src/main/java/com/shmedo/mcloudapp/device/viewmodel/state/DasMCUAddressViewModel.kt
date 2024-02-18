package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasMCUAddressViewModel : ViewModel() {
    val address = NonNullObservableField("")//地址
}