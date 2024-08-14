package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class TcpDebugViewModel : ViewModel() {
    val debugMode = NonNullObservableField("关")

    val tcpConnected = NonNullObservableField(false)
}