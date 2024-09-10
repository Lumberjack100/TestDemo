package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50HomeViewModel : CommonDeviceHomeViewModel() {
    //是否测量中
    val isMeasuring = NonNullObservableField(false)

    val resultantDisplacement = NonNullObservableField("--")//合位移量
    val xDisplacement = NonNullObservableField("--")  //x 轴位移量
    val yDisplacement = NonNullObservableField("--")  //y 轴位移量
    val zDisplacement = NonNullObservableField("--")  //z 轴位移量

}