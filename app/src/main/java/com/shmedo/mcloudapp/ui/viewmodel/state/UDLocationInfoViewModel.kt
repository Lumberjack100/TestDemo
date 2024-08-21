package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDLocationInfoViewModel : ViewModel() {
    val utcTime = NonNullObservableField("")//UTC 时间
    val longitude = NonNullObservableField("")//经度
    val latitude = NonNullObservableField("")//纬度
    val altitude = NonNullObservableField("") //海拔
    val locationMethod = NonNullObservableField("")//定位方式
    val pdop = NonNullObservableField("")
}