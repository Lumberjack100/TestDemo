package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702ReservoirCapacityViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isOpened = NonNullObservableField(true)

    //坐标点数量
    val pointCount = NonNullObservableField(0)
}