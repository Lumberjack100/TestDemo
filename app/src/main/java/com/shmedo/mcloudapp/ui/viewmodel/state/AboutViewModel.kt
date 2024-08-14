package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AboutViewModel : ViewModel() {
    val appVersion = NonNullObservableField("")
    val caseNumber = NonNullObservableField("")//备案号
}