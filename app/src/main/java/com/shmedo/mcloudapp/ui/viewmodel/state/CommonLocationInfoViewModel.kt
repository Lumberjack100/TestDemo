package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class CommonLocationInfoViewModel : ViewModel() {
    val utcTime = NonNullObservableField("")//UTC 时间
    val longitude = NonNullObservableField("")//经度
    val latitude = NonNullObservableField("")//纬度
    val elevation = NonNullObservableField("") //高程

}