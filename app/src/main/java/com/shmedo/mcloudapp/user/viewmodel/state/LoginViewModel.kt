package com.shmedo.mcloudapp.user.viewmodel.state
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/12/7 <br></br>
 * 描述：     TODO
 */
class LoginViewModel : BaseViewModel() {

    @JvmField
    val name = NonNullObservableField("")

    @JvmField
    val password = NonNullObservableField("")

    @JvmField
    val phone = NonNullObservableField("")

    @JvmField
    val code = NonNullObservableField("")

    @JvmField
    val eyeOpen = NonNullObservableField(false)

    @JvmField
    val isAccountLogin = NonNullObservableField(true)

}
