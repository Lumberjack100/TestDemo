package com.shmedo.mcloudapp.user.viewmodel.state

import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.ResourceUtils
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.R

class MineViewModel : ViewModel() {
    @JvmField
    val imageUrl = NonNullObservableField("")

    @JvmField
    val placeHolder = NonNullObservableField(ResourceUtils.getDrawable(R.drawable.ic_account))

    @JvmField
    val name = NonNullObservableField("")

    @JvmField
    val title = NonNullObservableField("")

    @JvmField
    val company = NonNullObservableField("")

    @JvmField
    val appVersion = NonNullObservableField("")
}