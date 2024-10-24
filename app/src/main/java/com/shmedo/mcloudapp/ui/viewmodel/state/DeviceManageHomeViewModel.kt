package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DeviceManageHomeViewModel : ViewModel() {
    val scanQRCodeResult = NonNullObservableField("")//
    val companyName = NonNullObservableField("")//
    var companyIndex = 0

    val isHasListSuperInfoPermission = NonNullObservableField(false)//
    val isShowCompanySelectionPopupView = NonNullObservableField(false)//

}