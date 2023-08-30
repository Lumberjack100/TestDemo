package com.shmedo.mcloudapp.user.viewmodel.state

import com.blankj.utilcode.util.ResourceUtils
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.R

class UserInfoHomeViewModel : BaseViewModel() {
    @JvmField
    val imageUrl = NonNullObservableField("")
    @JvmField
    val placeHolder = NonNullObservableField(ResourceUtils.getDrawable(R.drawable.ic_account))
    @JvmField
    val name = NonNullObservableField("")
    @JvmField
    val post = NonNullObservableField("")
    @JvmField
    val phone = NonNullObservableField("")
    @JvmField
    val email = NonNullObservableField("")
}