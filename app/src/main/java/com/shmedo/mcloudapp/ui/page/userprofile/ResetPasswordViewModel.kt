package com.shmedo.mcloudapp.ui.page.userprofile

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class ResetPasswordViewModel : ViewModel() {
    val newPassword = NonNullObservableField("")
    val confirmPassword = NonNullObservableField("")
}