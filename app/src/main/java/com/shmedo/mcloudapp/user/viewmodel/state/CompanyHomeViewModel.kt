package com.shmedo.mcloudapp.user.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class CompanyHomeViewModel : ViewModel() {
    val companyName = NonNullObservableField("")
    val companyType = NonNullObservableField("")
    val industryName = NonNullObservableField("")
    val companyPhone = NonNullObservableField("")
    val companyAddress = NonNullObservableField("")
    val companyWebsite = NonNullObservableField("")
    val companyIntro = NonNullObservableField("")
}