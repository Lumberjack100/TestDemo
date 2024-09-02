package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDHomeViewModel : CommonDeviceHomeViewModel() {
    val waterSurfaceElevation = NonNullObservableField("")//水面高程
    val airDistance = NonNullObservableField("")//空高距离
    val installationAngle = NonNullObservableField("")//安装角度
    val measurementTime = NonNullObservableField("") //测量时间

}