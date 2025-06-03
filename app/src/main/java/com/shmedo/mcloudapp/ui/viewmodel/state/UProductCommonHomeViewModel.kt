package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UProductCommonHomeViewModel : CommonDeviceHomeViewModel() {
    var productType = NonNullObservableField(ProductType.UnKnown)

    val xAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //x 轴角度
    val yAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //y 轴角度
    val zAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //z 轴角度

    val lFInitial = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE) //初始测量值
    val lFCurrent = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//实时测量值
    val lFCumulative = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//累计变化量

}