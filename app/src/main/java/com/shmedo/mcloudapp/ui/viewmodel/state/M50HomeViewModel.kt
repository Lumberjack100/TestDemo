package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50HomeViewModel : CommonDeviceHomeViewModel() {

    val resultantDisplacement = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//合位移量
    val xDisplacement = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //x 轴位移量
    val yDisplacement = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //y 轴位移量
    val zDisplacement = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //z 轴位移量

}