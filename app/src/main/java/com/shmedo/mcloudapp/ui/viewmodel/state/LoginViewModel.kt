package com.shmedo.mcloudapp.ui.viewmodel.state
import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/12/7 <br></br>
 * 描述：     TODO
 */
class LoginViewModel : ViewModel() {
    val account = NonNullObservableField("")
    val password = NonNullObservableField("")
    val phone = NonNullObservableField("")
    val code = NonNullObservableField("")
    val eyeOpen = NonNullObservableField(false)
    val isAccountLogin = NonNullObservableField(true)
}
