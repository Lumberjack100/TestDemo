package com.shmedo.mcloudapp.user.fragment

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class ResetPasswordViewModel : ViewModel() {
    val newPassword = NonNullObservableField("")
    val confirmPassword = NonNullObservableField("")
}