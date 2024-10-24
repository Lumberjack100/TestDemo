package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DeviceManageHomeViewModel : ViewModel() {
    val scanQRCodeResult = NonNullObservableField("")//
    val companyName = NonNullObservableField("新疆克力多铁矿选矿厂尾矿库在线监测")//
    var companyIndex = 0
}