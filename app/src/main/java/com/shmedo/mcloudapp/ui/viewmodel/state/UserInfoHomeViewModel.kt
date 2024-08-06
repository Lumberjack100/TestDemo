package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.ResourceUtils
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.R

class UserInfoHomeViewModel : ViewModel() {

    val imageUrl = NonNullObservableField("")

    val placeHolder = NonNullObservableField(ResourceUtils.getDrawable(R.drawable.ic_account))

    val name = NonNullObservableField("")

    val post = NonNullObservableField("")

    val phone = NonNullObservableField("")

    val email = NonNullObservableField("")
}