package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M20SWorkModelViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val model = NonNullObservableField("")//模式
    val frontCalc = NonNullObservableField("")//0:关闭前端解算  1:打开前端解算  2:根据网络状态开启前端解算  默认2
}