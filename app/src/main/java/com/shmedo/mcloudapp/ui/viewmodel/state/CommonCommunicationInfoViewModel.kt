package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class CommonCommunicationInfoViewModel : ViewModel() {
    //4G信号强度
    val signalValue = NonNullObservableField(0)
}