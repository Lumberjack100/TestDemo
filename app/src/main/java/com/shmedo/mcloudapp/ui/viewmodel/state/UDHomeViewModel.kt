package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDHomeViewModel : CommonDeviceHomeViewModel() {
    val waterSurfaceElevation = NonNullObservableField("5.333m")//水面高程
    val airDistance = NonNullObservableField("5.333m")//空高距离
    val installationAngle = NonNullObservableField("89.3°")//安装角度
    val measurementTime = NonNullObservableField("2023-08-07 12:36:00") //测量时间


    //是否测量中
    val isMeasuring = NonNullObservableField(false)

}