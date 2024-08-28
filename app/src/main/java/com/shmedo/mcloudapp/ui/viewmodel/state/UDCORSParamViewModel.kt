package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDCORSParamViewModel : ViewModel() {
    val isOpened = NonNullObservableField(false)

    //域名
    val domain = NonNullObservableField("")
    //端口
    val port = NonNullObservableField("")
    //差分账号
    val diffAccount = NonNullObservableField("")
    //差分密码
    val diffPassword = NonNullObservableField("")
}