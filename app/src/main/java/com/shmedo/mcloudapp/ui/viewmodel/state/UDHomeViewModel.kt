package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDHomeViewModel : CommonDeviceHomeViewModel() {
    val waterSurfaceElevation = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//水面高程
    val airDistance = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//空高距离
    val installationAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//安装角度
    val todayRainfall = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//今日雨量
    val measurementTime = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE) //测量时间

    //是否测量中
    val isMeasuring = NonNullObservableField(false)
    var measureDataLoadingDialogId = ""
}