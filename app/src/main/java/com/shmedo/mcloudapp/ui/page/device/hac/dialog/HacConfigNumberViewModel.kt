package com.shmedo.mcloudapp.ui.page.device.hac.dialog


import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.ui.viewmodel.state.BaseDialogViewModel

class HacConfigNumberViewModel : BaseDialogViewModel() {
    val number = NonNullObservableField("")

    fun updateInitialState(
        title: String,
        number: String,
    ) {
        this.title.set(title)
        this.number.set(number)
    }

}
