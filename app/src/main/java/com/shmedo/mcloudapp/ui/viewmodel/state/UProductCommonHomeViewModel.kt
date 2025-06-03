package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UProductCommonHomeViewModel : CommonDeviceHomeViewModel() {
    var productType = NonNullObservableField(ProductType.UnKnown)

    /***   LR200  ***/
    val xAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //x 轴角度
    val yAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //y 轴角度
    val zAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //z 轴角度

    val lFInitial = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE) //初始测量值
    val lFCurrent = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//实时测量值
    val lFCumulative = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)//累计变化量

    /***   UR  ***/
    //24小时雨量
    val rain24h = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)// 24小时雨量


    /***   UI  ***/
    val xInitialAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //x 轴初始角度
    val yInitialAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //y 轴初始角度
    val zInitialAngle = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //z 轴初始角度

    val xAcc = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //x 轴加速度
    val yAcc = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //y 轴加速度
    val zAcc = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  //z 轴加速度
}