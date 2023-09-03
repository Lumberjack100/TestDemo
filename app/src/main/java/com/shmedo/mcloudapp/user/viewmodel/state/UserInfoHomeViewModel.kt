package com.shmedo.mcloudapp.user.viewmodel.state

import com.blankj.utilcode.util.ResourceUtils
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.R

class UserInfoHomeViewModel : BaseViewModel() {

    val imageUrl = NonNullObservableField("")

    val placeHolder = NonNullObservableField(ResourceUtils.getDrawable(R.drawable.ic_account))

    val name = NonNullObservableField("")

    val post = NonNullObservableField("")

    val phone = NonNullObservableField("")

    val email = NonNullObservableField("")
}