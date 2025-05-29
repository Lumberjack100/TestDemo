package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UProductCommonHomeViewModel : CommonDeviceHomeViewModel() {
    val xAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //x 轴角度
    val yAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //y 轴角度
    val zAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //z 轴角度
}