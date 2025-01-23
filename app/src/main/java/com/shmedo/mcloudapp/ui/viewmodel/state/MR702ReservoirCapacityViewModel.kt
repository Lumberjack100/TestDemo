package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702ReservoirCapacityViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isOpened = NonNullObservableField(true)

    val pointCount = NonNullObservableField("0")//坐标点数量
    val x1 = NonNullObservableField("")//X1坐标值，1位小数 单位：米
    val y1 = NonNullObservableField("")//Y1坐标值，1位小数 单位：米
    val x2 = NonNullObservableField("")//X2坐标值，1位小数 单位：米
    val y2 = NonNullObservableField("")//Y2坐标值，1位小数 单位：米
    val x3 = NonNullObservableField("")//X3坐标值，1位小数 单位：米
    val y3 = NonNullObservableField("")//Y3坐标值，1位小数 单位：米
}