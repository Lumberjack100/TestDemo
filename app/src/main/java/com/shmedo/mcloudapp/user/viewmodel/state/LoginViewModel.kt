package com.shmedo.mcloudapp.user.viewmodel.state
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/12/7 <br></br>
 * 描述：     TODO
 */
class LoginViewModel : BaseViewModel() {


    val name = NonNullObservableField("")


    val password = NonNullObservableField("")


    val phone = NonNullObservableField("")


    val code = NonNullObservableField("")


    val eyeOpen = NonNullObservableField(false)


    val isAccountLogin = NonNullObservableField(true)

}
