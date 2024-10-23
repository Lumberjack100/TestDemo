package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DeviceManageHomeViewModel : ViewModel() {
    val scanQRCodeResult = NonNullObservableField("")//
    val company = NonNullObservableField("")//

}